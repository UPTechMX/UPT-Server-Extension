CREATE OR REPLACE FUNCTION public.suitability_index_values(
	layers_list integer[],
	filters_list integer[],
	settings_list text[],
	study_area bigint,
	operation integer,
	projection integer)
    RETURNS TABLE(json text) 
    LANGUAGE 'plpgsql'

    COST 100
    VOLATILE 
    ROWS 1000
AS $$
DECLARE
    intersected geometry = NULL;
    filter_pol geometry;
    study_area_wkt text;
    data_row record;
    layer_loop text;
	first_time boolean=true;
BEGIN
    DROP TABLE IF EXISTS mmu_layers;
    CREATE TEMPORARY TABLE mmu_layers (
        user_layer_id bigint,
        value double precision,
        mmu_code text,
        geometry geometry
    );
    CREATE INDEX mmu_layers_geometry_idx
    ON mmu_layers USING gist
    (geometry);
    
    CREATE INDEX mmu_layers_mmu_code_idx
    ON mmu_layers USING btree
    (mmu_code ASC NULLS LAST);

    DROP TABLE IF EXISTS filters;
    CREATE TEMPORARY TABLE filters (
        "geometry" geometry
    );
    CREATE INDEX filters_geometry_idx
    ON filters USING gist
    (geometry);

    DROP TABLE IF EXISTS study_filtered;
    CREATE TEMPORARY TABLE study_filtered (
        "geometry" geometry,
        study_area geometry
    );
    CREATE INDEX study_filtered_geometry_idx
    ON study_filtered USING gist
    (geometry);
    CREATE INDEX study_filtered_study_area_idx
    ON study_filtered USING gist
    (study_area);
    -- Load study area
    SELECT
        st_astext (geometry) INTO study_area_wkt
    FROM
        user_layer_data
    WHERE
        user_layer_id = study_area;
    
    insert into mmu_layers(user_layer_id, mmu_code, value, geometry)
    select 
            user_layer_data.user_layer_id
            , (user_layer_data.property_json ->>st_layers.layer_mmu_code)::text as mmu_code
            , case when (user_layer_data.property_json ->>st_layers.layer_field)::double precision is null then 0 else (user_layer_data.property_json ->>st_layers.layer_field)::double precision end as value
            , user_layer_data.geometry
    from 
            user_layer_data
            inner join st_layers on st_layers.user_layer_id=user_layer_data.user_layer_id
    where 
            st_layers.id =any(layers_list)
            and st_intersects(st_geomfromtext(study_area_wkt), user_layer_data.geometry);
    
    
    FOR filter_pol IN (
        SELECT
            geometry
        FROM
            user_layer_data
            INNER JOIN st_filters ON user_layer_data.user_layer_id = st_filters.user_layer_id
        WHERE
            st_filters.id = ANY (filters_list))
    LOOP
        IF first_time THEN
            intersected = filter_pol;
			first_time=false;
        ELSE
            IF (operation = 2) THEN
                intersected = ST_CollectionExtract(st_intersection (intersected, filter_pol), 3 );
            ELSIF (operation = 3) then
                intersected = ST_CollectionExtract(st_difference (intersected, filter_pol), 3 );
            ELSE
                intersected = ST_CollectionExtract(st_union (intersected, filter_pol), 3 );
            END IF;
        END IF;
    END LOOP;
    
    IF ARRAY_LENGTH(filters_list, 1) IS NULL THEN
        INSERT INTO filters ("geometry")
        SELECT
            st_geomfromtext (study_area_wkt);
        INSERT INTO study_filtered ("geometry", study_area)
        SELECT st_geomfromtext (study_area_wkt),st_geomfromtext (study_area_wkt);
    ELSE
        INSERT INTO filters ("geometry")
        SELECT
            intersected;
        INSERT INTO study_filtered ("geometry", study_area)
        SELECT
            st_intersection (st_geomfromtext (study_area_wkt),intersected) AS geometry,
            st_geomfromtext (study_area_wkt)
        FROM
            filters;
    END IF;


    DROP TABLE IF EXISTS vals_settings;
    create temp table vals_settings as
    SELECT
                    user_config.user_layer_id,
                    user_config.st_layers_id,
                    user_config.smaller_better,
                    user_config.weight,
                    user_config.normalization_method,
                    -- test if normalization method is reference or observe
                    CASE WHEN user_config.normalization_method != 1 and  user_config.normalization_method != 3  THEN
                            user_config.range_max
                    ELSE
                            vals_obs_max_min.max
                    END AS range_max,
                    CASE WHEN user_config.normalization_method != 1 and  user_config.normalization_method != 3 THEN
                            user_config.range_min
                    ELSE
                            vals_obs_max_min.min
                    END AS range_min,
                    dev,
                    mean
            FROM
                    st_settings
                    RIGHT JOIN (
                            SELECT
                                    st_layers.user_layer_id,
                                    (config.value ->> 'st_layer_id')::int AS st_layers_id,
                                    (config.value ->> 'normalization_method')::int AS normalization_method,
                                    (config.value ->> 'smaller_better')::int AS smaller_better,
                                    (config.value ->> 'range_max')::double precision AS range_max,
                                    (config.value ->> 'range_min')::double precision AS range_min,
                                    (config.value ->> 'weight')::double precision AS weight
                            FROM
                                    json_array_elements((SELECT unnest(settings_list)::json)) as config
                                    INNER JOIN st_layers ON (config.value ->> 'st_layer_id')::bigint = st_layers.id
                    ) as user_config ON st_settings.id = user_config.st_layers_id
                    INNER JOIN (
                            SELECT
                                    mmu_layers.user_layer_id,
                                    max(mmu_layers.value) AS max,
                                    min(mmu_layers.value) AS min,
                                    stddev_pop(mmu_layers.value) AS dev,
                                    avg(mmu_layers.value) AS mean
                            FROM
                                    mmu_layers
                                    INNER JOIN st_layers ON st_layers.user_layer_id = mmu_layers.user_layer_id,
                                    study_filtered
                            WHERE
                                    st_layers.id = ANY (layers_list)
                                    AND st_intersects (study_filtered.study_area, mmu_layers.geometry)
                            GROUP BY
                                    mmu_layers.user_layer_id
                    )as vals_obs_max_min ON user_config.user_layer_id = vals_obs_max_min.user_layer_id
            WHERE
                    user_config.st_layers_id = ANY (layers_list);
	
    CREATE INDEX vals_settings_user_layer_id_idx
    ON vals_settings USING btree
    (user_layer_id);

    create temp table unique_mmu as
    select distinct mmu_code,geometry from mmu_layers;

    CREATE INDEX unique_mmu_mmu_code_idx
    ON unique_mmu USING btree
    (mmu_code);

    delete from unique_mmu
    where not st_intersects(unique_mmu.geometry,(select geometry from study_filtered));

    update unique_mmu set geometry = st_intersection(unique_mmu.geometry,study_filtered.geometry)
    from study_filtered,(select st_boundary(geometry) as geometry from study_filtered) c1
    where st_intersects(c1.geometry,unique_mmu.geometry);
    

    create temp table total as
    SELECT
            sum(vals_settings.weight) AS weight,
            count(vals_settings.weight) AS num_layers
    FROM
            vals_settings
    WHERE
            st_layers_id = ANY (layers_list);
	-- evaluate index values for all mmu
    create temp table mmu_index_adjusted AS (
            SELECT
                    mmu_settings.user_layer_id,
                    mmu_code,
                    CASE WHEN mmu_settings.normalization_method != 3 THEN
                            weight * (value - range_min) / (range_max - range_min)::double precision
                    ELSE
                            weight*(((value - mean) / dev::double precision) - ((range_min - mean) / dev::double precision))::double precision / (((range_max - mean) / dev::double precision) - ((range_min - mean) / dev::double precision) )::double precision 
                    END AS value,
                    smaller_better,
                    weight
            FROM
                    (
                            SELECT
                                    mmu_layers.user_layer_id,
                                    mmu_layers.value,
                                    mmu_layers.mmu_code,
                                    study_filtered.geometry,
                                    mmu_layers.geometry,
                                    vals_settings.smaller_better,
                                    vals_settings.weight / total.weight::double precision AS weight,
                                    vals_settings.range_max,
                                    vals_settings.range_min,
                                    vals_settings.normalization_method,
                                    vals_settings.dev,
                                    vals_settings.mean
                            FROM
                                    mmu_layers
                                    INNER JOIN st_layers ON st_layers.user_layer_id = mmu_layers.user_layer_id
                                    INNER JOIN vals_settings ON mmu_layers.user_layer_id = vals_settings.user_layer_id
                                    INNER JOIN total ON 1 = 1,
                                    study_filtered
                            WHERE
                                    vals_settings.st_layers_id = ANY (layers_list)
                                    AND st_intersects (study_filtered.geometry, mmu_layers.geometry)
                    ) as mmu_settings
    );
    CREATE INDEX mmu_index_adjusted_mmu_code_idx
    ON mmu_index_adjusted USING btree
    (mmu_code);
	--
    create temp table mmu_index_resutls as
    SELECT
            mmu_code,
            round(CAST(sum(value) / total.num_layers AS numeric), 0) AS value
    FROM
            (
                    SELECT
                            user_layer_id,
                            mmu_code,
                            CASE WHEN smaller_better = 0 THEN
                                    100*value
                            ELSE
                                    100*(1 - value)
                            END AS value
                    FROM
                            (
                                    SELECT
                                            user_layer_id,
                                            mmu_code,
                                            CASE WHEN value < 0 THEN
                                                    0
                                            WHEN value > 1 THEN
                                                    1
                                            ELSE
                                                    value
                                            END AS value,
                                            smaller_better,
                                            weight
                                    FROM
                                            mmu_index_adjusted
                            ) as mmu_index,
                            total
            ) as mmu_adjust,
            total
    GROUP BY
            mmu_code,
            total.num_layers;
	
    CREATE INDEX mmu_index_resutls_value_idx
    ON mmu_index_resutls USING btree
    (value);
	--
    create temp table test_geoms AS (
            SELECT
                    replace(st_geometrytype (geometry), 'ST_', '') AS point_type,
                    ST_ForceRHR (st_setsrid (geometry, projection)) AS geom,
                    value
            FROM
                    (
                            SELECT
                                    mmu_index_resutls.value,
                                    st_union(st_collectionextract (unique_mmu.geometry, 3)) AS geometry
                            FROM
                                    mmu_index_resutls
                                    INNER JOIN unique_mmu ON unique_mmu.mmu_code = mmu_index_resutls.mmu_code
                            GROUP BY
                                    mmu_index_resutls.value,unique_mmu.geometry
                    ) as mmu_geometries
    );
    -- get study_area
    RETURN query 
	SELECT
		json_build_object(
			'type',
			'FeatureCollection',
			'crs',
			json_build_object(
				'type', 
				'name', 
				'properties', 
				json_build_object(
					'name', 
					concat('EPSG:', projection::text)
				)
			), 
			'features', 
			json_agg(
				json_build_object(
					'type', 
					'Feature',
					'geometry', 
					ST_AsGeoJSON (st_setsrid(geom,0))::json, 
					'properties', 
					json_build_object(
						'type', 
						point_type, 
						'value', 
						value
					)
				)
			)
		)::text
	FROM
		test_geoms;
END;
$$;
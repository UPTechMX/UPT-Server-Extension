begin;
    create table if not exists up_indicators(
            id serial NOT NULL,
            indicator character varying(60) COLLATE pg_catalog."default" NOT NULL,
            created timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
            updated timestamp with time zone,
            CONSTRAINT up_indicators_pkey PRIMARY KEY (id),
            CONSTRAINT up_indicators_indicator_key UNIQUE (indicator)
        );

    insert into up_indicators(indicator)
        values('pop_total') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('footprint_km2') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('land_consumption_km') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('infill_area_km2') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('pop_density') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('ghg_emissions') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('energy_consumption') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('tot_water') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('energy_lighting') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('energy_water') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('energy_transport') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('energy_swaste') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('energy_buildings') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('municipal_service_costs')ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('infrastructure_costs') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('infrastructure_new_costs') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('infrastructure_infill_costs') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('job_prox') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('transit_prox') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('school_proximity') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('elementary_school_proximity') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('secondary_school_proximity') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('high_school_proximity') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('public_space_proximity')ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('sports_proximity') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('worship_proximity') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('university_proximity') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('health_proximity') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('nursery_proximity') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('public_service_proximity') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('cultural_facility_proximity') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('market_proximity') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('hazard_exp') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('hu_tot') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('vhu_tot')ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('vhu_rate') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('land_consumption_pct') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('energy_gasoline') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('energy_diesel') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('transport_energy') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('collection_energy') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('pop_prox_jobs') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('pop_prox_transit') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('pop_prox_school') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('pop_prox_elementary_school') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('pop_prox_secondary_school') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('pop_prox_high_school') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('pop_prox_public_service') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('pop_prox_sports') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('pop_prox_worship') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('pop_prox_university') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('pop_prox_health') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('pop_prox_nursery') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('pop_prox_public_space') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('pop_prox_cultural_facility') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('pop_prox_market') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('pop_prox_hazards') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('pop_infill') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('pop_expan') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('overcrowding') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('house_deficit') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('unpermited_housing') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('intersections_density') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('sidewalks_coverage') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('pedestrian_crossing_coverage') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('lighting_coverage') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('water_metering') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('energy_metering')ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('solid_waste_annual_collection_costs') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('additional_annual_municipal_revenues') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('financial_balance') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('prim_road_km') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('prim_road_km2') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('sec_road_km') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('sec_road_km2') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('ter_road_km') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('ter_road_km2') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('unpermitted_housing') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('hu_cost')ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values ('infrastructure_costs_consolidation') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('municipal_expenditure') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('municipal_revenue_pop') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('base_vacant_rate') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('street_coef') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('tree_cover') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('carbon_sequestration') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('tot_cycle') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('cycle_cover') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('avge_cycle_track') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('covkwh')ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('cotkwh') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('so2kwh') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('pm2_5kwh') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('noxkwh') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('cokwh') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('pm10kwh') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('carbon_seq_nr') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('pop_prox_cycle') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('nrisk_prox') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('pspace_capita') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('cycle_prox') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('roads_density') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('emissions_transport') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('agric_consumption') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('greenland_consumption')ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('other_land_consumption') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('energy_security') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('ren_energy') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('plrunning_cost') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('cwrunning_cost') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('solidw_coverage') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('city_data') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('indirect_data') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('borrowed_data') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('energy_wwt') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('wwt_pct') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('wwtrunning_cost') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('inv_cost') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('cw_coverage')
            ,('localren_energy') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('local_energy') ON CONFLICT (indicator)
            DO nothing;
        insert into up_indicators(indicator)
            values('ww')
            ON CONFLICT (indicator)
            DO nothing;

    create table if not exists up_indicators_translation(
            id serial NOT NULL,
            language character varying(20) COLLATE pg_catalog."default" NOT NULL,
            label character varying(60) COLLATE pg_catalog."default" ,
            units character varying(60) COLLATE pg_catalog."default" ,
            up_indicators_id int not null,
            created timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
            updated timestamp with time zone,
            CONSTRAINT up_indicators_translation_pkey PRIMARY KEY (id),
            CONSTRAINT up_indicators_id_fkey FOREIGN KEY (up_indicators_id)
                REFERENCES public.up_indicators (id) MATCH SIMPLE
                ON UPDATE NO ACTION
                ON DELETE CASCADE,
            CONSTRAINT up_indicators_translation_key UNIQUE (up_indicators_id,language)
        );

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','inhabitants','Total population'
        ,(select min(id) as id from up_indicators where indicator='pop_total' )
    ) on conflict(up_indicators_id,language) do nothing;


    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km2','Urban footprint'
        ,(select min(id) as id from up_indicators where indicator='footprint_km2' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km2','Land consumption'
        ,(select min(id) as id from up_indicators where indicator='land_consumption_km' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km2','Infill area'
        ,(select min(id) as id from up_indicators where indicator='infill_area_km2' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','pop/km2','Population density'
        ,(select min(id) as id from up_indicators where indicator='pop_density' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','kgCO2eq/capita/annum'
        ,'GHG emissions'
        ,(select min(id) as id from up_indicators where indicator='ghg_emissions' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','kWh/capita/annum'
        ,'Energy consumption'
        ,(select min(id) as id from up_indicators where indicator='energy_consumption' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','m3/capita/annum'
        ,'Water consumption'
        ,(select min(id) as id from up_indicators where indicator='tot_water' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','kWh/capita/annum'
        ,'Public lighting energy consumption'
        ,(select min(id) as id from up_indicators where indicator='energy_lighting' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','kWh/capita/annum'
        ,'Water provision energy consumption'
        ,(select min(id) as id from up_indicators where indicator='energy_water' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','kWh/capita/annum'
        ,'Commuting energy consumption'
        ,(select min(id) as id from up_indicators where indicator='energy_transport' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','kWh/capita/annum'
        ,'Solid waste collection energy consumption'
        ,(select min(id) as id from up_indicators where indicator='energy_swaste' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','kWh/capita/annum'
        ,'Dwellings energy consumption'
        ,(select min(id) as id from up_indicators where indicator='energy_buildings' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','MXN/capita/annum'
        ,'Municipal services cost'
        ,(select min(id) as id from up_indicators where indicator='municipal_service_costs' )
    )on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','USD'
        ,'Infrastructure costs'
        ,(select min(id) as id from up_indicators where indicator='infrastructure_costs' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','USD'
        ,'Expansion costs'
        ,(select min(id) as id from up_indicators where indicator='infrastructure_new_costs' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','USD'
        ,'Infill costs'
        ,(select min(id) as id from up_indicators where indicator='infrastructure_infill_costs' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','%'
        ,'Job proximity'
        ,(select min(id) as id from up_indicators where indicator='job_prox' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','%'
        ,'Public transport proximity'
        ,(select min(id) as id from up_indicators where indicator='transit_prox' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','%'
        ,'School proximity'
        ,(select min(id) as id from up_indicators where indicator='school_proximity' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','%'
        ,'Elementary school proximity'
        ,(select min(id) as id from up_indicators where indicator='elementary_school_proximity' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','%'
        ,'Secondary school proximity'
        ,(select min(id) as id from up_indicators where indicator='secondary_school_proximity' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','%'
        ,'High school proximity'
        ,(select min(id) as id from up_indicators where indicator='high_school_proximity' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','%'
        ,'Public space proximity'
        ,(select min(id) as id from up_indicators where indicator='public_space_proximity' )
    )on conflict(up_indicators_id,language) do nothing;
    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','%',
        'Sports facility proximity',
        (select min(id) as id from up_indicators where indicator='sports_proximity' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','%'
        ,'Worship place proximity'
        ,(select min(id) as id from up_indicators where indicator='worship_proximity' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','%'
        ,'University proximity'
        ,(select min(id) as id from up_indicators where indicator='university_proximity' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','%'
        ,'Health facility proximity'
        ,(select min(id) as id from up_indicators where indicator='health_proximity' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','%'
        ,'Nursery proximity'
        ,(select min(id) as id from up_indicators where indicator='nursery_proximity' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','%'
        ,'Public building proximity'
        ,(select min(id) as id from up_indicators where indicator='public_service_proximity' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','%'
        ,'Cultural space proximity'
        ,(select min(id) as id from up_indicators where indicator='cultural_facility_proximity' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','%'
        ,'Market proximity'
        ,(select min(id) as id from up_indicators where indicator='market_proximity' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','%'
        ,'Exposure to hazards'
        ,(select min(id) as id from up_indicators where indicator='hazard_exp' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','hu'
        ,'Total housing units'
        ,(select min(id) as id from up_indicators where indicator='hu_tot' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km2'
        ,'Vacant housing units'
        ,(select min(id) as id from up_indicators where indicator='vhu_tot' )
    )on conflict(up_indicators_id,language) do nothing;

    /* insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km2','vhu_tot' 
        ,(select min(id) as id from up_indicators where indicator='vhu_tot' )
    ) 
    on conflict(up_indicators_id,language) do nothing; */

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','%'
        ,'Percentage of land consumed'
        ,(select min(id) as id from up_indicators where indicator='land_consumption_pct' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','kWh/capita/annum'
        ,'Energy gasoline consumed'
        ,(select min(id) as id from up_indicators where indicator='energy_gasoline' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','kWh/capita/annum'
        ,'Energy diesel consumed'
        ,(select min(id) as id from up_indicators where indicator='energy_diesel' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','kWh/capita/annum'
        ,'Energy used to transport waste'
        ,(select min(id) as id from up_indicators where indicator='transport_energy' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','kWh/capita/annum'
        ,'Energy used in the collection stage'
        ,(select min(id) as id from up_indicators where indicator='collection_energy' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','inhabitants'
        ,'Number of inhabitants with proximity to jobs'
        ,(select min(id) as id from up_indicators where indicator='pop_prox_jobs' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','inhabitants'
        ,'Number of inhabitants with proximity to public transport'
        ,(select min(id) as id from up_indicators where indicator='pop_prox_transit' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','inhabitants'
        ,'Population with proximity to schools'
        ,(select min(id) as id from up_indicators where indicator='pop_prox_school' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','inhabitants'
        ,'Population with proximity to an elementary school'
        ,(select min(id) as id from up_indicators where indicator='pop_prox_elementary_school' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','inhabitants'
        ,'Population with proximity to a secondary school'
        ,(select min(id) as id from up_indicators where indicator='pop_prox_secondary_school' )
    )on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','inhabitants','Population with proximity to a high school'
        ,(select min(id) as id from up_indicators where indicator='pop_prox_high_school' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','inhabitants'
        ,'Population with proximity to public service buildings'
        ,(select min(id) as id from up_indicators where indicator='pop_prox_public_service' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','inhabitants'
        ,'Population with proximity to sports facilities'
        ,(select min(id) as id from up_indicators where indicator='pop_prox_sports' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','inhabitants'
        ,'Population with proximity to worship places'
        ,(select min(id) as id from up_indicators where indicator='pop_prox_worship' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','inhabitants'
        ,'Population with proximity to universities'
        ,(select min(id) as id from up_indicators where indicator='pop_prox_university' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','inhabitants'
        ,'Population with proximity to health facilities'
        ,(select min(id) as id from up_indicators where indicator='pop_prox_health' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','inhabitants'
        ,'Population with proximity to nursery facilities'
        ,(select min(id) as id from up_indicators where indicator='pop_prox_nursery' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','inhabitants'
        ,'Population with proximity to public spaces'
        ,(select min(id) as id from up_indicators where indicator='pop_prox_public_space' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','inhabitants'
        ,'Population with proximity to cultural facilities'
        ,(select min(id) as id from up_indicators where indicator='pop_prox_cultural_facility' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','inhabitants'
        ,'Population with proximity to markets'
        ,(select min(id) as id from up_indicators where indicator='pop_prox_market' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','inhabitants'
        ,'Population with proximity to hazards'
        ,(select min(id) as id from up_indicators where indicator='pop_prox_hazards' )
    )on conflict(up_indicators_id,language) do nothing;

    /* insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','inhabitants'
        ,'Population inside expansion polygon'
        ,(select min(id) as id from up_indicators where indicator='pop_prox_hazards' )
    ) 
    on conflict(up_indicators_id,language) do nothing; */

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','inhabitants'
        ,'Population inside expansion polygon'
        ,(select min(id) as id from up_indicators where indicator='pop_expan' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    /* Pending*/
    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km2'
        ,'Overcrowding'
        ,(select min(id) as id from up_indicators where indicator='overcrowding' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    /* Pending*/
    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km2'
        ,'house_deficit'
        ,(select min(id) as id from up_indicators where indicator='house_deficit' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    /* Pending*/
    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km2'
        ,'unpermited_housing'
        ,(select min(id) as id from up_indicators where indicator='unpermited_housing' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','int/km2'
        ,'Intersections per square kilometer'
        ,(select min(id) as id from up_indicators where indicator='intersections_density' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    /*Pending*/
    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km2'
        ,'sidewalks_coverage'
        ,(select min(id) as id from up_indicators where indicator='sidewalks_coverage' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    /*Pending*/
    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km2'
        ,'pedestrian_crossing_coverage'
        ,(select min(id) as id from up_indicators where indicator='pedestrian_crossing_coverage' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    /*Pending*/
    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km2'
        ,'lighting_coverage'
        ,(select min(id) as id from up_indicators where indicator='lighting_coverage' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    /*Pending*/
    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km2'
        ,'water_metering'
        ,(select min(id) as id from up_indicators where indicator='water_metering' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    /*Pending*/
    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km2'
        ,'energy_metering'
        ,(select min(id) as id from up_indicators where indicator='energy_metering' )
    )on conflict(up_indicators_id,language) do nothing;

    /*Pending*/
    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km2'
        ,'solid_waste_annual_collection_costs'
        ,(select min(id) as id from up_indicators where indicator='solid_waste_annual_collection_costs' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    /*Pending*/
    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km2'
        ,'additional_annual_municipal_revenues'
        ,(select min(id) as id from up_indicators where indicator='additional_annual_municipal_revenues' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    /*Pending*/
    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km2'
        ,'financial_balance'
        ,(select min(id) as id from up_indicators where indicator='financial_balance' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km'
        ,'Kilometers of primary roads'
        ,(select min(id) as id from up_indicators where indicator='prim_road_km' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km/km2'
        ,'Kilometers of primary roads per square kilometer'
        ,(select min(id) as id from up_indicators where indicator='prim_road_km2' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km'
        ,'Kilometers of secondary roads'
        ,(select min(id) as id from up_indicators where indicator='sec_road_km' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km/km2'
        ,'Kilometers of secondary roads per square kilometer'
        ,(select min(id) as id from up_indicators where indicator='sec_road_km2' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km'
        ,'Kilometers of tertiary roads'
        ,(select min(id) as id from up_indicators where indicator='ter_road_km' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km2'
        ,'Kilometers of tertiary roads per square kilometer'
        ,(select min(id) as id from up_indicators where indicator='ter_road_km2' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    /*Pending*/
    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km2'
        ,'unpermitted_housing'
        ,(select min(id) as id from up_indicators where indicator='unpermitted_housing' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','$/hu'
        ,'Construction cost per housing unit'
        ,(select min(id) as id from up_indicators where indicator='hu_cost' )
    )on conflict(up_indicators_id,language) do nothing;

    /*Pending*/
    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km2','infrastructure_costs_consolidation'
        ,(select min(id) as id from up_indicators where indicator='infrastructure_costs_consolidation' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    /*Pending*/
    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km2'
        ,'municipal_expenditure'
        ,(select min(id) as id from up_indicators where indicator='municipal_expenditure' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','$/inhabitants'
        ,'Municipal forecasted revenue'
        ,(select min(id) as id from up_indicators where indicator='municipal_revenue_pop' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    /*Pending*/
    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km2'
        ,'base_vacant_rate'
        ,(select min(id) as id from up_indicators where indicator='base_vacant_rate' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    /*Pending*/
    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km2'
        ,'street_coef'
        ,(select min(id) as id from up_indicators where indicator='street_coef' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','m2/capita'
        ,'Tree canopy cover'
        ,(select min(id) as id from up_indicators where indicator='tree_cover' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','TonCO2e/annum'
        ,'Carbon sequestration by natural reserve'
        ,(select min(id) as id from up_indicators where indicator='carbon_sequestration' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km'
        ,'Cycle track kilometers'
        ,(select min(id) as id from up_indicators where indicator='tot_cycle' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km/km2'
        ,'Cycle track density'
        ,(select min(id) as id from up_indicators where indicator='cycle_cover' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km'
        ,'Cycle track length'
        ,(select min(id) as id from up_indicators where indicator='avge_cycle_track' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','KgCOV/capita/annum'
        ,'Volatile organic compound emissions NEW'
        ,(select min(id) as id from up_indicators where indicator='covkwh' )
    )on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','KgCOT/capita/annum'
        ,'Total organic compound emissions'
        ,(select min(id) as id from up_indicators where indicator='cotkwh' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','KgSO2/capita/annum'
        ,'Sulfur dioxide emissions'
        ,(select min(id) as id from up_indicators where indicator='so2kwh' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','KgPM2.5/capita/annum'
        ,'Particulate emissions PM2.5'
        ,(select min(id) as id from up_indicators where indicator='pm2_5kwh' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','KgNOx/capita/annum'
        ,'Nitric oxide emisssions'
        ,(select min(id) as id from up_indicators where indicator='noxkwh' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','KgCO/capita/annum'
        ,'Carbon monoxide emissions'
        ,(select min(id) as id from up_indicators where indicator='cokwh' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','KgPM10/capita/annum'
        ,'Particulate emissions PM10'
        ,(select min(id) as id from up_indicators where indicator='pm10kwh' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','TonCO2e/annum'
        ,'Carbon sequestration by natural reserve'
        ,(select min(id) as id from up_indicators where indicator='carbon_seq_nr' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','inhabitants'
        ,'Population with proximity to cycle'
        ,(select min(id) as id from up_indicators where indicator='pop_prox_cycle' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    /*Pending*/
    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km2'
        ,'nrisk_prox'
        ,(select min(id) as id from up_indicators where indicator='nrisk_prox' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    /*Pending*/
    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km2/inhabitant'
        ,'Green area per capita'
        ,(select min(id) as id from up_indicators where indicator='pspace_capita' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','%'
        ,'Cycle proximity'
        ,(select min(id) as id from up_indicators where indicator='cycle_prox' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    /*Pending*/
    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km/km2'
        ,'Roads density'
        ,(select min(id) as id from up_indicators where indicator='roads_density' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','kgCO2eq/capita/annum'
        ,'Commuting emissions'
        ,(select min(id) as id from up_indicators where indicator='emissions_transport' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km2'
        ,'Agricultural land consumption'
        ,(select min(id) as id from up_indicators where indicator='agric_consumption' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km2'
        ,'Green land consumption'
        ,(select min(id) as id from up_indicators where indicator='greenland_consumption' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km2'
        ,'Other land consumption'
        ,(select min(id) as id from up_indicators where indicator='other_land_consumption' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km2'
        ,'Energy security - Electricity'
        ,(select min(id) as id from up_indicators where indicator='energy_security' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','%'
        ,'Renewable energy'
        ,(select min(id) as id from up_indicators where indicator='ren_energy' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    /* Pending*/
    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','km2'
        ,'plrunning_cost'
        ,(select min(id) as id from up_indicators where indicator='plrunning_cost' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','USD/capita/annum'
        ,'Construction waste management running cost'
        ,(select min(id) as id from up_indicators where indicator='cwrunning_cost' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','%'
        ,'Solid waste management coverage'
        ,(select min(id) as id from up_indicators where indicator='solidw_coverage' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','unit'
        ,'Data obtained from the city'
        ,(select min(id) as id from up_indicators where indicator='city_data' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','unit'
        ,'Data obtained from indirect sources'
        ,(select min(id) as id from up_indicators where indicator='indirect_data' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','unit'
        ,'Borrowed data'
        ,(select min(id) as id from up_indicators where indicator='borrowed_data' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','kWh/capita/annum'
        ,'Energy associated to wastewater treatment'
        ,(select min(id) as id from up_indicators where indicator='energy_wwt' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','%'
        ,'Wastewater treated percentage'
        ,(select min(id) as id from up_indicators where indicator='wwt_pct' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','USD/capita/annum'
        ,'Wastewater treatment running cost'
        ,(select min(id) as id from up_indicators where indicator='wwtrunning_cost' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','Million $'
        ,'Investment cost'
        ,(select min(id) as id from up_indicators where indicator='inv_cost' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','%'
        ,'Construction waste management coverage'
        ,(select min(id) as id from up_indicators where indicator='cw_coverage' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','GWh/year'
        ,'Ammount of energy generated locally with renewable sources'
        ,(select min(id) as id from up_indicators where indicator='localren_energy' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','GWh/year'
        ,'Locally produced electricity'
        ,(select min(id) as id from up_indicators where indicator='local_energy' )
    ) 
    on conflict(up_indicators_id,language) do nothing;

    insert into up_indicators_translation(
    language, units,
    label,
    up_indicators_id
    )
    values(
       'English','m3/year'
        ,'Total volume of waste water generated in the city'
        ,(select min(id) as id from up_indicators where indicator='ww' )
    )
    on conflict(up_indicators_id,language) do nothing;
    

    /*Modules translation*/
    create table if not exists up_modules_translation(
        id serial NOT NULL,
        language character varying(20) COLLATE pg_catalog."default" NOT NULL,
        name character varying(250) COLLATE pg_catalog."default" NOT NULL,
        label character varying(250) COLLATE pg_catalog."default" NOT NULL,
        tooltip text COLLATE pg_catalog."default" NOT NULL,
        created timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated timestamp with time zone,
        CONSTRAINT up_modules_translation_pkey PRIMARY KEY (id)
        ,CONSTRAINT up_modules_translation_language_name_key UNIQUE (language, name)
    );
    
    CREATE TABLE  if not exists public.up_scenario(
        id serial,
        name character varying(45) COLLATE pg_catalog."default" NOT NULL,
        description character varying(45) COLLATE pg_catalog."default" NOT NULL,
        owner_id integer,
        is_base integer,
        study_area bigint,
        created timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated timestamp with time zone,
        CONSTRAINT scenario_pkey PRIMARY KEY (id),
        CONSTRAINT up_scenario_oskari_users_id_fkey FOREIGN KEY (owner_id)
                REFERENCES public.oskari_users(id) MATCH SIMPLE
                ON UPDATE NO ACTION
                ON DELETE CASCADE,
        CONSTRAINT up_scenario_user_layer_id_fkey FOREIGN KEY (study_area)
                REFERENCES public.user_layer(id) MATCH SIMPLE
                ON UPDATE NO ACTION
                ON DELETE CASCADE
    );
    
    CREATE TABLE  if not exists public.up_assumptions(
        id serial,
        study_area bigint,
        scenario int,
        category varchar(45),
        name varchar(45),
        value double precision,
        units text,
        description text,
        "source" text,
        created timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated timestamp with time zone,
        CONSTRAINT up_assumptions_pkey PRIMARY KEY (id),
        CONSTRAINT up_assumptions_user_layer_id_fkey FOREIGN KEY (study_area)
                REFERENCES public.user_layer(id) MATCH SIMPLE
                ON UPDATE NO ACTION
                ON DELETE CASCADE,
        CONSTRAINT up_assumptions_up_scenario_id_fkey FOREIGN KEY (scenario) REFERENCES up_scenario (id)
            MATCH SIMPLE
                ON UPDATE NO ACTION
                ON DELETE CASCADE,
        CONSTRAINT up_assumptions_study_area_scenario_category_name_key UNIQUE (study_area,scenario,category,name)
    );
    
    
    CREATE TABLE  if not exists public.up_layers(
        id serial,
        language character varying(20) COLLATE pg_catalog."default" NOT NULL,
        name character varying(45) COLLATE pg_catalog."default" NOT NULL,
        label character varying(45) COLLATE pg_catalog."default" NOT NULL,
        description text,
        created timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated timestamp with time zone,
        CONSTRAINT up_layers_pkey PRIMARY KEY (id),
        CONSTRAINT up_layers_language_name_key UNIQUE (language, name)
    );
    CREATE TABLE  if not exists public.up_layers_fields(
        id serial,
        up_layers_id integer not null,
        name character varying(45) COLLATE pg_catalog."default" NOT NULL,
        label character varying(45) COLLATE pg_catalog."default" NOT NULL,
        language character varying(20),
        created timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated timestamp with time zone,
        CONSTRAINT up_layers_fields_pkey PRIMARY KEY (id),
        CONSTRAINT up_layers_fields_up_layers_id_fkey FOREIGN KEY (up_layers_id)
                REFERENCES public.up_layers(id) MATCH SIMPLE
                ON UPDATE NO ACTION
                ON DELETE CASCADE,
        CONSTRAINT up_layers_fields_up_layers_id_language_name_key 
        UNIQUE (up_layers_id,language, name)
    );

    create table if not exists st_table_fields(
        id serial,
        "table" character varying(45) COLLATE pg_catalog."default" NOT NULL,
        name character varying(45) COLLATE pg_catalog."default" NOT NULL,
        label character varying(45) COLLATE pg_catalog."default" NOT NULL,
        language character varying(20) COLLATE pg_catalog."default" NOT NULL,
        created timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated timestamp with time zone,
        CONSTRAINT st_layers_fields_pkey PRIMARY KEY (id),
        CONSTRAINT st_table_fields_language_name_key UNIQUE ("table", name, language)
    );

    
    create table if not exists up_scenario_modules(
        id serial NOT NULL,
        module integer NOT NULL,
        scenario integer NOT NULL,
        created timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated timestamp with time zone,
        CONSTRAINT up_scenario_modules_pkey PRIMARY KEY (id),
        CONSTRAINT up_scenario_modules_up_modules_translation_id_fkey FOREIGN KEY (module)
                REFERENCES public.up_modules_translation(id) MATCH SIMPLE
                ON UPDATE NO ACTION
                ON DELETE CASCADE,
        CONSTRAINT up_scenario_modules_up_scenario_id_fkey FOREIGN KEY (scenario)
                REFERENCES public.up_scenario(id) MATCH SIMPLE
                ON UPDATE NO ACTION
                ON DELETE CASCADE
    );
    
    create table if not exists up_scenarios_layers(
        id serial NOT NULL,
        up_scenarios_id integer,
        source_layer int,
        target_layer int,
        created timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated timestamp with time zone,
        CONSTRAINT up_scenarios_layers_pkey PRIMARY KEY (id),
        CONSTRAINT up_scenarios_layers_up_scenario_id_fkey FOREIGN KEY (up_scenarios_id)
        REFERENCES public.up_scenario(id) MATCH SIMPLE
                ON UPDATE NO ACTION
                ON DELETE CASCADE,
        CONSTRAINT up_scenarios_layers_user_layer_id_fkey FOREIGN KEY (source_layer)
                REFERENCES public.user_layer(id) MATCH SIMPLE
                ON UPDATE NO ACTION
                ON DELETE CASCADE,
        CONSTRAINT up_scenarios_layers_up_layers_id_fkey FOREIGN KEY (target_layer)
                REFERENCES public.up_layers(id) MATCH SIMPLE
                ON UPDATE NO ACTION
                ON DELETE CASCADE       
        
    );
    
    create table if not exists layers_space(
        id bigserial not null,
        user_layer_id int not null,
        space VARCHAR(50),-- allowed values public, suitability, urbanperformance
        created timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated timestamp with time zone,
        CONSTRAINT layers_space_pkey PRIMARY KEY (id),
        CONSTRAINT layers_space_user_layer_id_fkey FOREIGN KEY (user_layer_id)
                REFERENCES public.user_layer(id) MATCH SIMPLE
                ON UPDATE NO ACTION
                ON DELETE CASCADE
    );
    create table if not exists st_layers(
        id bigserial not null,
        user_layer_id bigint not null,
        layer_field text NOT NULL,
        layer_mmu_code text NOT NULL,
        st_layer_label text NOT NULL,
        created timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated timestamp with time zone,
        CONSTRAINT st_layers_pkey PRIMARY KEY (id),
        CONSTRAINT st_layers_user_layer_id_fkey FOREIGN KEY (user_layer_id) REFERENCES user_layer (id)
            MATCH SIMPLE
                ON UPDATE NO ACTION
                ON DELETE CASCADE,
        CONSTRAINT st_layers_user_layer_id_key UNIQUE (user_layer_id)
    );
    CREATE INDEX  if not exists st_layers_user_layer_id
    ON public.st_layers USING btree
    (user_layer_id ASC NULLS LAST)
    TABLESPACE pg_default;

    create table if not exists st_settings(
        id bigserial not null,
	st_layers_id BIGINT not null,
	normalization_method  int not null,
	range_min double PRECISION not null,
	range_max double PRECISION not null,
	smaller_better integer not null,
	weight double PRECISION not null,
        created timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated timestamp with time zone,
	CONSTRAINT st_settings_pkey PRIMARY KEY (id),
	CONSTRAINT st_settings_st_layers_id_fkey FOREIGN KEY (st_layers_id)
            REFERENCES public.st_layers(id) MATCH SIMPLE
            ON UPDATE NO ACTION
            ON DELETE CASCADE
    );
    CREATE INDEX  if not exists  st_settings_st_layers_id_idx
    ON public.st_settings USING btree
    (st_layers_id ASC NULLS LAST)
    TABLESPACE pg_default;

    CREATE TABLE if not exists st_operation(
        id serial,
        name character varying(20),
        CONSTRAINT st_operation_pkey PRIMARY KEY (id),
        CONSTRAINT st_operation_name_key UNIQUE (name)
    );
    insert into st_operation(name) values ('union'),('intersection'),('difference') on conflict(name) do nothing;

    create table if not exists st_join_options(
        id serial not null,
        value int not null,
        language varchar(20) not null,
        label varchar(50) not null,
        created timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated timestamp with time zone,
        CONSTRAINT st_join_options_options_pkey PRIMARY KEY (id),
        CONSTRAINT st_join_options_st_operation_fk
            FOREIGN KEY (value)
            REFERENCES st_operation(id)
            ON DELETE CASCADE,
        CONSTRAINT st_join_options_value_language_key UNIQUE (value,  language)
    );

    INSERT INTO public.st_join_options(
	value,  language, label)
	VALUES (1,'english','Union'),
        (2,'english','Intersection'),
        (3,'english','Difference') on conflict(value,  language) do nothing;

    

    CREATE TABLE if not exists st_normalization(
        id serial,
        name character varying(20),
        CONSTRAINT st_normalization_pkey PRIMARY KEY (id),
        CONSTRAINT st_normalization_name_key UNIQUE (name)
    );

    insert into st_normalization(name) values ('observe'),('benchmark'),('standardize') on conflict(name) do nothing;

    create table if not exists st_normalization_method_options(
	id serial not null,
    value int not null,
	language varchar(20) not null,
	label varchar(50) not null,
        created timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated timestamp with time zone,
	CONSTRAINT st_normalization_method_options_pkey PRIMARY KEY (id),
    CONSTRAINT st_normalization_method_options_st_normalization_fk
        FOREIGN KEY (value)
        REFERENCES st_normalization(id)
        ON DELETE CASCADE,
    CONSTRAINT st_normalization_method_options_value_language_key UNIQUE (value,  language)
    );

    INSERT INTO public.st_normalization_method_options(
	value, language, label)
	VALUES (1,'english','Observe'),
        (2,'english','Benchmark'),
        (3,'english','Standardize') on conflict(value,  language) do nothing;
    DELETE FROM public.st_normalization_method_options WHERE id = 1;

    create table if not exists st_normalization_type_options(
            id serial not null,
            value int not null,
            type  varchar(20) not null,
            language varchar(20) not null,
            label varchar(50) not null,
            created timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
            updated timestamp with time zone,
            CONSTRAINT st_normalization_type_options_pkey PRIMARY KEY (id)
    );

    create table if not exists st_filters(
        id bigserial not null,
        user_layer_id bigint not null,
        st_filter_label text NOT NULL,
        created timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated timestamp with time zone,
        CONSTRAINT st_filters_pkey PRIMARY KEY (id),
        CONSTRAINT st_filters_user_layer_id_fkey FOREIGN KEY (user_layer_id) REFERENCES user_layer (id)
            MATCH SIMPLE
                ON UPDATE NO ACTION
                ON DELETE CASCADE,
        CONSTRAINT st_filters_user_layer_id_key UNIQUE (user_layer_id)
    );
    CREATE INDEX  if not exists st_filters_user_layer_id_idx
    ON public.st_filters USING btree
    (user_layer_id ASC NULLS LAST)
    TABLESPACE pg_default;

    create table if not exists institutions(
        id bigserial not null,
        members integer,
        created timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated timestamp with time zone,
        CONSTRAINT institutions_pkey PRIMARY KEY (id),
        CONSTRAINT institutions_oskari_users_id_fkey FOREIGN KEY (members)
                REFERENCES public.oskari_users(id) MATCH SIMPLE
                ON UPDATE NO ACTION
                ON DELETE CASCADE
    );

    create table if not exists up_scenario_buffers(
        id bigserial not null,
        scenario integer,
        user_layer_id bigint not null,
        CONSTRAINT up_scenario_buffers_user_layer_id_fkey FOREIGN KEY (user_layer_id) REFERENCES user_layer (id)
            MATCH SIMPLE
                ON UPDATE NO ACTION
                ON DELETE CASCADE,
        CONSTRAINT scenario_user_layer_id_key UNIQUE (scenario,user_layer_id)
    );

    CREATE INDEX  if not exists  up_scenario_buffers_user_layer_id_idx
    ON public.up_scenario_buffers USING btree
    (user_layer_id ASC NULLS LAST)
    TABLESPACE pg_default;

    create table if not exists upt_user_layer_scope(
        id bigserial not null,
        user_layer_id bigint not null,
        is_public int,
        CONSTRAINT upt_user_layer_scope_user_layer_id_fkey FOREIGN KEY (user_layer_id) REFERENCES user_layer (id)
            MATCH SIMPLE
                ON UPDATE NO ACTION
                ON DELETE CASCADE,
        CONSTRAINT upt_user_layer_scope_id_key UNIQUE (user_layer_id)
    );

    CREATE INDEX  if not exists  upt_user_layer_scope_user_layer_id_idx
    ON public.upt_user_layer_scope USING btree
    (user_layer_id ASC NULLS LAST)
    TABLESPACE pg_default;

    
    CREATE TABLE if not exists st_tables
    (
        id serial,
        language character varying(20) COLLATE pg_catalog."default" NOT NULL,
        name character varying(45) COLLATE pg_catalog."default" NOT NULL,
        label character varying(45) COLLATE pg_catalog."default" NOT NULL,
        description text COLLATE pg_catalog."default",
        created timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated timestamp with time zone,
        CONSTRAINT st_tables_pkey PRIMARY KEY (id),
        CONSTRAINT st_tables_language_name_key UNIQUE (language, name)
    );
    CREATE TABLE if not exists st_tables_fields
    (
        id serial,
        st_tables_id integer NOT NULL,
        name character varying(45) COLLATE pg_catalog."default" NOT NULL,
        label character varying(45) COLLATE pg_catalog."default" NOT NULL,
        language character varying(20) COLLATE pg_catalog."default",
        created timestamp with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
        updated timestamp with time zone,
        CONSTRAINT st_tables_fields_pkey PRIMARY KEY (id),
        CONSTRAINT st_tables_fields_st_tables_id_language_name_key UNIQUE (st_tables_id, language, name),
        CONSTRAINT st_tables_fields_st_tables_id_fkey FOREIGN KEY (st_tables_id)
            REFERENCES st_tables (id) MATCH SIMPLE
            ON UPDATE NO ACTION
            ON DELETE CASCADE
    );
end;


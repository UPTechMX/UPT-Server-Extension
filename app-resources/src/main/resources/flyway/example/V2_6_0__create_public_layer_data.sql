CREATE TABLE public.public_layer_data
(
    id bigint NOT NULL DEFAULT nextval('public_layer_data_id_seq'::regclass),
    public_layer_id bigint NOT NULL,
    uuid character varying(64) COLLATE pg_catalog."default",
    feature_id character varying(64) COLLATE pg_catalog."default",
    property_json json,
    geometry geometry NOT NULL,
    created timestamp with time zone NOT NULL DEFAULT now(),
    updated timestamp with time zone,
    CONSTRAINT "public_layer_data_pKey" PRIMARY KEY (id),
    CONSTRAINT public_layer_data_user_layer_fkey FOREIGN KEY (public_layer_id)
        REFERENCES public.oskari_maplayer (id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE CASCADE
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;

ALTER TABLE public.public_layer_data
    OWNER to oskari;
COMMENT ON TABLE public.public_layer_data
    IS 'User imported layer features';

-- Index: public_layer_data_geom_idx

-- DROP INDEX public.public_layer_data_geom_idx;

CREATE INDEX public_layer_data_geom_idx
    ON public.public_layer_data USING gist
    (geometry)
    TABLESPACE pg_default;

-- Index: public_layer_data_user_layer_id_idx

-- DROP INDEX public.public_layer_data_user_layer_id_idx;

CREATE INDEX public_layer_data_public_layer_id_idx
    ON public.public_layer_data USING btree
    (public_layer_id)
    TABLESPACE pg_default;

-- Trigger: trigger_user_layer_update

-- DROP TRIGGER trigger_user_layer_update ON public.public_layer_data;

CREATE TRIGGER trigger_public_layer_update
    BEFORE INSERT OR UPDATE 
    ON public.public_layer_data
    FOR EACH ROW
    EXECUTE PROCEDURE public.procedure_public_layer_data_update();

-- SEQUENCE: public.user_layer_data_id_seq

-- DROP SEQUENCE public.user_layer_data_id_seq;

CREATE SEQUENCE public.public_layer_data_id_seq
    INCREMENT 1
    START 1
    MINVALUE 1
    MAXVALUE 9223372036854775807
    CACHE 1;

ALTER SEQUENCE public.public_layer_data_id_seq
    OWNER TO oskari;

-- Table: public.public_layer_data

-- DROP TABLE public.public_layer_data;
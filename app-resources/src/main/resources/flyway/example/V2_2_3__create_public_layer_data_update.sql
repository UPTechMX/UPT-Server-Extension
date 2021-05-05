CREATE OR REPLACE FUNCTION public.procedure_public_layer_data_update()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
  BEGIN
    IF (TG_OP = 'UPDATE')
    THEN
      NEW.updated := current_timestamp;
      RETURN NEW;
    ELSIF (TG_OP = 'INSERT')
      THEN
        NEW.created := current_timestamp;
        RETURN NEW;
    END IF;
    RETURN NEW;
  END;
  $function$

package org.oskari.example.st;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.example.PostStatus;
import org.oskari.example.Tables;
import org.springframework.web.client.RestTemplate;

@OskariActionRoute("st_copy_public_layers")
public class STCopyPublicLayers extends RestActionHandler {
  private static String stURL;
  private static String stUser;
  private static String stPassword;
  private static final Logger log = LogFactory.getLogger(
    STSettingsHandler.class
  );

  private static String stwsHost;
  private static String stwsPort;
  private static String stProjection;

  private JSONArray errors;
  private ObjectMapper Obj;

  @Override
  public void preProcess(ActionParameters params) throws ActionException {
    // common method called for all request methods
    log.info(params.getUser(), "accessing route", getName());
    PropertyUtil.loadProperties("/oskari-ext.properties");
    stURL = PropertyUtil.get("db.url");
    stUser = PropertyUtil.get("db.username");
    stPassword = PropertyUtil.get("db.password");

    stwsHost = PropertyUtil.get("stws.db.host");
    stwsPort = PropertyUtil.get("stws.db.port");
    stProjection =
      PropertyUtil
        .get("oskari.native.srs")
        .substring(PropertyUtil.get("oskari.native.srs").indexOf(":") + 1);

    errors = new JSONArray();
    Obj = new ObjectMapper();
  }

  @Override
  public void handleGet(ActionParameters params) throws ActionException {
    try {
      params.requireLoggedInUser();
      errors.put(
        JSONHelper.createJSONObject(
          Obj.writeValueAsString(
            new PostStatus("OK", "This method have not been implemented")
          )
        )
      );
      ResponseHelper.writeResponse(
        params,
        new JSONObject().put("Errors", errors)
      );
    } catch (JsonProcessingException | JSONException ex) {
      java
        .util.logging.Logger.getLogger(STCopyLayers.class.getName())
        .log(Level.SEVERE, null, ex);
    }
  }

  @Override
  public void handlePost(ActionParameters params) throws ActionException {
    PostStatus status = null;
    Long user_id = params.getUser().getId();
    try {
      params.requireLoggedInUser();
      if (
        params.getRequiredParam("layerSTName") != null &&
        params.getRequiredParam("layerName") != null &&
        params.getRequiredParam("tableST") != null &&
        params.getRequiredParam("table") != null &&
        params.getRequiredParam("studyAreaId") != null
      ) {
        switch (params.getRequiredParam("layerSTName")) {
          case "mmu":
            this.setMmu(
                params.getRequiredParam("layerSTName"),
                params.getRequiredParam("layerName"),
                params.getRequest().getParameterValues("tableST"),
                params.getRequest().getParameterValues("table"),
                params.getRequiredParam("studyAreaId"),
                user_id
              );
            break;
          case "amenities":
            this.setAmenities(
                params.getRequiredParam("layerSTName"),
                params.getRequiredParam("layerName"),
                params.getRequest().getParameterValues("tableST"),
                params.getRequest().getParameterValues("table"),
                params.getRequiredParam("studyAreaId"),
                user_id
              );
            break;
        }
      }
      ResponseHelper.writeResponse(params, errors);
    } catch (Exception e) {
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
          )
        );
        ResponseHelper.writeError(
          params,
          "",
          500,
          new JSONObject().put("Errors", errors)
        );
      } catch (JsonProcessingException ex) {
        java
          .util.logging.Logger.getLogger(LayersSTHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java
          .util.logging.Logger.getLogger(LayersSTHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
    }
  }

  @Override
  public void handlePut(ActionParameters params) throws ActionException {
    params.requireLoggedInUser();
  }

  @Override
  public void handleDelete(ActionParameters params) throws ActionException {
    params.requireLoggedInUser();
  }

  private void setAmenities(
    String layerUP,
    String layer,
    String[] tableup,
    String[] table,
    String studyArea,
    Long user_id
  )
    throws Exception {
    String tableUP[] = new String[tableup.length + 4];
    System.arraycopy(tableup, 0, tableUP, 0, tableup.length);
    tableUP[tableup.length] = "study_area";
    tableUP[tableup.length + 1] = "oskari_code";
    tableUP[tableup.length + 2] = "layer_id";
    tableUP[tableup.length + 3] = "user_id";

    PostStatus postStatus = new PostStatus();
    String values = " ";
    for (int i = 0; i < tableUP.length; i++) {
      switch (tableUP[i]) {
        case "location":
          values +=
            " st_astext(st_transform(st_setsrid(" +
            table[i] +
            "," +
            stProjection +
            "),4326)) as " +
            tableUP[i];
          break;
        case "study_area":
          values += studyArea + " as " + tableUP[i];
          break;
        case "layer_id":
          values += layer + " as " + tableUP[i];
          break;
        case "user_id":
          values += user_id.toString() + " as " + tableUP[i];
          break;
        case "oskari_code":
          values += "public_layer_data.id as " + tableUP[i];
          break;
        default:
          values +=
            " trim(both '\"' from CAST(property_json->'" +
            table[i] +
            "' AS text))  as " +
            tableUP[i];
          break;
      }
      if (i < tableUP.length - 1) {
        values += ",";
      } else {
        values = values.replaceAll(",$", "");
      }
    }
    String errorMsg = "";
    String query = "";
    try (
      Connection connection = DriverManager.getConnection(
        stURL,
        stUser,
        stPassword
      )
    ) {
      Statement statement = connection.createStatement();
      query =
        "select distinct " +
        values +
        " from oskari_maplayer\n" +
        " inner join public_layer_data on oskari_maplayer.id = public_layer_data.public_layer_id\n" +
        " where oskari_maplayer.id=" +
        layer;

      errors.put(
        JSONHelper.createJSONObject(
          Obj.writeValueAsString(
            new PostStatus("OK", "Executing query: " + query)
          )
        )
      );

      ResultSet data = statement.executeQuery(query);
      ArrayList<STAmenities> data_in = new ArrayList<>();

      while (data.next()) {
        Object o = new STAmenities();
        Class<?> c = o.getClass();
        for (String tableUP1 : tableUP) {
          Field f = c.getDeclaredField(tableUP1);
          f.setAccessible(true);
          switch (tableUP1) {
            case "study_area":
              {
                Long val = (Long) data.getLong(tableUP1);
                f.set(o, val);
                break;
              }
            case "layer_id":
              {
                Long val = (Long) data.getLong(tableUP1);
                f.set(o, val);
                break;
              }
            case "user_id":
              {
                Long val = (Long) data.getLong(tableUP1);
                f.set(o, val);
                break;
              }
            case "oskari_code":
              {
                Long val = (Long) data.getLong(tableUP1);
                f.set(o, val);
                break;
              }
            case "location":
              f.set(o, data.getString(tableUP1));
              break;
            case "fclass":
              f.set(o, data.getString(tableUP1));
              break;
            case "amenities_id":
              break;
            default:
              break;
          }
        }
        data_in.add((STAmenities) o);
      }
      Tables<STAmenities> final_data = new Tables<>(data_in);

      RestTemplate restTemplate = new RestTemplate();
      restTemplate.postForObject(
        "http://" + stwsHost + ":" + stwsPort + "/amenities/",
        final_data,
        PostStatus.class
      );
      errors.put(
        JSONHelper.createJSONObject(
          Obj.writeValueAsString(new PostStatus("OK", "Amenities data copied"))
        )
      );
    } catch (Exception e) {
      log.error(e);
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
          )
        );
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Detail", e.getMessage()))
          )
        );
      } catch (JsonProcessingException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
      throw new Exception();
    }
  }

  private void setMmu(
    String layerUP,
    String layer,
    String[] tableup,
    String[] table,
    String studyArea,
    Long user_id
  )
    throws Exception {
    PostStatus postStatus = new PostStatus();
    String values = "";
    String tableUP[] = new String[tableup.length + 4];
    System.arraycopy(tableup, 0, tableUP, 0, tableup.length);
    tableUP[tableup.length] = "study_area";
    tableUP[tableup.length + 1] = "oskari_code";
    tableUP[tableup.length + 2] = "layer_id";
    tableUP[tableup.length + 3] = "user_id";

    for (int i = 0; i < tableUP.length; i++) {
      switch (tableUP[i]) {
        case "location":
          values +=
            " st_astext(st_transform(st_setsrid(" +
            table[i] +
            "," +
            stProjection +
            "),4326)) as " +
            tableUP[i];
          break;
        case "study_area":
          values += studyArea + " as " + tableUP[i];
          break;
        case "layer_id":
          values += layer + " as " + tableUP[i];
          break;
        case "user_id":
          values += user_id + " as " + tableUP[i];
          break;
        case "oskari_code":
          values += " public_layer_data.id as " + tableUP[i];
          break;
        default:
          values +=
            " trim(both '\"' from CAST(property_json->'" +
            table[i] +
            "' AS text))  as " +
            tableUP[i];
          break;
      }
      if (i < tableUP.length - 1) {
        values += ",";
      } else {
        values = values.replaceAll(",$", "");
      }
    }
    String errorMsg = "";
    String query = "";
    try (
      Connection connection = DriverManager.getConnection(
        stURL,
        stUser,
        stPassword
      )
    ) {
      Statement statement = connection.createStatement();
      query =
        "select distinct " +
        values +
        " from oskari_maplayer\n" +
        " inner join public_layer_data on oskari_maplayer.id = public_layer_data.public_layer_id\n" +
        " where oskari_maplayer.id=" +
        layer;

      errors.put(
        JSONHelper.createJSONObject(
          Obj.writeValueAsString(
            new PostStatus("OK", "Executing query: " + query)
          )
        )
      );

      ResultSet data = statement.executeQuery(query);

      ArrayList<STMmu> data_in = new ArrayList<>();

      while (data.next()) {
        Object o = new STMmu();
        Class<?> c = o.getClass();
        for (int i = 0; i < tableUP.length; i++) {
          Field f = c.getDeclaredField(tableUP[i]);
          f.setAccessible(true);
          if (tableUP[i].equals("study_area")) {
            Long val = (Long) data.getLong(tableUP[i]);
            f.set(o, val);
          } else if (tableUP[i].equals("layer_id")) {
            Long val = (Long) data.getLong(tableUP[i]);
            f.set(o, val);
          } else if (tableUP[i].equals("user_id")) {
            Long val = (Long) data.getLong(tableUP[i]);
            f.set(o, val);
          } else if (tableUP[i].equals("oskari_code")) {
            Long val = (Long) data.getLong(tableUP[i]);
            f.set(o, val);
          } else if (tableUP[i].equals("location")) {
            f.set(o, data.getString(tableUP[i]));
          } else if (tableUP[i].equals("mmu_id")) {}
        }
        data_in.add((STMmu) o);
        //return postStatus;
      }
      Tables<STMmu> final_data = new Tables<>(data_in);

      RestTemplate restTemplate = new RestTemplate();
      postStatus =
        restTemplate.postForObject(
          "http://" + stwsHost + ":" + stwsPort + "/mmu/",
          final_data,
          PostStatus.class
        );

      errors.put(
        JSONHelper.createJSONObject(
          Obj.writeValueAsString(new PostStatus("OK", "MMU data copied"))
        )
      );
    } catch (Exception e) {
      log.error(e);
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
          )
        );
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Detail", e.getMessage()))
          )
        );
      } catch (JsonProcessingException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
      throw new Exception();
    }
  }
}

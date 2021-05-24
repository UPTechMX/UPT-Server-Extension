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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.example.PostStatus;
import org.oskari.example.UPTRoles;

@OskariActionRoute("st_settings")
public class STSettingsHandler extends RestActionHandler {
  private static String stURL;
  private static String stUser;
  private static String stPassword;
  private static final Logger log = LogFactory.getLogger(
    STSettingsHandler.class
  );
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
    stProjection =
      PropertyUtil
        .get("oskari.native.srs")
        .substring(PropertyUtil.get("oskari.native.srs").indexOf(":") + 1);

    errors = new JSONArray();
    Obj = new ObjectMapper();
  }

  @Override
  public void handleGet(ActionParameters params) throws ActionException {
    String errorMsg = "Filters get";
    Long user_id = params.getUser().getId();
    Long layerId = Long.parseLong(params.getRequiredParam("st_layer_id"));

    ArrayList<STSettings> modules = new ArrayList<>();
    try (
      Connection connection = DriverManager.getConnection(
        stURL,
        stUser,
        stPassword
      );
    ) {
      params.requireLoggedInUser();
      ArrayList<String> roles = new UPTRoles()
      .handleGet(params, params.getUser());
      if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
        throw new Exception("User privilege is not enough for this action");
      }

      PreparedStatement statement = connection.prepareStatement(
        "with study_area as(\n" +
        "	select st_transform(st_setsrid(geometry,?),4326) as geometry from user_layer_data where user_layer_id=?\n" +
        "),layers as(\n" +
        "	select user_layer.id from user_layer,study_area where  st_intersects(st_geomfromtext(user_layer.wkt,4326),study_area.geometry) \n" +
        ")\n" +
        "SELECT \n" +
        "	st_settings.id, \n" +
        "	st_layers_id as st_layer_id, \n" +
        "	normalization_method, \n" +
        "	range_min, \n" +
        "	range_max, \n" +
        "	smaller_better, \n" +
        "	weight,\n" +
        "	st_layers.st_layer_label as label\n" +
        "FROM public.st_settings\n" +
        "right join st_layers on st_layers.id=st_settings.st_layers_id\n" +
        ",layers\n" +
        "where st_layers.user_layer_id in(layers.id)"
      );
      statement.setInt(1, Integer.parseInt(stProjection));
      statement.setInt(2, layerId.intValue());

      errors.put(
        JSONHelper.createJSONObject(
          Obj.writeValueAsString(
            new PostStatus("OK", "Executing query: " + statement.toString())
          )
        )
      );

      ResultSet data = statement.executeQuery();
      while (data.next()) {
        STSettings layer = new STSettings(String.valueOf(layerId));
        layer.id = data.getLong("id");
        layer.normalization_method =
          data.getInt("normalization_method") != 0
            ? data.getInt("normalization_method")
            : 1;
        layer.range_min =
          data.getDouble("range_min") != 0 ? data.getDouble("range_min") : 0;
        layer.range_max =
          data.getDouble("range_max") != 0 ? data.getDouble("range_max") : 1;
        layer.smaller_better =
          data.getInt("smaller_better") != 0
            ? data.getInt("smaller_better")
            : 0;
        layer.weight =
          data.getDouble("weight") != 0 ? data.getDouble("weight") : 1;
        layer.label = data.getString("label");
        modules.add(layer);
      }
      if (modules.isEmpty()) {
        STSettings layer = new STSettings(String.valueOf(layerId));
        modules.add(layer);
      }
      JSONArray out = new JSONArray();

      for (STSettings index : modules) {
        //Convert to Json Object
        ObjectMapper Obj = new ObjectMapper();
        final JSONObject json = JSONHelper.createJSONObject(
          Obj.writeValueAsString(index)
        );
        out.put(json);
      }
      errors.put(
        JSONHelper.createJSONObject(
          Obj.writeValueAsString(
            new PostStatus("OK", "Filters getter executed")
          )
        )
      );

      ResponseHelper.writeResponse(params, out);
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
          .util.logging.Logger.getLogger(STSettingsHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java
          .util.logging.Logger.getLogger(STSettingsHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }

      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);
    }
  }

  @Override
  public void handlePost(ActionParameters params) throws ActionException {
    Long st_layer_id = Long.parseLong(params.getRequiredParam("st_layer_id"));
    Integer normalization_method = Integer.parseInt(
      params.getRequiredParam("normalization_method")
    );
    Double range_min = Double.parseDouble(params.getRequiredParam("range_min"));
    Double range_max = Double.parseDouble(params.getRequiredParam("range_max"));
    Integer smaller_better = Integer.parseInt(
      params.getRequiredParam("smaller_better")
    );
    Double weight = Double.parseDouble(params.getRequiredParam("weight"));

    PostStatus status = new PostStatus();
    try (
      Connection connection = DriverManager.getConnection(
        stURL,
        stUser,
        stPassword
      );
      PreparedStatement statement = connection.prepareStatement(
        "INSERT INTO public.st_settings(\n" +
        "	st_layers_id, normalization_method, range_min, range_max, smaller_better, weight)\n" +
        "	VALUES (?, ?, ?, ?, ?, ?);"
      );
    ) {
      params.requireLoggedInUser();
      ArrayList<String> roles = new UPTRoles()
      .handleGet(params, params.getUser());
      if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
        throw new Exception("User privilege is not enough for this action");
      }

      statement.setLong(1, st_layer_id);
      statement.setInt(2, normalization_method);
      statement.setDouble(3, range_min);
      statement.setDouble(4, range_max);
      statement.setInt(5, smaller_better);
      statement.setDouble(6, weight);

      errors.put(
        JSONHelper.createJSONObject(
          Obj.writeValueAsString(
            new PostStatus("OK", "Executing query: " + statement.toString())
          )
        )
      );

      statement.execute();

      errors.put(
        JSONHelper.createJSONObject(
          Obj.writeValueAsString(new PostStatus("OK", "Settings registered"))
        )
      );
      ResponseHelper.writeResponse(
        params,
        new JSONObject().put("Errors", errors)
      );
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
          .util.logging.Logger.getLogger(STSettingsHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java
          .util.logging.Logger.getLogger(STSettingsHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }

      log.error(e);
    }
  }

  @Override
  public void handlePut(ActionParameters params) throws ActionException {
    Long id = Long.parseLong(params.getRequiredParam("id"));
    Long st_layer_id = Long.parseLong(params.getRequiredParam("st_layer_id"));
    Integer normalization_method = Integer.parseInt(
      params.getRequiredParam("normalization_method")
    );
    Double range_min = Double.parseDouble(params.getRequiredParam("range_min"));
    Double range_max = Double.parseDouble(params.getRequiredParam("range_max"));
    Integer smaller_better = Integer.parseInt(
      params.getRequiredParam("smaller_better")
    );
    Double weight = Double.parseDouble(params.getRequiredParam("weight"));

    PostStatus status = new PostStatus();
    String query = "";
    try (
      Connection connection = DriverManager.getConnection(
        stURL,
        stUser,
        stPassword
      );
      PreparedStatement statement = connection.prepareStatement(
        "update public.st_settings set(st_layers_id,normalization_method,range_min,range_max,smaller_better,weight)= ( ?, ?, ?, ?, ?, ?) where id=?;"
      );
    ) {
      params.requireLoggedInUser();
      ArrayList<String> roles = new UPTRoles()
      .handleGet(params, params.getUser());
      if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
        throw new Exception("User privilege is not enough for this action");
      }

      statement.setLong(1, st_layer_id);
      statement.setInt(2, normalization_method);
      statement.setDouble(3, range_min);
      statement.setDouble(4, range_max);
      statement.setInt(5, smaller_better);
      statement.setDouble(6, weight);
      statement.setLong(7, id);

      errors.put(
        JSONHelper.createJSONObject(
          Obj.writeValueAsString(
            new PostStatus("OK", "Executing query: " + statement.toString())
          )
        )
      );

      statement.execute();

      errors.put(
        JSONHelper.createJSONObject(
          Obj.writeValueAsString(new PostStatus("OK", "Settings updated"))
        )
      );
      ResponseHelper.writeResponse(
        params,
        new JSONObject().put("Errors", errors)
      );
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
          .util.logging.Logger.getLogger(STSettingsHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java
          .util.logging.Logger.getLogger(STSettingsHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
      log.error(e);
    }
  }

  @Override
  public void handleDelete(ActionParameters params) throws ActionException {
    Long id = Long.parseLong(params.getRequiredParam("id"));

    PostStatus status = new PostStatus();
    String query = "";
    try (
      Connection connection = DriverManager.getConnection(
        stURL,
        stUser,
        stPassword
      );
      PreparedStatement statement = connection.prepareStatement(
        "delete from public.st_settings where  id = ?;"
      );
    ) {
      params.requireLoggedInUser();
      ArrayList<String> roles = new UPTRoles()
      .handleGet(params, params.getUser());
      if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
        throw new Exception("User privilege is not enough for this action");
      }

      errors.put(
        JSONHelper.createJSONObject(
          Obj.writeValueAsString(
            new PostStatus("OK", "Executing query: " + statement.toString())
          )
        )
      );

      statement.setLong(1, id);
      statement.execute();

      errors.put(
        JSONHelper.createJSONObject(
          Obj.writeValueAsString(new PostStatus("OK", "Settings deleted"))
        )
      );
      ResponseHelper.writeResponse(
        params,
        new JSONObject().put("Errors", errors)
      );
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
          .util.logging.Logger.getLogger(STSettingsHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java
          .util.logging.Logger.getLogger(STSettingsHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
      log.error(e);
    }
  }
}

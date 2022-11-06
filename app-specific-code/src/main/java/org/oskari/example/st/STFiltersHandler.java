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

@OskariActionRoute("st_filters")
public class STFiltersHandler extends RestActionHandler {
  private static String stURL;
  private static String stUser;
  private static String stPassword;
  private static final Logger log = LogFactory.getLogger(LayersSTHandler.class);

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

    errors = new JSONArray();
    Obj = new ObjectMapper();
  }

  @Override
  public void handleGet(ActionParameters params) throws ActionException {
    String errorMsg = "Filters get";
    Long user_id = params.getUser().getId();
    Long study_area;
    study_area = Long.parseLong(params.getRequiredParam("study_area"));
    ArrayList<STFilters> modules = new ArrayList<>();
    try (
        Connection connection = DriverManager.getConnection(
            stURL,
            stUser,
            stPassword);
        PreparedStatement statement = connection.prepareStatement(
            "with study_area as(\n" +
                "	select geometry from user_layer_data where user_layer_id=?\n" +
                "), user_layers as(\n" +
                "	select distinct st_filters.id,st_filters.user_layer_id,st_filter_label,st_filter_label as label\n" +
                "	from st_filters\n" +
                "		inner join user_layer_data on user_layer_data.user_layer_id = st_filters.user_layer_id\n" +
                "		, study_area\n" +
                "	where \n" +
                "		st_intersects(study_area.geometry,user_layer_data.geometry)\n" +
                "		--and user_layer_data.user_layer_id=?\n" +
                "), public_layers as(\n" +
                "	select distinct st_filters.id,st_filters.user_layer_id,st_filter_label,st_filter_label as label\n" +
                "	from st_filters\n" +
                "		inner join user_layer_data on user_layer_data.user_layer_id = st_filters.user_layer_id\n" +
                "		inner join layers_space on layers_space.user_layer_id = st_filters.user_layer_id\n" +
                "		, study_area\n" +
                "	where \n" +
                "		st_intersects(study_area.geometry,user_layer_data.geometry)\n" +
                "		and layers_space.space in ('public','suitability')\n" +
                "), all_layers as(\n" +
                "	select id,user_layer_id,st_filter_label,label from user_layers\n" +
                "	union all \n" +
                "	select id,user_layer_id,st_filter_label,label from public_layers	\n" +
                ") \n" +
                "select distinct id,user_layer_id,st_filter_label,label from all_layers  order by label ");) {
      params.requireLoggedInUser();
      ArrayList<String> roles = new UPTRoles()
          .handleGet(params, params.getUser());
      if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
        throw new Exception("User privilege is not enough for this action");
      }

      statement.setLong(1, study_area);
      errors.put(
          JSONHelper.createJSONObject(
              Obj.writeValueAsString(
                  new PostStatus("OK", "Executing query: " + statement.toString()))));

      ResultSet data = statement.executeQuery();

      while (data.next()) {
        STFilters layer = new STFilters();
        layer.id = data.getLong("id");
        layer.user_layer_id = data.getLong("user_layer_id");
        layer.st_filter_label = data.getString("st_filter_label");
        layer.label = data.getString("label");
        modules.add(layer);
      }

      JSONArray out = new JSONArray();
      for (STFilters index : modules) {
        // Convert to Json Object
        JSONObject json = JSONHelper.createJSONObject(
            Obj.writeValueAsString(index));
        out.put(json);
      }

      errors.put(
          JSONHelper.createJSONObject(
              Obj.writeValueAsString(
                  new PostStatus("OK", "Filters getter executed"))));

      ResponseHelper.writeResponse(params, out);
    } catch (SQLException e) {
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);

      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        ResponseHelper.writeError(
            params,
            "",
            500,
            new JSONObject().put("Errors", errors));
      } catch (JsonProcessingException ex) {
        java.util.logging.Logger.getLogger(STFiltersHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java.util.logging.Logger.getLogger(STFiltersHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
    } catch (JsonProcessingException ex) {
      java.util.logging.Logger.getLogger(STFiltersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
    } catch (Exception e) {
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);

      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        ResponseHelper.writeError(
            params,
            "",
            500,
            new JSONObject().put("Errors", errors));
      } catch (JsonProcessingException ex) {
        java.util.logging.Logger.getLogger(STFiltersHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java.util.logging.Logger.getLogger(STFiltersHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
    }
  }

  @Override
  public void handlePost(ActionParameters params) throws ActionException {
    Integer filterID = Integer.parseInt(params.getRequiredParam("filterId"));
    String filterLabel = params.getRequiredParam("filterLabel");

    PostStatus status = new PostStatus();
    String query = "";
    try (
        Connection connection = DriverManager.getConnection(
            stURL,
            stUser,
            stPassword);
        PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO public.st_filters( user_layer_id, st_filter_label)VALUES ( ?, ?);");) {
      params.requireLoggedInUser();
      ArrayList<String> roles = new UPTRoles()
          .handleGet(params, params.getUser());
      if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
        throw new Exception("User privilege is not enough for this action");
      }

      statement.setInt(1, filterID);
      statement.setString(2, filterLabel);

      errors.put(
          JSONHelper.createJSONObject(
              Obj.writeValueAsString(
                  new PostStatus("OK", "Executing query: " + statement.toString()))));

      statement.execute();

      errors.put(
          JSONHelper.createJSONObject(
              Obj.writeValueAsString(new PostStatus("OK", "Filter registered"))));
      ResponseHelper.writeResponse(
          params,
          new JSONObject().put("Errors", errors));
    } catch (Exception e) {
      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        ResponseHelper.writeError(
            params,
            "",
            500,
            new JSONObject().put("Errors", errors));
      } catch (JsonProcessingException ex) {
        java.util.logging.Logger.getLogger(STFiltersHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java.util.logging.Logger.getLogger(STFiltersHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
    }
  }

  @Override
  public void handlePut(ActionParameters params) throws ActionException {
    Integer filterID = Integer.parseInt(params.getRequiredParam("filterId"));
    String filterLabel = params.getRequiredParam("filterLabel");

    PostStatus status = new PostStatus();
    String query = "";
    try (
        Connection connection = DriverManager.getConnection(
            stURL,
            stUser,
            stPassword);
        PreparedStatement statement = connection.prepareStatement(
            "update public.st_filters set st_filter_label =? where id=?;");) {
      params.requireLoggedInUser();
      ArrayList<String> roles = new UPTRoles()
          .handleGet(params, params.getUser());
      if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
        throw new Exception("User privilege is not enough for this action");
      }

      statement.setString(1, filterLabel);
      statement.setInt(2, filterID);

      errors.put(
          JSONHelper.createJSONObject(
              Obj.writeValueAsString(
                  new PostStatus("OK", "Executing query: " + statement.toString()))));

      statement.execute();

      errors.put(
          JSONHelper.createJSONObject(
              Obj.writeValueAsString(new PostStatus("OK", "filter updated"))));
      ResponseHelper.writeResponse(
          params,
          new JSONObject().put("Errors", errors));
    } catch (Exception e) {
      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        ResponseHelper.writeError(
            params,
            "",
            500,
            new JSONObject().put("Errors", errors));
      } catch (JsonProcessingException ex) {
        java.util.logging.Logger.getLogger(STFiltersHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java.util.logging.Logger.getLogger(STFiltersHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
      log.error(e);
    }
  }

  @Override
  public void handleDelete(ActionParameters params) throws ActionException {
    Integer filterID = Integer.parseInt(params.getRequiredParam("filterId"));
    String filterLabel = params.getRequiredParam("filterLabel");

    PostStatus status = new PostStatus();
    String query = "";
    try (
        Connection connection = DriverManager.getConnection(
            stURL,
            stUser,
            stPassword);
        PreparedStatement statement = connection.prepareStatement(
            "delete from public.st_filters where id = ?;");) {
      params.requireLoggedInUser();
      ArrayList<String> roles = new UPTRoles()
          .handleGet(params, params.getUser());
      if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
        throw new Exception("User privilege is not enough for this action");
      }

      statement.setInt(1, filterID);

      errors.put(
          JSONHelper.createJSONObject(
              Obj.writeValueAsString(
                  new PostStatus("OK", "Executing query: " + statement.toString()))));

      statement.execute();

      errors.put(
          JSONHelper.createJSONObject(
              Obj.writeValueAsString(new PostStatus("OK", "Filter delete"))));
      ResponseHelper.writeResponse(
          params,
          new JSONObject().put("Errors", errors));
    } catch (Exception e) {
      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        ResponseHelper.writeError(
            params,
            "",
            500,
            new JSONObject().put("Errors", errors));
      } catch (JsonProcessingException ex) {
        java.util.logging.Logger.getLogger(STFiltersHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java.util.logging.Logger.getLogger(STFiltersHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
    }
  }
}

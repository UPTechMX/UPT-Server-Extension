package org.oskari.upt.up;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.upt.PostStatus;
import org.oskari.upt.UPTRoles;
import org.springframework.web.client.RestTemplate;

@OskariActionRoute("up_public_assumptions")
public class UPPublicAssumptionsHandler extends RestActionHandler {
  private static String upURL;
  private static String upUser;
  private static String upPassword;

  private static String upwsHost;
  private static String upwsPort;
  private static final Logger log = LogFactory.getLogger(
      UPAssumptionsHandler.class);

  private JSONArray errors;
  private ObjectMapper Obj;

  @Override
  public void preProcess(ActionParameters params) throws ActionException {
    // common method called for all request methods
    log.info(params.getUser(), "accessing route", getName());
    PropertyUtil.loadProperties("/oskari-ext.properties");
    upURL = PropertyUtil.get("up.db.URL");
    upUser = PropertyUtil.get("up.db.user");
    upPassword = PropertyUtil.get("up.db.password");

    upwsHost = PropertyUtil.get("upws.db.host");
    upwsPort = PropertyUtil.get("upws.db.port");

    errors = new JSONArray();
    Obj = new ObjectMapper();
  }

  @Override
  public void handleGet(ActionParameters params) throws ActionException {
    String errorMsg = "UPAssumptions get";
    Long user_id = params.getUser().getId();
    Integer scenario_id = Integer.parseInt(
        params.getRequiredParam("scenario_id"));

    ArrayList<UPAssumptions> modules = new ArrayList<>();
    try (
        Connection connection = DriverManager.getConnection(
            upURL,
            upUser,
            upPassword);) {
      params.requireLoggedInUser();
      ArrayList<String> roles = new UPTRoles()
          .handleGet(params, params.getUser());
      if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
        throw new Exception("User privilege is not enough for this action");
      }

      PreparedStatement statement = connection.prepareStatement(
          "select id,study_area,scenario,category,name,value,units,description,source \n" +
              "from up_public_assumptions \n" +
              "where scenario=?");
      statement.setInt(1, scenario_id);
      boolean status = statement.execute();
      if (status) {
        ResultSet data = statement.getResultSet();

        while (data.next()) {
          UPAssumptions layer = new UPAssumptions(scenario_id);
          layer.id = data.getInt("id");
          layer.study_area = data.getLong("study_area");
          layer.scenario = data.getInt("scenario");
          layer.category = data.getString("category");
          layer.name = data.getString("name");
          layer.value = data.getDouble("value");
          layer.units = data.getString("units");
          layer.description = data.getString("description");
          layer.source = data.getString("source");
          modules.add(layer);
        }
      }
      if (modules.isEmpty()) {
        UPAssumptions layer = new UPAssumptions(scenario_id);
        modules.add(layer);
      }

      JSONArray out = new JSONArray();
      for (UPAssumptions index : modules) {
        // Convert to Json Object

        JSONObject json = null;
        try {
          json = JSONHelper.createJSONObject(Obj.writeValueAsString(index));
          System.out.println(json.toString());
          out.put(json);
        } catch (JsonProcessingException ex) {
          java.util.logging.Logger.getLogger(UPAssumptionsHandler.class.getName())
              .log(Level.SEVERE, null, ex);
        }
      }
      ResponseHelper.writeResponse(params, out);
    } catch (Exception e) {
      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        ResponseHelper.writeError(
            null,
            "",
            500,
            new JSONObject().put("Errors", errors));
      } catch (JsonProcessingException ex) {
        java.util.logging.Logger.getLogger(UPAssumptionsHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java.util.logging.Logger.getLogger(UPAssumptionsHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);
    }
  }

  @Override
  public void handlePost(ActionParameters params) throws ActionException {
    // Just UPTAdmin can use this method
    String errorMsg = "UPAssumptions post";
    Long user_id = params.getUser().getId();

    Long study_area = Long.parseLong(params.getRequiredParam("study_area"));
    Integer scenario = Integer.parseInt(params.getRequiredParam("scenario"));
    String category = params.getRequiredParam("category");
    String name = params.getRequiredParam("name");
    Double value = Double.parseDouble(params.getRequiredParam("value"));
    String units = params.getRequiredParam("units");
    String description = params.getRequiredParam("description");
    String source = params.getRequiredParam("source");

    PostStatus status = new PostStatus();
    status.status = "Error";
    JSONObject json = null;
    try (
        Connection connection = DriverManager.getConnection(
            upURL,
            upUser,
            upPassword);) {
      params.requireLoggedInUser();
      ArrayList<String> roles = new UPTRoles()
          .handleGet(params, params.getUser());
      if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
        throw new Exception("User privilege is not enough for this action");
      }

      PreparedStatement statement = connection.prepareStatement(
          "insert into up_public_assumptions(study_area,scenario,category,name,value,units,description,source) \n" +
              "values(?,?,?,?,?,?,?,?)\n");
      statement.setLong(1, study_area);
      statement.setInt(2, scenario);
      statement.setString(3, category);
      statement.setString(4, name);
      statement.setDouble(5, value);
      statement.setString(6, units);
      statement.setString(7, description);
      statement.setString(8, source);
      json = JSONHelper.createJSONObject(Obj.writeValueAsString(status));
      ResponseHelper.writeResponse(params, json);
    } catch (Exception e) {
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);
      status.message += errorMsg;

      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        ResponseHelper.writeError(
            null,
            "",
            500,
            new JSONObject().put("Errors", errors));
      } catch (JsonProcessingException ex) {
        java.util.logging.Logger.getLogger(UPAssumptionsHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java.util.logging.Logger.getLogger(UPAssumptionsHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
    }
  }

  @Override
  public void handlePut(ActionParameters params) throws ActionException {
    // Just UPTAdmin can use this method
    String errorMsg = "UPAssumptions post";
    JSONObject json = null;
    try (
        Connection connection = DriverManager.getConnection(
            upURL,
            upUser,
            upPassword);) {
      params.requireLoggedInUser();
      ArrayList<String> roles = new UPTRoles()
          .handleGet(params, params.getUser());
      if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
        throw new Exception("User privilege is not enough for this action");
      }

      Long user_id = params.getUser().getId();
      Long id = Long.parseLong(params.getRequiredParam("id"));
      Long study_area = Long.parseLong(params.getRequiredParam("study_area"));
      Integer scenario = Integer.parseInt(params.getRequiredParam("scenario"));
      String category = params.getRequiredParam("category");
      String name = params.getRequiredParam("name");
      Double value = Double.parseDouble(params.getRequiredParam("value"));
      String units = params.getRequiredParam("units");
      String description = params.getRequiredParam("description");
      String source = params.getRequiredParam("source");

      PreparedStatement statement = connection.prepareStatement(
          "update up_public_assumptions \n" +
              "set (study_area, \n" +
              "scenario,category, \n" +
              "name,value,units, \n" +
              "description,source)=\n" +
              "(?, ?,?,?,?,?,?,?) \n" +
              " where id=? \n");
      statement.setLong(1, study_area);
      statement.setLong(2, scenario);
      statement.setString(3, category);
      statement.setString(4, name);
      statement.setDouble(5, value);
      statement.setString(6, units);
      statement.setString(7, description);
      statement.setString(8, source);
      statement.setLong(9, id);

      statement.execute();

      setCreateAssumptions(scenario, params);
    } catch (Exception e) {
      errorMsg = errorMsg + e.toString();
      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        ResponseHelper.writeError(
            params,
            "",
            500,
            new JSONObject().put("Errors", errors));
      } catch (JsonProcessingException | JSONException ex) {
        java.util.logging.Logger.getLogger(UPAssumptionsHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
    }
  }

  protected void setCreateAssumptions(
      Integer scenario_id,
      ActionParameters params)
      throws Exception {
    try {
      Assumptions val = new Assumptions();
      val.scenario = Integer.parseInt(params.getRequiredParam("scenario"));
      val.category = params.getRequiredParam("category");
      val.name = params.getRequiredParam("name");
      val.value = Double.parseDouble(params.getRequiredParam("value"));

      RestTemplate restTemplate = new RestTemplate();
      Map<String, String> param = new HashMap<String, String>();
      param.put("assumptions_id", params.getRequiredParam("id"));
      restTemplate.put(
          "http://" + upwsHost + ":" + upwsPort + "/assumptions/",
          val,
          param);
    } catch (Exception e) {
      errors.put(
          JSONHelper.createJSONObject(
              Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
      throw new Exception();
    }
  }
}

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

@OskariActionRoute("delete_public_data")
public class DeleteDataHandlerPubStdArea extends RestActionHandler {
  private static String upURL;
  private static String upUser;
  private static String upPassword;

  private static String upwsHost;
  private static String upwsPort;
  private static String upProjection;

  private JSONArray errors;
  private ObjectMapper Obj;

  private static final Logger log = LogFactory.getLogger(
      DeleteDataHandler.class);

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
    upProjection = PropertyUtil
        .get("oskari.native.srs")
        .substring(PropertyUtil.get("oskari.native.srs").indexOf(":") + 1);

    errors = new JSONArray();
    Obj = new ObjectMapper();
  }

  @Override
  public void handleGet(ActionParameters params) throws ActionException {
    params.requireLoggedInUser();
  }

  @Override
  public void handleDelete(ActionParameters params) throws ActionException {
    params.requireLoggedInUser();
    String errorMsg = "Layers UP get ";
    try {
      ArrayList<String> roles = new UPTRoles()
          .handleGet(params, params.getUser());
      if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
        throw new Exception("User privilege is not enough for this action");
      }

      if (params.getRequiredParam("layerUPName") != null &&
          params.getRequiredParam("scenarioId") != null) {
        switch (params.getRequiredParam("layerUPName")) {
          case "mmu":
            this.deleteMmu(params.getRequiredParam("scenarioId"));
            break;
          case "mmu_info":
            this.deleteMmuInfo(params.getRequiredParam("scenarioId"));
            break;
          case "transit":
            this.deleteTransit(params.getRequiredParam("scenarioId"));
            break;
          case "roads":
            this.deleteRoads(params.getRequiredParam("scenarioId"));
            break;
          case "jobs":
            this.deleteJobs(params.getRequiredParam("scenarioId"));
            break;
          case "footprint":
            this.deleteFootprint(params.getRequiredParam("scenarioId"));
            break;
          case "risk":
            this.deleteRisk(params.getRequiredParam("scenarioId"));
            break;
          case "amenities":
            this.deleteAmenities(params.getRequiredParam("scenarioId"));
            break;
          case "assumptions":
            this.deleteAssumptions(params.getRequiredParam("scenarioId"));
            break;
          default:
            break;
        }
      }
      ResponseHelper.writeResponse(
          params,
          new JSONObject().put("OK", "Data deleted"));
    } catch (Exception e) {
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
        java.util.logging.Logger.getLogger(DeleteDataHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java.util.logging.Logger.getLogger(DeleteDataHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
    }
  }

  private void deleteAmenities(String scenarioId) throws Exception {
    try {
      Map<String, String> params = new HashMap<String, String>();
      params.put("id", scenarioId);

      RestTemplate restTemplate = new RestTemplate();
      restTemplate.delete(
          "http://" + upwsHost + ":" + upwsPort + "/amenities/{id}",
          params);
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
        java.util.logging.Logger.getLogger(DeleteDataHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java.util.logging.Logger.getLogger(DeleteDataHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
      throw new Exception();
    }
  }

  private void deleteMmu(String scenarioId) throws Exception {
    try {
      Map<String, String> params = new HashMap<String, String>();
      params.put("id", scenarioId);

      RestTemplate restTemplate = new RestTemplate();
      restTemplate.delete(
          "http://" + upwsHost + ":" + upwsPort + "/mmu/{id}",
          params);
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
        java.util.logging.Logger.getLogger(DeleteDataHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java.util.logging.Logger.getLogger(DeleteDataHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
      throw new Exception();
    }
  }

  private void deleteMmuInfo(String scenarioId) throws Exception {
    try {
      Map<String, String> params = new HashMap<String, String>();
      params.put("id", scenarioId);

      RestTemplate restTemplate = new RestTemplate();
      restTemplate.delete(
          "http://" + upwsHost + ":" + upwsPort + "/mmu_info/{id}",
          params);
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
        java.util.logging.Logger.getLogger(DeleteDataHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java.util.logging.Logger.getLogger(DeleteDataHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
      throw new Exception();
    }
  }

  private void deleteTransit(String scenarioId) throws Exception {
    try {
      Map<String, String> params = new HashMap<String, String>();
      params.put("id", scenarioId);

      RestTemplate restTemplate = new RestTemplate();
      restTemplate.delete(
          "http://" + upwsHost + ":" + upwsPort + "/transit/{id}",
          params);
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
        java.util.logging.Logger.getLogger(DeleteDataHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java.util.logging.Logger.getLogger(DeleteDataHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
      throw new Exception();
    }
  }

  private void deleteRoads(String scenarioId) throws Exception {
    try {
      Map<String, String> params = new HashMap<String, String>();
      params.put("id", scenarioId);

      RestTemplate restTemplate = new RestTemplate();
      restTemplate.delete(
          "http://" + upwsHost + ":" + upwsPort + "/roads/{id}",
          params);
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
        java.util.logging.Logger.getLogger(DeleteDataHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java.util.logging.Logger.getLogger(DeleteDataHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
      throw new Exception();
    }
  }

  private void deleteJobs(String scenarioId) throws Exception {
    try {
      Map<String, String> params = new HashMap<String, String>();
      params.put("id", scenarioId);

      RestTemplate restTemplate = new RestTemplate();
      restTemplate.delete(
          "http://" + upwsHost + ":" + upwsPort + "/jobs/{id}",
          params);
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
        java.util.logging.Logger.getLogger(DeleteDataHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java.util.logging.Logger.getLogger(DeleteDataHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
      throw new Exception();
    }
  }

  private void deleteFootprint(String scenarioId) throws Exception {
    try {
      Map<String, String> params = new HashMap<String, String>();
      params.put("id", scenarioId);

      RestTemplate restTemplate = new RestTemplate();
      restTemplate.delete(
          "http://" + upwsHost + ":" + upwsPort + "/footprint/{id}",
          params);
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
        java.util.logging.Logger.getLogger(DeleteDataHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java.util.logging.Logger.getLogger(DeleteDataHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
      throw new Exception();
    }
  }

  private void deleteRisk(String scenarioId) throws Exception {
    try {
      Map<String, String> params = new HashMap<String, String>();
      params.put("id", scenarioId);

      RestTemplate restTemplate = new RestTemplate();
      restTemplate.delete(
          "http://" + upwsHost + ":" + upwsPort + "/risk/{id}",
          params);
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
        java.util.logging.Logger.getLogger(DeleteDataHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java.util.logging.Logger.getLogger(DeleteDataHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
      throw new Exception();
    }
  }

  private void deleteAssumptions(String scenarioId) throws Exception {
    try (
        Connection connection = DriverManager.getConnection(
            upURL,
            upUser,
            upPassword)) {
      Map<String, String> params = new HashMap<String, String>();
      params.put("id", scenarioId);

      RestTemplate restTemplate = new RestTemplate();
      restTemplate.delete(
          "http://" + upwsHost + ":" + upwsPort + "/assumptions/{id}",
          params);

      PreparedStatement statement = connection.prepareStatement(
          "DELETE FROM public.up_public_assumptions\n" + "	WHERE scenario=?;");
      statement.setInt(1, Integer.parseInt(scenarioId));
      statement.execute();
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
        java.util.logging.Logger.getLogger(DeleteDataHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java.util.logging.Logger.getLogger(DeleteDataHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
      throw new Exception();
    }
  }
}

package org.oskari.example.up;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.service.OskariComponentManager;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import javax.servlet.http.HttpServletRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.example.PostStatus;
import org.oskari.example.Tables;
import org.oskari.example.UPTRoles;
import org.oskari.example.st.STLayersHandler;
import org.oskari.log.AuditLog;
import org.oskari.map.userlayer.service.UserLayerDbService;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@OskariActionRoute("up_public_scenario")
public class UPScenarioHandlerPubStdArea extends RestActionHandler {
  private static String upURL;
  private static String upUser;
  private static String upPassword;

  private static String upwsHost;
  private static String upwsPort;
  private static String upProjection;
  private static Long user_id;

  private JSONArray errors;
  private ObjectMapper Obj;

  private static final Logger log = LogFactory.getLogger(
    UPScenarioHandler.class
  );

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
    upProjection =
      PropertyUtil
        .get("oskari.native.srs")
        .substring(PropertyUtil.get("oskari.native.srs").indexOf(":") + 1);
    user_id = params.getUser().getId();

    errors = new JSONArray();
    Obj = new ObjectMapper();
  }

  @Override
  public void handleGet(ActionParameters params) throws ActionException {
    String errorMsg = "Scenario UP get ";
    ResponseEntity<List<ScenarioUP>> returns = null;
    try {
      params.requireLoggedInUser();
      ArrayList<String> roles = new UPTRoles()
      .handleGet(params, params.getUser());
      if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
        throw new Exception("User privilege is not enough for this action");
      }

      Connection connection = DriverManager.getConnection(
        upURL,
        upUser,
        upPassword
      );
      PreparedStatement statement = connection.prepareStatement(
        "select id from up_public_scenario where owner_id=?"
      );
      statement.setLong(1, params.getUser().getId());

      ResultSet uScenarios = statement.executeQuery();
      ArrayList<Integer> scenarios = new ArrayList<>();
      while (uScenarios.next()) {
        scenarios.add(uScenarios.getInt("id"));
      }

      String transactionUrl =
        "http://" + upwsHost + ":" + upwsPort + "/scenario/";
      RestTemplate restTemplate = new RestTemplate();
      returns =
        restTemplate.exchange(
          transactionUrl,
          HttpMethod.GET,
          null,
          new ParameterizedTypeReference<List<ScenarioUP>>() {}
        );

      List<ScenarioUP> response = returns.getBody();
      JSONArray out = new JSONArray();
      for (ScenarioUP index : response) {
        //Convert to Json Object
        if (scenarios.contains(index.scenario_id)) {
          final JSONObject json = JSONHelper.createJSONObject(
            Obj.writeValueAsString(index)
          );
          out.put(json);
        }
      }
      errors.put(
        JSONHelper.createJSONObject(
          Obj.writeValueAsString(new PostStatus("OK", "Getting scenarios"))
        )
      );
      ResponseHelper.writeResponse(params, out);
    } catch (Exception e) {
      errorMsg = errorMsg + e.getMessage();
      log.error(e, errorMsg);
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
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
    }
  }

  @Override
  public void handlePost(ActionParameters params) throws ActionException {
    String errorMsg = "Scenario UP post ";
    try {
      params.requireLoggedInUser();
      ArrayList<String> roles = new UPTRoles()
      .handleGet(params, params.getUser());
      if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
        throw new Exception("User privilege is not enough for this action");
      }

      ScenarioUP scenario = new ScenarioUP();

      scenario.setName(params.getRequiredParam("name"));
      scenario.setOwnerId(Integer.parseInt(user_id.toString()));
      scenario.setDescription(params.getRequiredParam("name"));
      scenario.setIsBase(Integer.parseInt(params.getRequiredParam("isBase")));
      scenario.setStudyArea(
        Integer.parseInt(params.getRequiredParam("studyAreaId"))
      );
      String indicator = params.getRequiredParam("indicators");
      String studyArea = params.getRequiredParam("studyAreaId");
      String[] indicators = indicator.split(java.util.regex.Pattern.quote("_"));
      //Create Scenario
      long row = this.setScenario(scenario);
      if (row > 0) {
        scenario.setScenarioId((int) row);
        //Create Indicators for sceanrio
        for (String index : indicators) {
          this.setScenarioIndicators(index, row);
        }
        boolean status = setCreateScenario(scenario);
        if (!status) {
          scenario.setScenarioId(-1);
          scenario.setName("Error: scenario not created");
        }

        //Get Assumptions
        Map<Integer, Assumptions> Layers = new TreeMap<>();
        PostStatus assumptionState = new PostStatus();
        try (
          Connection connection = DriverManager.getConnection(
            upURL,
            upUser,
            upPassword
          );
          PreparedStatement statement = connection.prepareStatement(
            "SELECT  ? as study_area, ? as scenario, category, name, value,units,description,source\n" +
            "FROM public.up_public_assumptions\n" +
            "where scenario=(select min(scenario) from public.up_public_assumptions where study_area=?)"
          );
        ) {
          statement.setLong(1, Long.parseLong(studyArea));
          statement.setLong(2, row);
          statement.setLong(3, Long.parseLong(studyArea));

          errors.put(
            JSONHelper.createJSONObject(
              Obj.writeValueAsString(
                new PostStatus("Executing query: ", statement.toString())
              )
            )
          );

          ResultSet data = statement.executeQuery();

          Integer i = 0;
          while (data.next()) {
            Assumptions assumption = new Assumptions();
            assumption.scenario = data.getInt("scenario");
            assumption.study_area = data.getLong("study_area");
            assumption.category = data.getString("category");
            assumption.name = data.getString("name");
            assumption.value = data.getDouble("value");
            assumption.units = data.getString("units");
            assumption.description = data.getString("description");
            assumption.source = data.getString("source");
            assumption.owner_id = user_id;

            Layers.put(i, assumption);
            i++;
          }
          //create assumptions for new study areas
          if (Layers.size() > 0) {
            assumptionState = saveAssumptions(Layers);
            setCreateAssumptions(scenario.scenario_id, studyArea);
          }
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
              .util.logging.Logger.getLogger(STLayersHandler.class.getName())
              .log(Level.SEVERE, null, ex);
          } catch (JSONException ex) {
            java
              .util.logging.Logger.getLogger(STLayersHandler.class.getName())
              .log(Level.SEVERE, null, ex);
          }
          errorMsg += e.toString();
          log.error(e);
          return;
        }

        try (
          Connection connection = DriverManager.getConnection(
            upURL,
            upUser,
            upPassword
          );
          PreparedStatement statement = connection.prepareStatement(
            " select case when count(study_area) is null then 0 when count(study_area) =0 then 0 else 1 end as has_assumptions from up_public_assumptions where study_area=?"
          );
        ) {
          statement.setLong(1, Long.parseLong(studyArea));
          errors.put(
            JSONHelper.createJSONObject(
              Obj.writeValueAsString(
                new PostStatus("Executing query: ", statement.toString())
              )
            )
          );
          ResultSet data = statement.executeQuery();

          while (data.next()) {
            scenario.has_assumptions = data.getInt("has_assumptions");
          }
        } catch (Exception e) {
          errorMsg += e.toString();
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
              .util.logging.Logger.getLogger(STLayersHandler.class.getName())
              .log(Level.SEVERE, null, ex);
          } catch (JSONException ex) {
            java
              .util.logging.Logger.getLogger(STLayersHandler.class.getName())
              .log(Level.SEVERE, null, ex);
          }
          return;
        }
      }
      ResponseHelper.writeResponse(
        params,
        JSONHelper.createJSONObject(Obj.writeValueAsString(scenario))
      );
    } catch (Exception e) {
      errorMsg += e.getMessage();
      log.error(e, errorMsg);
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
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
    }
  }

  @Override
  public void handlePut(ActionParameters params) throws ActionException {
    String errorMsg = "Scenario UP post ";
    try {
      params.requireLoggedInUser();
      ArrayList<String> roles = new UPTRoles()
      .handleGet(params, params.getUser());
      if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
        throw new Exception("User privilege is not enough for this action");
      }

      ScenarioUP scenario = new ScenarioUP();
      scenario.setScenarioId(
        Integer.parseInt(params.getRequiredParam("scenarioId"))
      );
      scenario.setName(params.getRequiredParam("name"));
      scenario.setOwnerId(Integer.parseInt(user_id.toString()));
      scenario.setDescription(params.getRequiredParam("description"));
      scenario.setIsBase(Integer.parseInt(params.getRequiredParam("isBase")));
      scenario.setStudyArea(
        Integer.parseInt(params.getRequiredParam("studyArea"))
      );

      String studyArea = params.getRequiredParam("studyArea");

      //Create Scenario
      long row = this.updateScenario(scenario);
      if (row > 0) {
        boolean status = setUpdateScenario(scenario);
        if (!status) {
          scenario.setScenarioId(-1);
          scenario.setName("Error: scenario not created");
        }
      }
      final JSONObject json = JSONHelper.createJSONObject(
        Obj.writeValueAsString(scenario)
      );

      ResponseHelper.writeResponse(params, json);
    } catch (Exception e) {
      errorMsg = errorMsg + e.getMessage();
      log.error(e, errorMsg);
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
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
    }
  }

  @Override
  public void handleDelete(ActionParameters params) throws ActionException {
    params.requireLoggedInUser();
    String errorMsg = "Scenario UP post ";
    try {
      ArrayList<String> roles = new UPTRoles()
      .handleGet(params, params.getUser());
      if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
        throw new Exception("User privilege is not enough for this action");
      }

      Integer scenarioId = Integer.parseInt(
        params.getRequiredParam("scenario_id")
      );
      //delete Scenario
      boolean row = deleteScenarioUP(scenarioId.toString());
      if (row) {
        row = deleteAssumptionsUP(scenarioId.toString());
      }
      if (row) {
        Connection connection = DriverManager.getConnection(
          upURL,
          upUser,
          upPassword
        );
        PreparedStatement statement = connection.prepareStatement(
          "SELECT public_layer_id FROM up_scenario_buffers where scenario=?"
        );
        statement.setInt(1, scenarioId);
        statement.executeQuery();
        ResultSet res = statement.getResultSet();
        while (res.next()) {
          HttpServletRequest requestParam = params.getRequest();
          row = deleteBuffersUP(params, res.getLong("user_layer_id"));
        }
      }
      if (row) {
        deleteScenarioIndicators(Long.parseLong(scenarioId.toString()));
        row = this.deleteScenario(scenarioId);
      }
      log.debug(
        "User:  " + user_id.toString() + " -> " + scenarioId.toString()
      );

      final JSONObject json = JSONHelper.createJSONObject(
        Obj.writeValueAsString(scenarioId)
      );
      ResponseHelper.writeResponse(params, json);
    } catch (Exception e) {
      errorMsg = errorMsg + e.getMessage();
      log.error(e, errorMsg);
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
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
    }
  }

  protected void setCreateAssumptions(Integer scenario_id, String study_area)
    throws Exception {
    try (
      Connection connection = DriverManager.getConnection(
        upURL,
        upUser,
        upPassword
      )
    ) {
      Statement statement = connection.createStatement();
      String query =
        "SELECT " +
        scenario_id +
        " as scenario, category, name, value\n" +
        "FROM public.up_public_assumptions\n" +
        "where study_area=" +
        study_area;
      ResultSet data_set = statement.executeQuery(query);
      ArrayList<Assumptions> data_in = new ArrayList<>();
      while (data_set.next()) {
        Assumptions val = new Assumptions();
        val.scenario = data_set.getInt("scenario");
        val.category = data_set.getString("category");
        val.name = data_set.getString("name");
        val.value = data_set.getDouble("value");
        val.owner_id = user_id;
        data_in.add(val);
      }
      Tables<Assumptions> final_data = new Tables<>(data_in);

      RestTemplate restTemplate = new RestTemplate();
      PostStatus postStatus = restTemplate.postForObject(
        "http://" + upwsHost + ":" + upwsPort + "/assumptions/",
        final_data,
        PostStatus.class
      );
    } catch (Exception e) {
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
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

  protected boolean setCreateScenario(ScenarioUP scenario) throws Exception {
    try {
      ScenarioUP returns;
      RestTemplate restTemplate = new RestTemplate();
      returns =
        restTemplate.postForObject(
          "http://" + upwsHost + ":" + upwsPort + "/scenario/",
          scenario,
          ScenarioUP.class
        );
      return returns.getScenarioId() != -1;
    } catch (Exception e) {
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
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

  protected boolean setUpdateScenario(ScenarioUP scenario) throws Exception {
    try {
      RestTemplate restTemplate = new RestTemplate();
      Map<String, String> params = new HashMap<String, String>();
      params.put("scenario_id", scenario.getScenarioId().toString());
      restTemplate.put(
        "http://" + upwsHost + ":" + upwsPort + "/scenario/",
        scenario,
        params
      );
      return true;
    } catch (Exception e) {
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
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

  protected long setScenario(ScenarioUP scenario) throws Exception {
    String errorMsg = "Scenario UP post ";
    long result;
    try (
      Connection connection = DriverManager.getConnection(
        upURL,
        upUser,
        upPassword
      );
      PreparedStatement statement = connection.prepareStatement(
        "INSERT INTO public.up_public_scenario(name, description, owner_id, study_area, is_base) VALUES (?, ?, ?, ?, ?);",
        Statement.RETURN_GENERATED_KEYS
      );
    ) {
      statement.setString(1, scenario.getName());
      statement.setString(2, scenario.getDescription());
      statement.setInt(3, scenario.getOwneId());
      statement.setLong(4, scenario.getStudyArea());
      statement.setInt(5, scenario.getIsBase());
      errors.put(
        JSONHelper.createJSONObject(
          Obj.writeValueAsString(
            new PostStatus("Executing query: ", statement.toString())
          )
        )
      );
      result = statement.executeUpdate();
      ResultSet rs = statement.getGeneratedKeys();
      if (rs.next()) {
        return rs.getLong(1);
      }
      return result;
    } catch (Exception e) {
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
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

  protected long updateScenario(ScenarioUP scenario) throws Exception {
    String errorMsg = "Scenario UP post ";
    Integer result;
    try (
      Connection connection = DriverManager.getConnection(
        upURL,
        upUser,
        upPassword
      );
      PreparedStatement statement = connection.prepareStatement(
        "UPDATE public.up_public_scenario\n" +
        "SET name=?, description=?, owner_id=?, is_base=?, study_area=?\n" +
        "WHERE id=?;"
      );
    ) {
      statement.setString(1, scenario.getName());
      statement.setString(2, scenario.getDescription());
      statement.setInt(3, scenario.getOwneId());
      statement.setInt(4, scenario.getIsBase());
      statement.setInt(5, scenario.getStudyArea());
      statement.setInt(6, scenario.getScenarioId());
      statement.execute();
      return scenario.getScenarioId();
    } catch (Exception e) {
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
          )
        );
      } catch (JsonProcessingException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);
      result = scenario.getScenarioId();
      throw new Exception();
    }
  }

  protected boolean setScenarioIndicators(String module, long scenario)
    throws Exception {
    String errorMsg = "Scenario UP post ";
    boolean result = false;
    try (
      Connection connection = DriverManager.getConnection(
        upURL,
        upUser,
        upPassword
      );
      PreparedStatement statement = connection.prepareStatement(
        "INSERT INTO public.up_public_scenario_modules( module, scenario)	VALUES (  (SELECT id FROM public.up_modules_translation where name='" +
        module +
        "'), " +
        scenario +
        ");",
        Statement.RETURN_GENERATED_KEYS
      );
    ) {
      result = statement.execute();
      return result;
    } catch (Exception e) {
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
          )
        );
      } catch (JsonProcessingException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);
      throw new Exception();
    }
  }

  protected PostStatus saveAssumptions(Map<Integer, Assumptions> Layers)
    throws Exception {
    PostStatus status = new PostStatus();
    try (
      Connection connection = DriverManager.getConnection(
        upURL,
        upUser,
        upPassword
      );
      PreparedStatement statement = connection.prepareStatement(
        "INSERT INTO public.up_public_assumptions(study_area, scenario, category, name, value,units,description,source) VALUES (?, ?, ?, ?, ?, ?, ?, ?);",
        Statement.RETURN_GENERATED_KEYS
      );
    ) {
      connection.setAutoCommit(false);
      for (Map.Entry m : Layers.entrySet()) {
        statement.setLong(1, ((Assumptions) m.getValue()).study_area);
        statement.setInt(2, ((Assumptions) m.getValue()).scenario);
        statement.setString(3, ((Assumptions) m.getValue()).category);
        statement.setString(4, ((Assumptions) m.getValue()).name);
        statement.setDouble(5, ((Assumptions) m.getValue()).value);
        statement.setString(6, ((Assumptions) m.getValue()).units);
        statement.setString(7, ((Assumptions) m.getValue()).description);
        statement.setString(8, ((Assumptions) m.getValue()).source);
        statement.addBatch();
      }
      int[] rows = statement.executeBatch();
      connection.commit();
      status.status = "Success";
      status.message = rows.length + " rows inserted succesfully";
      return status;
    } catch (Exception e) {
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
          )
        );
      } catch (JsonProcessingException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
      status.status = "Error";
      status.message = "There was an error when inserting data " + e.toString();
      throw new Exception();
    }
  }

  protected boolean deleteScenario(Integer scenario) throws Exception {
    String errorMsg = "Scenario UP post ";
    try (
      Connection connection = DriverManager.getConnection(
        upURL,
        upUser,
        upPassword
      );
      PreparedStatement statement = connection.prepareStatement(
        "delete from public.up_public_scenario where id=?;"
      );
    ) {
      statement.setInt(1, scenario);
      statement.execute();
      return true;
    } catch (Exception e) {
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
          )
        );
      } catch (JsonProcessingException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);
      throw new Exception();
    }
  }

  boolean deleteScenarioUP(String scenario) throws Exception {
    String errorMsg = "Scenario UP delete ";
    try {
      Map<String, String> params = new HashMap<String, String>();
      params.put("id", scenario);

      RestTemplate restTemplate = new RestTemplate();
      restTemplate.delete(
        "http://" + upwsHost + ":" + upwsPort + "/scenario/{id}",
        params
      );
      return true;
    } catch (Exception e) {
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
          )
        );
      } catch (JsonProcessingException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);
      throw new Exception();
    }
  }

  protected boolean deleteScenarioIndicators(Long scenario) throws Exception {
    String errorMsg = "Scenario UP delete ";
    boolean result = false;
    try (
      Connection connection = DriverManager.getConnection(
        upURL,
        upUser,
        upPassword
      );
      PreparedStatement statement = connection.prepareStatement(
        "delete from public.up_public_scenario where id=?;"
      );
    ) {
      statement.setLong(1, scenario);
      result = statement.execute();
      return result;
    } catch (Exception e) {
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
          )
        );
      } catch (JsonProcessingException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);
      throw new Exception();
    }
  }

  protected boolean deleteAssumptionsUP(String scenario) throws Exception {
    String errorMsg = "Scenario UP delete ";
    try {
      Map<String, String> params = new HashMap<String, String>();
      params.put("id", scenario);

      RestTemplate restTemplate = new RestTemplate();
      restTemplate.delete(
        "http://" + upwsHost + ":" + upwsPort + "/assumptions/{id}",
        params
      );
      return true;
    } catch (Exception e) {
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
          )
        );
      } catch (JsonProcessingException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);
      throw new Exception();
    }
  }

  protected boolean deleteBuffersUP(ActionParameters params, Long layer_id)
    throws Exception {
    String errorMsg = "Scenario UP delete ";
    UserLayerDbService userLayerDbService;
    try {
      userLayerDbService =
        OskariComponentManager.getComponentOfType(UserLayerDbService.class);
      long id = layer_id;
      UserLayer userLayer = userLayerDbService.getUserLayerById(id);
      if (userLayer == null) {
        throw new ActionParamsException("UserLayer doesn't exist: " + id);
      }
      if (!userLayer.isOwnedBy(params.getUser().getUuid())) {
        throw new ActionDeniedException("UserLayer belongs to another user");
      }
      userLayerDbService.deleteUserLayer(userLayer);
      AuditLog
        .user(params.getClientIp(), params.getUser())
        .withParam("layer_id", userLayer.getId())
        .deleted(AuditLog.ResourceType.USERLAYER);
      JSONObject response = JSONHelper.createJSONObject("result", "success");
      return true;
    } catch (Exception e) {
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
          )
        );
      } catch (JsonProcessingException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);
      throw new Exception();
    }
  }
}

package org.oskari.example.up;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.*;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.example.PostStatus;
import org.oskari.example.Tables;
import org.oskari.example.UPTRoles;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Dummy Rest action route
 */
@CrossOrigin(origins = { "*" }, maxAge = 6000)
@OskariActionRoute("ScenarioUPHandler")
public class ScenarioUPHandler extends RestActionHandler {
  private static String upURL;
  private static String upUser;
  private static String upPassword;

  private static String upwsHost;
  private static String upwsPort;
  private static final Logger log = LogFactory.getLogger(
    ScenarioUPHandler.class
  );

  private JSONArray errors;
  private ObjectMapper Obj;

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
    params.requireLoggedInUser();
    String errorMsg = "Scenario UP get ";
    ResponseEntity<List<ScenarioUP>> returns = null;
    try {
      ArrayList<String> roles = new UPTRoles()
      .handleGet(params, params.getUser());
      if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
        throw new Exception("User privilege is not enough for this action");
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
        ObjectMapper Obj = new ObjectMapper();
        final JSONObject json = JSONHelper.createJSONObject(
          Obj.writeValueAsString(index)
        );
        out.put(json);
      }

      Long user_id = params.getUser().getId();
      log.debug("User:  " + user_id.toString() + " -> " + out.toString());
      ResponseHelper.writeResponse(params, out);
    } catch (Exception e) {
      errorMsg = errorMsg + e.getMessage();
      log.error(e, errorMsg);
    }
  }

  @Override
  public void handlePost(ActionParameters params) throws ActionException {
    params.requireLoggedInUser();
    // throw new ActionException("This will be logged including stack trace");
    String errorMsg = "Scenario UP post ";
    try {
      ArrayList<String> roles = new UPTRoles()
      .handleGet(params, params.getUser());
      if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
        throw new Exception("User privilege is not enough for this action");
      }

      Long user_id = params.getUser().getId();
      if ("evaluate".equals(params.getRequiredParam("action"))) {
        String scenarios = String.join(
          ",",
          params.getRequest().getParameterValues("scenariosId")
        );
        String scenario = String.join(
          "_",
          params.getRequest().getParameterValues("scenariosId")
        );
        //String indicators = params.getRequiredParam("indicators");

        //Get scenario indicators
        Connection connection = DriverManager.getConnection(
          upURL,
          upUser,
          upPassword
        );
        Statement statement = connection.createStatement();
        ResultSet data = statement.executeQuery(
          "select up_modules_translation.name from up_modules_translation\n" +
          "inner join up_scenario_modules on up_modules_translation.id=up_scenario_modules.module\n" +
          "inner join up_scenario on up_scenario.id=up_scenario_modules.scenario\n" +
          "where up_scenario.id in (" +
          scenarios +
          ") "
        );
        String indicators = "";
        while (data.next()) {
          indicators += data.getString("name") + "_";
        }
        indicators = indicators.substring(0, indicators.length() - 1);
        boolean results = evaluateScenario(
          user_id.toString(),
          scenario,
          indicators
        );
        ObjectMapper mapper = new ObjectMapper();
        log.debug(
          "User:  " +
          user_id.toString() +
          " -> " +
          mapper.writeValueAsString(results)
        );

        ResponseHelper.writeResponse(
          params,
          JSONHelper.createJSONObject(mapper.writeValueAsString(results))
        );
      }
      if ("evaluate_public".equals(params.getRequiredParam("action"))) {
        String scenariosPublic = String.join(
          ",",
          params.getRequest().getParameterValues("scenariosPublicId")
        );
        String scenario = String.join(
          "_",
          params.getRequest().getParameterValues("scenariosPublicId")
        );
        //String indicators = params.getRequiredParam("indicators");

        //Get scenario indicators
        Connection connection = DriverManager.getConnection(
          upURL,
          upUser,
          upPassword
        );
        Statement statement = connection.createStatement();
        ResultSet data = statement.executeQuery(
          "select up_modules_translation.name from up_modules_translation\n" +
          "inner join up_public_scenario_modules on up_modules_translation.id=up_public_scenario_modules.module\n" +
          "inner join up_public_scenario on up_public_scenario.id=up_public_scenario_modules.scenario\n" +
          "where up_public_scenario.id in (" +
          scenariosPublic +
          ")"
        );
        String indicators = "";
        while (data.next()) {
          indicators += data.getString("name") + "_";
        }
        indicators = indicators.substring(0, indicators.length() - 1);
        boolean results = evaluateScenario(
          user_id.toString(),
          scenario,
          indicators
        );
        ObjectMapper mapper = new ObjectMapper();
        log.debug(
          "User:  " +
          user_id.toString() +
          " -> " +
          mapper.writeValueAsString(results)
        );

        ResponseHelper.writeResponse(
          params,
          JSONHelper.createJSONObject(mapper.writeValueAsString(results))
        );
      }
      if ("evaluate_both".equals(params.getRequiredParam("action"))) {
        String scenarios = String.join(
          ",",
          params.getRequest().getParameterValues("scenariosId")
        );
        String scenariosPublic = String.join(
          ",",
          params.getRequest().getParameterValues("scenariosPublicId")
        );
        String scenario =
          String.join(
            "_",
            params.getRequest().getParameterValues("scenariosId")
          ) +
          "_" +
          String.join(
            "_",
            params.getRequest().getParameterValues("scenariosPublicId")
          );
        //String indicators = params.getRequiredParam("indicators");

        //Get scenario indicators
        Connection connection = DriverManager.getConnection(
          upURL,
          upUser,
          upPassword
        );
        Statement statement = connection.createStatement();
        ResultSet data = statement.executeQuery(
          "select up_modules_translation.name from up_modules_translation\n" +
          "inner join up_scenario_modules on up_modules_translation.id=up_scenario_modules.module\n" +
          "inner join up_scenario on up_scenario.id=up_scenario_modules.scenario\n" +
          "where up_scenario.id in (" +
          scenarios +
          ") " +
          "union select up_modules_translation.name from up_modules_translation\n" +
          "inner join up_public_scenario_modules on up_modules_translation.id=up_public_scenario_modules.module\n" +
          "inner join up_public_scenario on up_public_scenario.id=up_public_scenario_modules.scenario\n" +
          "where up_public_scenario.id in (" +
          scenariosPublic +
          ")"
        );
        String indicators = "";
        while (data.next()) {
          indicators += data.getString("name") + "_";
        }
        indicators = indicators.substring(0, indicators.length() - 1);
        boolean results = evaluateScenario(
          user_id.toString(),
          scenario,
          indicators
        );
        ObjectMapper mapper = new ObjectMapper();
        log.debug(
          "User:  " +
          user_id.toString() +
          " -> " +
          mapper.writeValueAsString(results)
        );

        ResponseHelper.writeResponse(
          params,
          JSONHelper.createJSONObject(mapper.writeValueAsString(results))
        );
      } else if ("add".equals(params.getRequiredParam("action"))) {
        ScenarioUP scenario = new ScenarioUP();
        scenario.setName(params.getRequiredParam("name"));
        scenario.setOwnerId(Integer.parseInt(user_id.toString()));
        scenario.setDescription(params.getRequiredParam("name"));
        scenario.setIsBase(
          params.getRequiredParam("isBase").equals("true") ? 1 : 0
        );
        String indicator = params.getRequiredParam("indicators");
        String studyArea = params.getRequiredParam("studyAreaId");
        String[] indicators = indicator.split(
          java.util.regex.Pattern.quote("_")
        );
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
              "select c1.*,c2.*\n" +
              "from (\n" +
              "	select user_layer.id as study_area FROM user_layer\n" +
              "	left join up_assumptions on up_assumptions.study_area = user_layer.id\n" +
              "	where lower(layer_name) like '%study%' and up_assumptions.id is null\n" +
              ")c1\n" +
              "inner join \n" +
              "(\n" +
              "	SELECT -1 as scenario, category, name, value,units,description,source\n" +
              "	FROM public.up_assumptions\n" +
              "	where study_area=(select min(study_area) from public.up_assumptions)\n" +
              ") c2 on 1=1"
            );
          ) {
            ResultSet data = statement.executeQuery();
            if (status) {
              //ResultSet data = statement.getResultSet();
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

                Layers.put(i, assumption);
                i++;
              }
              //create assumptions for new study areas
              assumptionState = saveAssumptions(Layers);

              assumptionState =
                setCreateAssumptions(scenario.scenario_id, studyArea);
            }
          } catch (SQLException e) {
            assumptionState.status = "Error";
            assumptionState.message = errorMsg + e.toString();
            ObjectMapper Obj = new ObjectMapper();
            final JSONObject json = JSONHelper.createJSONObject(
              Obj.writeValueAsString(scenario)
            );
          }
        }
        System.out.println("row " + row);
        log.debug(
          "User:  " + user_id.toString() + " -> " + scenario.toString()
        );
        ObjectMapper Obj = new ObjectMapper();
        final JSONObject json = JSONHelper.createJSONObject(
          Obj.writeValueAsString(scenario)
        );

        ResponseHelper.writeResponse(params, json);
      } else if ("status".equals(params.getRequiredParam("action"))) {
        ResponseEntity<List<ScenarioExecutionUP>> returns = null;
        try {
          //String transactionUrl = "http://" + upwsHost + ":" + upwsPort + "/scenario_status/";
          String scenario = String.join(
            "_",
            params.getRequest().getParameterValues("scenariosId")
          );
          UriComponentsBuilder uriBuilder = UriComponentsBuilder
            .fromHttpUrl(
              "http://" + upwsHost + ":" + upwsPort + "/scenario_status/"
            )
            .queryParam("scenario", scenario);
          RestTemplate restTemplate = new RestTemplate();
          returns =
            restTemplate.exchange(
              uriBuilder.toUriString(),
              HttpMethod.GET,
              null,
              new ParameterizedTypeReference<List<ScenarioExecutionUP>>() {}
            );

          List<ScenarioExecutionUP> response = returns.getBody();
          JSONArray out = new JSONArray();
          for (ScenarioExecutionUP index : response) {
            //Convert to Json Object
            ObjectMapper Obj = new ObjectMapper();
            final JSONObject json = JSONHelper.createJSONObject(
              Obj.writeValueAsString(index)
            );
            out.put(json);
          }

          log.debug("User:  " + user_id.toString() + " -> " + out.toString());
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
            //ResponseHelper.writeError(null, "", 500, new JSONObject().put("Errors", errors));
          } catch (JsonProcessingException ex) {
            java
              .util.logging.Logger.getLogger(ScenarioUPHandler.class.getName())
              .log(Level.SEVERE, null, ex);
          }
        }
        //                ScenarioExecutionUP returns = new ScenarioExecutionUP();
        //                try {
        //                    String scenario = String.join(",", params.getRequiredParam("scenarios"));
        //                    RestTemplate restTemplate = new RestTemplate();
        //                    //user, scenario, indicators
        //                    MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        //                    map.add("scenario", scenario);
        //
        //                    returns = restTemplate.postForObject("http://" + upwsHost + ":" + upwsPort + "/scenario_status/", map, ScenarioExecutionUP.class);
        //                    ObjectMapper mapper = new ObjectMapper();
        //                    String jsonString = mapper.writeValueAsString(returns);
        //                    log.debug("User:  " + user_id + " -> " + jsonString );
        //                    ResponseHelper.writeResponse(params, jsonString);
        //                } catch (Exception e) {
        //                    errorMsg = errorMsg + e.getMessage() ;
        //                    ObjectMapper mapper = new ObjectMapper();
        //                    String jsonString = mapper.writeValueAsString(returns);
        //                    log.debug("User:  " + user_id.toString() + " -> " + jsonString);
        //                    ResponseHelper.writeResponse(params, errorMsg);
        //
        //                }
      }
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
          .util.logging.Logger.getLogger(ScenarioUPHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java
          .util.logging.Logger.getLogger(ScenarioUPHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
    }
  }

  @Override
  public void handlePut(ActionParameters params) throws ActionException {
    params.requireLoggedInUser();
    Long user_id = params.getUser().getId();
    try {
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
      String indicator = params.getRequiredParam("indicators");
      String[] indicators = indicator.split(java.util.regex.Pattern.quote("_"));
      //Create Scenario
      long row = this.setScenario(scenario);

      //Create Indicators for sceanrio
      //            for (String index:indicators){
      //                this.setScenarioIndicators(index,scenario)
      //            }
      System.out.println("row " + row);
      log.debug("User:  " + user_id.toString() + " -> " + scenario.toString());
      ResponseHelper.writeResponse(params, null);
    } catch (Exception e) {
      log.debug("User:  " + user_id.toString() + " -> " + e.toString());
      ResponseHelper.writeResponse(params, null);
    }
    //        throw new ActionParamsException("Notify there was something wrong with the params");
  }

  @Override
  public void handleDelete(ActionParameters params) throws ActionException {
    params.requireLoggedInUser();
    throw new ActionDeniedException("Not deleting anything");
  }

  private void getUserParams(User user, ActionParameters params)
    throws ActionParamsException {}

  protected boolean evaluateScenario(
    String user,
    String scenario,
    String indicators
  ) {
    String errorMsg = "Scenario UP post ";
    ScenarioExecutionUP returns = new ScenarioExecutionUP();

    try {
      RestTemplate restTemplate = new RestTemplate();
      //user, scenario, indicators
      MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
      map.add("user", user);
      map.add("scenario", scenario);
      map.add("indicators", indicators);

      //            returns = restTemplate.postForObject(PropertyUtil.get("urbanperformance.server")+"/scenario_evaluation/",map, ScenarioExecutionUP.class);
      returns =
        restTemplate.postForObject(
          "http://" + upwsHost + ":" + upwsPort + "/scenario_evaluation/",
          map,
          ScenarioExecutionUP.class
        );
      ObjectMapper mapper = new ObjectMapper();
      String jsonString = mapper.writeValueAsString(returns);
      //log.debug("User:  " + user + " -> " + jsonString + System.getProperty("urbanperformance.server"));
      return true;
    } catch (Exception e) {
      errorMsg =
        errorMsg +
        e.getMessage() +
        System.getProperty("urbanperformance.server");
      log.error(e, errorMsg);
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
          )
        );
        //ResponseHelper.writeError(null, "", 500, new JSONObject().put("Errors", errors));
      } catch (JsonProcessingException ex) {
        java
          .util.logging.Logger.getLogger(ScenarioUPHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
      return false;
    }
    //        ObjectMapper mapper = new ObjectMapper();
    //        String jsonString = mapper.writeValueAsString(returns);
    //        return returns;
  }

  protected long setScenario(ScenarioUP scenario) {
    String errorMsg = "Scenario UP post ";
    long result;
    try (
      Connection connection = DriverManager.getConnection(
        upURL,
        upUser,
        upPassword
      );
      PreparedStatement statement = connection.prepareStatement(
        "INSERT INTO public.up_scenario(name, description, owner_id, is_base) VALUES (?, ?, ?, ?);",
        Statement.RETURN_GENERATED_KEYS
      );
    ) {
      statement.setString(1, scenario.getName());
      statement.setString(2, scenario.getDescription());
      statement.setInt(3, scenario.getOwneId());
      statement.setInt(4, scenario.getIsBase());
      statement.setInt(5, scenario.getStudyArea());
      result = statement.executeUpdate();
      ResultSet rs = statement.getGeneratedKeys();
      if (rs.next()) {
        return rs.getLong(1);
      }
      return result;
    } catch (SQLException e) {
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);
      result = 0;
    }
    return result;
  }

  protected PostStatus saveAssumptions(Map<Integer, Assumptions> Layers) {
    PostStatus status = new PostStatus();
    try (
      Connection connection = DriverManager.getConnection(
        upURL,
        upUser,
        upPassword
      );
      PreparedStatement statement = connection.prepareStatement(
        "INSERT INTO public.up_assumptions(study_area, scenario, category, name, value,units,description,source) VALUES (?, ?, ?, ?, ?, ?, ?, ?);",
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
    } catch (SQLException e) {
      status.status = "Error";
      status.message = "There was an error when inserting data " + e.toString();
    }
    return status;
  }

  protected boolean setScenarioIndicators(String module, long scenario) {
    String errorMsg = "Scenario UP post ";
    boolean result = false;
    try (
      Connection connection = DriverManager.getConnection(
        upURL,
        upUser,
        upPassword
      );
      PreparedStatement statement = connection.prepareStatement(
        "INSERT INTO public.up_scenario_modules( module, scenario)	VALUES (  (SELECT id FROM public.up_modules_translation where name='" +
        module +
        "'), " +
        scenario +
        ");",
        Statement.RETURN_GENERATED_KEYS
      );
    ) {
      result = statement.execute();
      return result;
    } catch (SQLException e) {
      errorMsg = errorMsg + e.toString() + e.getSQLState();
      log.error(e, errorMsg);
    }
    return result;
  }

  protected boolean setCreateScenario(ScenarioUP scenario)
    throws JsonProcessingException {
    ScenarioUP returns;
    RestTemplate restTemplate = new RestTemplate();
    returns =
      restTemplate.postForObject(
        "http://" + upwsHost + ":" + upwsPort + "/scenario/",
        scenario,
        ScenarioUP.class
      );
    return returns.getScenarioId() != -1;
  }

  protected PostStatus setCreateAssumptions(
    Integer scenario_id,
    String study_area
  ) {
    PostStatus postStatus;
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
        "FROM public.up_assumptions\n" +
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
        data_in.add(val);
      }
      Tables<Assumptions> final_data = new Tables<>(data_in);

      RestTemplate restTemplate = new RestTemplate();
      postStatus =
        restTemplate.postForObject(
          "http://" + upwsHost + ":" + upwsPort + "/assumptions/",
          final_data,
          PostStatus.class
        );
    } catch (SQLException e) {
      postStatus = new PostStatus();
      postStatus.status = "Error";
      postStatus.message = "There was an error getting data " + e.toString();
    } catch (Exception e) {
      postStatus = new PostStatus();
      postStatus.status = "Error";
      postStatus.message = e.toString();
    }
    return postStatus;
  }
}

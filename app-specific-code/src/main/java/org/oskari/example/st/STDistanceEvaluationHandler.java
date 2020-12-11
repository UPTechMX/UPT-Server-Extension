package org.oskari.example.st;

import java.util.List;
import java.util.logging.Level;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.example.PostStatus;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import java.util.ArrayList;
import org.oskari.example.UPTRoles;


@OskariActionRoute("evaluate_distances")
public class STDistanceEvaluationHandler extends RestActionHandler {

    private static String upURL;
    private static String upUser;
    private static String upPassword;

    private static String stwsHost;
    private static String stwsPort;
    private static final Logger log = LogFactory.getLogger(STDistanceEvaluationHandler.class);

    private JSONArray errors;
    private ObjectMapper Obj;

    public void preProcess(ActionParameters params) throws ActionException {
        // common method called for all request methods
        log.info(params.getUser(), "accessing route", getName());
        PropertyUtil.loadProperties("/oskari-ext.properties");
        upURL = PropertyUtil.get("up.db.URL");
        upUser = PropertyUtil.get("up.db.user");
        upPassword = PropertyUtil.get("up.db.password");

        stwsHost = PropertyUtil.get("stws.db.host");
        stwsPort = PropertyUtil.get("stws.db.port");

        errors = new JSONArray();
        Obj = new ObjectMapper();
    }

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        
        ResponseEntity<List<STDistanceExecution>> returns = null;
        try {
            params.requireLoggedInUser();
            ArrayList<String> roles = new UPTRoles().handleGet(params,params.getUser());
            if (!roles.contains("uptadmin") && !roles.contains("uptuser") ){
                throw new Exception("User privilege is not enough for this action");
            }
            
            Long user_id = params.getUser().getId();
            String studyArea = params.getRequiredParam("study_area");
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl("http://" + stwsHost + ":" + stwsPort + "/distances_status/")
                    .queryParam("study_area", studyArea)
                    .queryParam("user_id", user_id);
            RestTemplate restTemplate = new RestTemplate();
            returns = restTemplate.exchange(
                    uriBuilder.toUriString(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<STDistanceExecution>>() {
            });

            List<STDistanceExecution> response = returns.getBody();
            JSONArray out = new JSONArray();
            for (STDistanceExecution index : response) {
                //Convert to Json Object
                ObjectMapper Obj = new ObjectMapper();
                final JSONObject json = JSONHelper.createJSONObject(Obj.writeValueAsString(index));
                out.put(json);
            }
            ResponseHelper.writeResponse(params, out);
        } catch (Exception e) {
            log.error(e, e.getMessage());
            try {
                errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
                errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
                ResponseHelper.writeError(params, "", 500, new JSONObject().put("Errors", errors));
            } catch (JsonProcessingException ex) {
                java.util.logging.Logger.getLogger(STDistanceEvaluationHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JSONException ex) {
                java.util.logging.Logger.getLogger(STDistanceEvaluationHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        
        String errorMsg = "Scenario UP post ";
        try {
            params.requireLoggedInUser();
            ArrayList<String> roles = new UPTRoles().handleGet(params,params.getUser());
            if (!roles.contains("uptadmin") && !roles.contains("uptuser") ){
                throw new Exception("User privilege is not enough for this action");
            }
            Long user_id = params.getUser().getId();

            String study_area = params.getRequiredParam("study_area");
            evaluateScenario(user_id.toString(), study_area);
            errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("OK", "Distances evaluation started"))));
            ResponseHelper.writeResponse(params, new JSONObject().put("Errors", errors));
        } catch (Exception e) {
            errorMsg = errorMsg + e.getMessage();
            log.error(e, errorMsg);
            try {
                errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
                ResponseHelper.writeError(params, "", 500, new JSONObject().put("Errors", errors));
            } catch (JsonProcessingException ex) {
                java.util.logging.Logger.getLogger(STDistanceEvaluationHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JSONException ex) {
                java.util.logging.Logger.getLogger(STDistanceEvaluationHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void handlePut(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
        throw new ActionDeniedException("Not implemented");
    }

    @Override
    public void handleDelete(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
        throw new ActionDeniedException("Not implemented");
    }

    protected void evaluateScenario(String user, String scenario) throws Exception{
        String errorMsg = "Scenario UP post ";
        STDistanceExecution returns = new STDistanceExecution();

        try {
            RestTemplate restTemplate = new RestTemplate();
            
            MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
            map.add("user_id", user);
            map.add("study_area", scenario);

            returns = restTemplate.postForObject("http://" + stwsHost + ":" + stwsPort + "/distances_evaluation/", map, STDistanceExecution.class);
            ObjectMapper mapper = new ObjectMapper();
            String jsonString = mapper.writeValueAsString(returns);
        } catch (Exception e) {
            errorMsg = errorMsg + e.getMessage() + System.getProperty("urbanperformance.server");
            log.error(e, errorMsg);
            try {
                errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
                errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Detail", e.getMessage() ))));
            } catch (JsonProcessingException ex) {
                java.util.logging.Logger.getLogger(STDistanceEvaluationHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            throw new Exception();
        }
    }
}

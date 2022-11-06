package org.oskari.upt.up;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.upt.PostStatus;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import java.util.ArrayList;
import org.oskari.upt.UPTRoles;

@OskariActionRoute("classification_manager")
public class UPFClassesHandler extends RestActionHandler {

    private static String upURL;
    private static String upUser;
    private static String upPassword;

    private static String upwsHost;
    private static String upwsPort;
    private static final Logger log = LogFactory.getLogger(UPFClassesHandler.class);

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
        String errorMsg = "Results UP post ";
        try {
            params.requireLoggedInUser();
            ArrayList<String> roles = new UPTRoles().handleGet(params, params.getUser());
            if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
                throw new Exception("User privilege is not enough for this action");
            }

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<List<UPFClasses>> returns = null;
            returns = restTemplate.exchange(
                    "http://" + upwsHost + ":" + upwsPort + "/classification/",
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<UPFClasses>>() {
                    });

            Long user_id = params.getUser().getId();
            List<UPFClasses> response = returns.getBody();

            JSONArray out = new JSONArray();
            ObjectMapper Obj = new ObjectMapper();
            for (UPFClasses index : response) {
                // Convert to Json Object
                out.put(JSONHelper.createJSONObject(Obj.writeValueAsString(index)));
            }

            log.debug("User:  " + user_id.toString() + " -> " + out.toString());
            ResponseHelper.writeResponse(params, out);
        } catch (Exception e) {
            errorMsg = errorMsg + e.getMessage();
            log.error(e, errorMsg);
            try {
                errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
                ResponseHelper.writeError(null, "", 500, new JSONObject().put("Errors", errors));
            } catch (JsonProcessingException ex) {
                java.util.logging.Logger.getLogger(UPFClassesHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JSONException ex) {
                java.util.logging.Logger.getLogger(UPFClassesHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {

        try {
            params.requireLoggedInUser();
            ArrayList<String> roles = new UPTRoles().handleGet(params, params.getUser());
            if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
                throw new Exception("User privilege is not enough for this action");
            }

            PostStatus postStatus = new PostStatus();
            RestTemplate restTemplate = new RestTemplate();
            UPFClasses data = new UPFClasses();

            data.category = params.getRequiredParam("category");
            data.name = params.getRequiredParam("name");
            data.fclass = params.getRequiredParam("fclass");
            postStatus = restTemplate.postForObject("http://" + upwsHost + ":" + upwsPort + "/classification/", data,
                    PostStatus.class);
        } catch (Exception e) {
            try {
                errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
                ResponseHelper.writeError(null, "", 500, new JSONObject().put("Errors", errors));
            } catch (JsonProcessingException ex) {
                java.util.logging.Logger.getLogger(UPFClassesHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JSONException ex) {
                java.util.logging.Logger.getLogger(UPFClassesHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void handlePut(ActionParameters params) throws ActionException {

        try {
            params.requireLoggedInUser();
            ArrayList<String> roles = new UPTRoles().handleGet(params, params.getUser());
            if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
                throw new Exception("User privilege is not enough for this action");
            }

            PostStatus postStatus = new PostStatus();
            UPFClasses data = new UPFClasses();

            data.category = params.getRequiredParam("category");
            data.name = params.getRequiredParam("name");
            data.fclass = params.getRequiredParam("fclass");

            RestTemplate restTemplate = new RestTemplate();
            Map<String, Integer> param = new HashMap<String, Integer>();
            param.put("id", Integer.parseInt(params.getRequiredParam("classification_id")));
            restTemplate.put("http://" + upwsHost + ":" + upwsPort + "/classification/{id}", data, param);
        } catch (Exception e) {
            log.error(e);
            try {
                errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
                ResponseHelper.writeError(null, "", 500, new JSONObject().put("Errors", errors));
            } catch (JsonProcessingException ex) {
                java.util.logging.Logger.getLogger(UPFClassesHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JSONException ex) {
                java.util.logging.Logger.getLogger(UPFClassesHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void handleDelete(ActionParameters params) throws ActionException {

        try {
            params.requireLoggedInUser();
            ArrayList<String> roles = new UPTRoles().handleGet(params, params.getUser());
            if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
                throw new Exception("User privilege is not enough for this action");
            }

            Map<String, Integer> param = new HashMap<String, Integer>();
            param.put("id", Integer.parseInt(params.getRequiredParam("classification_id")));

            RestTemplate restTemplate = new RestTemplate();
            restTemplate.delete("http://" + upwsHost + ":" + upwsPort + "/classification/{id}", param);
        } catch (Exception e) {
            log.error(e);
            try {
                errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
                ResponseHelper.writeError(null, "", 500, new JSONObject().put("Errors", errors));
            } catch (JsonProcessingException ex) {
                java.util.logging.Logger.getLogger(UPFClassesHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JSONException ex) {
                java.util.logging.Logger.getLogger(UPFClassesHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}

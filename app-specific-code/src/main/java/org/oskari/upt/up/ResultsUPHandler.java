package org.oskari.upt.up;

import org.oskari.upt.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.logging.Level;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
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
import java.util.ArrayList;

@OskariActionRoute("scenario-results")
public class ResultsUPHandler extends RestActionHandler {
    private static String upURL;
    private static String upUser;
    private static String upPassword;

    private static String upwsHost;
    private static String upwsPort;
    private static final Logger log = LogFactory.getLogger(ResultsUPHandler.class);
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
        // ResultsScenarioUP dataUP = new ResultsScenarioUP();
        String errorMsg = "Results UP post ";
        try {
            params.requireLoggedInUser();
            ArrayList<String> roles = new UPTRoles().handleGet(params, params.getUser());
            if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
                throw new Exception("User privilege is not enough for this action");
            }

            String transactionUrl = "http://" + upwsHost + ":" + upwsPort + "/scenario-results/";

            UriComponentsBuilder builder = UriComponentsBuilder
                    .fromUriString(transactionUrl)
                    // Add query parameter
                    .queryParam("scenario", String.join("_", params.getRequest().getParameterValues("scenariosId")));

            RestTemplate restTemplate = new RestTemplate();

            ResponseEntity<ResultsScenarioUP[]> responseEntity = restTemplate.getForEntity(
                    transactionUrl + String.join("_", params.getRequest().getParameterValues("scenariosId")),
                    ResultsScenarioUP[].class);
            ResultsScenarioUP[] returns = responseEntity.getBody();

            setIndicators(returns);
            ResultSet indicators = getIndicators(params);
            while (indicators.next()) {
                for (ResultsScenarioUP res : returns) {
                    for (ResultsValuesUP result : res.results) {
                        if (result != null && indicators.getString("indicator") != null
                                && result.name.equals(indicators.getString("indicator"))) {
                            result.label = indicators.getString("label");
                            result.units = indicators.getString("units");
                            break;
                        }
                    }
                }
            }
            // Change range for percentage results
            DecimalFormatSymbols formatSymbols = new DecimalFormatSymbols(Locale.getDefault());
            formatSymbols.setDecimalSeparator('.');
            formatSymbols.setGroupingSeparator(' ');
            DecimalFormat format = new DecimalFormat("0.00", formatSymbols);
            // format.setMaximumFractionDigits(2);
            for (ResultsScenarioUP res : returns) {
                for (ResultsValuesUP result : res.results) {
                    if (result != null && result.units != null && result.units.equals("%")) {
                        result.value = Float.parseFloat(format.format(result.value));
                    }
                }
            }
            JSONArray out = new JSONArray();
            ObjectMapper Obj = new ObjectMapper();
            for (ResultsScenarioUP res : returns) {
                out.put(JSONHelper.createJSONObject(Obj.writeValueAsString(res)));
            }

            ResponseHelper.writeResponse(params, out);
        } catch (Exception e) {
            errorMsg = errorMsg + e.getMessage();
            log.error(e, errorMsg);
            try {
                errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
                errors.put(
                        JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.getMessage()))));
                ResponseHelper.writeError(params, "", 500, new JSONObject().put("Errors", errors));
            } catch (JsonProcessingException ex) {
                java.util.logging.Logger.getLogger(ResultsUPHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JSONException ex) {
                java.util.logging.Logger.getLogger(ResultsUPHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
        throw new ActionException("This will be logged including stack trace");
    }

    @Override
    public void handlePut(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
        throw new ActionParamsException("Notify there was something wrong with the params");
    }

    @Override
    public void handleDelete(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
        throw new ActionDeniedException("Not deleting anything");
    }

    private void getUserParams(User user, ActionParameters params) throws ActionParamsException {
    }

    protected void setIndicators(ResultsScenarioUP[] returns) throws Exception {
        try (
                Connection connection = DriverManager.getConnection(
                        upURL,
                        upUser,
                        upPassword)) {
            PreparedStatement statement = connection.prepareStatement("insert into up_indicators(indicator)\n" +
                    "values(?) on conflict(indicator) do nothing");
            connection.setAutoCommit(false);
            for (ResultsScenarioUP res : returns) {
                for (ResultsValuesUP result : res.results) {
                    statement.setString(1, result.name);
                    statement.addBatch();
                }
            }
            statement.executeBatch();
            connection.commit();
        } catch (Exception e) {
            try {
                errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
                errors.put(
                        JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.getMessage()))));
            } catch (JsonProcessingException ex) {
                java.util.logging.Logger.getLogger(ResultsUPHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            throw new Exception();
        }
    }

    protected ResultSet getIndicators(ActionParameters params) throws Exception {
        ResultSet result = null;
        try (
                Connection connection = DriverManager.getConnection(
                        upURL,
                        upUser,
                        upPassword)) {
            Statement statement = connection.createStatement();
            result = statement.executeQuery("select indicator,label,units from up_indicators\n" +
                    "inner join up_indicators_translation ON up_indicators_translation.up_indicators_id = up_indicators.id\n"
                    +
                    "where language='English'");
            return result;
        } catch (Exception e) {
            try {
                errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
                errors.put(
                        JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.getMessage()))));
            } catch (JsonProcessingException ex) {
                java.util.logging.Logger.getLogger(ResultsUPHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            throw new Exception();
        }
    }

}

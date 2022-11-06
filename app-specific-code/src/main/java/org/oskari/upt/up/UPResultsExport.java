package org.oskari.upt.up;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.logging.Level;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.upt.PostStatus;
import org.oskari.upt.ResultsScenarioUP;
import org.oskari.upt.ResultsValuesUP;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;

@OskariActionRoute("up_results_export")

public class UPResultsExport extends RestActionHandler {

    private static String upURL;
    private static String upUser;
    private static String upPassword;
    private static String upwsHost;
    private static String upwsPort;
    private static String upProjection;
    private User user_logged;
    private JSONArray errors;
    private ObjectMapper Obj;
    private static final Logger log = LogFactory.getLogger(UPResultsExport.class);

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
        upProjection = PropertyUtil.get("oskari.native.srs")
                .substring(PropertyUtil.get("oskari.native.srs").indexOf(":") + 1);

        errors = new JSONArray();
        Obj = new ObjectMapper();

        user_logged = params.getUser();

    }

    public void handleGet(ActionParameters params) {
        // ResultsScenarioUP dataUP = new ResultsScenarioUP();
        String errorMsg = "Results UP post ";
        try {
            params.requireLoggedInUser();
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

            /****/
            // Structuring data
            ArrayList<ArrayList<String>> rows = new ArrayList<>();
            boolean head = true;
            ArrayList<String> headers = new ArrayList<>();
            for (ResultsScenarioUP res : returns) {
                if (head) {
                    headers.add("");
                    headers.add(res.scenario_id.toString() + " " + res.name);
                    head = false;
                } else {
                    headers.add(res.scenario_id.toString() + " " + res.name);
                }
            }
            rows.add(headers);

            // get all indicators
            ArrayList<String> allIndicators = new ArrayList<>();
            for (ResultsScenarioUP res : returns) {
                for (ResultsValuesUP result : res.results) {
                    if (!allIndicators.contains(result.name + " (" + result.units + ")")) {
                        allIndicators.add(result.name + " (" + result.units + ")");
                    }
                }
            }
            Collections.sort(allIndicators);
            for (String a : allIndicators) {
                rows.add(new ArrayList<>(Collections.nCopies(rows.get(0).size(), "")));
            }

            // add values
            Integer cont = 0;
            for (ResultsScenarioUP res : returns) {
                cont++;
                for (ResultsValuesUP result : res.results) {
                    if (!rows.get(allIndicators.indexOf(result.name + " (" + result.units + ")") + 1)
                            .contains(result.name + " (" + result.units + ")")) {
                        rows.get(allIndicators.indexOf(result.name + " (" + result.units + ")") + 1).add(0,
                                result.name + " (" + result.units + ")");
                    }
                    rows.get(allIndicators.indexOf(result.name + " (" + result.units + ")") + 1).add(cont,
                            result.value.toString());
                }
            }

            ByteArrayOutputStream csvWriter = new ByteArrayOutputStream();
            for (ArrayList<String> rowData : rows) {
                csvWriter.write(String.join(",", rowData).getBytes());
                csvWriter.write("\n".getBytes());
            }
            /****/
            ResponseHelper.writeResponse(params, 200, "text/csv", csvWriter);
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

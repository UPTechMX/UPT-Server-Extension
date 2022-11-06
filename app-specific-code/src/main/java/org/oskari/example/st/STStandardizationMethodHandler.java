package org.oskari.example.st;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.example.PostStatus;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.oskari.example.UPTRoles;

@OskariActionRoute("st_standardization")
public class STStandardizationMethodHandler extends RestActionHandler {

    private static String stURL;
    private static String stUser;
    private static String stPassword;
    private static final Logger log = LogFactory.getLogger(STSettingsHandler.class);

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

        String errorMsg = "Operation method get";
        Long user_id = params.getUser().getId();

        ArrayList<STStandardizationMethod> modules = new ArrayList<>();
        try (
                Connection connection = DriverManager.getConnection(
                        stURL,
                        stUser,
                        stPassword);) {
            params.requireLoggedInUser();
            ArrayList<String> roles = new UPTRoles().handleGet(params, params.getUser());
            if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
                throw new Exception("User privilege is not enough for this action");
            }

            PreparedStatement statement = connection.prepareStatement(
                    "SELECT id, value, language, label\n"
                            + "FROM public.st_normalization_method_options;");

            errors.put(JSONHelper.createJSONObject(
                    Obj.writeValueAsString(new PostStatus("OK", "Executing query: " + statement.toString()))));

            ResultSet data = statement.executeQuery();
            while (data.next()) {
                STStandardizationMethod layer = new STStandardizationMethod();
                layer.id = data.getInt("id");
                layer.value = data.getInt("value");
                layer.language = data.getString("language");
                layer.label = data.getString("label");
                modules.add(layer);
            }
            ;

            JSONArray out = new JSONArray();
            for (STStandardizationMethod index : modules) {
                // Convert to Json Object
                final JSONObject json = JSONHelper.createJSONObject(Obj.writeValueAsString(index));
                out.put(json);
            }
            ResponseHelper.writeResponse(params, out);
        } catch (Exception e) {
            try {
                errorMsg = errorMsg + e.toString();
                log.error(e, errorMsg);
                errors.put(JSONHelper.createJSONObject(
                        Obj.writeValueAsString(new PostStatus("Error", "Executing query: " + e.toString()))));
                ResponseHelper.writeError(null, "", 500, new JSONObject().put("Errors", errors));
            } catch (JsonProcessingException ex) {
                java.util.logging.Logger.getLogger(STStandardizationMethodHandler.class.getName()).log(Level.SEVERE,
                        null, ex);
            } catch (JSONException ex) {
                java.util.logging.Logger.getLogger(STStandardizationMethodHandler.class.getName()).log(Level.SEVERE,
                        null, ex);
            }
        }
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {

        String errorMsg = "Standarization method get";
        Long user_id = params.getUser().getId();

        Integer value = Integer.parseInt(params.getRequiredParam("value"));

        String language = params.getRequiredParam("language");
        String label = params.getRequiredParam("label");
        // PostStatus status = new PostStatus();
        try (
                Connection connection = DriverManager.getConnection(
                        stURL,
                        stUser,
                        stPassword);) {
            params.requireLoggedInUser();
            ArrayList<String> roles = new UPTRoles().handleGet(params, params.getUser());
            if (!roles.contains("uptadmin")) {
                throw new Exception("User privilege is not enough for this action");
            }

            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO public.st_normalization_method_options(\n"
                            + "value, language, label)\n"
                            + "VALUES (?, ?, ?);");

            statement.setLong(1, value);
            statement.setString(2, language);
            statement.setString(3, label);

            errors.put(JSONHelper.createJSONObject(
                    Obj.writeValueAsString(new PostStatus("OK", "Executing query: " + statement.toString()))));

            statement.execute();

            errors.put(JSONHelper.createJSONObject(
                    Obj.writeValueAsString(new PostStatus("OK", "Standarization method registered"))));
            ResponseHelper.writeResponse(params, new JSONObject().put("Errors", errors));
        } catch (Exception e) {
            try {
                errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
                ResponseHelper.writeError(params, "", 500, new JSONObject().put("Errors", errors));
            } catch (JsonProcessingException ex) {
                java.util.logging.Logger.getLogger(STStandardizationMethodHandler.class.getName()).log(Level.SEVERE,
                        null, ex);
            } catch (JSONException ex) {
                java.util.logging.Logger.getLogger(STStandardizationMethodHandler.class.getName()).log(Level.SEVERE,
                        null, ex);
            }
            errorMsg = errorMsg + e.toString();
            log.error(e, errorMsg);
        }
    }

    @Override
    public void handlePut(ActionParameters params) throws ActionException {

        String errorMsg = "Operation method put";
        Long user_id = params.getUser().getId();

        Integer id = Integer.parseInt(params.getRequiredParam("id"));
        Integer value = Integer.parseInt(params.getRequiredParam("value"));
        String language = params.getRequiredParam("language");
        String label = params.getRequiredParam("label");
        try (
                Connection connection = DriverManager.getConnection(
                        stURL,
                        stUser,
                        stPassword);) {
            params.requireLoggedInUser();
            ArrayList<String> roles = new UPTRoles().handleGet(params, params.getUser());
            if (!roles.contains("uptadmin")) {
                throw new Exception("User privilege is not enough for this action");
            }

            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE public.st_normalization_method_options\n"
                            + "SET value=?, language=?, label=?\n"
                            + "WHERE id=?;");
            statement.setLong(1, value);
            statement.setString(2, language);
            statement.setString(3, label);
            statement.setInt(4, id);

            ResultSet data = statement.executeQuery();
            errors.put(JSONHelper
                    .createJSONObject(Obj.writeValueAsString(new PostStatus("OK", "Operation method registered"))));
            ResponseHelper.writeResponse(params, new JSONObject().put("Errors", errors));
        } catch (Exception e) {
            try {
                errors.put(JSONHelper.createJSONObject(
                        Obj.writeValueAsString(new PostStatus("Error", "Query error:" + e.toString()))));
                ResponseHelper.writeResponse(params, new JSONObject().put("Errors", errors));
            } catch (JsonProcessingException ex) {
                java.util.logging.Logger.getLogger(STStandardizationMethodHandler.class.getName()).log(Level.SEVERE,
                        null, ex);
            } catch (JSONException ex) {
                java.util.logging.Logger.getLogger(STStandardizationMethodHandler.class.getName()).log(Level.SEVERE,
                        null, ex);
            }

            errorMsg = errorMsg + e.toString();
            log.error(e, errorMsg);
        }
    }

    @Override
    public void handleDelete(ActionParameters params) throws ActionException {

        String errorMsg = "Standarization method delete";
        Long user_id = params.getUser().getId();

        Integer id = Integer.parseInt(params.getRequiredParam("id"));
        try (
                Connection connection = DriverManager.getConnection(
                        stURL,
                        stUser,
                        stPassword);) {
            params.requireLoggedInUser();
            ArrayList<String> roles = new UPTRoles().handleGet(params, params.getUser());
            if (!roles.contains("uptadmin")) {
                throw new Exception("User privilege is not enough for this action");
            }

            PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM public.st_normalization_method_options\n"
                            + "WHERE id=?;");
            statement.setInt(1, id);

            statement.execute();

            errors.put(JSONHelper.createJSONObject(
                    Obj.writeValueAsString(new PostStatus("OK", "Standarization method registered"))));
            ResponseHelper.writeResponse(params, new JSONObject().put("Errors", errors));

        } catch (Exception e) {
            try {
                errors.put(JSONHelper.createJSONObject(
                        Obj.writeValueAsString(new PostStatus("Error", "Query error: " + e.toString()))));
                ResponseHelper.writeResponse(params, new JSONObject().put("Errors", errors));
            } catch (JsonProcessingException ex) {
                java.util.logging.Logger.getLogger(STStandardizationMethodHandler.class.getName()).log(Level.SEVERE,
                        null, ex);
            } catch (JSONException ex) {
                java.util.logging.Logger.getLogger(STStandardizationMethodHandler.class.getName()).log(Level.SEVERE,
                        null, ex);
            }
            errorMsg = errorMsg + e.toString();
            log.error(e, errorMsg);
        }
    }
}

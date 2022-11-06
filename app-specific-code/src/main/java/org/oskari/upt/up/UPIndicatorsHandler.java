package org.oskari.upt.up;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.logging.Level;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.upt.PostStatus;

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

@OskariActionRoute("indicators_manager")
public class UPIndicatorsHandler extends RestActionHandler {

    private static String upURL;
    private static String upUser;
    private static String upPassword;

    private static String upwsHost;
    private static String upwsPort;
    private static final Logger log = LogFactory.getLogger(UPIndicatorsHandler.class);

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

        JSONArray indicators = new JSONArray();
        ObjectMapper Obj = new ObjectMapper();
        try (
                Connection connection = DriverManager.getConnection(
                        upURL,
                        upUser,
                        upPassword)) {
            params.requireLoggedInUser();
            ArrayList<String> roles = new UPTRoles().handleGet(params, params.getUser());
            if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
                throw new Exception("User privilege is not enough for this action");
            }

            PreparedStatement statement = connection.prepareStatement(
                    "SELECT id, indicator\n"
                            + "	FROM public.up_indicators;");
            ResultSet result = statement.executeQuery();
            while (result.next()) {
                UPIndicators data = new UPIndicators();
                data.id = result.getInt("id");
                data.indicator = result.getString("indicator");
                indicators.put(JSONHelper.createJSONObject(Obj.writeValueAsString(data)));
            }
            ResponseHelper.writeResponse(params, indicators);
        } catch (Exception e) {
            try {
                errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
                ResponseHelper.writeError(params, "", 500, new JSONObject().put("Errors", errors));
            } catch (JsonProcessingException | JSONException ex) {
                java.util.logging.Logger.getLogger(UPIndicatorsHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {

        try (
                Connection connection = DriverManager.getConnection(
                        upURL,
                        upUser,
                        upPassword)) {
            params.requireLoggedInUser();
            ArrayList<String> roles = new UPTRoles().handleGet(params, params.getUser());
            if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
                throw new Exception("User privilege is not enough for this action");
            }

            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO public.up_indicators(\n"
                            + "	indicator)\n"
                            + "	VALUES (?) on conflict(indicator) do nothing;");
            statement.setString(1, params.getRequiredParam("indicator"));
            statement.execute();
        } catch (Exception e) {
            try {
                errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
                ResponseHelper.writeError(params, "", 500, new JSONObject().put("Errors", errors));
            } catch (JsonProcessingException ex) {
                java.util.logging.Logger.getLogger(UPIndicatorsHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JSONException ex) {
                java.util.logging.Logger.getLogger(UPIndicatorsHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void handlePut(ActionParameters params) throws ActionException {

        try (
                Connection connection = DriverManager.getConnection(
                        upURL,
                        upUser,
                        upPassword)) {
            params.requireLoggedInUser();
            ArrayList<String> roles = new UPTRoles().handleGet(params, params.getUser());
            if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
                throw new Exception("User privilege is not enough for this action");
            }

            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE public.up_indicators\n"
                            + "	SET indicator=?\n"
                            + "	WHERE id=?;");
            statement.setString(1, params.getRequiredParam("indicator"));
            statement.setInt(2, Integer.parseInt(params.getRequiredParam("id")));
            statement.execute();
        } catch (Exception e) {
            try {
                errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
                ResponseHelper.writeError(params, "", 500, new JSONObject().put("Errors", errors));
            } catch (JsonProcessingException ex) {
                java.util.logging.Logger.getLogger(UPIndicatorsHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JSONException ex) {
                java.util.logging.Logger.getLogger(UPIndicatorsHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    @Override
    public void handleDelete(ActionParameters params) throws ActionException {
        try (
                Connection connection = DriverManager.getConnection(
                        upURL,
                        upUser,
                        upPassword)) {
            params.requireLoggedInUser();
            ArrayList<String> roles = new UPTRoles().handleGet(params, params.getUser());
            if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
                throw new Exception("User privilege is not enough for this action");
            }

            PreparedStatement statement = connection.prepareStatement(
                    "DELETE FROM public.up_indicators\n"
                            + "	WHERE id=?;");
            statement.setInt(1, Integer.parseInt(params.getRequiredParam("id")));
            statement.execute();
        } catch (Exception e) {
            try {
                errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
                ResponseHelper.writeError(params, "", 500, new JSONObject().put("Errors", errors));
            } catch (JsonProcessingException ex) {
                java.util.logging.Logger.getLogger(UPIndicatorsHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JSONException ex) {
                java.util.logging.Logger.getLogger(UPIndicatorsHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}

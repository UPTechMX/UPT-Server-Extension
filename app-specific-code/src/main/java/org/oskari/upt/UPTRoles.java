package org.oskari.upt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.annotation.OskariActionRoute;
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.logging.Level;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.upt.up.UPTUserRoles;

@OskariActionRoute("upt_roles")
public class UPTRoles extends RestActionHandler {
    private static String upURL;
    private static String upUser;
    private static String upPassword;

    private static String upwsHost;
    private static String upwsPort;
    private static final Logger log = LogFactory.getLogger(UPTRoles.class);

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
        params.requireLoggedInUser();
        Long user_id = params.getUser().getId();
        try (
                Connection connection = DriverManager.getConnection(
                        upURL,
                        upUser,
                        upPassword)) {
            PreparedStatement statement = connection.prepareStatement(
                    "select oskari_roles.name from oskari_users\n" +
                            "inner join oskari_users_roles\n" +
                            "on oskari_users_roles.user_id=oskari_users.id\n" +
                            "inner join oskari_roles on oskari_roles.id=oskari_users_roles.role_id\n" +
                            "where oskari_users.id=?");
            statement.setLong(1, user_id);
            ResultSet data = statement.executeQuery();
            ArrayList<String> roles = new ArrayList<>();
            while (data.next()) {
                roles.add(data.getString("name"));
            }
            UPTUserRoles result = new UPTUserRoles(roles.toArray(new String[0]));
            ResponseHelper.writeResponse(params, JSONHelper.createJSONObject(Obj.writeValueAsString(result)));
        } catch (Exception e) {
            try {
                errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
                ResponseHelper.writeError(params, "", 500, new JSONObject().put("Errors", errors));
            } catch (JsonProcessingException | JSONException ex) {
                java.util.logging.Logger.getLogger(UPTRoles.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public ArrayList handleGet(ActionParameters params, User user) throws Exception {
        this.preProcess(params);
        Long user_id = user.getId();

        Connection connection = DriverManager.getConnection(
                upURL,
                upUser,
                upPassword);
        PreparedStatement statement = connection.prepareStatement(
                "select oskari_roles.name from oskari_users\n" +
                        "inner join oskari_users_roles\n" +
                        "on oskari_users_roles.user_id=oskari_users.id\n" +
                        "inner join oskari_roles on oskari_roles.id=oskari_users_roles.role_id\n" +
                        "where oskari_users.id=?");
        statement.setLong(1, user_id);
        ResultSet data = statement.executeQuery();
        ArrayList<String> roles = new ArrayList<>();
        while (data.next()) {
            roles.add(data.getString("name"));
        }
        return roles;
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
        throw new ActionParamsException("Not implemented");
    }

    @Override
    public void handlePut(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
        throw new ActionParamsException("Not implemented");
    }

    @Override
    public void handleDelete(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
        throw new ActionParamsException("Not implemented");
    }
}

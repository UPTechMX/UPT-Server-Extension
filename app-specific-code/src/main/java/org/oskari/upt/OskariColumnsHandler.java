package org.oskari.upt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("list_oskari_columns")
public class OskariColumnsHandler extends RestActionHandler {

    private static String upURL;
    private static String upUser;
    private static String upPassword;

    private static final Logger log = LogFactory.getLogger(OskariColumnsHandler.class);

    @Override
    public void preProcess(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
        // common method called for all request methods
        log.info(params.getUser(), "accessing route", getName());
        PropertyUtil.loadProperties("/oskari-ext.properties");
        upURL = PropertyUtil.get("up.db.URL");
        upUser = PropertyUtil.get("up.db.user");
        upPassword = PropertyUtil.get("up.db.password");
    }

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        String errorMsg = "Layers ST get ";
        Long user_id = params.getUser().getId();
        try {
            params.requireLoggedInUser();
            ArrayList<String> roles = new UPTRoles().handleGet(params, params.getUser());
            if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
                throw new Exception("User privilege is not enough for this action");
            }

            Layers layers = new Layers();
            layers.setColumns(getColumns(params.getRequiredParam("layer_id")));
            ObjectMapper Obj = new ObjectMapper();
            final JSONObject json = JSONHelper.createJSONObject(Obj.writeValueAsString(layers));
            ResponseHelper.writeResponse(params, json);
        } catch (Exception e) {
            errorMsg = errorMsg + e.getMessage();
            log.error(e, errorMsg);
        }
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
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

    private ArrayList<String> getColumns(String id) {
        String errorMsg = "getLayers";
        ArrayList<String> layers = new ArrayList<String>();
        try (
                Connection connection = DriverManager.getConnection(
                        upURL,
                        upUser,
                        upPassword);) {
            Statement statement = connection.createStatement();

            ResultSet data = statement.executeQuery(
                    "with cols as("
                            + " select id,fields "
                            + " from user_layer "
                            + " where id=" + id
                            + " ) "
                            + " select name "
                            + " from cols,json_populate_recordset(null::record,cols.fields) as(name text) "
                            + " where name !='the_geom' "
                            + " union all "
                            + " select 'geometry';");
            while (data.next()) {
                layers.add(data.getString("name"));
            }
            return layers;
        } catch (SQLException e) {
            errorMsg = errorMsg + e.toString();
            log.error(e, errorMsg);
        }
        return layers;
    }
}

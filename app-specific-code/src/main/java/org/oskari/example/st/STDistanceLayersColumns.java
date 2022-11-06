package org.oskari.example.st;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONObject;
import org.oskari.example.Layers;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("list_suitability_layer_columns")

public class STDistanceLayersColumns extends RestActionHandler {

    private static String stURL;
    private static String stUser;
    private static String stPassword;

    private static String stwsHost;
    private static String stwsPort;
    private static String stProjection;
    private Long user_id;

    Map<Integer, STSettings> stLayers;

    private static final Logger log = LogFactory.getLogger(LayersSTHandler.class);

    @Override
    public void preProcess(ActionParameters params) throws ActionException {
        // common method called for all request methods
        log.info(params.getUser(), "accessing route", getName());
        PropertyUtil.loadProperties("/oskari-ext.properties");
        stURL = PropertyUtil.get("db.url");
        stUser = PropertyUtil.get("db.username");
        stPassword = PropertyUtil.get("db.password");

        stwsHost = PropertyUtil.get("stws.db.host");
        stwsPort = PropertyUtil.get("stws.db.port");
        stProjection = PropertyUtil.get("oskari.native.srs")
                .substring(PropertyUtil.get("oskari.native.srs").indexOf(":") + 1);
        user_id = params.getUser().getId();
    }

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        Layers layers = new Layers();
        try {
            params.requireLoggedInUser();
            layers.setColumns(getColumns(params.getRequiredParam("layer_id")));
            ObjectMapper Obj = new ObjectMapper();
            final JSONObject json = JSONHelper.createJSONObject(Obj.writeValueAsString(layers));
            ResponseHelper.writeResponse(params, json);
        } catch (JsonProcessingException ex) {
            java.util.logging.Logger.getLogger(STDistanceLayersColumns.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
    }

    @Override
    public void handleDelete(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
    }

    @Override
    public void handlePut(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
    }

    private ArrayList<String> getColumns(String id) {
        String errorMsg = "getColumns";
        ArrayList<String> layers = new ArrayList<String>();
        try (
                Connection connection = DriverManager.getConnection(
                        stURL,
                        stUser,
                        stPassword);) {
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

package org.oskari.upt.st;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONObject;
import org.oskari.upt.Data;
import org.oskari.upt.Directories;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.oskari.upt.UPTRoles;

@OskariActionRoute("list_oskari_layers")
public class STOskariLayers extends RestActionHandler {

    private static String stURL;
    private static String stUser;
    private static String stPassword;

    private static String stwsHost;
    private static String stwsPort;
    private static String stProjection;
    private Long user_id;
    private String user_uuid;
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
        user_uuid = params.getUser().getUuid();
    }

    @Override
    public void handleGet(ActionParameters params) throws ActionException {

        ArrayList<Directories> directories = new ArrayList<Directories>();
        Data tree = new Data();
        String errorMsg = "Oskari Layers get ";
        Long studyArea = params.getRequiredParamLong("studyArea");
        try {
            params.requireLoggedInUser();
            ArrayList<String> roles = new UPTRoles().handleGet(params, params.getUser());
            if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
                throw new Exception("User privilege is not enough for this action");
            }
            // Get directories
            Directories dir = new Directories();
            dir.setData("my_data");
            dir.setLabel("My Data");
            dir.setIcon(null);
            directories.add(dir);

            // Get layers
            ArrayList<Directories> layers = getLayers(studyArea);
            dir.setChildren(layers);

            JSONArray out = new JSONArray();
            for (Directories index : directories) {
                // Convert to Json Object
                ObjectMapper Obj = new ObjectMapper();
                final JSONObject json = JSONHelper.createJSONObject(Obj.writeValueAsString(index));
                out.put(json);
            }
            ObjectMapper Obj = new ObjectMapper();
            tree.setData(directories);
            final JSONObject outs = JSONHelper.createJSONObject(Obj.writeValueAsString(tree));
            log.debug("User:  " + user_id.toString());
            ResponseHelper.writeResponse(params, outs);
        } catch (Exception e) {
            errorMsg = errorMsg + e.getMessage();
            log.error(e, errorMsg);
        }
    }

    private ArrayList<Directories> getLayers(Long studyArea) {
        String errorMsg = "getLayers";
        ArrayList<Directories> children = new ArrayList<Directories>();
        try (
                Connection connection = DriverManager.getConnection(
                        stURL,
                        stUser,
                        stPassword);
                PreparedStatement statement = connection.prepareStatement("with study_area as(\n" +
                        "	select geometry from user_layer_data where user_layer_id=?\n" +
                        "), user_layers as(\n" +
                        "    select user_layer.id,\n" +
                        "    layer_name ,\n" +
                        "    case when is_public is null then 0 else is_public end as is_public\n" +
                        "    from user_layer\n" +
                        "    left join upt_user_layer_scope on upt_user_layer_scope.user_layer_id=user_layer.id\n" +
                        "    where user_layer.uuid=? or upt_user_layer_scope.is_public=1\n" +
                        "), public_layers as(\n" +
                        "   select name as id, layer_name, capabilities::json->>'geom' as wkt from oskari_maplayer" +
                        ")\n" +
                        "select id,layer_name\n" +
                        "from user_layers\n" +
                        ",study_area\n" +
                        ",public_layers\n" +
                        "where st_intersects(st_geomfromtext(user_layers.wkt,4326),\n" +
                        "st_transform(st_setsrid(study_area.geometry,?),4326)) \n" +
                        "or st_intersects(st_geomfromtext(user_layers.wkt,4326),\n" +
                        "st_transform(st_setsrid(study_area.geometry,?),4326)) \n");) {
            statement.setLong(1, studyArea);
            statement.setString(2, user_uuid);
            statement.setInt(3, Integer.parseInt(stProjection));
            statement.setInt(4, Integer.parseInt(stProjection));
            boolean status = statement.execute();
            if (status) {
                ResultSet data = statement.getResultSet();
                while (data.next()) {
                    Directories child = new Directories();
                    child.setData(data.getString("id"));
                    child.setLabel(data.getString("layer_name"));
                    child.setExpandedIcon(null);
                    child.setCollapsedIcon(null);
                    child.setType("layer");
                    children.add(child);
                }
                return children;
            }
            return children;
        } catch (SQLException e) {
            errorMsg = errorMsg + e.toString();
            log.error(e, errorMsg);
        }
        return children;
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
}

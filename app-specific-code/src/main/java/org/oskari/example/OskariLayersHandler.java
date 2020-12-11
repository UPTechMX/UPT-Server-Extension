package org.oskari.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
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

@OskariActionRoute("lst_oskari_lay")
public class OskariLayersHandler extends RestActionHandler {

    private static String upURL;
    private static String upUser;
    private static String upPassword;
    
    private static final Logger log = LogFactory.getLogger(OskariLayersHandler.class);
    String user_uuid;

    @Override
    public void preProcess(ActionParameters params) throws ActionException {
        // common method called for all request methods
        log.info(params.getUser(), "accessing route", getName());
        PropertyUtil.loadProperties("/oskari-ext.properties");
        upURL = PropertyUtil.get("up.db.URL");
        upUser = PropertyUtil.get("up.db.user");
        upPassword = PropertyUtil.get("up.db.password");

        user_uuid = params.getUser().getUuid();
    }

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        
        String errorMsg = "Layers UP get ";
        Data tree = new Data();
        ArrayList<Directories> directories = new ArrayList<Directories>();
        Long user_id = params.getUser().getId();
        try {
            params.requireLoggedInUser();
            ArrayList<String> roles = new UPTRoles().handleGet(params,params.getUser());
            if (!roles.contains("uptadmin") && !roles.contains("uptuser") ){
                throw new Exception("User privilege is not enough for this action");
            }
            
            //Get directories
            Directories dir = new Directories();
            dir.setData("my_data");
            dir.setLabel("My Data");
            dir.setIcon(null);
            directories.add(dir);

            //Get layers
            ArrayList<Directories> layers = getLayers();
            dir.setChildren(layers);

            JSONArray out = new JSONArray();
            for (Directories index : directories) {
                //Convert to Json Object
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

    private ArrayList<Directories> getLayers() {
        String errorMsg = "getLayers";
        ArrayList<Directories> children = new ArrayList<Directories>();
        try (
                Connection connection = DriverManager.getConnection(
                        upURL,
                        upUser,
                        upPassword);
                        PreparedStatement statement = connection.prepareStatement("select id,layer_name from user_layer where uuid=? and lower(layer_name) not like '%buffer%' and lower(layer_name) not like '%distance%'");) {
                            statement.setString(1, user_uuid);
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
}

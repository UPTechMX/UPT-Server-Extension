package org.oskari.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.example.st.STFiltersHandler;
import org.oskari.example.st.STSettings;
import org.oskari.example.st.STSettingsHandler;

@OskariActionRoute("share_layers")
public class UPTShareLayerHandler  extends RestActionHandler {
    private static String stURL;
    private static String stUser;
    private static String stPassword;
    private static final Logger log = LogFactory.getLogger(STSettingsHandler.class);
    private static String stProjection;
    
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
        stProjection = PropertyUtil.get("oskari.native.srs").substring(PropertyUtil.get("oskari.native.srs").indexOf(":") + 1);
        
        errors = new JSONArray();
        Obj = new ObjectMapper();
    }

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        String errorMsg = "share_layers get";
        try (
                Connection connection = DriverManager.getConnection(
                        stURL,
                        stUser,
                        stPassword);) {
            params.requireLoggedInUser();
            ArrayList<String> roles = new UPTRoles().handleGet(params,params.getUser());
            if (!roles.contains("uptadmin") && !roles.contains("uptuser") ){
                throw new Exception("User privilege is not enough for this action");
            }
            
            PreparedStatement statement = connection.prepareStatement("select user_layer.id ,layer_name,case when is_public is null then 0 else is_public end as is_public \n" +
                " from user_layer \n" +
                " left join upt_user_layer_scope on upt_user_layer_scope.user_layer_id=user_layer.id " +
                "where uuid=?");
            statement.setString(1, params.getUser().getUuid());
            
            errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("OK", "Executing query: " + statement.toString()))));
            
            ResultSet data = statement.executeQuery();
            ArrayList<UPTShareLayer> allLayers=new ArrayList<>();
            while (data.next()) {
                UPTShareLayer layer = new UPTShareLayer();
                layer.id = data.getLong("id");
                layer.name = data.getString("layer_name");;
                layer.is_public = data.getInt("is_public");
                allLayers.add(layer);
            };
            
            JSONArray out = new JSONArray();
            for (UPTShareLayer index : allLayers) {
                //Convert to Json Object
                ObjectMapper Obj = new ObjectMapper();
                final JSONObject json = JSONHelper.createJSONObject(Obj.writeValueAsString(index));
                out.put(json);
            }            
            ResponseHelper.writeResponse(params, out);
        } catch (Exception e) {
            try {
                errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
                ResponseHelper.writeError(params, "", 500, new JSONObject().put("Errors", errors));
            } catch (JsonProcessingException | JSONException ex) {
                java.util.logging.Logger.getLogger(STSettingsHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            errorMsg = errorMsg + e.toString();
            log.error(e, errorMsg);
        }
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        String errorMsg = "share_layers post";
        try (
                Connection connection = DriverManager.getConnection(
                        stURL,
                        stUser,
                        stPassword);) {
            params.requireLoggedInUser();
            ArrayList<String> roles = new UPTRoles().handleGet(params,params.getUser());
            if (!roles.contains("uptadmin") && !roles.contains("uptuser") ){
                throw new Exception("User privilege is not enough for this action");
            }
            
            PreparedStatement statement = connection.prepareStatement("insert into upt_user_layer_scope(user_layer_id,is_public) values (?,?) on conflict(user_layer_id) do update set is_public=?;");
            statement.setLong(1, Long.parseLong(params.getRequiredParam("id")));
            statement.setInt(2, Integer.parseInt(params.getRequiredParam("is_public")));
            statement.setInt(3, Integer.parseInt(params.getRequiredParam("is_public")));
            
            errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("OK", "Executing query: " + statement.toString()))));
            statement.execute();
            
            ResponseHelper.writeResponse(params, JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Success", "Layer shared"))));
        } catch (Exception e) {
            try {
                errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
                ResponseHelper.writeError(params, "", 500, new JSONObject().put("Errors", errors));
            } catch (JsonProcessingException | JSONException ex) {
                java.util.logging.Logger.getLogger(STSettingsHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            errorMsg = errorMsg + e.toString();
            log.error(e, errorMsg);
        }
    }

    @Override
    public void handlePut(ActionParameters params) throws ActionException {
        String errorMsg = "share_layers update";
        try (
                Connection connection = DriverManager.getConnection(
                        stURL,
                        stUser,
                        stPassword);) {
            params.requireLoggedInUser();
            ArrayList<String> roles = new UPTRoles().handleGet(params,params.getUser());
            if (!roles.contains("uptadmin") && !roles.contains("uptuser") ){
                throw new Exception("User privilege is not enough for this action");
            }
            
            PreparedStatement statement = connection.prepareStatement("update upt_user_layer_scope set is_public = ? where user_layer_id=?");
            statement.setInt(1, Integer.parseInt(params.getRequiredParam("is_public")));
            statement.setLong(2, Long.parseLong(params.getRequiredParam("id")));
            
            errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("OK", "Executing query: " + statement.toString()))));
            statement.execute();
            
            ResponseHelper.writeResponse(params, JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Success", "Layer share updated"))));
        } catch (Exception e) {
            try {
                errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
                ResponseHelper.writeError(params, "", 500, new JSONObject().put("Errors", errors));
            } catch (JsonProcessingException | JSONException ex) {
                java.util.logging.Logger.getLogger(STSettingsHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            errorMsg = errorMsg + e.toString();
            log.error(e, errorMsg);
        }
    }

    @Override
    public void handleDelete(ActionParameters params) throws ActionException {
        
    }
    
}

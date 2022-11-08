package org.oskari.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.logging.Level;
import org.json.JSONArray;
import org.oskari.example.st.STLayersHandler;

@OskariActionRoute("fix_data")
public class UPTDataCleanHandler extends RestActionHandler {
  private static String upURL;
  private static String upUser;
  private static String upPassword;

  private static String upwsHost;
  private static String upwsPort;
  private static String upProjection;
  private static final Logger log = LogFactory.getLogger(
      UPTDataCleanHandler.class);

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
    upProjection = PropertyUtil
        .get("oskari.native.srs")
        .substring(PropertyUtil.get("oskari.native.srs").indexOf(":") + 1);

    errors = new JSONArray();
    Obj = new ObjectMapper();
  }

  @Override
  public void handlePost(ActionParameters params) throws ActionException {
    params.requireLoggedInUser();
    throw new ActionException("This will be logged including stack trace");
  }

  @Override
  public void handleGet(ActionParameters params) throws ActionException {
    try (
        Connection connection = DriverManager.getConnection(
            upURL,
            upUser,
            upPassword);
        PreparedStatement statment = connection.prepareStatement(
            "with datafix as(\n" +
                "	select user_layer_data.id\n" +
                "	from user_layer_data \n" +
                "	where user_layer_data.user_layer_id in(\n" +
                "		select id from user_layer \n" +
                "	) and ST_NDims(geometry)>2\n" +
                ") update user_layer_data \n" +
                "set geometry = st_force2d(geometry) \n" +
                "from datafix\n" +
                "where user_layer_data.id = datafix.id");
        PreparedStatement statment2 = connection.prepareStatement(
            "with datafix as(\n" +
                "	select layer_name,user_layer_data.id,st_transform(st_setsrid(geometry,?),4326) \n" +
                "	from user_layer_data\n" +
                "	inner join user_layer on user_layer_data.user_layer_id = user_layer.id\n" +
                "	where user_layer_data.user_layer_id in(\n" +
                "		select id from user_layer\n" +
                "	) and st_srid(geometry)>0\n" +
                ")\n" +
                "update user_layer_data\n" +
                "set geometry=st_setsrid(user_layer_data.geometry,0)\n" +
                "from datafix\n" +
                "where user_layer_data.id=datafix.id");
        PreparedStatement statment3 = connection.prepareStatement(
            "with datafix as(\n" +
                "	select public_layer_data.id\n" +
                "	from public_layer_data \n" +
                "	where public_layer_data.public_layer_id in(\n" +
                "		select id from oskari_maplayer \n" +
                "	) and ST_NDims(geometry)>2\n" +
                ") update public_layer_data \n" +
                "set geometry = st_force2d(geometry) \n" +
                "from datafix\n" +
                "where public_layer_data.id = datafix.id");
        PreparedStatement statment4 = connection.prepareStatement(
            "with datafix as(\n" +
                "	select layer_name,public_layer_data.id,st_transform(st_setsrid(geometry,?),4326) \n" +
                "	from public_layer_data\n" +
                "	inner join oskari_maplayer on public_layer_data.public_layer_id = oskari_maplayer.id\n" +
                "	where public_layer_data.public_layer_id in(\n" +
                "		select id from oskari_maplayer\n" +
                "	) and st_srid(geometry)>0\n" +
                ")\n" +
                "update public_layer_data\n" +
                "set geometry=st_setsrid(public_layer_data.geometry,0)\n" +
                "from datafix\n" +
                "where public_layer_data.id=datafix.id");) {
      params.requireLoggedInUser();
      ArrayList<String> roles = new UPTRoles()
          .handleGet(params, params.getUser());
      if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
        throw new Exception("User privilege is not enough for this action");
      }

      statment.execute();
      statment2.setInt(1, Integer.parseInt(upProjection));
      statment2.execute();
      statment3.execute();
      statment4.setInt(1, Integer.parseInt(upProjection));
      statment4.execute();
    } catch (Exception e) {
      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.getMessage()))));
      } catch (JsonProcessingException ex) {
        java.util.logging.Logger.getLogger(STLayersHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
      log.error(e);
    }
  }

  @Override
  public void handlePut(ActionParameters params) throws ActionException {
    params.requireLoggedInUser();
    throw new ActionParamsException(
        "Notify there was something wrong with the params");
  }

  @Override
  public void handleDelete(ActionParameters params) throws ActionException {
    params.requireLoggedInUser();
    throw new ActionDeniedException("Not deleting anything");
  }
}

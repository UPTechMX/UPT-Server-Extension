package org.oskari.upt;

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
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONObject;
import org.oskari.upt.up.LayersUPHandler;

@OskariActionRoute("wfs_listing")
public class WfsNamesHandler extends RestActionHandler {
  private static String upURL;
  private static String upUser;
  private static String upPassword;

  private static final Logger log = LogFactory.getLogger(LayersUPHandler.class);

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
    String errorMsg = "getStudyAreas";
    ArrayList<StudyAreaUP> layers = new ArrayList<StudyAreaUP>();
    try (
        Connection connection = DriverManager.getConnection(
            upURL,
            upUser,
            upPassword);) {
      params.requireLoggedInUser();
      ArrayList<String> roles = new UPTRoles()
          .handleGet(params, params.getUser());
      if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
        throw new Exception("User privilege is not enough for this action");
      }

      PreparedStatement statement = connection.prepareStatement(
          "with public_layers as(\n" +
              "   SELECT distinct oskari_maplayer.id, name as layer_name FROM oskari_maplayer\n" +
              "   WHERE type = 'wfslayer'\n" +
              "   )\n" +
              "select id, layer_name from public_layers"
      // "select id,layer_name from user_layer where uuid=? and lower(layer_name) not
      // like '%buffer%' and lower(layer_name) not like '%distance%'"
      );
      statement.execute();
      ResultSet data = statement.getResultSet();
      while (data.next()) {
        StudyAreaUP child = new StudyAreaUP();
        child.setId("pub_" + data.getInt("id"));
        child.setName(data.getString("layer_name"));
        layers.add(child);
      }

      JSONArray out = new JSONArray();
      for (StudyAreaUP index : layers) {
        // Convert to Json Object
        ObjectMapper Obj = new ObjectMapper();
        final JSONObject json = JSONHelper.createJSONObject(
            Obj.writeValueAsString(index));
        out.put(json);
      }
      ResponseHelper.writeResponse(params, out);
    } catch (Exception e) {
      errorMsg = errorMsg + e.toString();
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
  }

  @Override
  public void handleDelete(ActionParameters params) throws ActionException {
    params.requireLoggedInUser();
  }
}

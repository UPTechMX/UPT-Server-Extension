package org.oskari.example.up;

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
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Level;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.example.PostStatus;
import org.oskari.example.Tables;
import org.oskari.example.UPTRoles;
import org.springframework.web.client.RestTemplate;

@OskariActionRoute("copy_public_data")
public class CopyPublicDataHandler extends RestActionHandler {
  private static String upURL;
  private static String upUser;
  private static String upPassword;

  private static String upwsHost;
  private static String upwsPort;
  private static String upProjection;

  private JSONArray errors;
  private ObjectMapper Obj;

  String user_uuid;

  private static final Logger log = LogFactory.getLogger(CopyDataHandler.class);

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
    upProjection =
      PropertyUtil
        .get("oskari.native.srs")
        .substring(PropertyUtil.get("oskari.native.srs").indexOf(":") + 1);

    user_uuid = params.getUser().getUuid();
    errors = new JSONArray();
    Obj = new ObjectMapper();
  }

  @Override
  public void handleGet(ActionParameters params) throws ActionException {
    params.requireLoggedInUser();
  }

  @Override
  public void handlePost(ActionParameters params) throws ActionException {
    String errorMsg = "Layers UP get ";
    try {
      params.requireLoggedInUser();
      ArrayList<String> roles = new UPTRoles()
      .handleGet(params, params.getUser());
      if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
        throw new Exception("User privilege is not enough for this action");
      }

      PostStatus status = null;

      switch (params.getRequiredParam("layerUPName")) {
        case "mmu":
          this.setMmu(
              params.getRequiredParam("layerUPName"),
              params.getRequiredParam("layerName"),
              params.getRequest().getParameterValues("tableUP"),
              params.getRequest().getParameterValues("table"),
              params.getRequiredParam("scenarioId")
            );
          break;
        case "mmu_info":
          this.setMmuInfo(
              params.getRequiredParam("layerUPName"),
              params.getRequiredParam("layerName"),
              params.getRequest().getParameterValues("tableUP"),
              params.getRequest().getParameterValues("table"),
              params.getRequiredParam("scenarioId")
            );
          break;
        case "transit":
          this.setTransit(
              params.getRequiredParam("layerUPName"),
              params.getRequiredParam("layerName"),
              params.getRequest().getParameterValues("tableUP"),
              params.getRequest().getParameterValues("table"),
              params.getRequiredParam("scenarioId")
            );
          break;
        case "transit_info":
          this.setTransitInfo(
              params.getRequiredParam("layerUPName"),
              params.getRequiredParam("layerName"),
              params.getRequest().getParameterValues("tableUP"),
              params.getRequest().getParameterValues("table"),
              params.getRequiredParam("scenarioId")
            );

          break;
        case "roads":
          this.setRoads(
              params.getRequiredParam("layerUPName"),
              params.getRequiredParam("layerName"),
              params.getRequest().getParameterValues("tableUP"),
              params.getRequest().getParameterValues("table"),
              params.getRequiredParam("scenarioId")
            );

          break;
        case "roads_info":
          this.setRoadsInfo(
              params.getRequiredParam("layerUPName"),
              params.getRequiredParam("layerName"),
              params.getRequest().getParameterValues("tableUP"),
              params.getRequest().getParameterValues("table"),
              params.getRequiredParam("scenarioId")
            );

          break;
        case "jobs":
          this.setJobs(
              params.getRequiredParam("layerUPName"),
              params.getRequiredParam("layerName"),
              params.getRequest().getParameterValues("tableUP"),
              params.getRequest().getParameterValues("table"),
              params.getRequiredParam("scenarioId")
            );

          break;
        case "jobs_info":
          this.setJobsInfo(
              params.getRequiredParam("layerUPName"),
              params.getRequiredParam("layerName"),
              params.getRequest().getParameterValues("tableUP"),
              params.getRequest().getParameterValues("table"),
              params.getRequiredParam("scenarioId")
            );

          break;
        case "footprint":
          this.setFootprint(
              params.getRequiredParam("layerUPName"),
              params.getRequiredParam("layerName"),
              params.getRequest().getParameterValues("tableUP"),
              params.getRequest().getParameterValues("table"),
              params.getRequiredParam("scenarioId")
            );

          break;
        case "risk":
          this.setRisk(
              params.getRequiredParam("layerUPName"),
              params.getRequiredParam("layerName"),
              params.getRequest().getParameterValues("tableUP"),
              params.getRequest().getParameterValues("table"),
              params.getRequiredParam("scenarioId")
            );

          break;
        case "risk_info":
          this.setRiskInfo(
              params.getRequiredParam("layerUPName"),
              params.getRequiredParam("layerName"),
              params.getRequest().getParameterValues("tableUP"),
              params.getRequest().getParameterValues("table"),
              params.getRequiredParam("scenarioId")
            );

          break;
        case "amenities":
          this.setAmenities(
              params.getRequiredParam("layerUPName"),
              params.getRequiredParam("layerName"),
              params.getRequest().getParameterValues("tableUP"),
              params.getRequest().getParameterValues("table"),
              params.getRequiredParam("scenarioId")
            );
          break;
        case "amenities_info":
          this.setAmenitiesInfo(
              params.getRequiredParam("layerUPName"),
              params.getRequiredParam("layerName"),
              params.getRequest().getParameterValues("tableUP"),
              params.getRequest().getParameterValues("table"),
              params.getRequiredParam("scenarioId")
            );
          break;
        default:
          break;
      }
      ResponseHelper.writeResponse(
        params,
        new JSONObject().put("Errors", errors)
      );
    } catch (Exception e) {
      errorMsg = errorMsg + e.getMessage();
      log.error(e, errorMsg);
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
          )
        );
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.getMessage()))
          )
        );
        ResponseHelper.writeError(
          params,
          "",
          500,
          new JSONObject().put("Errors", errors)
        );
      } catch (JsonProcessingException ex) {
        java
          .util.logging.Logger.getLogger(CopyDataHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java
          .util.logging.Logger.getLogger(CopyDataHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
    }
  }

  private void setAmenities(
    String layerUP,
    String layer,
    String[] tableup,
    String[] table,
    String scenarioId
  ) {
    String tableUP[] = new String[tableup.length + 2];
    System.arraycopy(tableup, 0, tableUP, 0, tableup.length);
    tableUP[tableup.length] = "scenario";
    tableUP[tableup.length + 1] = "oskari_code";

    String values = " ";
    for (int i = 0; i < tableUP.length; i++) {
      switch (tableUP[i]) {
        case "location":
          values +=
            " st_astext(st_transform(st_setsrid(" +
            table[i] +
            "," +
            upProjection +
            "),4326)) as " +
            tableUP[i];
          break;
        case "scenario":
          values += scenarioId + " as " + tableUP[i];
          break;
        case "oskari_code":
          values += " public_layer_data.id as " + tableUP[i];
          break;
        default:
          values +=
            " trim(both '\"' from CAST(property_json->'" +
            table[i] +
            "' AS text))  as " +
            tableUP[i];
          break;
      }
      if (i < tableUP.length - 1) {
        values += ",";
      } else {
        values = values.replaceAll(",$", "");
      }
    }
    String errorMsg = "";
    String query = "";
    try (
      Connection connection = DriverManager.getConnection(
        upURL,
        upUser,
        upPassword
      )
    ) {
      Statement statement = connection.createStatement();
      query =
        "select distinct " +
        values +
        " from oskari_maplayer\n" +
        " inner join public_layer_data on oskari_maplayer.id = public_layer_data.public_layer_id\n" +
        " where oskari_maplayer.id=" +
        layer +
        " and public_layer_data.uuid='" +
        user_uuid +
        "'";

      errors.put(
        JSONHelper.createJSONObject(
          Obj.writeValueAsString(
            new PostStatus(
              "OK",
              "Amenities executing query:" + statement.toString()
            )
          )
        )
      );
      ResultSet data = statement.executeQuery(query);

      ArrayList<UPAmenities> data_in = new ArrayList<>();

      while (data.next()) {
        Object o = new UPAmenities();
        Class<?> c = o.getClass();
        for (int i = 0; i < tableUP.length; i++) {
          Field f = c.getDeclaredField(tableUP[i]);
          f.setAccessible(true);
          if (
            !tableUP[i].equals("scenario") &&
            !tableUP[i].equals("amenities_id") &&
            !tableUP[i].equals("oskari_code")
          ) {
            f.set(o, data.getString(tableUP[i]));
          } else if (tableUP[i].equals("scenario")) {
            Integer val = (Integer) data.getInt(tableUP[i]);
            f.set(o, val);
          } else if (tableUP[i].equals("oskari_code")) {
            Long val = (Long) data.getLong(tableUP[i]);
            f.set(o, val);
          } else if (tableUP[i].equals("location")) {
            f.set(o, data.getString(tableUP[i]));
          } else if (tableUP[i].equals("amenities_id")) {}
        }
        data_in.add((UPAmenities) o);
      }
      Tables<UPAmenities> final_data = new Tables<UPAmenities>(data_in);

      RestTemplate restTemplate = new RestTemplate();
      restTemplate.postForObject(
        "http://" + upwsHost + ":" + upwsPort + "/amenities/",
        final_data,
        PostStatus.class
      );
    } catch (Exception e) {
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
          )
        );
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.getMessage()))
          )
        );
        ResponseHelper.writeError(
          null,
          "",
          500,
          new JSONObject().put("Errors", errors)
        );
      } catch (JsonProcessingException ex) {
        java
          .util.logging.Logger.getLogger(CopyDataHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java
          .util.logging.Logger.getLogger(CopyDataHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
      log.error(e, errorMsg + query);
    }
  }

  private void setAmenitiesInfo(
    String layerUP,
    String layer,
    String[] tableup,
    String[] table,
    String scenarioId
  )
    throws Exception {
    PostStatus postStatus = new PostStatus();
    postStatus.status = "OK";
    String values = "";

    String tableUP[] = new String[tableup.length + 3];
    System.arraycopy(tableup, 0, tableUP, 0, tableup.length);
    tableUP[tableup.length] = "scenario";
    tableUP[tableup.length + 1] = "oskari_code";
    tableUP[tableup.length + 2] = "name";

    for (int i = 0; i < tableUP.length; i++) {
      switch (tableUP[i]) {
        case "value":
          values += " '" + table[i] + "' as name, ";
          values +=
            " trim(both '\"' from CAST(property_json->'" +
            table[i] +
            "' AS text))::double precision as value";
          break;
        case "scenario":
          values += scenarioId + " as " + tableUP[i];
          break;
        case "oskari_code":
          values += "public_layer_data.id as " + tableUP[i];
          break;
        default:
          break;
      }
      if (i < tableUP.length - 1) {
        values += ",";
      } else {
        values = values.replaceAll(",$", "");
      }
    }

    String query = "";
    Connection connection = DriverManager.getConnection(
      upURL,
      upUser,
      upPassword
    );

    Statement statement = connection.createStatement();
    query =
      "select distinct " +
      values.replaceAll(",,", ",") +
      " from oskari_maplayer\n" +
      " inner join public_layer_data on oskari_maplayer.id = public_layer_data.public_layer_id\n" +
      " where oskari_maplayer.id=" +
      layer +
      " and public_layer_data.uuid='" +
      user_uuid +
      "'";

    errors.put(
      JSONHelper.createJSONObject(
        Obj.writeValueAsString(
          new PostStatus(
            "OK",
            "Amenities attribures executing query:" + statement.toString()
          )
        )
      )
    );

    ResultSet data = statement.executeQuery(query);
    ArrayList<UPAmenitiesInfo> data_in = new ArrayList<>();

    while (data.next()) {
      Object o = new UPAmenitiesInfo();
      Class<?> c = o.getClass();
      for (String tableUP1 : tableUP) {
        Field f = c.getDeclaredField(tableUP1);
        f.setAccessible(true);
        switch (tableUP1) {
          case "oskari_code":
            f.set(o, data.getLong(tableUP1));
            break;
          case "name":
            f.set(o, data.getString(tableUP1));
            break;
          case "scenario":
            f.set(o, data.getInt(tableUP1));
            break;
          case "value":
            f.set(o, data.getFloat(tableUP1));
            break;
          default:
            break;
        }
      }
      data_in.add((UPAmenitiesInfo) o);
    }
    Tables<UPAmenitiesInfo> final_data = new Tables<UPAmenitiesInfo>(data_in);

    RestTemplate restTemplate = new RestTemplate();
    restTemplate.postForObject(
      "http://" + upwsHost + ":" + upwsPort + "/amenities_info/",
      final_data,
      PostStatus.class
    );
  }

  private void setMmu(
    String layerUP,
    String layer,
    String[] tableup,
    String[] table,
    String scenarioId
  )
    throws Exception {
    PostStatus postStatus = new PostStatus();
    postStatus.status = "OK";
    String values = "";
    String tableUP[] = new String[tableup.length + 2];
    System.arraycopy(tableup, 0, tableUP, 0, tableup.length);
    tableUP[tableup.length] = "scenario";
    tableUP[tableup.length + 1] = "oskari_code";

    for (int i = 0; i < tableUP.length; i++) {
      switch (tableUP[i]) {
        case "location":
          values +=
            " st_astext(st_transform(st_setsrid(" +
            table[i] +
            "," +
            upProjection +
            "),4326)) as " +
            tableUP[i];
          break;
        case "scenario":
          values += scenarioId + " as " + tableUP[i];
          break;
        case "oskari_code":
          values += " public_layer_data.id as " + tableUP[i];
          break;
        default:
          values +=
            " trim(both '\"' from CAST(property_json->'" +
            table[i] +
            "' AS text))  as " +
            tableUP[i];
          break;
      }
      if (i < tableUP.length - 1) {
        values += ",";
      } else {
        values = values.replaceAll(",$", "");
      }
    }

    String query = "";

    Connection connection = DriverManager.getConnection(
      upURL,
      upUser,
      upPassword
    );

    Statement statement = connection.createStatement();
    query =
      "select distinct " +
      values.replaceAll(",,", ",") +
      " from oskari_maplayer\n" +
      " inner join public_layer_data on oskari_maplayer.id = public_layer_data.public_layer_id\n" +
      " where oskari_maplayer.id=" +
      layer +
      " and public_layer_data.uuid='" +
      user_uuid +
      "'";

    errors.put(
      JSONHelper.createJSONObject(
        Obj.writeValueAsString(
          new PostStatus(
            "OK",
            "Minimum mapping unit executing query:" + statement.toString()
          )
        )
      )
    );

    ResultSet data = statement.executeQuery(query);

    ArrayList<UPMmu> data_in = new ArrayList<>();

    while (data.next()) {
      Object o = new UPMmu();
      Class<?> c = o.getClass();
      for (int i = 0; i < tableUP.length; i++) {
        Field f = c.getDeclaredField(tableUP[i]);
        f.setAccessible(true);
        if (tableUP[i].equals("scenario")) {
          Integer val = (Integer) data.getInt(tableUP[i]);
          f.set(o, val);
        } else if (tableUP[i].equals("location")) {
          f.set(o, data.getString(tableUP[i]));
        } else if (tableUP[i].equals("oskari_code")) {
          f.set(o, data.getLong(tableUP[i]));
        } else {
          f.set(o, data.getString(tableUP[i]));
        }
      }
      data_in.add((UPMmu) o);
    }
    Tables<UPMmu> final_data = new Tables<UPMmu>(data_in);

    RestTemplate restTemplate = new RestTemplate();
    restTemplate.postForObject(
      "http://" + upwsHost + ":" + upwsPort + "/mmu/",
      final_data,
      PostStatus.class
    );
  }

  private void setMmuInfo(
    String layerUP,
    String layer,
    String[] tableup,
    String[] table,
    String scenarioId
  )
    throws Exception {
    String values = "";

    String tableUP[] = new String[tableup.length + 3];
    System.arraycopy(tableup, 0, tableUP, 0, tableup.length);
    tableUP[tableup.length] = "scenario";
    tableUP[tableup.length + 1] = "oskari_code";
    tableUP[tableup.length + 2] = "name";

    for (int i = 0; i < tableUP.length; i++) {
      switch (tableUP[i]) {
        case "value":
          values += " '" + table[i] + "' as name, ";
          values +=
            " trim(both '\"' from CAST(property_json->'" +
            table[i] +
            "' AS text))::double precision as value";
          break;
        case "oskari_code":
          values += "public_layer_data.id as " + tableUP[i];
          break;
        case "scenario":
          values += scenarioId + " as " + tableUP[i];
          break;
        default:
          break;
      }
      if (i < tableUP.length - 1) {
        values += ",";
      } else {
        values = values.replaceAll(",$", "");
      }
    }

    String query = "";

    Connection connection = DriverManager.getConnection(
      upURL,
      upUser,
      upPassword
    );

    Statement statement = connection.createStatement();
    query =
      "select distinct " +
      values.replaceAll(",,", ",") +
      " from oskari_maplayer\n" +
      " inner join public_layer_data on oskari_maplayer.id = public_layer_data.public_layer_id\n" +
      " where oskari_maplayer.id=" +
      layer +
      " and public_layer_data.uuid='" +
      user_uuid +
      "'";

    errors.put(
      JSONHelper.createJSONObject(
        Obj.writeValueAsString(
          new PostStatus(
            "OK",
            "Minimum mapping unit attributes executing query:" +
            statement.toString()
          )
        )
      )
    );

    ResultSet data = statement.executeQuery(query);
    ArrayList<UPMmuInfo> data_in = new ArrayList<>();

    while (data.next()) {
      Object o = new UPMmuInfo();
      Class<?> c = o.getClass();
      for (String tableUP1 : tableUP) {
        Field f = c.getDeclaredField(tableUP1);
        f.setAccessible(true);
        switch (tableUP1) {
          case "oskari_code":
            f.set(o, data.getLong(tableUP1));
            break;
          case "name":
            f.set(o, data.getString(tableUP1));
            break;
          case "scenario":
            f.set(o, data.getInt(tableUP1));
            break;
          case "value":
            f.set(o, data.getFloat(tableUP1));
            break;
          default:
            break;
        }
      }
      data_in.add((UPMmuInfo) o);
    }
    Tables<UPMmuInfo> final_data = new Tables<UPMmuInfo>(data_in);

    RestTemplate restTemplate = new RestTemplate();

    restTemplate.postForObject(
      "http://" + upwsHost + ":" + upwsPort + "/mmu_info/",
      final_data,
      PostStatus.class
    );
  }

  private void setTransit(
    String layerUP,
    String layer,
    String[] tableup,
    String[] table,
    String scenarioId
  )
    throws Exception {
    String values = "";
    String tableUP[] = new String[tableup.length + 2];
    System.arraycopy(tableup, 0, tableUP, 0, tableup.length);
    tableUP[tableup.length] = "scenario";
    tableUP[tableup.length + 1] = "oskari_code";

    for (int i = 0; i < tableUP.length; i++) {
      switch (tableUP[i]) {
        case "location":
          values +=
            "st_astext(st_transform(st_setsrid(" +
            table[i] +
            "," +
            upProjection +
            "),4326)) as " +
            tableUP[i];
          break;
        case "scenario":
          values += scenarioId + " as " + tableUP[i];
          break;
        case "oskari_code":
          values += " public_layer_data.id as " + tableUP[i];
          break;
        default:
          values +=
            " trim(both '\"' from CAST(property_json->'" +
            table[i] +
            "' AS text))  as " +
            tableUP[i];
          break;
      }
      if (i < tableUP.length - 1) {
        values += ",";
      } else {
        values = values.replaceAll(",$", "");
      }
    }

    String errorMsg = "";
    String query = "";
    Connection connection = DriverManager.getConnection(
      upURL,
      upUser,
      upPassword
    );

    Statement statement = connection.createStatement();
    query =
      "select distinct " +
      values +
      " from oskari_maplayer\n" +
      " inner join public_layer_data on oskari_maplayer.id = public_layer_data.public_layer_id\n" +
      " where oskari_maplayer.id=" +
      layer +
      " and public_layer_data.uuid='" +
      user_uuid +
      "'";

    errors.put(
      JSONHelper.createJSONObject(
        Obj.writeValueAsString(
          new PostStatus("OK", "Executing query:" + statement.toString())
        )
      )
    );

    ResultSet data = statement.executeQuery(query);

    ArrayList<UPTransit> data_in = new ArrayList<>();

    while (data.next()) {
      Object o = new UPTransit();
      Class<?> c = o.getClass();
      for (int i = 0; i < tableUP.length; i++) {
        Field f = c.getDeclaredField(tableUP[i]);
        f.setAccessible(true);
        if (
          !tableUP[i].equals("scenario") &&
          !tableUP[i].equals("transit_id") &&
          !tableUP[i].equals("oskari_code")
        ) {
          f.set(o, data.getString(tableUP[i]));
        } else if (tableUP[i].equals("scenario")) {
          Integer val = (Integer) data.getInt(tableUP[i]);
          f.set(o, val);
        } else if (tableUP[i].equals("oskari_code")) {
          Long val = (Long) data.getLong(tableUP[i]);
          f.set(o, val);
        } else if (tableUP[i].equals("location")) {
          f.set(o, data.getString(tableUP[i]));
        } else if (tableUP[i].equals("transit_id")) {}
      }
      data_in.add((UPTransit) o);
      //return postStatus;
    }
    Tables<UPTransit> final_data = new Tables<UPTransit>(data_in);

    RestTemplate restTemplate = new RestTemplate();
    restTemplate.postForObject(
      "http://" + upwsHost + ":" + upwsPort + "/transit/",
      final_data,
      PostStatus.class
    );
  }

  private void setTransitInfo(
    String layerUP,
    String layer,
    String[] tableup,
    String[] table,
    String scenarioId
  )
    throws Exception {
    String values = "";

    String tableUP[] = new String[tableup.length + 3];
    System.arraycopy(tableup, 0, tableUP, 0, tableup.length);
    tableUP[tableup.length] = "scenario";
    tableUP[tableup.length + 1] = "oskari_code";
    tableUP[tableup.length + 2] = "name";

    for (int i = 0; i < tableUP.length; i++) {
      switch (tableUP[i]) {
        case "value":
          values += " '" + table[i] + "' as name, ";
          values +=
            " trim(both '\"' from CAST(property_json->'" +
            table[i] +
            "' AS text))::double precision as value";
          break;
        case "scenario":
          values += scenarioId + " as " + tableUP[i];
          break;
        case "oskari_code":
          values += "public_layer_data.id as " + tableUP[i];
          break;
        default:
          break;
      }
      if (i < tableUP.length - 1) {
        values += ",";
      } else {
        values = values.replaceAll(",$", "");
      }
    }

    String query = "";
    Connection connection = DriverManager.getConnection(
      upURL,
      upUser,
      upPassword
    );

    Statement statement = connection.createStatement();
    query =
      "select distinct " +
      values.replaceAll(",,", ",") +
      " from oskari_maplayer\n" +
      " inner join public_layer_data on oskari_maplayer.id = public_layer_data.public_layer_id\n" +
      " where oskari_maplayer.id=" +
      layer +
      " and public_layer_data.uuid='" +
      user_uuid +
      "'";
    ResultSet data = statement.executeQuery(query);
    ArrayList<UPTransitInfo> data_in = new ArrayList<>();

    while (data.next()) {
      Object o = new UPTransitInfo();
      Class<?> c = o.getClass();
      for (String tableUP1 : tableUP) {
        Field f = c.getDeclaredField(tableUP1);
        f.setAccessible(true);
        switch (tableUP1) {
          case "oskari_code":
            f.set(o, data.getLong(tableUP1));
            break;
          case "name":
            f.set(o, data.getString(tableUP1));
            break;
          case "scenario":
            f.set(o, data.getInt(tableUP1));
            break;
          case "value":
            f.set(o, data.getFloat(tableUP1));
            break;
          default:
            break;
        }
      }
      data_in.add((UPTransitInfo) o);
    }
    Tables<UPTransitInfo> final_data = new Tables<UPTransitInfo>(data_in);

    RestTemplate restTemplate = new RestTemplate();
    restTemplate.postForObject(
      "http://" + upwsHost + ":" + upwsPort + "/transit_info/",
      final_data,
      PostStatus.class
    );
  }

  private void setRoads(
    String layerUP,
    String layer,
    String[] tableup,
    String[] table,
    String scenarioId
  )
    throws Exception {
    String values = "";
    String tableUP[] = new String[tableup.length + 2];
    System.arraycopy(tableup, 0, tableUP, 0, tableup.length);
    tableUP[tableup.length] = "scenario";
    tableUP[tableup.length + 1] = "oskari_code";

    for (int i = 0; i < tableUP.length; i++) {
      switch (tableUP[i]) {
        case "location":
          values +=
            "st_astext(st_transform(st_setsrid(" +
            table[i] +
            "," +
            upProjection +
            "),4326)) as " +
            tableUP[i];
          break;
        case "scenario":
          values += scenarioId + " as " + tableUP[i];
          break;
        case "oskari_code":
          values += " public_layer_data.id as " + tableUP[i];
          break;
        default:
          values +=
            " trim(both '\"' from CAST(property_json->'" +
            table[i] +
            "' AS text))  as " +
            tableUP[i];
          break;
      }
      if (i < tableUP.length - 1) {
        values += ",";
      } else {
        values = values.replaceAll(",$", "");
      }
    }

    String errorMsg = "";
    String query = "";

    Connection connection = DriverManager.getConnection(
      upURL,
      upUser,
      upPassword
    );

    Statement statement = connection.createStatement();
    query =
      "select distinct " +
      values +
      " from oskari_maplayer\n" +
      " inner join public_layer_data on oskari_maplayer.id = public_layer_data.public_layer_id\n" +
      " where oskari_maplayer.id=" +
      layer +
      " and public_layer_data.uuid='" +
      user_uuid +
      "'";

    errors.put(
      JSONHelper.createJSONObject(
        Obj.writeValueAsString(
          new PostStatus("OK", "Roads executing query:" + statement.toString())
        )
      )
    );

    ResultSet data = statement.executeQuery(query);

    ArrayList<UPRoads> data_in = new ArrayList<>();

    while (data.next()) {
      Object o = new UPRoads();
      Class<?> c = o.getClass();
      for (int i = 0; i < tableUP.length; i++) {
        Field f = c.getDeclaredField(tableUP[i]);
        f.setAccessible(true);
        if (
          !tableUP[i].equals("scenario") &&
          !tableUP[i].equals("roads_id") &&
          !tableUP[i].equals("oskari_code")
        ) {
          f.set(o, data.getString(tableUP[i]));
        } else if (tableUP[i].equals("scenario")) {
          Integer val = (Integer) data.getInt(tableUP[i]);
          f.set(o, val);
        } else if (tableUP[i].equals("oskari_code")) {
          Long val = (Long) data.getLong(tableUP[i]);
          f.set(o, val);
        } else if (tableUP[i].equals("location")) {
          f.set(o, data.getString(tableUP[i]));
        } else if (tableUP[i].equals("roads_id")) {}
      }
      data_in.add((UPRoads) o);
      //return postStatus;
    }
    Tables<UPRoads> final_data = new Tables<UPRoads>(data_in);

    RestTemplate restTemplate = new RestTemplate();
    restTemplate.postForObject(
      "http://" + upwsHost + ":" + upwsPort + "/roads/",
      final_data,
      PostStatus.class
    );
  }

  private void setRoadsInfo(
    String layerUP,
    String layer,
    String[] tableup,
    String[] table,
    String scenarioId
  )
    throws Exception {
    String values = "";

    String tableUP[] = new String[tableup.length + 3];
    System.arraycopy(tableup, 0, tableUP, 0, tableup.length);
    tableUP[tableup.length] = "scenario";
    tableUP[tableup.length + 1] = "oskari_code";
    tableUP[tableup.length + 2] = "name";

    for (int i = 0; i < tableUP.length; i++) {
      switch (tableUP[i]) {
        case "value":
          values += " '" + table[i] + "' as name, ";
          values +=
            " trim(both '\"' from CAST(property_json->'" +
            table[i] +
            "' AS text))::double precision as value";
          break;
        case "scenario":
          values += scenarioId + " as " + tableUP[i];
          break;
        case "oskari_code":
          values += "public_layer_data.id as " + tableUP[i];
          break;
        default:
          break;
      }
      if (i < tableUP.length - 1) {
        values += ",";
      } else {
        values = values.replaceAll(",$", "");
      }
    }

    String query = "";

    Connection connection = DriverManager.getConnection(
      upURL,
      upUser,
      upPassword
    );

    Statement statement = connection.createStatement();
    query =
      "select distinct " +
      values.replaceAll(",,", ",") +
      " from oskari_maplayer\n" +
      " inner join public_layer_data on oskari_maplayer.id = public_layer_data.public_layer_id\n" +
      " where oskari_maplayer.id=" +
      layer +
      " and public_layer_data.uuid='" +
      user_uuid +
      "'";

    errors.put(
      JSONHelper.createJSONObject(
        Obj.writeValueAsString(
          new PostStatus(
            "OK",
            "Roads attributes executing query:" + statement.toString()
          )
        )
      )
    );

    ResultSet data = statement.executeQuery(query);
    ArrayList<UPRoadsInfo> data_in = new ArrayList<>();

    while (data.next()) {
      Object o = new UPRoadsInfo();
      Class<?> c = o.getClass();
      for (String tableUP1 : tableUP) {
        Field f = c.getDeclaredField(tableUP1);
        f.setAccessible(true);
        switch (tableUP1) {
          case "oskari_code":
            f.set(o, data.getLong(tableUP1));
            break;
          case "name":
            f.set(o, data.getString(tableUP1));
            break;
          case "scenario":
            f.set(o, data.getInt(tableUP1));
            break;
          case "value":
            f.set(o, data.getFloat(tableUP1));
            break;
          default:
            break;
        }
      }
      data_in.add((UPRoadsInfo) o);
    }
    Tables<UPRoadsInfo> final_data = new Tables<UPRoadsInfo>(data_in);

    RestTemplate restTemplate = new RestTemplate();
    restTemplate.postForObject(
      "http://" + upwsHost + ":" + upwsPort + "/roads_info/",
      final_data,
      PostStatus.class
    );
  }

  private void setJobs(
    String layerUP,
    String layer,
    String[] tableup,
    String[] table,
    String scenarioId
  )
    throws Exception {
    String values = " ";
    String tableUP[] = new String[tableup.length + 2];
    System.arraycopy(tableup, 0, tableUP, 0, tableup.length);
    tableUP[tableup.length] = "scenario";
    tableUP[tableup.length + 1] = "oskari_code";

    for (int i = 0; i < tableUP.length; i++) {
      switch (tableUP[i]) {
        case "location":
          values +=
            "st_astext(st_transform(st_setsrid(" +
            table[i] +
            "," +
            upProjection +
            "),4326)) as " +
            tableUP[i];
          break;
        case "scenario":
          values += scenarioId + " as " + tableUP[i];
          break;
        case "oskari_code":
          values += " public_layer_data.id as " + tableUP[i];
          break;
        default:
          values +=
            " trim(both '\"' from CAST(property_json->'" +
            table[i] +
            "' AS text))  as " +
            tableUP[i];
          break;
      }
      if (i < tableUP.length - 1) {
        values += ",";
      } else {
        values = values.replaceAll(",$", "");
      }
    }

    String errorMsg = "";
    String query = "";

    Connection connection = DriverManager.getConnection(
      upURL,
      upUser,
      upPassword
    );

    Statement statement = connection.createStatement();
    query =
      "select distinct " +
      values +
      " from oskari_maplayer\n" +
      " inner join public_layer_data on oskari_maplayer.id = public_layer_data.public_layer_id\n" +
      " where oskari_maplayer.id=" +
      layer +
      " and public_layer_data.uuid='" +
      user_uuid +
      "'";
    ResultSet data = statement.executeQuery(query);

    ArrayList<UPJobs> data_in = new ArrayList<>();

    while (data.next()) {
      Object o = new UPJobs();
      Class<?> c = o.getClass();
      for (int i = 0; i < tableUP.length; i++) {
        Field f = c.getDeclaredField(tableUP[i]);
        f.setAccessible(true);
        if (
          !tableUP[i].equals("scenario") &&
          !tableUP[i].equals("jobs_id") &&
          !tableUP[i].equals("oskari_code")
        ) {
          f.set(o, data.getString(tableUP[i]));
        } else if (tableUP[i].equals("scenario")) {
          Integer val = (Integer) data.getInt(tableUP[i]);
          f.set(o, val);
        } else if (tableUP[i].equals("oskari_code")) {
          Long val = (Long) data.getLong(tableUP[i]);
          f.set(o, val);
        } else if (tableUP[i].equals("location")) {
          f.set(o, data.getString(tableUP[i]));
        } else if (tableUP[i].equals("jobs_id")) {}
      }

      data_in.add((UPJobs) o);
    }
    Tables<UPJobs> final_data = new Tables<UPJobs>(data_in);

    RestTemplate restTemplate = new RestTemplate();
    restTemplate.postForObject(
      "http://" + upwsHost + ":" + upwsPort + "/jobs/",
      final_data,
      PostStatus.class
    );
  }

  private void setJobsInfo(
    String layerUP,
    String layer,
    String[] tableup,
    String[] table,
    String scenarioId
  )
    throws Exception {
    String values = "";

    String tableUP[] = new String[tableup.length + 3];
    System.arraycopy(tableup, 0, tableUP, 0, tableup.length);
    tableUP[tableup.length] = "scenario";
    tableUP[tableup.length + 1] = "oskari_code";
    tableUP[tableup.length + 2] = "name";

    for (int i = 0; i < tableUP.length; i++) {
      switch (tableUP[i]) {
        case "value":
          values += " '" + table[i] + "' as name, ";
          values +=
            " trim(both '\"' from CAST(property_json->'" +
            table[i] +
            "' AS text))::double precision as value";
          break;
        case "scenario":
          values += scenarioId + " as " + tableUP[i];
          break;
        case "oskari_code":
          values += "public_layer_data.id as " + tableUP[i];
          break;
        default:
          break;
      }
      if (i < tableUP.length - 1) {
        values += ",";
      } else {
        values = values.replaceAll(",$", "");
      }
    }

    String query = "";
    Connection connection = DriverManager.getConnection(
      upURL,
      upUser,
      upPassword
    );

    Statement statement = connection.createStatement();
    query =
      "select distinct " +
      values.replaceAll(",,", ",") +
      " from oskari_maplayer\n" +
      " inner join public_layer_data on oskari_maplayer.id = public_layer_data.public_layer_id\n" +
      " where oskari_maplayer.id=" +
      layer +
      " and public_layer_data.uuid='" +
      user_uuid +
      "'";

    errors.put(
      JSONHelper.createJSONObject(
        Obj.writeValueAsString(
          new PostStatus(
            "OK",
            "Jobs attributes executing query:" + statement.toString()
          )
        )
      )
    );

    ResultSet data = statement.executeQuery(query);
    ArrayList<UPJobsInfo> data_in = new ArrayList<>();

    while (data.next()) {
      Object o = new UPJobsInfo();
      Class<?> c = o.getClass();
      for (String tableUP1 : tableUP) {
        Field f = c.getDeclaredField(tableUP1);
        f.setAccessible(true);
        switch (tableUP1) {
          case "oskari_code":
            f.set(o, data.getLong(tableUP1));
            break;
          case "name":
            f.set(o, data.getString(tableUP1));
            break;
          case "scenario":
            f.set(o, data.getInt(tableUP1));
            break;
          case "value":
            f.set(o, data.getFloat(tableUP1));
            break;
          default:
            break;
        }
      }
      data_in.add((UPJobsInfo) o);
    }
    Tables<UPJobsInfo> final_data = new Tables<UPJobsInfo>(data_in);

    RestTemplate restTemplate = new RestTemplate();
    restTemplate.postForObject(
      "http://" + upwsHost + ":" + upwsPort + "/jobs_info/",
      final_data,
      PostStatus.class
    );
  }

  private void setFootprint(
    String layerUP,
    String layer,
    String[] tableup,
    String[] table,
    String scenarioId
  )
    throws Exception {
    String values = "";
    String tableUP[] = new String[tableup.length + 1];
    tableUP[tableup.length] = "scenario";
    System.arraycopy(tableup, 0, tableUP, 0, tableup.length);

    String group = "";

    for (int i = 0; i < tableUP.length; i++) {
      switch (tableUP[i]) {
        case "name":
          values +=
            " trim(both '\"' from CAST(property_json->'" +
            table[i] +
            "' AS text)) as " +
            tableUP[i];
          group += "CAST(property_json->'" + table[i] + "' AS text)";
          break;
        case "value":
          values +=
            " trim(both '\"' from CAST(property_json->'" +
            table[i] +
            "' AS text)) as " +
            tableUP[i];
          break;
        case "location":
          //values += "st_astext("+table[i]+")";
          values +=
            " st_astext(st_union(st_transform(st_setsrid(" +
            table[i] +
            "," +
            upProjection +
            "),4326))) as " +
            tableUP[i];
          break;
        case "scenario":
          values += scenarioId + " as " + tableUP[i];
          break;
        default:
          values +=
            " trim(both '\"' from CAST(property_json->'" +
            table[i] +
            "' AS text))  as " +
            tableUP[i];
          break;
      }
      if (i < tableUP.length - 1) {
        values += ",";
      } else {
        values = values.replaceAll(",$", "");
      }
    }
    String errorMsg = "";
    String query = "";
    Connection connection = DriverManager.getConnection(
      upURL,
      upUser,
      upPassword
    );

    Statement statement = connection.createStatement();
    query =
      "select distinct " +
      values.replaceAll(",,", ",") +
      " from oskari_maplayer\n" +
      " inner join public_layer_data on oskari_maplayer.id = public_layer_data.public_layer_id\n" +
      " where oskari_maplayer.id=" +
      layer +
      " and public_layer_data.uuid='" +
      user_uuid +
      "'" +
      " group by " +
      group;

    System.out.println(query);
    errors.put(
      JSONHelper.createJSONObject(
        Obj.writeValueAsString(
          new PostStatus(
            "OK",
            "Footprint executing query:" + statement.toString()
          )
        )
      )
    );

    ResultSet data = statement.executeQuery(query);

    ArrayList<UPFootprint> data_in = new ArrayList<>();

    while (data.next()) {
      Object o = new UPFootprint();
      Class<?> c = o.getClass();
      for (int i = 0; i < tableUP.length; i++) {
        Field f = c.getDeclaredField(tableUP[i]);
        f.setAccessible(true);
        if (tableUP[i].equals("scenario")) {
          Integer val = (Integer) data.getInt(tableUP[i]);
          f.set(o, val);
        } else if (tableUP[i].equals("location")) {
          f.set(o, data.getString(tableUP[i]));
        } else if (tableUP[i].equals("footprint_id")) {} else if (
          tableUP[i].equals("name")
        ) {
          f.set(o, data.getString(tableUP[i]));
        } else if (tableUP[i].equals("value")) {
          f.set(o, data.getFloat(tableUP[i]));
        }
      }
      data_in.add((UPFootprint) o);
    }
    Tables<UPFootprint> final_data = new Tables<UPFootprint>(data_in);

    RestTemplate restTemplate = new RestTemplate();
    restTemplate.postForObject(
      "http://" + upwsHost + ":" + upwsPort + "/footprint/",
      final_data,
      PostStatus.class
    );
  }

  private void setRisk(
    String layerUP,
    String layer,
    String[] tableup,
    String[] table,
    String scenarioId
  )
    throws Exception {
    String values = "";
    String tableUP[] = new String[tableup.length + 2];
    tableUP[tableup.length] = "scenario";
    tableUP[tableup.length + 1] = "oskari_code";
    System.arraycopy(tableup, 0, tableUP, 0, tableup.length);
    for (int i = 0; i < tableUP.length; i++) {
      switch (tableUP[i]) {
        case "fclass":
          values +=
            " trim(both '\"' from CAST(property_json->'" +
            table[i] +
            "' AS text)) as " +
            tableUP[i];
          break;
        case "location":
          //values += "st_astext("+table[i]+")";
          values +=
            " st_astext(st_transform(st_setsrid(" +
            table[i] +
            "," +
            upProjection +
            "),4326)) as " +
            table[i];
          break;
        case "scenario":
          values += scenarioId + " as scenario";
          break;
        case "oskari_code":
          values += " public_layer_data.id as oskari_code";
          break;
        default:
          values +=
            " trim(both '\"' from CAST(property_json->'" +
            table[i] +
            "' AS text))  as " +
            table[i];
          break;
      }
      if (i < tableUP.length - 1) {
        values += ",";
      } else {
        values = values.replaceAll(",$", "");
      }
    }
    String errorMsg = "";
    String query = "";

    Connection connection = DriverManager.getConnection(
      upURL,
      upUser,
      upPassword
    );

    Statement statement = connection.createStatement();
    query =
      "select distinct " +
      values.replaceAll(",,", ",") +
      " from oskari_maplayer\n" +
      " inner join public_layer_data on oskari_maplayer.id = public_layer_data.public_layer_id\n" +
      " where oskari_maplayer.id=" +
      layer +
      " and public_layer_data.uuid='" +
      user_uuid +
      "'";

    errors.put(
      JSONHelper.createJSONObject(
        Obj.writeValueAsString(
          new PostStatus("OK", "Risk executing query:" + statement.toString())
        )
      )
    );

    ResultSet data = statement.executeQuery(query);

    ArrayList<UPRisk> data_in = new ArrayList<>();

    while (data.next()) {
      Object o = new UPRisk();
      Class<?> c = o.getClass();
      for (int i = 0; i < tableUP.length; i++) {
        Field f = c.getDeclaredField(tableUP[i]);
        f.setAccessible(true);
        if (tableUP[i].equals("scenario")) {
          Integer val = (Integer) data.getInt(tableUP[i]);
          f.set(o, val);
        } else if (tableUP[i].equals("oskari_code")) {
          Long val = (Long) data.getLong(tableUP[i]);
          f.set(o, val);
        } else if (tableUP[i].equals("location")) {
          f.set(o, data.getString(table[i]));
        } else if (tableUP[i].equals("risk_id")) {} else if (
          tableUP[i].equals("fclass")
        ) {
          f.set(o, data.getString(tableUP[i]));
        } else if (tableUP[i].equals("value")) {
          f.set(o, data.getFloat(tableUP[i]));
        } else {}
      }
      data_in.add((UPRisk) o);
    }
    Tables<UPRisk> final_data = new Tables<UPRisk>(data_in);

    RestTemplate restTemplate = new RestTemplate();
    restTemplate.postForObject(
      "http://" + upwsHost + ":" + upwsPort + "/risk/",
      final_data,
      PostStatus.class
    );
  }

  private void setRiskInfo(
    String layerUP,
    String layer,
    String[] tableup,
    String[] table,
    String scenarioId
  )
    throws Exception {
    String values = "";

    String tableUP[] = new String[tableup.length + 3];
    System.arraycopy(tableup, 0, tableUP, 0, tableup.length);
    tableUP[tableup.length] = "scenario";
    tableUP[tableup.length + 1] = "oskari_code";
    tableUP[tableup.length + 2] = "name";

    for (int i = 0; i < tableUP.length; i++) {
      switch (tableUP[i]) {
        case "value":
          values += " '" + table[i] + "' as name, ";
          values +=
            " trim(both '\"' from CAST(property_json->'" +
            table[i] +
            "' AS text))::double precision as value";
          break;
        case "scenario":
          values += scenarioId + " as " + tableUP[i];
          break;
        case "oskari_code":
          values += "public_layer_data.id as " + tableUP[i];
          break;
        default:
          break;
      }
      if (i < tableUP.length - 1) {
        values += ",";
      } else {
        values = values.replaceAll(",$", "");
      }
    }

    String query = "";

    Connection connection = DriverManager.getConnection(
      upURL,
      upUser,
      upPassword
    );

    Statement statement = connection.createStatement();
    query =
      "select distinct " +
      values.replaceAll(",,", ",") +
      " from oskari_maplayer\n" +
      " inner join public_layer_data on oskari_maplayer.id = public_layer_data.public_layer_id\n" +
      " where oskari_maplayer.id=" +
      layer +
      " and public_layer_data.uuid='" +
      user_uuid +
      "'";

    errors.put(
      JSONHelper.createJSONObject(
        Obj.writeValueAsString(
          new PostStatus(
            "OK",
            "Risk attributes executing query:" + statement.toString()
          )
        )
      )
    );

    ResultSet data = statement.executeQuery(query);
    ArrayList<UPRiskInfo> data_in = new ArrayList<>();

    while (data.next()) {
      Object o = new UPRiskInfo();
      Class<?> c = o.getClass();
      for (String tableUP1 : tableUP) {
        Field f = c.getDeclaredField(tableUP1);
        f.setAccessible(true);
        switch (tableUP1) {
          case "oskari_code":
            f.set(o, data.getLong(tableUP1));
            break;
          case "name":
            f.set(o, data.getString(tableUP1));
            break;
          case "scenario":
            f.set(o, data.getInt(tableUP1));
            break;
          case "value":
            f.set(o, data.getFloat(tableUP1));
            break;
          default:
            break;
        }
      }
      data_in.add((UPRiskInfo) o);
    }
    Tables<UPRiskInfo> final_data = new Tables<UPRiskInfo>(data_in);

    RestTemplate restTemplate = new RestTemplate();
    restTemplate.postForObject(
      "http://" + upwsHost + ":" + upwsPort + "/risk_info/",
      final_data,
      PostStatus.class
    );
  }
}

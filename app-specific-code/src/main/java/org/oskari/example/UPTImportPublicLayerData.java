package org.oskari.example;

import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Envelope;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.control.feature.AbstractWFSFeaturesHandler;
import fi.nls.oskari.control.feature.GetWFSFeaturesHandler;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.util.WFSDescribeFeatureHelper;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.*;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.example.st.LayersSTHandler;
import org.oskari.example.st.STLayersHandler;
import org.oskari.service.util.ServiceFactory;

@OskariActionRoute("UPTImportPublicLayerData")
public class UPTImportPublicLayerData extends RestActionHandler {
  private static String stURL;
  private static String stUser;
  private static String stPassword;
  private static String user_uuid;
  private static final Logger log = LogFactory.getLogger(LayersSTHandler.class);

  private JSONArray errors;
  private ObjectMapper Obj;
  private static GetWFSFeaturesHandlerTest testFeatures = new GetWFSFeaturesHandlerTest();

  @Override
  public void preProcess(ActionParameters params) throws ActionException {
    // common method called for all request methods
    log.info(params.getUser(), "accessing route", getName());
    PropertyUtil.loadProperties("/oskari-ext.properties");
    stURL = PropertyUtil.get("db.url");
    stUser = PropertyUtil.get("db.username");
    stPassword = PropertyUtil.get("db.password");

    user_uuid = params.getUser().getUuid();
    errors = new JSONArray();
    Obj = new ObjectMapper();
    testFeatures.init();
  }

  @Override
  public void handleGet(ActionParameters params) throws ActionException {
    String errorMsg = "Layers get";
    Long user_id = params.getUser().getId();
    user_uuid = params.getUser().getUuid();
    Long study_area;
    study_area = Long.parseLong("6");
    //study_area = Long.parseLong(params.getRequiredParam("study_area"));
    try {
      //ArrayList<STLayers> modules = new ArrayList<>();
      study_area = Long.parseLong("6");
      testFeatures.testGetFeatures(study_area, user_uuid, params);
    } catch (SQLException e) {
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
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
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);
    } catch (JsonProcessingException ex) {
      java
        .util.logging.Logger.getLogger(STLayersHandler.class.getName())
        .log(Level.SEVERE, null, ex);
    } catch (Exception e) {
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
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
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
    }
    /* try (
      Connection connection = DriverManager.getConnection(
        stURL,
        stUser,
        stPassword
      );
      PreparedStatement statement = connection.prepareStatement(
        "with study_area as(\n" +
        "    select geometry from user_layer_data where user_layer_id=?\n" +
        "), user_layers as(\n" +
        "    select distinct st_layers.id as id, st_layers.st_layer_label, st_layer_label as label ,st_layers.user_layer_id,layer_field,layer_mmu_code,is_public\n" +
        "    from st_layers\n" +
        "    inner join user_layer_data on user_layer_data.user_layer_id = st_layers.user_layer_id\n" +
        "    inner join user_layer on user_layer.id = user_layer_data.user_layer_id\n" +
        "    left join upt_user_layer_scope on upt_user_layer_scope.user_layer_id=user_layer.id\n" +
        "    , study_area\n" +
        "    where \n" +
        "    (user_layer.uuid=? or upt_user_layer_scope.is_public=1) and\n" +
        "    st_intersects(study_area.geometry,user_layer_data.geometry)\n" +
        ")\n" +
        "select id, st_layer_label, label,user_layer_id,layer_field,layer_mmu_code,is_public from user_layers"
      );
    ) {
      params.requireLoggedInUser();
      ArrayList<String> roles = new UPTRoles()
      .handleGet(params, params.getUser());
      if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
        throw new Exception("User privilege is not enough for this action");
      }

      statement.setLong(1, study_area);
      statement.setString(2, user_uuid);

      errors.put(
        JSONHelper.createJSONObject(
          Obj.writeValueAsString(
            new PostStatus("OK", "Executing query: " + statement.toString())
          )
        )
      );
      //statement.setLong(2, user_id);
      boolean status = statement.execute();
      if (status) {
        ResultSet data = statement.getResultSet();
        while (data.next()) {
          STLayers layer = new STLayers();
          layer.id = data.getLong("id");
          layer.label = data.getString("label");
          layer.st_layer_label = data.getString("st_layer_label");
          layer.user_layer_id = data.getLong("user_layer_id");
          layer.layer_field = data.getString("layer_field");
          layer.layer_mmu_code = data.getString("layer_mmu_code");
          modules.add(layer);
        }
      } else {
        STLayers layer = new STLayers();
        layer.id = -1L;
        layer.label = statement.toString();
        modules.add(layer);
      }

      JSONArray out = new JSONArray();
      for (STLayers index : modules) {
        //Convert to Json Object
        ObjectMapper Obj = new ObjectMapper();
        final JSONObject json = JSONHelper.createJSONObject(
          Obj.writeValueAsString(index)
        );
        out.put(json);
      }

      errors.put(
        JSONHelper.createJSONObject(
          Obj.writeValueAsString(new PostStatus("OK", "Layers executed"))
        )
      );

      ResponseHelper.writeResponse(params, out);
    } catch (SQLException e) {
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
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
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);
    } catch (JsonProcessingException ex) {
      java
        .util.logging.Logger.getLogger(STLayersHandler.class.getName())
        .log(Level.SEVERE, null, ex);
    } catch (Exception e) {
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
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
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
    } */
  }

  @Override
  public void handlePost(ActionParameters params) throws ActionException {
    String errorMsg = "Layers get";
    Long user_id = params.getUser().getId();
    user_uuid = params.getUser().getUuid();
    Long study_area;
    study_area = Long.parseLong("6");
    //study_area = Long.parseLong(params.getRequiredParam("study_area"));
    OskariLayer ml = LAYER_SERVICE.find(study_area.intValue());
    CoordinateReferenceSystem webMercator = CRS.decode("EPSG:3857", true);
    // PropertyUtil.addProperty("oskari.native.srs", "EPSG:" + stProjection, true);
    PropertyUtil.addProperty("oskari.native.srs", "EPSG:3857", true);
    Envelope envelope = new Envelope(
      -20016250.811,
      19934883.938,
      20097617.684,
      -19772150.192
    );
    ReferencedEnvelope bbox = new ReferencedEnvelope(envelope, webMercator);

    String layerUrl = ml.getUrl();
    String layerVersion = ml.getVersion();
    String layerTypename = ml.getName();

    String id = study_area.toString();
    OskariLayer layer = new OskariLayer();
    layer.setId(Integer.parseInt(id));
    layer.setType(OskariLayer.TYPE_WFS);
    layer.setUrl(layerUrl);
    layer.setName(layerTypename);

    SimpleFeatureCollection sfc = handler.featureClient.getFeatures(
      study_area.toString(),
      layer,
      bbox,
      webMercator,
      Optional.empty()
    );
    SimpleFeatureIterator iterator = sfc.features();
    try {
      while (iterator.hasNext()) {
        SimpleFeature feature = iterator.next();
        JSONArray names = new JSONArray();
        JSONArray attributes = new JSONArray(feature.getAttributes());
        JSONObject fullFeature = new JSONObject();
        List<AttributeDescriptor> list = feature
          .getType()
          .getAttributeDescriptors();
        Iterator<AttributeDescriptor> attrIterator = list.iterator();
        try {
          while (attrIterator.hasNext()) {
            AttributeDescriptor attr = attrIterator.next();
            names.put(attr.getLocalName());
          }
        } finally {}
        //attributes.put(attributes);
        System.out.println("ID: " + feature.getID());
        for (int i = 0; i < names.length(); i++) {
          fullFeature.put(
            names.get(i).toString(),
            attributes.get(i).toString()
          );
        }
        System.out.println("Full Feature: " + fullFeature);
        Iterator<String> featureKeys = fullFeature.keys();
        String geomKey = "";
        try {
          while (featureKeys.hasNext()) {
            String tmp = featureKeys.next();
            if (tmp.contains("geom")) {
              geomKey = tmp;
            }
          }
        } finally {
          System.out.println("NECESSARY KEY!!!!!!!!!! " + geomKey);
        }
        PostStatus status = new PostStatus();
        String query = "";
        try (
          Connection connection = DriverManager.getConnection(
            stURL,
            stUser,
            stPassword
          );
          PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO public.public_layer_data(public_layer_id, uuid, feature_id,property_json, geometry)VALUES ( ?, ?, ?,?,ST_GeomFromText(?));"
          );
        ) {
          params.requireLoggedInUser();
          ArrayList<String> roles = new UPTRoles()
          .handleGet(params, params.getUser());
          if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
            throw new Exception("User privilege is not enough for this action");
          }

          statement.setLong(1, study_area);
          statement.setString(2, uuid);
          statement.setString(3, feature.getID());
          statement.setString(4, fullFeature.toString());
          statement.setString(5, fullFeature.get(geomKey).toString());

          errors.put(
            JSONHelper.createJSONObject(
              Obj.writeValueAsString(
                new PostStatus("OK", "Executing query: " + statement.toString())
              )
            )
          );
          System.out.println("QUERY!!!!!" + statement.toString());
          status.message = statement.toString();
          //statement.execute();

          errors.put(
            JSONHelper.createJSONObject(
              Obj.writeValueAsString(new PostStatus("OK", "Layer registered"))
            )
          );
          ResponseHelper.writeResponse(
            params,
            new JSONObject().put("Errors", errors)
          );
        } catch (SQLException e) {
          log.error(e);
          try {
            errors.put(
              JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))
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
              .util.logging.Logger.getLogger(STLayersHandler.class.getName())
              .log(Level.SEVERE, null, ex);
          } catch (JSONException ex) {
            java
              .util.logging.Logger.getLogger(STLayersHandler.class.getName())
              .log(Level.SEVERE, null, ex);
          }
        } catch (JsonProcessingException ex) {
          java
            .util.logging.Logger.getLogger(STLayersHandler.class.getName())
            .log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
          java
            .util.logging.Logger.getLogger(STLayersHandler.class.getName())
            .log(Level.SEVERE, null, ex);
        } catch (Exception e) {
          try {
            errors.put(
              JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))
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
              .util.logging.Logger.getLogger(STLayersHandler.class.getName())
              .log(Level.SEVERE, null, ex);
          } catch (JSONException ex) {
            java
              .util.logging.Logger.getLogger(STLayersHandler.class.getName())
              .log(Level.SEVERE, null, ex);
          }
        }
      }
    } finally {
      iterator.close();
    }
    CoordinateReferenceSystem actualCRS = sfc
      .getSchema()
      .getGeometryDescriptor()
      .getCoordinateReferenceSystem();
    assertTrue(CRS.equalsIgnoreMetadata(webMercator, actualCRS));
  }

  @Override
  public void handlePut(ActionParameters params) throws ActionException {
    String errorMsg = "Layers get";
    Long user_id = params.getUser().getId();
    user_uuid = params.getUser().getUuid();
    Long study_area;
    study_area = Long.parseLong("6");
    //study_area = Long.parseLong(params.getRequiredParam("study_area"));
    try {
      //ArrayList<STLayers> modules = new ArrayList<>();
      study_area = Long.parseLong("6");
      testFeatures.testGetFeatures(study_area, user_uuid, params);
    } catch (SQLException e) {
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
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
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);
    } catch (JsonProcessingException ex) {
      java
        .util.logging.Logger.getLogger(STLayersHandler.class.getName())
        .log(Level.SEVERE, null, ex);
    } catch (Exception e) {
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
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
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
    }
  }

  @Override
  public void handleDelete(ActionParameters params) throws ActionException {
    String errorMsg = "Layers get";
    Long user_id = params.getUser().getId();
    user_uuid = params.getUser().getUuid();
    Long study_area;
    //study_area = Long.parseLong(params.getRequiredParam("study_area"));
    study_area = Long.parseLong("6");
    try {
      //ArrayList<STLayers> modules = new ArrayList<>();
      study_area = Long.parseLong("6");
      testFeatures.testGetFeatures(study_area, user_uuid, params);
    } catch (SQLException e) {
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
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
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);
    } catch (JsonProcessingException ex) {
      java
        .util.logging.Logger.getLogger(STLayersHandler.class.getName())
        .log(Level.SEVERE, null, ex);
    } catch (Exception e) {
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
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
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java
          .util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
      }
    }
  }
}

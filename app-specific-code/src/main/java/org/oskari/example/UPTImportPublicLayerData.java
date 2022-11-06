package org.oskari.example;

import static org.junit.Assert.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Envelope;
import org.geotools.geometry.jts.ReferencedEnvelope;
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
  private static OskariLayerService LAYER_SERVICE = ServiceFactory.getMapLayerService();
  private UPTGetWFSFeaturesHandler handler = new UPTGetWFSFeaturesHandler();

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
    handler.init();
  }

  @Override
  public void handleGet(ActionParameters params) throws ActionException {
    String errorMsg = "Layers get";
    Long user_id = params.getUser().getId();
    user_uuid = params.getUser().getUuid();
    Long study_area;
    study_area = Long.parseLong("6");
    // study_area = Long.parseLong(params.getRequiredParam("study_area"));
    try {
      // ArrayList<STLayers> modules = new ArrayList<>();
      study_area = Long.parseLong("6");
      testFeatures.testGetFeatures(study_area, user_uuid, params);
    } catch (SQLException e) {
      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        ResponseHelper.writeError(
            params,
            "",
            500,
            new JSONObject().put("Errors", errors));
      } catch (JsonProcessingException ex) {
        java.util.logging.Logger.getLogger(STLayersHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java.util.logging.Logger.getLogger(STLayersHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);
    } catch (JsonProcessingException ex) {
      java.util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
    } catch (Exception e) {
      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        ResponseHelper.writeError(
            params,
            "",
            500,
            new JSONObject().put("Errors", errors));
      } catch (JsonProcessingException ex) {
        java.util.logging.Logger.getLogger(STLayersHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java.util.logging.Logger.getLogger(STLayersHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
    }
  }

  @Override
  public void handlePost(ActionParameters params) throws ActionException {
    String errorMsg = "WFS get";
    PostStatus status = new PostStatus();
    String query = "";
    Long user_id = params.getUser().getId();
    String[] study_areas;
    study_areas = params.getRequest().getParameterValues("studyAreasId");
    try (
        Connection connection = DriverManager.getConnection(
            stURL,
            stUser,
            stPassword);) {
      params.requireLoggedInUser();
      ArrayList<String> roles = new UPTRoles()
          .handleGet(params, params.getUser());
      if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
        throw new Exception("User privilege is not enough for this action");
      }
      PreparedStatement statement = connection.prepareStatement(
          "INSERT INTO public.public_layer_data(public_layer_id, uuid, feature_id,property_json, geometry)VALUES ( ?, ?, ?,?::json,ST_GeomFromText(?));");
      connection.setAutoCommit(false);

      for (String ids : study_areas) {
        OskariLayer ml = LAYER_SERVICE.find(Integer.parseInt(ids));
        JSONArray featureArray = new JSONArray();
        CoordinateReferenceSystem webMercator = CRS.decode("EPSG:3857", true);
        // PropertyUtil.addProperty("oskari.native.srs", "EPSG:" + stProjection, true);
        PropertyUtil.addProperty("oskari.native.srs", "EPSG:3857", true);
        Envelope envelope = new Envelope(
            -20016250.811,
            19934883.938,
            20097617.684,
            -19772150.192);
        ReferencedEnvelope bbox = new ReferencedEnvelope(
            -20016250.811,
            19934883.938,
            20097617.684,
            -19772150.192,
            webMercator);

        SimpleFeatureCollection sfc = handler.featureClient.getFeatures(
            ids.toString(),
            ml,
            bbox,
            webMercator,
            Optional.empty());
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
            } finally {
            }
            // attributes.put(attributes);
            for (int i = 0; i < names.length(); i++) {
              fullFeature.put(
                  names.get(i).toString(),
                  attributes.get(i).toString());
            }
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
            }
            // featureArray.put(fullFeature);

            statement.setLong(1, Long.parseLong(ids));
            statement.setString(2, user_uuid);
            statement.setString(3, feature.getID());
            statement.setString(4, fullFeature.toString());
            statement.setString(5, fullFeature.get(geomKey).toString());
            statement.addBatch();

            CoordinateReferenceSystem actualCRS = sfc
                .getSchema()
                .getGeometryDescriptor()
                .getCoordinateReferenceSystem();
            assertTrue(CRS.equalsIgnoreMetadata(webMercator, actualCRS));
            errors.put(
                JSONHelper.createJSONObject(
                    Obj.writeValueAsString(
                        new PostStatus(
                            "OK",
                            "Executing query: " + statement.toString()))));
          }
        } finally {
          iterator.close();
        }
      }
      int[] inserted = statement.executeBatch();
      connection.commit();
      statement.close();
      errors.put(
          JSONHelper.createJSONObject(
              Obj.writeValueAsString(new PostStatus("OK", "WFS registered"))));
      ResponseHelper.writeResponse(
          params,
          new JSONObject().put("Errors", errors));
    } catch (SQLException e) {
      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        ResponseHelper.writeError(
            params,
            "",
            500,
            new JSONObject().put("Errors", errors));
      } catch (JsonProcessingException ex) {
        java.util.logging.Logger.getLogger(STLayersHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java.util.logging.Logger.getLogger(STLayersHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);
    } catch (JsonProcessingException ex) {
      java.util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
    } catch (Exception e) {
      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        ResponseHelper.writeError(
            params,
            "",
            500,
            new JSONObject().put("Errors", errors));
      } catch (JsonProcessingException ex) {
        java.util.logging.Logger.getLogger(STLayersHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java.util.logging.Logger.getLogger(STLayersHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);
    }
  }

  @Override
  public void handlePut(ActionParameters params) throws ActionException {
    String errorMsg = "Layers get";
    Long user_id = params.getUser().getId();
    user_uuid = params.getUser().getUuid();
    Long study_area;
    study_area = Long.parseLong("6");
    // study_area = Long.parseLong(params.getRequiredParam("study_area"));
    try {
      // ArrayList<STLayers> modules = new ArrayList<>();
      study_area = Long.parseLong("6");
      testFeatures.testGetFeatures(study_area, user_uuid, params);
    } catch (SQLException e) {
      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        ResponseHelper.writeError(
            params,
            "",
            500,
            new JSONObject().put("Errors", errors));
      } catch (JsonProcessingException ex) {
        java.util.logging.Logger.getLogger(STLayersHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java.util.logging.Logger.getLogger(STLayersHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);
    } catch (JsonProcessingException ex) {
      java.util.logging.Logger.getLogger(STLayersHandler.class.getName())
          .log(Level.SEVERE, null, ex);
    } catch (Exception e) {
      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        ResponseHelper.writeError(
            params,
            "",
            500,
            new JSONObject().put("Errors", errors));
      } catch (JsonProcessingException ex) {
        java.util.logging.Logger.getLogger(STLayersHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java.util.logging.Logger.getLogger(STLayersHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
    }
  }

  @Override
  public void handleDelete(ActionParameters params) throws ActionException {
    String errorMsg = "Layers get";
    user_uuid = params.getUser().getUuid();
    String[] study_areas;
    study_areas = params.getRequest().getParameterValues("studyAreasId");
    PostStatus status = new PostStatus();
    String query = "";
    try (
        Connection connection = DriverManager.getConnection(
            stURL,
            stUser,
            stPassword);
        PreparedStatement statement = connection.prepareStatement(
            "delete from public.public_layer_data where public_layer_id = ? and uuid = ?;");) {
      params.requireLoggedInUser();
      ArrayList<String> roles = new UPTRoles()
          .handleGet(params, params.getUser());
      if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
        throw new Exception("User privilege is not enough for this action");
      }
      connection.setAutoCommit(false);

      for (String ids : study_areas) {
        statement.setLong(1, Long.parseLong(ids));
        statement.setString(2, user_uuid);
        statement.addBatch();
      }

      errors.put(
          JSONHelper.createJSONObject(
              Obj.writeValueAsString(
                  new PostStatus("OK", "Executing query: " + statement.toString()))));

      int[] inserted = statement.executeBatch();
      connection.commit();
      statement.close();

      errors.put(
          JSONHelper.createJSONObject(
              Obj.writeValueAsString(new PostStatus("OK", "Filter delete"))));
      ResponseHelper.writeResponse(
          params,
          new JSONObject().put("Errors", errors));
    } catch (Exception e) {
      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        ResponseHelper.writeError(
            params,
            "",
            500,
            new JSONObject().put("Errors", errors));
      } catch (JsonProcessingException ex) {
        java.util.logging.Logger.getLogger(STLayersHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java.util.logging.Logger.getLogger(STLayersHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
    }
  }

  public void getFeatures(Long study_area) {
    try {
      OskariLayer ml = LAYER_SERVICE.find(study_area.intValue());
      JSONArray featureArray = new JSONArray();
      CoordinateReferenceSystem webMercator = CRS.decode("EPSG:3857", true);
      // PropertyUtil.addProperty("oskari.native.srs", "EPSG:" + stProjection, true);
      PropertyUtil.addProperty("oskari.native.srs", "EPSG:3857", true);
      Envelope envelope = new Envelope(
          -20016250.811,
          19934883.938,
          20097617.684,
          -19772150.192);
      ReferencedEnvelope bbox = new ReferencedEnvelope(
          -20016250.811,
          19934883.938,
          20097617.684,
          -19772150.192,
          webMercator);

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
          Optional.empty());
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
          } finally {
          }
          for (int i = 0; i < names.length(); i++) {
            fullFeature.put(
                names.get(i).toString(),
                attributes.get(i).toString());
          }
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
          }
          featureArray.put(fullFeature);
        }
      } finally {
        iterator.close();
      }
      CoordinateReferenceSystem actualCRS = sfc
          .getSchema()
          .getGeometryDescriptor()
          .getCoordinateReferenceSystem();
      assertTrue(CRS.equalsIgnoreMetadata(webMercator, actualCRS));
    } catch (Exception e) {
    }
  }
}

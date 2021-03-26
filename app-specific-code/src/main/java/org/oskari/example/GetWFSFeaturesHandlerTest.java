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

public class GetWFSFeaturesHandlerTest extends RestActionHandler {
  private static String stURL;
  private static String stUser;
  private static String stPassword;
  private static final Logger log = LogFactory.getLogger(LayersSTHandler.class);
  private UPTGetWFSFeaturesHandler handler;
  private static String stProjection;
  private static OskariLayerService LAYER_SERVICE = ServiceFactory.getMapLayerService();
  private JSONArray errors;
  private ObjectMapper Obj;

  /* @Override
  public void preProcess(ActionParameters params) throws ActionException {
    // common method called for all request methods
    log.info(params.getUser(), "accessing route", getName());
    PropertyUtil.loadProperties("/oskari-ext.properties");
    stURL = PropertyUtil.get("db.url");
    stUser = PropertyUtil.get("db.username");
    stPassword = PropertyUtil.get("db.password");

    //user_uuid = params.getUser().getUuid();
    errors = new JSONArray();
    Obj = new ObjectMapper();
  } */

  @Before
  public void init(ActionParameters params) {
    log.info(params.getUser(), "accessing route", getName());
    PropertyUtil.loadProperties("/oskari-ext.properties");
    stURL = PropertyUtil.get("db.url");
    stUser = PropertyUtil.get("db.username");
    stPassword = PropertyUtil.get("db.password");

    //user_uuid = params.getUser().getUuid();
    errors = new JSONArray();
    Obj = new ObjectMapper();
    handler = new UPTGetWFSFeaturesHandler();
    handler.init();
    stProjection =
      PropertyUtil
        .get("oskari.native.srs")
        .substring(PropertyUtil.get("oskari.native.srs").indexOf(":") + 1);
  }

  @Test
  @Ignore("Depends on an outside resource")
  public void testGetFeatures(
    Long studyArea,
    String uuid,
    ActionParameters params
  )
    throws Exception {
    String errorMsg = "Layers get";
    OskariLayer ml = LAYER_SERVICE.find(studyArea.intValue());
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

    String id = studyArea.toString();
    OskariLayer layer = new OskariLayer();
    layer.setId(Integer.parseInt(id));
    layer.setType(OskariLayer.TYPE_WFS);
    layer.setUrl(layerUrl);
    layer.setName(layerTypename);

    SimpleFeatureCollection sfc = handler.featureClient.getFeatures(
      studyArea.toString(),
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
        for (int i = 0; i < names.length(); i++) {
          fullFeature.put(
            names.get(i).toString(),
            attributes.get(i).toString()
          );
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
        } finally {}
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

          statement.setLong(1, studyArea);
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
          status.message = statement.toString();

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
}

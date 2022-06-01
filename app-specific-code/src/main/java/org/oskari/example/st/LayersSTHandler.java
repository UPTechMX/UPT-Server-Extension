package org.oskari.example.st;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Envelope;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.control.feature.GetWFSFeaturesHandler;
import fi.nls.oskari.control.layer.GetWFSDescribeFeatureHandler;
import fi.nls.oskari.db.DatasourceHelper;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.map.layer.OskariLayerService;
import fi.nls.oskari.map.layer.OskariLayerServiceMybatisImpl;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.util.WFSDescribeFeatureHelper;
import java.lang.reflect.Field;
import java.sql.Array;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.logging.Level;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.example.Amenities;
import org.oskari.example.Data;
import org.oskari.example.Directories;
import org.oskari.example.GetWFSFeaturesHandlerTest;
import org.oskari.example.Layers;
import org.oskari.example.MmuUP;
import org.oskari.example.PostStatus;
import org.oskari.example.StudyAreaUP;
import org.oskari.example.Tables;
import org.oskari.example.UPTRoles;
import org.oskari.example.up.UPFields;
import org.oskari.example.up.UPFieldsList;
import org.oskari.service.util.ServiceFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@OskariActionRoute("LayersSTHandler")
public class LayersSTHandler extends RestActionHandler {
  private static String stURL;
  private static String stUser;
  private static String stPassword;

  private static String stwsHost;
  private static String stwsPort;
  private static String stProjection;

  private JSONArray errors;
  private ObjectMapper Obj;

  Map<Integer, STSettings> stLayers;
  String user_uuid;

  private static final Logger log = LogFactory.getLogger(LayersSTHandler.class);

  private static OskariLayerService LAYER_SERVICE = ServiceFactory.getMapLayerService();
  private static GetWFSDescribeFeatureHandler describeFeature = new GetWFSDescribeFeatureHandler();
  private static GetWFSFeaturesHandler featuresList = new GetWFSFeaturesHandler();
  private static GetWFSFeaturesHandlerTest testFeatures = new GetWFSFeaturesHandlerTest();

  public LayersSTHandler() {
    this.stLayers = new TreeMap<>();
  }

  @Override
  public void preProcess(ActionParameters params) throws ActionException {
    // common method called for all request methods
    log.info(params.getUser(), "accessing route", getName());
    user_uuid = params.getUser().getUuid();
    PropertyUtil.loadProperties("/oskari-ext.properties");
    stURL = PropertyUtil.get("db.url");
    stUser = PropertyUtil.get("db.username");
    stPassword = PropertyUtil.get("db.password");

    stwsHost = PropertyUtil.get("stws.db.host");
    stwsPort = PropertyUtil.get("stws.db.port");
    stProjection = PropertyUtil
        .get("oskari.native.srs")
        .substring(PropertyUtil.get("oskari.native.srs").indexOf(":") + 1);
    errors = new JSONArray();
    Obj = new ObjectMapper();
    describeFeature.init();
    featuresList.init();
    testFeatures.init();
  }

  @Override
  public void handleGet(ActionParameters params) throws ActionException {
    String errorMsg = "Layers ST get ";
    Data tree = new Data();
    ArrayList<Directories> directories = new ArrayList<Directories>();
    Long user_id = params.getUser().getId();
    try {
      params.requireLoggedInUser();
      ArrayList<String> roles = new UPTRoles()
          .handleGet(params, params.getUser());
      if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
        throw new Exception("User privilege is not enough for this action");
      }
      if ("list_directories".equals(params.getRequiredParam("action"))) {
        // Get directories
        Directories dir = new Directories();
        dir.setData("my_data");
        dir.setLabel("My Data");
        dir.setIcon(null);
        directories.add(dir);

        Directories pdir = new Directories();
        pdir.setData("public_data");
        pdir.setLabel("Public data");
        pdir.setIcon(null);
        directories.add(pdir);

        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("OK", "Getting directories"))));
        // Get layers
        ArrayList<Directories> layers = getLayers();
        dir.setChildren(layers);

        ArrayList<Directories> public_layers = getPublicLayers();
        pdir.setChildren(public_layers);

        JSONArray out = new JSONArray();
        for (Directories index : directories) {
          // Convert to Json Object

          final JSONObject json = JSONHelper.createJSONObject(
              Obj.writeValueAsString(index));
          out.put(json);
        }

        tree.setData(directories);
        final JSONObject outs = JSONHelper.createJSONObject(
            Obj.writeValueAsString(tree));

        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(
                    new PostStatus("OK", "Listing directories executed"))));

        ResponseHelper.writeResponse(params, outs);
      } else if ("list_layers".equals(params.getRequiredParam("action"))) {
        // Get directories
        Directories dir = new Directories();
        dir.setData("my_data");
        dir.setLabel("My Data");
        dir.setIcon(null);
        directories.add(dir);

        Directories pdir = new Directories();
        pdir.setData("public_data");
        pdir.setLabel("Public data");
        pdir.setIcon(null);
        directories.add(pdir);

        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(
                    new PostStatus("OK", "Getting Oskari layers "))));
        // Get layers
        ArrayList<Directories> layers = getLayers();
        dir.setChildren(layers);

        ArrayList<Directories> public_layers = getPublicLayers();
        pdir.setChildren(public_layers);

        JSONArray out = new JSONArray();
        for (Directories index : directories) {
          // Convert to Json Object

          final JSONObject json = JSONHelper.createJSONObject(
              Obj.writeValueAsString(index));
          out.put(json);
        }

        tree.setData(directories);
        final JSONObject outs = JSONHelper.createJSONObject(
            Obj.writeValueAsString(tree));

        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(
                    new PostStatus("OK", "Listing oskari layers executed"))));

        ResponseHelper.writeResponse(params, outs);
      } else if ("list_st_layers".equals(params.getRequiredParam("action"))) {
        ArrayList<STSettings> settingsList = new ArrayList<>();

        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(
                    new PostStatus("OK", "Getting suitability layers"))));
        // Get layers
        ArrayList<STLayers> layers = getSTLayers(
            user_id,
            Long.parseLong(params.getRequiredParam("study_area")));
        for (STLayers index : layers) {
          settingsList.addAll(getSettings(user_id, index.id));
        }

        JSONArray out = new JSONArray();
        for (STSettings index : settingsList) {
          // Convert to Json Object
          final JSONObject json = JSONHelper.createJSONObject(
              Obj.writeValueAsString(index));
          out.put(json);
        }

        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(
                    new PostStatus("OK", "Listing suitability layers executed"))));

        ResponseHelper.writeResponse(params, out);
      } else if ("list_st_public_layers".equals(params.getRequiredParam("action"))) {
        ArrayList<STPublicSettings> settingsList = new ArrayList<>();

        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(
                    new PostStatus("OK", "Getting suitability layers"))));
        // Get layers
        ArrayList<STPublicLayers> layers = getSTPublicLayers(
            user_id,
            Long.parseLong(params.getRequiredParam("study_area")));
        for (STPublicLayers index : layers) {
          settingsList.addAll(getPublicSettings(user_id, index.id));
        }

        JSONArray out = new JSONArray();
        for (STPublicSettings index : settingsList) {
          // Convert to Json Object
          final JSONObject json = JSONHelper.createJSONObject(
              Obj.writeValueAsString(index));
          out.put(json);
        }

        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(
                    new PostStatus("OK", "Listing suitability layers executed"))));

        ResponseHelper.writeResponse(params, out);
      } else if ("list_layers".equals(params.getRequiredParam("action"))) {
        // Get directories
        Directories dir = new Directories();
        dir.setData("my_data");
        dir.setLabel("My Data");
        dir.setIcon(null);
        directories.add(dir);

        Directories pdir = new Directories();
        pdir.setData("public_data");
        pdir.setLabel("Public data");
        pdir.setIcon(null);
        directories.add(pdir);

        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(
                    new PostStatus("OK", "Getting Oskari layers "))));
        // Get layers
        ArrayList<Directories> layers = getLayers();
        dir.setChildren(layers);

        ArrayList<Directories> public_layers = getPublicLayers();
        pdir.setChildren(public_layers);

        JSONArray out = new JSONArray();
        for (Directories index : directories) {
          // Convert to Json Object

          final JSONObject json = JSONHelper.createJSONObject(
              Obj.writeValueAsString(index));
          out.put(json);
        }

        tree.setData(directories);
        final JSONObject outs = JSONHelper.createJSONObject(
            Obj.writeValueAsString(tree));

        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(
                    new PostStatus("OK", "Listing oskari layers executed"))));

        ResponseHelper.writeResponse(params, outs);
      } else if ("list_st_layers_pub_std_area".equals(params.getRequiredParam("action"))) {
        ArrayList<STSettings> settingsList = new ArrayList<>();

        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(
                    new PostStatus("OK", "Getting suitability layers"))));
        // Get layers
        ArrayList<STLayers> layers = getSTLayersPubStdArea(
            user_id,
            Long.parseLong(params.getRequiredParam("study_area")));
        for (STLayers index : layers) {
          settingsList.addAll(getSettings(user_id, index.id));
        }

        JSONArray out = new JSONArray();
        for (STSettings index : settingsList) {
          // Convert to Json Object
          final JSONObject json = JSONHelper.createJSONObject(
              Obj.writeValueAsString(index));
          out.put(json);
        }

        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(
                    new PostStatus("OK", "Listing suitability layers executed"))));

        ResponseHelper.writeResponse(params, out);
      } else if ("list_st_public_layers_pub_std_area".equals(
          params.getRequiredParam("action"))) {
        ArrayList<STPublicSettings> settingsList = new ArrayList<>();

        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(
                    new PostStatus("OK", "Getting suitability layers"))));
        // Get layers
        ArrayList<STPublicLayers> layers = getSTPublicLayersPubStdArea(
            user_id,
            Long.parseLong(params.getRequiredParam("study_area")));
        for (STPublicLayers index : layers) {
          settingsList.addAll(getPublicSettings(user_id, index.id));
        }

        JSONArray out = new JSONArray();
        for (STPublicSettings index : settingsList) {
          // Convert to Json Object
          final JSONObject json = JSONHelper.createJSONObject(
              Obj.writeValueAsString(index));
          out.put(json);
        }

        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(
                    new PostStatus("OK", "Listing suitability layers executed"))));

        ResponseHelper.writeResponse(params, out);
      } else if ("list_st_filters".equals(params.getRequiredParam("action"))) {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(
                    new PostStatus("OK", "Getting suitability filters"))));
        // Get layers
        ArrayList<STLayers> layers = getSTFilters(
            user_id,
            new Long(params.getRequiredParam("study_area")));

        JSONArray out = new JSONArray();
        for (STLayers index : layers) {
          // Convert to Json Object

          final JSONObject json = JSONHelper.createJSONObject(
              Obj.writeValueAsString(index));
          out.put(json);
        }

        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(
                    new PostStatus("OK", "Getting suitability filters executed"))));

        ResponseHelper.writeResponse(params, out);
      } else if ("list_columns".equals(params.getRequiredParam("action"))) {
        Layers layers = new Layers();

        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(
                    new PostStatus("OK", "Getting oskari columns"))));

        layers.setColumns(getColumns(params.getRequiredParam("layer_id")));

        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(
                    new PostStatus("OK", "Getting oskari executed"))));

        // final JSONObject json =
        // JSONHelper.createJSONObject(Obj.writeValueAsString(layers));
        final JSONObject json = JSONHelper.createJSONObject(
            Obj.writeValueAsString(layers));
        ResponseHelper.writeResponse(params, json);
      } else if ("list_public_columns".equals(params.getRequiredParam("action"))) {
        Layers layers = new Layers();
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(
                    new PostStatus("OK", "Getting oskari columns"))));
        // testFeatures.testGetExternalFeatures();
        OskariLayer ml = LAYER_SERVICE.find(
            Integer.parseInt(params.getRequiredParam("layer_id")));

        JSONObject mapFields = WFSDescribeFeatureHelper.getFeatureTypesTextOrNumeric(
            ml,
            params.getRequiredParam("layer_id"));
        JSONObject propertyTypes = mapFields.getJSONObject("propertyTypes");
        JSONArray ptArray = propertyTypes.names();
        ArrayList<String> pt = new ArrayList<String>();
        if (ptArray != null) {
          for (int i = 0; i < ptArray.length(); i++) {
            pt.add(ptArray.getString(i));
          }
        }
        pt.removeIf(s -> s.contains("the_geom"));
        pt.add("geometry");

        layers.setColumns(pt);

        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(
                    new PostStatus("OK", "Getting oskari executed"))));

        // final JSONObject json =
        // JSONHelper.createJSONObject(Obj.writeValueAsString(layers));
        final JSONObject json = JSONHelper.createJSONObject(
            Obj.writeValueAsString(layers));
        ResponseHelper.writeResponse(params, json);
      } else if ("list_st_columns".equals(params.getRequiredParam("action"))) {
        STFieldsList layers = null;
        if ("layers".equals(params.getRequiredParam("table"))) {
          errors.put(
              JSONHelper.createJSONObject(
                  Obj.writeValueAsString(
                      new PostStatus("OK", "Getting suitability layres columns"))));
          layers = getSTColumns("st_layers");
        } else if ("filters".equals(params.getRequiredParam("table"))) {
          errors.put(
              JSONHelper.createJSONObject(
                  Obj.writeValueAsString(
                      new PostStatus("OK", "Getting suitability filters columns"))));
          layers = getSTColumns("st_filters");
        }

        final JSONObject json = JSONHelper.createJSONObject(
            Obj.writeValueAsString(layers));
        ResponseHelper.writeResponse(params, json);
      } else if ("list_study_areas".equals(params.getRequiredParam("action"))) {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(
                    new PostStatus("OK", "Getting oskari layers"))));
        // Get directories
        JSONArray out = new JSONArray();
        ArrayList<StudyAreaUP> response = getStudyAreas();
        for (StudyAreaUP index : response) {
          // Convert to Json Object

          final JSONObject json = JSONHelper.createJSONObject(
              Obj.writeValueAsString(index));
          out.put(json);
        }

        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(
                    new PostStatus("OK", "Listing oskari layers executed"))));
        ResponseHelper.writeResponse(params, out);
      } else if ("list_st_settings".equals(params.getRequiredParam("action"))) {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(
                    new PostStatus("OK", "Getting suitability settings"))));
        // Get directories
        JSONArray out = new JSONArray();
        ArrayList<STSettings> response = getSettings(
            user_id,
            Long.parseLong(params.getRequiredParam("st_layer_id")));
        for (STSettings index : response) {
          // Convert to Json Object

          final JSONObject json = JSONHelper.createJSONObject(
              Obj.writeValueAsString(index));
          out.put(json);
        }

        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(
                    new PostStatus("OK", "Listing suitability settings executed"))));

        ResponseHelper.writeResponse(params, out);
      } else if ("list_st_norm_method".equals(params.getRequiredParam("action"))) {
        errorMsg = "list_st_norm_method";
        ArrayList<SettingsMethod> setting = new ArrayList<>();
        try (
            Connection connection = DatasourceHelper
                .getInstance()
                .getDataSource()
                .getConnection();
            PreparedStatement statement = connection.prepareStatement(
                "SELECT method, value, label\n" +
                    "	FROM public.st_normalization_method_options\n" +
                    "	where \"language\"=?;");) {
          statement.setString(1, "english");

          errors.put(
              JSONHelper.createJSONObject(
                  Obj.writeValueAsString(
                      new PostStatus("OK", "Executing query:" + statement.toString()))));

          boolean status = statement.execute();
          if (status) {
            ResultSet data = statement.getResultSet();
            while (data.next()) {
              SettingsMethod child = new SettingsMethod();
              child.method = data.getString("method");
              child.value = data.getInt("value");
              child.label = data.getString("label");
              setting.add(child);
            }
          }
          JSONArray out = new JSONArray();
          for (SettingsMethod index : setting) {
            // Convert to Json Object

            final JSONObject json = JSONHelper.createJSONObject(
                Obj.writeValueAsString(index));
            out.put(json);
          }

          errors.put(
              JSONHelper.createJSONObject(
                  Obj.writeValueAsString(
                      new PostStatus("OK", "Getting normalization methods executed"))));

          ResponseHelper.writeResponse(params, out);
        } catch (SQLException e) {
          errorMsg = errorMsg + e.toString();
          log.error(e, errorMsg);
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
      } else if ("list_st_norm_type".equals(params.getRequiredParam("action"))) {
        errorMsg = "list_st_norm_method";
        ArrayList<SettingsType> setting = new ArrayList<>();
        try (
            Connection connection = DatasourceHelper
                .getInstance()
                .getDataSource()
                .getConnection();
            PreparedStatement statement = connection.prepareStatement(
                "SELECT type, value, label\n" +
                    "	FROM public.st_normalization_type_options\n" +
                    "	where \"language\"=?;");) {
          statement.setString(1, "english");
          boolean status = statement.execute();
          if (status) {
            ResultSet data = statement.getResultSet();
            while (data.next()) {
              SettingsType child = new SettingsType();
              child.type = data.getString("type");
              child.value = data.getInt("value");
              child.label = data.getString("label");
              setting.add(child);
            }
          }
          JSONArray out = new JSONArray();
          for (SettingsType index : setting) {
            // Convert to Json Object

            final JSONObject json = JSONHelper.createJSONObject(
                Obj.writeValueAsString(index));
            out.put(json);
          }
          ResponseHelper.writeResponse(params, out);
        } catch (SQLException e) {
          errorMsg = errorMsg + e.toString();
          log.error(e, errorMsg);
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
      } else if ("list_st_join_methods".equals(params.getRequiredParam("action"))) {
        errorMsg = "list_st_norm_method";
        ArrayList<SettingsType> setting = new ArrayList<>();
        try (
            Connection connection = DatasourceHelper
                .getInstance()
                .getDataSource()
                .getConnection();
            PreparedStatement statement = connection.prepareStatement(
                "SELECT join_option as type, value, label\n" +
                    "	FROM public.st_join_options\n" +
                    "	where \"language\"=?;");) {
          statement.setString(1, "english");
          boolean status = statement.execute();
          if (status) {
            ResultSet data = statement.getResultSet();
            while (data.next()) {
              SettingsType child = new SettingsType();
              child.type = data.getString("type");
              child.value = data.getInt("value");
              child.label = data.getString("label");
              setting.add(child);
            }
          }
          JSONArray out = new JSONArray();
          for (SettingsType index : setting) {
            // Convert to Json Object

            final JSONObject json = JSONHelper.createJSONObject(
                Obj.writeValueAsString(index));
            out.put(json);
          }
          ResponseHelper.writeResponse(params, out);
        } catch (SQLException e) {
          errorMsg = errorMsg + e.toString();
          log.error(e, errorMsg);
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
      } else if ("list_remote_st_tables".equals(params.getRequiredParam("action"))) {
        // Get tables
        Directories dir = new Directories();
        dir.setData("scenario");
        dir.setLabel("scenario");
        dir.setIcon(null);
        directories.add(dir);

        ArrayList<Directories> layers = getRemoteSTTables();

        dir.setChildren(layers);

        JSONArray out = new JSONArray();
        for (Directories index : layers) {
          // Convert to Json Object

          final JSONObject json = JSONHelper.createJSONObject(
              Obj.writeValueAsString(index));
          out.put(json);
        }
        log.debug("User:  " + user_id.toString());
        ResponseHelper.writeResponse(params, out);
      } else if ("list_remote_st_columns".equals(params.getRequiredParam("action"))) {
        UPFieldsList layers = getRemoteSTColumns(
            params.getRequiredParam("layer_id"));
        final JSONObject json = JSONHelper.createJSONObject(
            Obj.writeValueAsString(layers));
        ResponseHelper.writeResponse(params, json);
      }
    } catch (Exception e) {
      errorMsg = errorMsg + e.getMessage();
      log.error(e, errorMsg);

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
        java.util.logging.Logger.getLogger(LayersSTHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java.util.logging.Logger.getLogger(LayersSTHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
    }
  }

  @Override
  public void handlePost(ActionParameters params) throws ActionException {
    String errorMsg = "Layers ST get ";
    try {
      params.requireLoggedInUser();
      ArrayList<String> roles = new UPTRoles()
          .handleGet(params, params.getUser());
      if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
        throw new Exception("User privilege is not enough for this action");
      }

      PostStatus status = null;
      if ("index_values".equals(params.getRequiredParam("action"))) {
        indexSuitability(params);
      } else if ("public_index_values".equals(params.getRequiredParam("action"))) {
        publicIndexSuitability(params);
      } else if ("copy_data".equals(params.getRequiredParam("action"))) {
        if (params.getRequiredParam("layerSTName") != null &&
            params.getRequiredParam("layerName") != null &&
            params.getRequiredParam("tableST") != null &&
            params.getRequiredParam("table") != null) {
          if (null == params.getRequiredParam("layerSTName")) {
            status = this.getAmenities(
                params.getRequiredParam("layerSTName"),
                params.getRequiredParam("layerName"),
                params.getRequest().getParameterValues("tableST"),
                params.getRequest().getParameterValues("table"),
                params.getRequiredParam("studyAreaID"));
            status.message += params.getRequiredParam("studyAreaID");
          } else {
            switch (params.getRequiredParam("layerSTName")) {
              case "mmu":
                status = this.setMmu(
                    params.getRequiredParam("layerSTName"),
                    params.getRequiredParam("layerName"),
                    params.getRequest().getParameterValues("tableST"),
                    params.getRequest().getParameterValues("table"),
                    params.getRequiredParam("studyAreaID"));
                status.message += params.getRequiredParam("studyAreaID");
                break;
            }
          }
        }
      } else {
        if ("match_layers".equals(params.getRequiredParam("action"))) {
          if (params.getRequiredParam("layerId") != null &&
              params.getRequiredParam("layerLabel") != null &&
              params.getRequiredParam("field") != null) {
            status = this.setSTLayers(
                Long.parseLong(params.getRequiredParam("layerId")),
                params.getRequiredParam("layerLabel"),
                params.getRequiredParam("field"));
            status.message += params.getRequiredParam("layerId");
          }
        } else if ("match_filters".equals(params.getRequiredParam("action"))) {
          if (params.getRequiredParam("filterId") != null &&
              params.getRequiredParam("filterLabel") != null) {
            status = this.setSTFilters(
                Integer.parseInt(params.getRequiredParam("filterId")),
                params.getRequiredParam("filterLabel"));
            status.message += params.getRequiredParam("filterId");
          }
        } else if ("set_settings".equals(params.getRequiredParam("action"))) {
          if (params.getRequiredParam("data") != null) {
            status = new PostStatus();
            String[] settings = (String[]) params
                .getRequest()
                .getParameterValues("data");
            for (int i = 0; i < settings.length; i++) {
              ObjectMapper objectMapper = new ObjectMapper();
              STSettings setting = objectMapper.readValue(
                  settings[i],
                  STSettings.class);
              stLayers.put(i, setting);
            }
            status.status = "Success";
            status.message += "Settings OK";
          }
        } else if ("save_settings".equals(params.getRequiredParam("action"))) {
          if (params.getRequiredParam("data") != null) {
            status = new PostStatus();
            String[] settings = (String[]) params
                .getRequest()
                .getParameterValues("data");
            for (int i = 0; i < settings.length; i++) {
              ObjectMapper objectMapper = new ObjectMapper();
              STSettings setting = objectMapper.readValue(
                  settings[i],
                  STSettings.class);
              stLayers.put(i, setting);
            }
            status = saveSettings();
          }
        }

        final JSONObject outs;
        errorMsg = Obj.writeValueAsString(status);
        outs = JSONHelper.createJSONObject(Obj.writeValueAsString(status));
        ResponseHelper.writeResponse(params, outs);
      }
    } catch (Exception e) {
      errorMsg = errorMsg + e.getMessage();
      log.error(e, errorMsg);

      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        ResponseHelper.writeError(
            params,
            "",
            500,
            new JSONObject().put("Errors", errors));
      } catch (JsonProcessingException | JSONException ex) {
        java.util.logging.Logger.getLogger(LayersSTHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
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

  private void getUserParams(User user, ActionParameters params)
      throws ActionParamsException {
  }

  private ArrayList<Directories> getLayers() throws Exception {
    String errorMsg = "getLayers";
    ArrayList<Directories> children = new ArrayList<Directories>();
    try (
        Connection connection = DatasourceHelper
            .getInstance()
            .getDataSource()
            .getConnection();
        PreparedStatement statement = connection.prepareStatement(
            "with user_layers as(\n" +
                "    select user_layer.id,\n" +
                "    layer_name" +
                "    from user_layer\n" +
                "    left join upt_user_layer_scope on upt_user_layer_scope.user_layer_id=user_layer.id\n" +
                "    where (user_layer.uuid=? or upt_user_layer_scope.is_public=1) and lower(layer_name) not like '%buffer%'\n"
                +
                ")\n" +
                "select  id,layer_name\n" +
                "from user_layers");) {
      statement.setString(1, user_uuid);

      boolean status = statement.execute();
      if (status) {
        ResultSet data = statement.getResultSet();
        while (data.next()) {
          Directories child = new Directories();
          child.setData("priv_" + data.getString("id"));
          child.setLabel(data.getString("layer_name"));
          child.setExpandedIcon(null);
          child.setCollapsedIcon(null);
          child.setType("layer");
          children.add(child);
        }
        return children;
      }
      return children;
    } catch (Exception e) {
      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        // ResponseHelper.writeError(null, "", 500, new JSONObject().put("Errors",
        // errors));
      } catch (JsonProcessingException ex) {
        java.util.logging.Logger.getLogger(STLayersHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
      throw new Exception();
    }
  }

  private ArrayList<Directories> getPublicLayers() throws Exception {
    String errorMsg = "getPublicLayers";
    ArrayList<Directories> children = new ArrayList<Directories>();
    try (
        Connection connection = DatasourceHelper
            .getInstance()
            .getDataSource()
            .getConnection();
        PreparedStatement statement = connection.prepareStatement(
            "with public_layers as(\n" +
                "   SELECT distinct oskari_maplayer.id, name as layer_name FROM oskari_maplayer\n" +
                "   INNER JOIN public_layer_data on oskari_maplayer.id = public_layer_data.public_layer_id WHERE type = 'wfslayer' OR type = 'wmslayer' AND public_layer_data.uuid = ?\n"
                +
                "   )\n" +
                "select id, layer_name from public_layers");) {
      statement.setString(1, user_uuid);

      boolean status = statement.execute();
      if (status) {
        ResultSet data = statement.getResultSet();
        while (data.next()) {
          Directories child = new Directories();
          child.setData("pub_" + data.getString("id"));
          child.setLabel(data.getString("layer_name"));
          child.setExpandedIcon(null);
          child.setCollapsedIcon(null);
          child.setType("layer");
          children.add(child);
        }
        return children;
      }
      return children;
    } catch (Exception e) {
      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        // ResponseHelper.writeError(null, "", 500, new JSONObject().put("Errors",
        // errors));
      } catch (JsonProcessingException ex) {
        java.util.logging.Logger.getLogger(STLayersHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
      throw new Exception();
    }
  }

  private PostStatus saveSettings() throws Exception {
    PostStatus status = new PostStatus();
    try (
        Connection connection = DatasourceHelper
            .getInstance()
            .getDataSource()
            .getConnection();
        PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO public.st_settings(normalization_method, range_min, range_max, smaller_better, weight)	VALUES (?, ?, ?, ?, ?);",
            Statement.RETURN_GENERATED_KEYS);) {
      connection.setAutoCommit(false);
      for (Map.Entry m : this.stLayers.entrySet()) {
        statement.setInt(1, ((STSettings) m.getValue()).normalization_method);
        statement.setDouble(2, ((STSettings) m.getValue()).range_min);
        statement.setDouble(3, ((STSettings) m.getValue()).range_max);
        statement.setInt(4, ((STSettings) m.getValue()).smaller_better);
        statement.setDouble(5, ((STSettings) m.getValue()).weight);
        statement.addBatch();
      }

      int[] rows = statement.executeBatch();
      status.status = "Success";
      status.message = rows.length + " rows inserted succesfully";
      return status;
    } catch (Exception e) {
      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        // ResponseHelper.writeError(null, "", 500, new JSONObject().put("Errors",
        // errors));
      } catch (JsonProcessingException ex) {
        java.util.logging.Logger.getLogger(LayersSTHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
      throw new Exception();
    }
  }

  private ArrayList<STLayers> getSTLayers(Long user_id, Long study_area)
      throws Exception {
    String errorMsg = "getSTLayers";
    ArrayList<STLayers> modules = new ArrayList<>();
    try (
        Connection connection = DatasourceHelper
            .getInstance()
            .getDataSource()
            .getConnection();) {
      PreparedStatement statement = connection.prepareStatement(
          "with study_area as(\n" +
              "	select st_transform(st_setsrid(geometry,?),4326) as geometry from user_layer_data where user_layer_id=?\n"
              +
              "),layers as(\n" +
              "	select user_layer.id from user_layer\n" +
              "    left join upt_user_layer_scope on upt_user_layer_scope.user_layer_id=user_layer.id\n" +
              "    ,study_area \n" +
              "    where  (user_layer.uuid=? or upt_user_layer_scope.is_public=1) and st_intersects(st_geomfromtext(user_layer.wkt,4326),study_area.geometry) \n"
              +
              ")\n" +
              "select st_layers.id, st_layers.st_layer_label as label \n" +
              "from st_layers,layers\n" +
              "where st_layers.user_layer_id in(layers.id)\n"
      // "where st_public_layers.public_layer_id in(public_layers.id)\n"
      );
      statement.setInt(1, Integer.parseInt(stProjection));
      statement.setLong(2, study_area);
      statement.setString(3, user_uuid);

      ResultSet data = statement.executeQuery();

      while (data.next()) {
        STLayers layer = new STLayers();
        layer.id = data.getLong("id");
        layer.label = data.getString("label");
        modules.add(layer);
      }
      return modules;
    } catch (Exception e) {
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);
      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        // ResponseHelper.writeError(null, "", 500, new JSONObject().put("Errors",
        // errors));
      } catch (JsonProcessingException ex) {
        java.util.logging.Logger.getLogger(
            STStandardizationMethodHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
      throw new Exception();
    }
  }

  private ArrayList<STPublicLayers> getSTPublicLayers(
      Long user_id,
      Long study_area)
      throws Exception {
    String errorMsg = "getSTPublicLayers";
    ArrayList<STPublicLayers> modules = new ArrayList<>();
    try (
        Connection connection = DatasourceHelper
            .getInstance()
            .getDataSource()
            .getConnection();) {
      PreparedStatement statement = connection.prepareStatement(
          "with study_area as(\n" +
              "	select st_transform(st_setsrid(geometry,?),4326) as geometry from user_layer_data where user_layer_id=?\n"
              +
              "), public_layers as(\n" +
              "    select distinct st_public_layers.id as id, st_public_layers.st_layer_label, st_layer_label as label ,st_public_layers.public_layer_id,layer_field,layer_mmu_code, ST_AsText(study_area.geometry) as geometry\n"
              +
              "    from st_public_layers\n" +
              "    inner join public_layer_data on public_layer_data.public_layer_id = st_public_layers.public_layer_id\n"
              +
              "    , study_area\n" +
              "    where\n" +
              "    st_intersects(st_geomfromtext(user_layer.wkt,4326),public_layer_data.geometry)\n"
              +
              ")\n" +
              "select st_public_layers.id, st_public_layers.st_layer_label as label from st_public_layers\n"
      // "where st_public_layers.public_layer_id in(public_layers.id)\n"
      );
      statement.setInt(1, Integer.parseInt(stProjection));
      statement.setLong(2, study_area);

      ResultSet data = statement.executeQuery();

      while (data.next()) {
        STPublicLayers layer = new STPublicLayers();
        layer.id = data.getLong("id");
        layer.label = data.getString("label");
        modules.add(layer);
      }
      return modules;
    } catch (Exception e) {
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);
      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        // ResponseHelper.writeError(null, "", 500, new JSONObject().put("Errors",
        // errors));
      } catch (JsonProcessingException ex) {
        java.util.logging.Logger.getLogger(
            STStandardizationMethodHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
      throw new Exception();
    }
  }

  private ArrayList<STLayers> getSTLayersPubStdArea(
      Long user_id,
      Long study_area)
      throws Exception {
    String errorMsg = "getSTLayers";
    ArrayList<STLayers> modules = new ArrayList<>();
    try (
        Connection connection = DatasourceHelper
            .getInstance()
            .getDataSource()
            .getConnection();) {
      PreparedStatement statement = connection.prepareStatement(
          "with study_area as(\n" +
              "	select geometry from public_layer_data where public_layer_id=?\n"
              +
              "),layers as(\n" +
              "	select user_layer.id from user_layer\n" +
              "    left join upt_user_layer_scope on upt_user_layer_scope.user_layer_id=user_layer.id\n" +
              "    ,study_area \n" +
              "    where  (user_layer.uuid=? or upt_user_layer_scope.is_public=1) and st_intersects(st_geomfromtext(user_layer.wkt,4326),study_area.geometry) \n"
              +
              ")\n" +
              "select st_layers.id, st_layers.st_layer_label as label \n" +
              "from st_layers,layers\n" +
              "where st_layers.user_layer_id in(layers.id)\n"
      // "where st_public_layers.public_layer_id in(public_layers.id)\n"
      );
      statement.setLong(1, study_area);
      statement.setString(2, user_uuid);

      ResultSet data = statement.executeQuery();

      while (data.next()) {
        STLayers layer = new STLayers();
        layer.id = data.getLong("id");
        layer.label = data.getString("label");
        modules.add(layer);
      }
      return modules;
    } catch (Exception e) {
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);
      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        // ResponseHelper.writeError(null, "", 500, new JSONObject().put("Errors",
        // errors));
      } catch (JsonProcessingException ex) {
        java.util.logging.Logger.getLogger(
            STStandardizationMethodHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
      throw new Exception();
    }
  }

  private ArrayList<STPublicLayers> getSTPublicLayersPubStdArea(
      Long user_id,
      Long study_area)
      throws Exception {
    String errorMsg = "getSTPublicLayers";
    ArrayList<STPublicLayers> modules = new ArrayList<>();
    try (
        Connection connection = DatasourceHelper
            .getInstance()
            .getDataSource()
            .getConnection();) {
      PreparedStatement statement = connection.prepareStatement(
          "with study_area as(\n" +
              "	select geometry from public_layer_data where public_layer_id=?\n" +
              "), public_layers as(\n" +
              "    select distinct st_public_layers.id as id, st_public_layers.st_layer_label, st_layer_label as label ,st_public_layers.public_layer_id,layer_field,layer_mmu_code, ST_AsText(study_area.geometry) as geometry\n"
              +
              "    from st_public_layers\n" +
              "    inner join public_layer_data on public_layer_data.public_layer_id = st_public_layers.public_layer_id\n"
              +
              "    , study_area\n" +
              "    where\n" +
              "    st_intersects(study_area.geometry,public_layer_data.geometry)\n" +
              ")\n" +
              "select id, st_layer_label as label from public_layers\n"
      // "where st_public_layers.public_layer_id in(public_layers.id)\n"
      );
      statement.setLong(1, study_area);

      ResultSet data = statement.executeQuery();

      while (data.next()) {
        STPublicLayers layer = new STPublicLayers();
        layer.id = data.getLong("id");
        layer.label = data.getString("label");
        modules.add(layer);
      }
      return modules;
    } catch (Exception e) {
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);
      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        // ResponseHelper.writeError(null, "", 500, new JSONObject().put("Errors",
        // errors));
      } catch (JsonProcessingException ex) {
        java.util.logging.Logger.getLogger(
            STStandardizationMethodHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
      throw new Exception();
    }
  }

  private ArrayList<STSettings> getSettings(Long user_id, Long layerId)
      throws Exception {
    String errorMsg = "getSTSettings";
    ArrayList<STSettings> modules = new ArrayList<>();
    try (
        Connection connection = DatasourceHelper
            .getInstance()
            .getDataSource()
            .getConnection();) {
      PreparedStatement statement = connection.prepareStatement(
          "SELECT \n" +
              "	st_settings.id, \n" +
              "	st_layers_id as st_layer_id, \n" +
              "	normalization_method, \n" +
              "	range_min, \n" +
              "	range_max, \n" +
              "	smaller_better, \n" +
              "	weight,\n" +
              "	st_layers.st_layer_label as label\n" +
              "FROM public.st_settings\n" +
              "right join st_layers on st_layers.id=st_settings.st_layers_id\n" +
              "where st_layers.id=?\n");
      statement.setInt(1, layerId.intValue());
      ResultSet data = statement.executeQuery();
      while (data.next()) {
        STSettings layer = new STSettings(String.valueOf(layerId));
        layer.id = data.getLong("id");
        layer.normalization_method = data.getInt("normalization_method") != 0
            ? data.getInt("normalization_method")
            : 1;
        layer.range_min = data.getDouble("range_min") != 0 ? data.getDouble("range_min") : 0;
        layer.range_max = data.getDouble("range_max") != 0 ? data.getDouble("range_max") : 1;
        layer.smaller_better = data.getInt("smaller_better") != 0
            ? data.getInt("smaller_better")
            : 0;
        layer.weight = data.getDouble("weight") != 0 ? data.getDouble("weight") : 1;
        layer.label = data.getString("label");
        modules.add(layer);
      }
      if (modules.isEmpty()) {
        STSettings layer = new STSettings(String.valueOf(layerId));
        modules.add(layer);
      }
      return modules;
    } catch (Exception e) {
      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        // ResponseHelper.writeError(null, "", 500, new JSONObject().put("Errors",
        // errors));
      } catch (JsonProcessingException ex) {
        java.util.logging.Logger.getLogger(
            STStandardizationMethodHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);
      throw new Exception();
    }
  }

  private ArrayList<STPublicSettings> getPublicSettings(
      Long user_id,
      Long layerId)
      throws Exception {
    String errorMsg = "getSTSettings";
    ArrayList<STPublicSettings> modules = new ArrayList<>();
    try (
        Connection connection = DatasourceHelper
            .getInstance()
            .getDataSource()
            .getConnection();) {
      PreparedStatement statement = connection.prepareStatement(
          "SELECT \n" +
              "	st_public_settings.id, \n" +
              "	st_layers_id, \n" +
              "	normalization_method, \n" +
              "	range_min, \n" +
              "	range_max, \n" +
              "	smaller_better, \n" +
              "	weight,\n" +
              "	st_public_layers.st_layer_label as label\n" +
              "FROM public.st_public_settings\n" +
              "right join st_public_layers on st_public_layers.id=st_public_settings.st_layers_id\n" +
              "where st_public_layers.id=?");
      statement.setInt(1, layerId.intValue());
      ResultSet data = statement.executeQuery();
      while (data.next()) {
        STPublicSettings layer = new STPublicSettings(String.valueOf(layerId));
        layer.id = data.getLong("id");
        layer.normalization_method = data.getInt("normalization_method") != 0
            ? data.getInt("normalization_method")
            : 1;
        layer.range_min = data.getDouble("range_min") != 0 ? data.getDouble("range_min") : 0;
        layer.range_max = data.getDouble("range_max") != 0 ? data.getDouble("range_max") : 1;
        layer.smaller_better = data.getInt("smaller_better") != 0
            ? data.getInt("smaller_better")
            : 0;
        layer.weight = data.getDouble("weight") != 0 ? data.getDouble("weight") : 1;
        layer.label = data.getString("label");
        modules.add(layer);
      }
      if (modules.isEmpty()) {
        STPublicSettings layer = new STPublicSettings(String.valueOf(layerId));
        modules.add(layer);
      }
      return modules;
    } catch (Exception e) {
      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        // ResponseHelper.writeError(null, "", 500, new JSONObject().put("Errors",
        // errors));
      } catch (JsonProcessingException ex) {
        java.util.logging.Logger.getLogger(
            STStandardizationMethodHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);
      throw new Exception();
    }
  }

  private ArrayList<STLayers> getSTFilters(Long user_id, Long study_area)
      throws Exception {
    String errorMsg = "getSTLayers";
    ArrayList<STLayers> modules = new ArrayList<>();
    try (
        Connection connection = DatasourceHelper
            .getInstance()
            .getDataSource()
            .getConnection();) {
      PreparedStatement statement = connection.prepareStatement(
          "select st_filters.id as id, st_filter_label as label\n" +
              "from st_filters\n" +
              "inner join user_layer on user_layer.id=st_filters.user_layer_id\n" +
              "left join upt_user_layer_scope on upt_user_layer_scope.user_layer_id=user_layer.id\n" +
              "where  (user_layer.uuid=? or upt_user_layer_scope.is_public=1)");
      statement.setString(1, user_uuid);
      ResultSet data = statement.executeQuery();

      while (data.next()) {
        STLayers layer = new STLayers();
        layer.id = data.getLong("id");
        layer.label = data.getString("label");
        modules.add(layer);
      }
      return modules;
    } catch (Exception e) {
      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        // ResponseHelper.writeError(null, "", 500, new JSONObject().put("Errors",
        // errors));
      } catch (JsonProcessingException ex) {
        java.util.logging.Logger.getLogger(
            STStandardizationMethodHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);
      throw new Exception();
    }
  }

  private ArrayList<String> getColumns(String id) throws Exception {
    String errorMsg = "getLayers";
    ArrayList<String> layers = new ArrayList<String>();
    try (
        Connection connection = DatasourceHelper
            .getInstance()
            .getDataSource()
            .getConnection();) {
      Statement statement = connection.createStatement();
      // statement.setInt(0, Integer.parseInt(id));
      // boolean status=statement.executeQuery();
      // if (status){
      ResultSet data = statement.executeQuery(
          "with cols as(" +
              " select id,fields " +
              " from user_layer " +
              " where id=" +
              id +
              " ) " +
              " select name " +
              " from cols,json_populate_recordset(null::record,cols.fields) as(name text) " +
              " where name !='the_geom' " +
              " union all " +
              " select 'geometry';");
      while (data.next()) {
        layers.add(data.getString("name"));
      }
      // return layers;
      // }
      return layers;
    } catch (Exception e) {
      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        // ResponseHelper.writeError(null, "", 500, new JSONObject().put("Errors",
        // errors));
      } catch (JsonProcessingException ex) {
        java.util.logging.Logger.getLogger(
            STStandardizationMethodHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);
      throw new Exception();
    }
  }

  private STFieldsList getSTColumns(String table) throws Exception {
    String errorMsg = "getSTLayers";

    ArrayList<String> layers = new ArrayList<String>();
    STFieldsList columns = new STFieldsList();
    try (
        Connection connection = DatasourceHelper
            .getInstance()
            .getDataSource()
            .getConnection();) {
      Statement statement = connection.createStatement();

      ResultSet data = statement.executeQuery(
          "SELECT column_name FROM information_schema.columns " +
              "WHERE table_schema = 'public' AND table_name   = '" +
              table +
              "' and COLUMN_name !='id' and COLUMN_name !='created' and COLUMN_name !='updated';");

      while (data.next()) {
        layers.add(data.getString("column_name"));

        PreparedStatement statement1 = connection.prepareStatement(
            "insert into st_table_fields(\"table\",name,language,label) values(?,?,?,?) on conflict(\"table\",language,name) do nothing;");
        statement1.setString(1, table);
        statement1.setString(2, data.getString("column_name"));
        statement1.setString(3, "english");
        statement1.setString(4, data.getString("column_name"));
        errorMsg += statement1.toString();
        statement1.execute();
      }

      PreparedStatement statement2 = connection.prepareStatement(
          "select * from st_table_fields where \"table\"=? and language=?;");
      statement2.setString(1, table);
      statement2.setString(2, "english");
      errorMsg += statement2.toString();
      ResultSet names = statement2.executeQuery();

      while (names.next()) {
        for (String field : layers) {
          errorMsg += " field: " + field;
          if (names.getString("name").equals(field)) {
            STFields column = new STFields();
            column.name = field;
            column.label = names.getString("label");
            // column.f_type=names.getString("f_type");
            columns.stFields.add(column);
          }
        }
      }

      return columns;
    } catch (Exception e) {
      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        // ResponseHelper.writeError(null, "", 500, new JSONObject().put("Errors",
        // errors));
      } catch (JsonProcessingException ex) {
        java.util.logging.Logger.getLogger(
            STStandardizationMethodHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);
      throw new Exception();
    }
  }

  private PostStatus setSTLayers(Long layerID, String layerLabel, String field)
      throws Exception {
    PostStatus status = new PostStatus();
    try (
        Connection connection = DatasourceHelper
            .getInstance()
            .getDataSource()
            .getConnection();
        PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO public.st_layers(user_layer_id, layer_field, st_layer_label)VALUES ( ?, ?, ?);");) {
      statement.setLong(1, layerID);
      statement.setString(2, field);
      statement.setString(3, layerLabel);
      status.message = statement.toString();
      statement.execute();
      status.status = "Success";
      status.message = "layer registered";
    } catch (Exception e) {
      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        // ResponseHelper.writeError(null, "", 500, new JSONObject().put("Errors",
        // errors));
      } catch (JsonProcessingException ex) {
        java.util.logging.Logger.getLogger(
            STStandardizationMethodHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
      throw new Exception();
    }

    return status;
  }

  private PostStatus setSTFilters(Integer filterID, String filterLabel)
      throws Exception {
    PostStatus status = new PostStatus();
    String query = "";
    try (
        Connection connection = DatasourceHelper
            .getInstance()
            .getDataSource()
            .getConnection();
        PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO public.st_filters( user_layer_id, st_filter_label)VALUES ( ?, ?);");) {
      statement.setInt(1, filterID);
      // statement.setString(2, field);
      statement.setString(2, filterLabel);
      statement.execute();
      status.status = "Success";
      status.message = "layer registered";
      return status;
    } catch (Exception e) {
      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        // ResponseHelper.writeError(null, "", 500, new JSONObject().put("Errors",
        // errors));
      } catch (JsonProcessingException ex) {
        java.util.logging.Logger.getLogger(
            STStandardizationMethodHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
      status.status = "Error";
      status.message = e.toString();
      throw new Exception();
    }
  }

  private ArrayList<StudyAreaUP> getStudyAreas() throws Exception {
    String errorMsg = "getStudyAreas";
    ArrayList<StudyAreaUP> layers = new ArrayList<StudyAreaUP>();
    try (
        Connection connection = DatasourceHelper
            .getInstance()
            .getDataSource()
            .getConnection();) {
      Statement statement = connection.createStatement();
      ResultSet data = statement.executeQuery(
          "SELECT id,layer_name FROM public.user_layer;");

      while (data.next()) {
        StudyAreaUP child = new StudyAreaUP();
        child.setId(data.getString("id"));
        child.setName(data.getString("layer_name"));
        layers.add(child);
      }
      // log.error(layers, "OK "+errorMsg);
      return layers;
    } catch (Exception e) {
      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        // ResponseHelper.writeError(null, "", 500, new JSONObject().put("Errors",
        // errors));
      } catch (JsonProcessingException ex) {
        java.util.logging.Logger.getLogger(
            STStandardizationMethodHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);
      throw new Exception();
    }
  }

  private void indexSuitability(ActionParameters params)
      throws ActionParamsException {
    String errorMsg = "getStudyAreas";
    try (
        Connection connection = DatasourceHelper
            .getInstance()
            .getDataSource()
            .getConnection();
        PreparedStatement statement = connection.prepareStatement(
            "select * from public.suitability_index_values(?,?,?,?,?,?,?,?,?)"
        // PreparedStatement statement = connection.prepareStatement(query
        );) {
      Array layers = connection.createArrayOf(
          "INTEGER",
          params.getRequest().getParameterValues("layers"));

      Array public_layers = connection.createArrayOf(
          "INTEGER",
          params.getRequest().getParameterValues("public_layers"));

      Array filters = connection.createArrayOf(
          "INTEGER",
          params.getRequest().getParameterValues("filters"));

      Array public_filters = connection.createArrayOf(
          "INTEGER",
          params.getRequest().getParameterValues("public_filters"));

      Array settings = connection.createArrayOf(
          "TEXT",
          params.getRequest().getParameterValues("settings"));

      Array public_settings = connection.createArrayOf(
          "TEXT",
          params.getRequest().getParameterValues("public_settings"));
      statement.setArray(1, layers);
      statement.setArray(2, public_layers);
      statement.setArray(3, filters);
      statement.setArray(4, public_filters);
      statement.setArray(5, settings);
      statement.setArray(6, public_settings);
      statement.setLong(
          7,
          Long.parseLong(params.getRequiredParam("studyArea")));
      statement.setInt(8, params.getRequiredParamInt("joinMethod"));
      statement.setInt(9, Integer.parseInt(stProjection));
      errors.put(
          JSONHelper.createJSONObject(
              Obj.writeValueAsString(new PostStatus("OK", statement.toString()))));
      ResultSet data = statement.executeQuery();
      JSONArray geoJson = new JSONArray();
      if (data.next()) {
        final JSONObject json = JSONHelper.createJSONObject(
            data.getString("json"));
        geoJson.put(json);
      }
      data.close();
      statement.close();
      connection.close();
      ResponseHelper.writeResponse(params, geoJson);
    } catch (SQLException e) {
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);
      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Detail", e.getMessage()))));
        ResponseHelper.writeError(
            params,
            "",
            500,
            new JSONObject().put("Errors", errors));
      } catch (JsonProcessingException | JSONException ex) {
        java.util.logging.Logger.getLogger(
            STStandardizationMethodHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
    } catch (Exception e) {
      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Detail", e.getMessage()))));
        ResponseHelper.writeError(
            params,
            "",
            500,
            new JSONObject().put("Errors", errors));
      } catch (JsonProcessingException | JSONException ex) {
        java.util.logging.Logger.getLogger(STLayersHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
    }
  }

  private void publicIndexSuitability(ActionParameters params)
      throws ActionParamsException {
    String errorMsg = "getStudyAreas";
    try (
        Connection connection = DatasourceHelper
            .getInstance()
            .getDataSource()
            .getConnection();
        PreparedStatement statement = connection.prepareStatement(
            "select * from public.suitability_public_index_values(?,?,?,?,?,?,?,?,?)"
        // PreparedStatement statement = connection.prepareStatement(query
        );) {
      Array layers = connection.createArrayOf(
          "INTEGER",
          params.getRequest().getParameterValues("layers"));

      Array public_layers = connection.createArrayOf(
          "INTEGER",
          params.getRequest().getParameterValues("public_layers"));

      Array filters = connection.createArrayOf(
          "INTEGER",
          params.getRequest().getParameterValues("filters"));

      Array public_filters = connection.createArrayOf(
          "INTEGER",
          params.getRequest().getParameterValues("public_filters"));

      Array settings = connection.createArrayOf(
          "TEXT",
          params.getRequest().getParameterValues("settings"));

      Array public_settings = connection.createArrayOf(
          "TEXT",
          params.getRequest().getParameterValues("public_settings"));
      statement.setArray(1, layers);
      statement.setArray(2, public_layers);
      statement.setArray(3, filters);
      statement.setArray(4, public_filters);
      statement.setArray(5, settings);
      statement.setArray(6, public_settings);
      statement.setLong(
          7,
          Long.parseLong(params.getRequiredParam("studyArea")));
      statement.setInt(8, params.getRequiredParamInt("joinMethod"));
      statement.setInt(9, Integer.parseInt(stProjection));
      errors.put(
          JSONHelper.createJSONObject(
              Obj.writeValueAsString(new PostStatus("OK", statement.toString()))));
      // ResultSet data = statement.executeQuery();
      ResultSet data = statement.executeQuery();
      JSONArray geoJson = new JSONArray();
      while (data.next()) {
        final JSONObject json = JSONHelper.createJSONObject(
            data.getString("json"));
        geoJson.put(json);
        break;
      }
      data.close();
      statement.close();
      connection.close();
      ResponseHelper.writeResponse(params, geoJson);
    } catch (SQLException e) {
      errorMsg = errorMsg + e.toString();
      log.error(e, errorMsg);
      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Detail", e.getMessage()))));
        ResponseHelper.writeError(
            params,
            "",
            500,
            new JSONObject().put("Errors", errors));
      } catch (JsonProcessingException | JSONException ex) {
        java.util.logging.Logger.getLogger(
            STStandardizationMethodHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
    } catch (Exception e) {
      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Detail", e.getMessage()))));
        ResponseHelper.writeError(
            params,
            "",
            500,
            new JSONObject().put("Errors", errors));
      } catch (JsonProcessingException | JSONException ex) {
        java.util.logging.Logger.getLogger(STLayersHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
    }
  }

  private PostStatus setMmu(
      String layerUP,
      String layer,
      String[] tableup,
      String[] table,
      String studyArea)
      throws Exception {
    PostStatus postStatus = new PostStatus();
    String values = "distinct ";
    String tableUP[] = new String[tableup.length + 1];
    tableUP[tableup.length] = "study_area";
    System.arraycopy(tableup, 0, tableUP, 0, tableup.length);
    for (int i = 0; i < tableUP.length; i++) {
      if (tableUP[i].equals("location")) {
        // values += "st_astext("+table[i]+")";
        values += " st_astext(st_transform(st_setsrid(st_pointonsurface(" +
            table[i] +
            ")," +
            stProjection +
            "),4326)) as " +
            table[i];
      } else if (tableUP[i].equals("study_area")) {
        values += studyArea + " as study_area";
      } else {
        values = " trim(both '\"' from CAST(property_json->'" +
            table[i] +
            "' AS text))  as " +
            table[i];
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
        Connection connection = DatasourceHelper
            .getInstance()
            .getDataSource()
            .getConnection();) {
      Statement statement = connection.createStatement();
      query = "select distinct " +
          values +
          " from user_layer\n" +
          " inner join user_layer_data on user_layer.id = user_layer_data.user_layer_id\n" +
          " where user_layer.id=" +
          layer;
      ResultSet data = statement.executeQuery(query);

      ArrayList<MmuUP> data_in = new ArrayList<>();

      while (data.next()) {
        Object o = new MmuUP();
        Class<?> c = o.getClass();
        for (int i = 0; i < tableUP.length; i++) {
          Field f = c.getDeclaredField(tableUP[i]);
          f.setAccessible(true);
          if (!tableUP[i].equals("study_area") && !tableUP[i].equals("mmu_id")) {
            f.set(o, data.getString(table[i]));
          } else if (tableUP[i].equals("study_area")) {
            Integer scen = (Integer) data.getInt(tableUP[i]);
            f.set(o, scen);
          } else if (tableUP[i].equals("location")) {
            f.set(o, data.getString(table[i]));
          } else if (tableUP[i].equals("mmu_id")) {
          }
        }
        log.debug(o, "reflection");
        data_in.add((MmuUP) o);
        // return postStatus;
      }
      Tables<MmuUP> final_data = new Tables<MmuUP>(data_in);

      RestTemplate restTemplate = new RestTemplate();
      postStatus = restTemplate.postForObject(
          "http://" + stwsHost + ":" + stwsPort + "/mmu/",
          final_data,
          PostStatus.class);
      return postStatus;
    } catch (Exception e) {
      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        // ResponseHelper.writeError(null, "", 500, new JSONObject().put("Errors",
        // errors));
      } catch (JsonProcessingException ex) {
        java.util.logging.Logger.getLogger(
            STStandardizationMethodHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
      log.error(e, errorMsg + query);
      throw new Exception();
    }
  }

  private PostStatus getAmenities(
      String layerUP,
      String layer,
      String[] tableUP_in,
      String[] table,
      String studyArea)
      throws Exception {
    String[] tableUP = new String[tableUP_in.length + 1];
    for (int i = 0; i < tableUP_in.length; i++) {
      tableUP[i] = tableUP_in[i];
    }
    tableUP[tableUP_in.length] = "study_area";

    PostStatus postStatus = new PostStatus();
    String values = "distinct ";
    for (int i = 0; i < tableUP.length; i++) {
      if (tableUP[i].equals("location")) {
        // values += "st_astext("+table[i]+")";
        values += " st_astext(st_transform(st_setsrid(" +
            table[i] +
            "," +
            stProjection +
            "),4326)) as " +
            table[i];
      } else if (tableUP[i].equals("study_area")) {
        values += studyArea + " as study_area";
      } else {
        values = " trim(both '\"' from CAST(property_json->'" +
            table[i] +
            "' AS text))  as " +
            table[i];
      }
      if (i < tableUP.length - 1) {
        values += ",";
      } else {
        values = values.replaceAll(",$", "");
        // values +=","+ scenarioId + " as scenario";
      }
    }
    String errorMsg = "";
    String query = "";
    try (
        Connection connection = DatasourceHelper
            .getInstance()
            .getDataSource()
            .getConnection();) {
      Statement statement = connection.createStatement();
      query = "select distinct " +
          values +
          " from user_layer\n" +
          " inner join user_layer_data on user_layer.id = user_layer_data.user_layer_id\n" +
          " where user_layer.id=" +
          layer;
      ResultSet data = statement.executeQuery(query);

      ArrayList<Amenities> data_in = new ArrayList<>();

      while (data.next()) {
        Object o = new Amenities();
        Class<?> c = o.getClass();
        for (int i = 0; i < tableUP.length; i++) {
          Field f = c.getDeclaredField(tableUP[i]);
          f.setAccessible(true);
          if (!tableUP[i].equals("study_area") &&
              !tableUP[i].equals("amenities_id")) {
            f.set(o, data.getString(table[i]));
          } else if (tableUP[i].equals("study_area")) {
            Integer scen = (Integer) data.getInt(tableUP[i]);
            f.set(o, scen);
          } else if (tableUP[i].equals("location")) {
            f.set(o, data.getString(table[i]));
          } else if (tableUP[i].equals("amenities_id")) {
          }
        }
        log.debug(o, "reflection");
        data_in.add((Amenities) o);
        // return postStatus;
      }
      Tables<Amenities> final_data = new Tables<Amenities>(data_in);

      RestTemplate restTemplate = new RestTemplate();
      postStatus = restTemplate.postForObject(
          "http://" + stwsHost + ":" + stwsPort + "/amenities/",
          final_data,
          PostStatus.class);
      return postStatus;
    } catch (Exception e) {
      log.error(e, errorMsg + query);

      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        // ResponseHelper.writeError(null, "", 500, new JSONObject().put("Errors",
        // errors));
      } catch (JsonProcessingException ex) {
        java.util.logging.Logger.getLogger(STLayersHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
      throw new Exception();
    }
  }

  private ArrayList<Directories> getRemoteSTTables() throws Exception {
    String errorMsg = "getUPLayers";
    ArrayList<Directories> children = new ArrayList<Directories>();
    try (
        Connection connection = DatasourceHelper
            .getInstance()
            .getDataSource()
            .getConnection();) {
      ResponseEntity<List<String>> returns = null;
      RestTemplate restTemplate = new RestTemplate();

      UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(
          "http://" + stwsHost + ":" + stwsPort + "/layers/");
      returns = restTemplate.exchange(
          uriBuilder.toUriString(),
          HttpMethod.GET,
          null,
          new ParameterizedTypeReference<List<String>>() {
          });

      List<String> res = returns.getBody();
      Statement statement = connection.createStatement();
      ResultSet tablesLabel = statement.executeQuery(
          "SELECT distinct name, label\n" +
              "	FROM public.st_tables" +
              "       where language='english' ");

      List<String> tables = new ArrayList<>();
      List<String> labels = new ArrayList<>();

      while (tablesLabel.next()) {
        tables.add(tablesLabel.getString("name"));
        labels.add(tablesLabel.getString("label"));
      }
      for (String table : res) {
        String label = table;
        for (String tab : tables) {
          if (tab.equals(table)) {
            label = labels.get(tables.indexOf(tab));
            break;
          }
        }
        Directories child = new Directories();
        child.setData(table);
        child.setLabel(label);
        child.setExpandedIcon(null);
        child.setCollapsedIcon(null);
        child.setType("layer");
        children.add(child);
      }

      return children;
    } catch (Exception e) {
      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        // ResponseHelper.writeError(null, "", 500, new JSONObject().put("Errors",
        // errors));
      } catch (JsonProcessingException ex) {
        java.util.logging.Logger.getLogger(STLayersHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
      throw new Exception();
    }
  }

  private UPFieldsList getRemoteSTColumns(String table) throws Exception {
    String errorMsg = "getUPLayers";
    UPFieldsList columns_labeled = new UPFieldsList();
    columns_labeled.upFields = new ArrayList<>();
    try (
        Connection connection = DatasourceHelper
            .getInstance()
            .getDataSource()
            .getConnection();) {
      ResponseEntity<ArrayList<String>> returns = null;
      RestTemplate restTemplate = new RestTemplate();

      UriComponentsBuilder uriBuilder = UriComponentsBuilder
          .fromHttpUrl("http://" + stwsHost + ":" + stwsPort + "/layers-columns/")
          .queryParam("table", table);

      returns = restTemplate.exchange(
          uriBuilder.toUriString(),
          HttpMethod.GET,
          null,
          new ParameterizedTypeReference<ArrayList<String>>() {
          });

      ArrayList<String> res = returns.getBody();
      for (String col : res) {
        errorMsg += " " + col;
        PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO public.st_tables_fields(st_tables_id, name, label, language)\n" +
                "VALUES ((select id from st_tables where name=? and language=? limit 1), ?, ?, ?)" +
                " on conflict(st_tables_id, name,language) do nothing;");
        statement.setString(1, table);
        statement.setString(2, "english");
        statement.setString(3, col);
        statement.setString(4, col);
        statement.setString(5, "english");
        errorMsg += " " + statement.toString();
        statement.execute();
      }

      PreparedStatement statement2 = connection.prepareStatement(
          "SELECT name, label\n" +
              " FROM public.st_tables_fields\n" +
              " where language=? and st_tables_id=(select id from st_tables where name=? and language=? limit 1); ");
      statement2.setString(1, "english");
      statement2.setString(2, table);
      statement2.setString(3, "english");
      ResultSet columnssLabel = statement2.executeQuery();

      while (columnssLabel.next()) {
        errorMsg += " 2 " + columnssLabel.getString("name");
        boolean is_available = false;
        for (String col : res) {
          if (col.equals(columnssLabel.getString("name"))) {
            is_available = true;
            break;
          }
        }
        if (is_available) {
          UPFields column_labeled = new UPFields();
          column_labeled.name = columnssLabel.getString("name");
          column_labeled.label = columnssLabel.getString("label");
          // column_labeled.f_type=columnssLabel.getString("type");
          columns_labeled.upFields.add(column_labeled);
        }
      }
      return columns_labeled;
    } catch (Exception e) {
      try {
        errors.put(
            JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
        // ResponseHelper.writeError(null, "", 500, new JSONObject().put("Errors",
        // errors));
      } catch (JsonProcessingException ex) {
        java.util.logging.Logger.getLogger(
            STStandardizationMethodHandler.class.getName())
            .log(Level.SEVERE, null, ex);
      }
      throw new Exception();
    }
  }
}

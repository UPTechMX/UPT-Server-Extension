package org.oskari.upt.up;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.io.IOUtils;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.control.userlayer.UserLayerHandlerHelper;
import org.oskari.upt.PostStatus;
import org.oskari.upt.UPTDataCleanHandler;
import org.oskari.upt.st.STDistanceEvaluationHandler;
import org.oskari.upt.st.STLayersHandler;
import org.oskari.geojson.GeoJSONReader2;
import org.oskari.geojson.GeoJSONSchemaDetector;
import org.oskari.log.AuditLog;
import org.oskari.map.userlayer.service.UserLayerDataService;
import org.oskari.map.userlayer.service.UserLayerDbService;
import org.oskari.map.userlayer.service.UserLayerDbServiceMybatisImpl;
import org.oskari.map.userlayer.service.UserLayerException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.layer.AbstractLayerAdminHandler;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.domain.map.UserDataStyle;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.domain.map.userlayer.UserLayerData;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.oskari.upt.UPTRoles;

@OskariActionRoute("scenario_buffers")
public class UPBuffersHandler extends AbstractLayerAdminHandler {

    private static final Logger LOG = LogFactory.getLogger(UPBuffersHandler.class);

    private static final String PROPERTY_USERLAYER_MAX_FILE_SIZE_MB = "userlayer.max.filesize.mb";
    private static final String PROPERTY_TARGET_EPSG = "oskari.native.srs";

    private static final String KEY_NAME = "name";
    private static final String KEY_DESC = "description";
    private static final String KEY_SOURCE = "source";
    private static final String KEY_STYLE = "style";

    private static final int KB = 1024 * 1024;
    private static final int MB = 1024 * KB;

    // Store files smaller than 128kb in memory instead of writing them to disk
    private static final int MAX_SIZE_MEMORY = 128 * KB;
    private static final String PARAM_SOURCE_EPSG_KEY = "crs";
    private final String targetEPSG = PropertyUtil.get(PROPERTY_TARGET_EPSG, "EPSG:4326");

    private final DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory(MAX_SIZE_MEMORY, null);
    private final int userlayerMaxFileSize = PropertyUtil.getOptional(PROPERTY_USERLAYER_MAX_FILE_SIZE_MB, 10) * MB;

    private UserLayerDbService userLayerService;

    private JSONArray errors;
    private ObjectMapper Obj;

    private static String upwsHost;
    private static String upwsPort;
    private static String upProjection;

    private static String upURL;
    private static String upUser;
    private static String upPassword;

    // attributes for layer creation
    private String mapSrs;
    private String name;
    private String desc;
    private String source;
    private String uuid;
    private String sourceEPSG;
    private String geojson_in;
    private String ip;
    private User user;
    private ArrayList<UserLayer> layers_store;
    private JSONArray layers_saved;

    public void setUserLayerService(UserLayerDbService userLayerService) {
        this.userLayerService = userLayerService;
    }

    @Override
    public void init() {
        errors = new JSONArray();

        upwsHost = PropertyUtil.get("upws.db.host");
        upwsPort = PropertyUtil.get("upws.db.port");
        upProjection = PropertyUtil.get("oskari.native.srs")
                .substring(PropertyUtil.get("oskari.native.srs").indexOf(":") + 1);

        upURL = PropertyUtil.get("db.url");
        upUser = PropertyUtil.get("db.username");
        upPassword = PropertyUtil.get("db.password");

        layers_store = new ArrayList<>();

        Obj = new ObjectMapper();
        if (userLayerService == null) {
            userLayerService = new UserLayerDbServiceMybatisImpl();
        }
    }

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        ResponseEntity<List<UPBuffers>> returns = null;
        try (Connection connection = DriverManager.getConnection(upURL, upUser, upPassword);) {
            params.requireLoggedInUser();
            ArrayList<String> roles = new UPTRoles().handleGet(params, params.getUser());
            if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
                throw new Exception("User privilege is not enough for this action");
            }

            String[] scenarios = params.getRequest().getParameterValues("scenariosId");
            Long user_id = params.getUser().getId();
            for (String scenario : scenarios) {
                Map<String, String> param = new HashMap<String, String>();
                param.put("scenario", scenario);
                param.put("user", user_id.toString());
                param.put("projection", upProjection);

                UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(
                        "http://" + upwsHost + ":" + upwsPort + "/scenario_evaluation/{scenario}/{user}/{projection}");

                RestTemplate restTemplate = new RestTemplate();
                returns = restTemplate.exchange(

                        uriBuilder.buildAndExpand(param).toUri(),
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<List<UPBuffers>>() {
                        });
                List<UPBuffers> response = returns.getBody();

                String Layer_name = "";
                PreparedStatement statement1 = connection
                        .prepareStatement("select name from up_scenario where id=? limit 1");
                statement1.setInt(1, Integer.parseInt(scenario));
                ResultSet layers_name = statement1.executeQuery();
                LOG.error(statement1.toString());
                if (layers_name.next()) {
                    Layer_name = layers_name.getString("name");
                    for (UPBuffers index : response) {
                        LOG.error(Obj.writeValueAsString(index));
                        mapSrs = "EPSG:" + upProjection;
                        name = Layer_name + "_" + index.name;
                        desc = "This layer was created by Urban Performance for the scenario " + Layer_name;
                        source = "Urban Performance";
                        uuid = params.getUser().getUuid();
                        sourceEPSG = "EPSG:" + upProjection;
                        geojson_in = index.buffer;
                        ip = params.getClientIp();
                        user = params.getUser();
                        this.handleLayerCreation(params);
                    }
                    this.registerLayers(Integer.parseInt(scenario));
                }
            }
            this.writeResponse(params);
        } catch (Exception e) {
            LOG.error(e, e.getMessage());
            try {
                errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
                errors.put(
                        JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.getMessage()))));
                ResponseHelper.writeError(params, "", 500, new JSONObject().put("Errors", errors));
            } catch (JsonProcessingException | JSONException ex) {
                java.util.logging.Logger.getLogger(STDistanceEvaluationHandler.class.getName()).log(Level.SEVERE, null,
                        ex);
            }
        }

    }

    public void handleLayerCreation(ActionParameters params) throws ActionException {

        Set<String> validFiles = new HashSet<>();
        FileItem jsonfile = null;

        try {
            CoordinateReferenceSystem sourceCRS = decodeCRS(sourceEPSG);
            CoordinateReferenceSystem targetCRS = decodeCRS(targetEPSG);

            ObjectMapper om = new ObjectMapper();
            TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String, Object>>() {
            };
            Map<String, Object> geojson = null;

            // read geoJson string from parameter instead of a file
            InputStream targetStream = null;

            targetStream = IOUtils.toInputStream(geojson_in);

            try (InputStream in = targetStream) {
                geojson = om.readValue(in, typeRef);
                if (geojson.containsKey("0")) {

                    geojson = (Map<String, Object>) geojson.get("0");
                    ArrayList tmpArr = (ArrayList) geojson.get("features");
                    if (tmpArr.size() > 0) {
                        Map<String, Object> tmp = (Map<String, Object>) tmpArr.get(0);
                        tmp = (Map<String, Object>) tmp.get("properties");

                        if (!(tmp.get("value") instanceof Double)) {
                            if (tmp.get("value") instanceof Integer) {
                                tmp.put("value", new Double((Integer) tmp.get("value")));
                            }
                        }
                    }

                }
            } catch (Exception e) {
                errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
                LOG.error("Failed reading the input stream " + e.toString());
                throw new ActionException("Found an error");
            }

            try {
                CoordinateReferenceSystem crs = CRS.decode("EPSG:3857");
                SimpleFeatureType schema = GeoJSONSchemaDetector.getSchema(geojson, crs);

                SimpleFeatureCollection original = GeoJSONReader2.toFeatureCollection(geojson, schema);

                try (SimpleFeatureIterator it = original.features()) {
                    while (it.hasNext()) {
                        SimpleFeature feature = it.next();
                        LOG.info(feature.getID() + " " + feature.getAttribute("value") + " de "
                                + feature.getAttributeCount());

                    }
                }

                UserLayer userLayer = store(original);

                AuditLog.user(ip, user)
                        .withParam("filename", name)
                        .withParam("id", userLayer.getId())
                        .added(AuditLog.ResourceType.USERLAYER);

                layers_store.add(userLayer);
            } catch (Exception ex) {
                LOG.error("Error while storing json layer... ", ex);
                throw new ActionException("Found an error");
            }

            UPTDataCleanHandler cleanner = new UPTDataCleanHandler();
            cleanner.handleGet(params);
        } catch (UserLayerException e) {
            if (!validFiles.isEmpty()) { // avoid to override with empty list
                e.addContent(UserLayerException.InfoType.FILES, validFiles);
            }
            LOG.error("User uuid:", uuid,
                    "zip:", jsonfile == null ? "no file" : name,
                    "info:", e.getOptions().toString());

            AuditLog.user(ip, user)
                    .withParam("filename", name)
                    .withMsg(e.getMessage())
                    .errored(AuditLog.ResourceType.USERLAYER);

            throw new ActionParamsException(e.getMessage(), e.getOptions());
        } catch (ActionException e) {
            LOG.error("User uuid:", uuid,
                    "zip:", jsonfile == null ? "no file" : name,
                    "files found (" + validFiles.size() + ") including:",
                    validFiles.stream().collect(Collectors.joining(",")));
            try {
                errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
            } catch (JsonProcessingException ex) {
                java.util.logging.Logger.getLogger(UPBuffersHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            throw e;
        } catch (Exception e) {
            try {
                errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
                errors.put(
                        JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.getMessage()))));
            } catch (JsonProcessingException ex) {
                java.util.logging.Logger.getLogger(STLayersHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        } finally {
        }
    }

    private CoordinateReferenceSystem decodeCRS(String epsg) throws UserLayerException {
        try {
            return epsg == null ? null : CRS.decode(epsg);
        } catch (Exception e) {
            throw new UserLayerException("Failed to decode CoordinateReferenceSystem from " + epsg,
                    UserLayerException.ErrorType.INVALID_EPSG);
        }
    }

    private Map<String, String> getFormParams(List<FileItem> fileItems) {
        return fileItems.stream()
                .filter(f -> f.isFormField())
                .collect(Collectors.toMap(
                        f -> f.getFieldName(),
                        f -> new String(f.get(), StandardCharsets.UTF_8)));
    }

    private UserLayer store(SimpleFeatureCollection fc)
            throws UserLayerException, ActionException {

        // userLayer.setStyle(createUserLayerStyle());
        JSONObject jsonStyle = createUserLayerStyle().parseUserLayerStyleToOskariJSON();
        String style = jsonStyle.toString();

        UserLayer userLayer = createUserLayer(fc, style);

        List<UserLayerData> userLayerDataList = UserLayerDataService.createUserLayerData(fc, uuid);

        userLayer.setFeatures_count(userLayerDataList.size());

        userLayer.setFeatures_skipped(fc.size() - userLayerDataList.size());

        userLayerService.insertUserLayerAndData(userLayer, userLayerDataList);

        return userLayer;
    }

    private UserLayer createUserLayer(SimpleFeatureCollection fc, String style) throws ActionException {
        return UserLayerDataService.createUserLayer(fc, uuid, name, desc, source, style);
    }

    private UserDataStyle createUserLayerStyle()
            throws UserLayerException, ActionParamsException {
        final UserDataStyle style = new UserDataStyle();
        style.initDefaultStyle();
        return style;
    }

    private void writeResponse(ActionParameters params) {

        layers_saved = new JSONArray();
        for (UserLayer ulayer : layers_store) {
            JSONObject userLayer = UserLayerDataService.parseUserLayer2JSON(ulayer, mapSrs);

            JSONHelper.putValue(userLayer, "featuresCount", ulayer.getFeatures_count());
            JSONObject permissions = UserLayerHandlerHelper.getPermissions();
            JSONHelper.putValue(userLayer, "permissions", permissions);
            // add warning if features were skipped
            if (ulayer.getFeatures_skipped() > 0) {
                JSONObject featuresSkipped = new JSONObject();
                JSONHelper.putValue(featuresSkipped, "featuresSkipped", ulayer.getFeatures_skipped());
                JSONHelper.putValue(userLayer, "warning", featuresSkipped);
            }
            layers_saved.put(userLayer);
        }

        ResponseHelper.writeResponse(params, layers_saved);
    }

    private void registerLayers(Integer scenario) {
        // save relation
        try (Connection connection = DriverManager.getConnection(upURL, upUser, upPassword);) {
            // connection.setAutoCommit(false);
            for (UserLayer ulayer : layers_store) {
                PreparedStatement statement = connection.prepareStatement(
                        "insert into up_scenario_buffers(scenario,user_layer_id) values(?,?) on conflict(scenario,user_layer_id) do nothing;");
                statement.setInt(1, scenario);
                statement.setLong(2, ulayer.getId());
                LOG.debug(statement.toString(), ulayer);
                statement.execute();
            }
        } catch (Exception e) {
            LOG.error(e);
            try {
                errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
                errors.put(
                        JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.getMessage()))));
            } catch (JsonProcessingException ex) {
                java.util.logging.Logger.getLogger(UPBuffersHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}

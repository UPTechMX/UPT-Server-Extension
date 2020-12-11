package org.oskari.example.st;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONObject;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.oskari.control.userlayer.UserLayerHandlerHelper;
import org.oskari.example.PostStatus;
import org.oskari.example.UPTDataCleanHandler;
import org.oskari.geojson.GeoJSONReader2;
import org.oskari.geojson.GeoJSONSchemaDetector;
import org.oskari.log.AuditLog;
import org.oskari.map.userlayer.service.UserLayerDataService;
import org.oskari.map.userlayer.service.UserLayerDbService;
import org.oskari.map.userlayer.service.UserLayerDbServiceMybatisImpl;
import org.oskari.map.userlayer.service.UserLayerException;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionConstants;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.layer.AbstractLayerAdminHandler;
import fi.nls.oskari.domain.map.UserDataStyle;
import fi.nls.oskari.domain.map.userlayer.UserLayer;
import fi.nls.oskari.domain.map.userlayer.UserLayerData;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import org.oskari.example.UPTRoles;



@OskariActionRoute("st_store_heatmap")
public class STGeoJsonHandler extends AbstractLayerAdminHandler {

    private static final Logger LOG = LogFactory.getLogger(STLayerSave.class);

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

    public void setUserLayerService(UserLayerDbService userLayerService) {
        this.userLayerService = userLayerService;
    }

    @Override
    public void init() {
        errors = new JSONArray();
        Obj = new ObjectMapper();
        if (userLayerService == null) {
            userLayerService = new UserLayerDbServiceMybatisImpl();
        }
    }

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
        ResponseHelper.writeResponse(params, "json servcie action.... should be a post with a json file... " + params.getUser().getFullName());

    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {


        String sourceEPSG = params.getHttpParam(PARAM_SOURCE_EPSG_KEY);
        
        SimpleFeatureCollection fc;
        Map<String, String> formParams;
        Set<String> validFiles = new HashSet<>();
        FileItem jsonfile = null;

        try {
            params.requireLoggedInUser();
            ArrayList<String> roles = new UPTRoles().handleGet(params,params.getUser());
            if (!roles.contains("uptadmin") && !roles.contains("uptuser") ){
                throw new Exception("User privilege is not enough for this action");
            }
            
            CoordinateReferenceSystem sourceCRS = decodeCRS(sourceEPSG);
            CoordinateReferenceSystem targetCRS = decodeCRS(targetEPSG);

            ObjectMapper om = new ObjectMapper();
            TypeReference<Map<String, Object>> typeRef = new TypeReference<Map<String,Object>>() {};
            Map<String, Object> geojson = null;

            //read geoJson string from parameter instead of a file 
            String initialString = "text";
            InputStream targetStream = null;
            targetStream = IOUtils.toInputStream(params.getRequiredParam("geojson"));
            
            //try (InputStream in = jsonfile.getInputStream()) {
            try (InputStream in = targetStream) {
                geojson = om.readValue(in, typeRef);
                if(geojson.containsKey("0")){
                    geojson = (Map<String, Object>)geojson.get("0");
                    ArrayList tmpArr = (ArrayList) geojson.get("features");
                    if(tmpArr.size() > 0) {
                        Map<String, Object> tmp = (Map<String, Object>)tmpArr.get(0);
                        tmp = (Map<String, Object>) tmp.get("properties");

                        
                        if(!(tmp.get("value") instanceof Double)){
                            if( tmp.get("value") instanceof Integer)
                                tmp.put("value", new Double((Integer)tmp.get("value")));
                        }
                    }
                }
            }catch(IOException ioex){
                LOG.error("Failed reading the input stream");
            }

            try{
            CoordinateReferenceSystem crs = CRS.decode("EPSG:3857");
            SimpleFeatureType schema = GeoJSONSchemaDetector.getSchema(geojson, crs);

            SimpleFeatureCollection original = GeoJSONReader2.toFeatureCollection(geojson, schema);


                try (SimpleFeatureIterator it = original.features()) {
                    while (it.hasNext()) {
                        SimpleFeature feature = it.next();

                    }
                }

            UserLayer userLayer = store(original, params.getUser().getUuid(), params);

            AuditLog.user(params.getClientIp(), params.getUser())
                    .withParam("filename", params.getRequiredParam("name"))
                    .withParam("id", userLayer.getId())
                    .added(AuditLog.ResourceType.USERLAYER);

                writeResponse(params, userLayer);

            } catch(Exception ex){
                LOG.error("Error while storing json layer... ", ex);
            }

            UPTDataCleanHandler cleanner = new UPTDataCleanHandler();
            cleanner.handleGet(params);
        } catch (UserLayerException e) {
            if (!validFiles.isEmpty()){ // avoid to override with empty list
                e.addContent(UserLayerException.InfoType.FILES, validFiles);
            }
            LOG.error("User uuid:", params.getUser().getUuid(),
                    "zip:", jsonfile == null ? "no file" : params.getRequiredParam("name"),
                    "info:", e.getOptions().toString());

            AuditLog.user(params.getClientIp(), params.getUser())
                    .withParam("filename", params.getRequiredParam("name"))
                    .withMsg(e.getMessage())
                    .errored(AuditLog.ResourceType.USERLAYER);

            throw new ActionParamsException(e.getMessage(), e.getOptions());
        } catch (ActionException e) {
            LOG.error("User uuid:", params.getUser().getUuid(),
                    "zip:", jsonfile == null ? "no file" : params.getRequiredParam("name"),
                    "files found ("+ validFiles.size() + ") including:",
                    validFiles.stream().collect(Collectors.joining(",")));
            throw e;
        } catch(Exception e){
            try {
                errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.toString()))));
                errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error", e.getMessage()))));
            } catch (JsonProcessingException ex) {
                java.util.logging.Logger.getLogger(STLayersHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        finally{
        }
    }


    private List<FileItem> getFileItems(HttpServletRequest request) throws ActionException {
        try {
            request.setCharacterEncoding("UTF-8");
            ServletFileUpload upload = new ServletFileUpload(diskFileItemFactory);
            upload.setSizeMax(userlayerMaxFileSize);
            return upload.parseRequest(request);
        } catch (UnsupportedEncodingException | FileUploadException e) {
            throw new ActionException("Failed to read request", e);
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

     private UserLayer store(SimpleFeatureCollection fc, String uuid, ActionParameters formParams)
            throws UserLayerException, ActionException {
            UserLayer userLayer = createUserLayer(fc, uuid, formParams);

            userLayer.setStyle(createUserLayerStyle(formParams));

            List<UserLayerData> userLayerDataList = UserLayerDataService.createUserLayerData(fc, uuid);

            userLayer.setFeatures_count(userLayerDataList.size());

            userLayer.setFeatures_skipped(fc.size() - userLayerDataList.size());

            userLayerService.insertUserLayer(userLayer, userLayerDataList);

            return userLayer;
    }

    private UserLayer createUserLayer(SimpleFeatureCollection fc, String uuid, ActionParameters params) throws ActionException {
        String name = params.getRequiredParam(KEY_NAME);
        String desc = params.getRequiredParam(KEY_DESC);
        String source = params.getRequiredParam(KEY_SOURCE);
        return UserLayerDataService.createUserLayer(fc, uuid, name, desc, source);
    }

    private UserDataStyle createUserLayerStyle(ActionParameters params)
            throws UserLayerException, ActionParamsException {
        final UserDataStyle style = new UserDataStyle();
        style.initDefaultStyle();
        return style;
    }

    private void writeResponse(ActionParameters params, UserLayer ulayer) {
        String mapSrs = params.getHttpParam(ActionConstants.PARAM_SRS);
        JSONObject userLayer = UserLayerDataService.parseUserLayer2JSON(ulayer, mapSrs);

        JSONHelper.putValue(userLayer, "featuresCount", ulayer.getFeatures_count());
        JSONObject permissions = UserLayerHandlerHelper.getPermissions();
        JSONHelper.putValue(userLayer, "permissions", permissions);
        //add warning if features were skipped
        if (ulayer.getFeatures_skipped() > 0) {
            JSONObject featuresSkipped = new JSONObject();
            JSONHelper.putValue(featuresSkipped, "featuresSkipped", ulayer.getFeatures_skipped());
            JSONHelper.putValue(userLayer, "warning", featuresSkipped);
        }
        ResponseHelper.writeResponse(params, userLayer);
    }


}

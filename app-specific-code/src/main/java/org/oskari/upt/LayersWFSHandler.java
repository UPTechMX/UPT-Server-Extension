package org.oskari.upt;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.locationtech.jts.geom.Envelope;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.referencing.CRS;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.springframework.http.ResponseEntity;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.map.OskariLayer;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;

/**
 * Dummy Rest action route
 */
@OskariActionRoute("LayersWfsHandler")
public class LayersWFSHandler extends RestActionHandler {
    private UPTGetWFSFeaturesHandler handler;
    private static String upURL;
    private static String upUser;
    private static String upPassword;

    private static String upwsHost;
    private static String upwsPort;
    private static String upProjection;

    private JSONArray errors;
    private ObjectMapper Obj;

    private static final Logger log = LogFactory.getLogger(LayersWFSHandler.class);

    /**
     *
     * @param params
     * @throws ActionException
     */
    @Override
    public void preProcess(ActionParameters params) throws ActionException {
        // common method called for all request methods
        log.info(params.getUser(), "accessing route", getName());
        PropertyUtil.loadProperties("/oskari-ext.properties");
        upURL = PropertyUtil.get("db.url");
        upUser = PropertyUtil.get("db.username");
        upPassword = PropertyUtil.get("up.db.password");
        upProjection = PropertyUtil.get("oskari.native.srs")
                .substring(PropertyUtil.get("oskari.native.srs").indexOf(":") + 1);

        errors = new JSONArray();
        Obj = new ObjectMapper();
    }

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        String errorMsg = "Layers WFS capsus get ";
        Data tree = new Data();
        ArrayList<Directories> directories = new ArrayList<>();
        Long user_id = params.getUser().getId();
        try {
            params.requireLoggedInUser();
            ArrayList<String> roles = new UPTRoles().handleGet(params, params.getUser());
            if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
                throw new Exception("User privilege is not enough for this action");
            }

            if ("list_layers".equals(params.getRequiredParam("action"))) {
                Directories dir = new Directories();
                dir.setData("my_wfs");
                dir.setLabel("WFS");
                dir.setIcon(null);
                directories.add(dir);
                ArrayList<Directories> layers = getWfsLayers();
                dir.setChildren(layers);
                JSONArray out = new JSONArray();
                for (Directories index : directories) {
                    final JSONObject json = JSONHelper.createJSONObject(Obj.writeValueAsString(index));
                    out.put(json);
                }

                tree.setData(directories);
                final JSONObject outs = JSONHelper.createJSONObject(Obj.writeValueAsString(tree));

                ResponseHelper.writeResponse(params, outs);
            } else if ("list_columns".equals(params.getRequiredParam("action"))) {
                Layers layers = new Layers();
                layers.setColumns(getWfsColumns(Integer.valueOf(params.getRequiredParam("layer_name"))));
                final JSONObject json = JSONHelper.createJSONObject(Obj.writeValueAsString(layers));
                ResponseHelper.writeResponse(params, json);
            }
        } catch (Exception e) {
            try {
                errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error ", e.toString()))));
                errors.put(
                        JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Detail ", e.getMessage()))));
                ResponseHelper.writeError(params, "", 500, new JSONObject().put("Errors", errors));
            } catch (JsonProcessingException ex) {
                java.util.logging.Logger.getLogger(LayersWFSHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (JSONException ex) {
                java.util.logging.Logger.getLogger(LayersWFSHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            errorMsg = errorMsg + e.getMessage();
            log.error(e, errorMsg);
        }
    }

    private ArrayList<Directories> getWfsLayers() throws Exception {
        String errorMsg = "getWfsLayers";
        ArrayList<Directories> children = new ArrayList<Directories>();
        try (
                // idealmente tendríamos que traer la conexión de oskari,a menos que se
                // garantice que son la misma
                Connection connection = DriverManager.getConnection(upURL, upUser, upPassword)) {
            // Idealmente hemos insertado a CAPSUS como data provider, buscamos su id y
            // limitamos a ese id:
            // PreparedStatement statement = connection.prepareStatement("select id, name,
            // url from oskari_maplayer WHERE dataprovider_id=ID_CAPSUS AND type LIKE
            // 'wfslayer' ;",
            PreparedStatement statement = connection
                    .prepareStatement("select id, name, url from oskari_maplayer WHERE type LIKE 'wfslayer' ;");
            boolean status = statement.execute();
            if (status) {
                ResultSet data = statement.getResultSet();
                while (data.next()) {
                    log.info(data.getString("url"));
                    Directories child = new Directories();
                    child.setData(data.getString("id"));
                    child.setLabel(data.getString("name"));
                    child.setExpandedIcon(null);
                    child.setCollapsedIcon(null);
                    child.setType("layer");
                    children.add(child);
                }
            }
        } catch (Exception e) {
            try {
                errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error ", e.toString()))));
                errors.put(
                        JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Detail ", e.getMessage()))));
            } catch (JsonProcessingException ex) {
                java.util.logging.Logger.getLogger(LayersWFSHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            errorMsg = errorMsg + e.toString();
            log.error(e, errorMsg);
            throw new Exception();
        }
        return children;
    }

    private ArrayList<String> getWfsColumns(Integer id) throws Exception {
        String errorMsg = "getWfsColumns";

        ArrayList<String> res = null;
        ResponseEntity<WFSResult> tmp;

        try (Connection connection = DriverManager.getConnection(upURL, upUser, upPassword)) {
            PreparedStatement pst = connection.prepareStatement(
                    "select name, url,version from oskari_maplayer WHERE type LIKE 'wfslayer' AND id=? ;");
            pst.setInt(1, id);
            boolean status = pst.execute();
            log.info("pst status : " + status);
            if (!status) {
                log.info("[capsus.LayersWFSHandler]: Error while connecting to the oskari db.");
                return null;
            }
            ResultSet data = pst.getResultSet();
            data.next();
            log.info("el nombrees ::: " + data.getString("name"));

            res = columns_reader(data.getString("url"), id.toString(), data.getString("name"));

        } catch (Exception e) {
            try {
                errors.put(JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Error ", e.toString()))));
                errors.put(
                        JSONHelper.createJSONObject(Obj.writeValueAsString(new PostStatus("Detail ", e.getMessage()))));
            } catch (JsonProcessingException ex) {
                java.util.logging.Logger.getLogger(LayersWFSHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            errorMsg = errorMsg + e.toString();
            log.info(e, errorMsg);
            log.error(e, errorMsg);
            throw new Exception();
        }
        return res;
    }

    private ArrayList<String> columns_reader(String url, String id, String name) throws Exception {
        log.info(url, id, name);
        handler = new UPTGetWFSFeaturesHandler();
        handler.init();
        OskariLayer layer = new OskariLayer();
        layer.setId(Integer.parseInt(id));
        layer.setType(OskariLayer.TYPE_WFS);
        layer.setUrl(url);
        layer.setName(name);
        CoordinateReferenceSystem webMercator = CRS.decode("EPSG:3857", true);
        PropertyUtil.addProperty("oskari.native.srs", "EPSG:" + upProjection, true);
        Envelope envelope = new Envelope(-13149614.848125, 4383204.949375, -12523442.7125, 5009377.085);
        ReferencedEnvelope bbox = new ReferencedEnvelope(
                -13149614.848125,
                4383204.949375,
                -12523442.7125,
                5009377.085,
                webMercator);

        SimpleFeatureCollection sfc = handler.featureClient.getFeatures(id, layer, bbox, webMercator, Optional.empty());

        List<AttributeDescriptor> columns = sfc.getSchema().getAttributeDescriptors();

        ArrayList<String> columns_list = new ArrayList<>();

        for (AttributeDescriptor column : columns) {
            columns_list.add(column.getLocalName());
        }
        return columns_list;
    }
}

class WFSResult {

    public String elementFormDefault;
    public String targetNamespace;
    public String targetPrefix;
    public FeatureType[] featureTypes;

    public WFSResult() {
    }

    public FeatureType[] getFeatureTypes() {
        return featureTypes;
    }

    public void setFeatureTypes(FeatureType[] featureTypes) {
        this.featureTypes = featureTypes;
    }

    public ArrayList<String> getColumns() {
        ArrayList<String> ret = new ArrayList();
        Property[] tmp;
        for (FeatureType featureType : featureTypes) {
            tmp = featureType.getProperties();
            for (Property tmp1 : tmp) {
                ret.add(tmp1.getName());
            }
        }
        return ret;
    }
}

class FeatureType {

    public String typeName;
    public Property[] properties;

    public FeatureType() {
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public void setProperties(Property[] properties) {
        this.properties = properties;
    }

    public String getTypeName() {
        return typeName;
    }

    public Property[] getProperties() {
        return properties;
    }

}

class Property {

    public String name;
    public String maxOccurs;
    public String minOccurs;
    public String nillable;
    public String type;
    public String localType;

    public Property() {
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setmaxOccurs(String maxOccurs) {
        this.maxOccurs = maxOccurs;
    }

    public void setminOccurs(String minOccurs) {
        this.minOccurs = minOccurs;
    }

    public void setnillable(String nillable) {
        this.nillable = nillable;
    }

    public void settype(String type) {
        this.type = type;
    }

    public void setlocalType(String localType) {
        this.localType = localType;
    }

    public String getName() {
        return name;
    }

    public String getmaxOccurs() {
        return maxOccurs;
    }

    public String getminOccurs() {
        return minOccurs;
    }

    public String getnillable() {
        return nillable;
    }

    public String gettype() {
        return type;
    }

    public String getlocalType() {
        return localType;
    }

}

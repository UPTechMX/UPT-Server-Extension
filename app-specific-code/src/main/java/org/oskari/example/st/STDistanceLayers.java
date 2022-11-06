package org.oskari.example.st;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONObject;
import org.oskari.example.Data;
import org.oskari.example.Directories;
import org.oskari.example.Layers;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("list_suitability_layers")

public class STDistanceLayers extends RestActionHandler {

    private static String stURL;
    private static String stUser;
    private static String stPassword;

    private static String stwsHost;
    private static String stwsPort;
    private static String stProjection;
    private Long user_id;

    Map<Integer, STSettings> stLayers;

    private static final Logger log = LogFactory.getLogger(LayersSTHandler.class);

    @Override
    public void preProcess(ActionParameters params) throws ActionException {
        // common method called for all request methods
        log.info(params.getUser(), "accessing route", getName());
        PropertyUtil.loadProperties("/oskari-ext.properties");
        stURL = PropertyUtil.get("db.url");
        stUser = PropertyUtil.get("db.username");
        stPassword = PropertyUtil.get("db.password");

        stwsHost = PropertyUtil.get("stws.db.host");
        stwsPort = PropertyUtil.get("stws.db.port");
        stProjection = PropertyUtil.get("oskari.native.srs")
                .substring(PropertyUtil.get("oskari.native.srs").indexOf(":") + 1);
        user_id = params.getUser().getId();
    }

    @Override
    public void handleGet(ActionParameters params) throws ActionException {

        ArrayList<Directories> directories = new ArrayList<Directories>();
        Data tree = new Data();
        String errorMsg = "Oskari Layers get ";
        String table = "";
        table += params.getHttpParam("table") == null ? "" : params.getHttpParam("table");
        try {
            params.requireLoggedInUser();
            // Get directories
            Directories dir = new Directories();
            dir.setData("scenario");
            dir.setLabel("scenario");
            dir.setIcon(null);
            directories.add(dir);

            JSONArray out = new JSONArray();
            if ("".equals(table)) {
                ArrayList<Directories> layers = getRemoteSTTables();
                dir.setChildren(layers);
                for (Directories index : layers) {
                    // Convert to Json Object
                    ObjectMapper Obj = new ObjectMapper();
                    JSONObject json = JSONHelper.createJSONObject(Obj.writeValueAsString(index));
                    out.put(json);
                }
                ResponseHelper.writeResponse(params, out);
            } else {
                Layers layers = new Layers();
                layers.setColumns(getRemoteSTColumns(table));
                ObjectMapper Obj = new ObjectMapper();
                JSONObject json = JSONHelper.createJSONObject(Obj.writeValueAsString(layers));
                ResponseHelper.writeResponse(params, json);
            }

        } catch (Exception e) {
            errorMsg = errorMsg + e.getMessage() + "table:" + table;
            log.error(e, errorMsg);
        }
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
    }

    @Override
    public void handleDelete(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
    }

    @Override
    public void handlePut(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
    }

    private ArrayList<Directories> getRemoteSTTables() {
        String errorMsg = "getUPLayers";
        ArrayList<Directories> children = new ArrayList<Directories>();

        ResponseEntity<List<String>> returns = null;
        RestTemplate restTemplate = new RestTemplate();

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromHttpUrl("http://" + stwsHost + ":" + stwsPort + "/tables/");

        returns = restTemplate.exchange(
                uriBuilder.toUriString(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<String>>() {
                });

        List<String> res = returns.getBody();

        for (String table : res) {
            Directories child = new Directories();
            child.setData(table);
            child.setLabel(table);
            child.setExpandedIcon(null);
            child.setCollapsedIcon(null);
            child.setType("layer");
            children.add(child);
        }
        return children;
    }

    private ArrayList<String> getRemoteSTColumns(String table) {
        String errorMsg = "getUPLayers";
        ResponseEntity<ArrayList<String>> returns = null;
        RestTemplate restTemplate = new RestTemplate();

        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromHttpUrl("http://" + stwsHost + ":" + stwsPort + "/tables-columns/")
                .queryParam("table", table);

        returns = restTemplate.exchange(
                uriBuilder.toUriString(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<ArrayList<String>>() {
                });
        ArrayList<String> res = returns.getBody();
        return res;
    }
}

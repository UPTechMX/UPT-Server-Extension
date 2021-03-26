package org.oskari.example.up;

import org.oskari.example.*;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONObject;
import org.oskari.example.up.UPFields;
import org.oskari.example.up.UPFieldsList;
import org.oskari.example.up.UPJobs;
import org.oskari.example.up.UPRoads;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;


@OskariActionRoute("LayersUPHandler")
public class LayersUPHandler extends RestActionHandler {

    private static String upURL;
    private static String upUser;
    private static String upPassword;

    private static String upwsHost;
    private static String upwsPort;
    private static String upProjection;
    
    String user_uuid;

    private static final Logger log = LogFactory.getLogger(LayersUPHandler.class);

    public void preProcess(ActionParameters params) throws ActionException {
        // common method called for all request methods
        user_uuid = params.getUser().getUuid();
        log.info(params.getUser(), "accessing route", getName());
        PropertyUtil.loadProperties("/oskari-ext.properties");
        upURL = PropertyUtil.get("up.db.URL");
        upUser = PropertyUtil.get("up.db.user");
        upPassword = PropertyUtil.get("up.db.password");

        upwsHost = PropertyUtil.get("upws.db.host");
        upwsPort = PropertyUtil.get("upws.db.port");
        upProjection = PropertyUtil.get("oskari.native.srs").substring(PropertyUtil.get("oskari.native.srs").indexOf(":") + 1);
    }

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        
        String errorMsg = "Layers UP get ";
        Data tree = new Data();
        ArrayList<Directories> directories = new ArrayList<Directories>();
        Long user_id = params.getUser().getId();
        try {
            params.requireLoggedInUser();
            ArrayList<String> roles = new UPTRoles().handleGet(params,params.getUser());
            if (!roles.contains("uptadmin") && !roles.contains("uptuser") ){
                throw new Exception("User privilege is not enough for this action");
            }
            
            if ("list_directories".equals(params.getRequiredParam("action"))) {
                //Get directories
                Directories dir = new Directories();
                dir.setData("my_data");
                dir.setLabel("My Data");
                dir.setIcon(null);
                directories.add(dir);

                //Get layers
                ArrayList<Directories> layers = getLayers();
                dir.setChildren(layers);

                JSONArray out = new JSONArray();
                for (Directories index : directories) {
                    //Convert to Json Object
                    ObjectMapper Obj = new ObjectMapper();
                    final JSONObject json = JSONHelper.createJSONObject(Obj.writeValueAsString(index));
                    out.put(json);
                }
                ObjectMapper Obj = new ObjectMapper();
                tree.setData(directories);
                final JSONObject outs = JSONHelper.createJSONObject(Obj.writeValueAsString(tree));
                ResponseHelper.writeResponse(params, outs);
            } else if ("list_layers".equals(params.getRequiredParam("action"))) {
                //Get directories
                Directories dir = new Directories();
                dir.setData("my_data");
                dir.setLabel("My Data");
                dir.setIcon(null);
                directories.add(dir);

                //Get layers
                ArrayList<Directories> layers = getLayers();
                dir.setChildren(layers);

                JSONArray out = new JSONArray();
                for (Directories index : directories) {
                    //Convert to Json Object
                    ObjectMapper Obj = new ObjectMapper();
                    final JSONObject json = JSONHelper.createJSONObject(Obj.writeValueAsString(index));
                    out.put(json);
                }
                ObjectMapper Obj = new ObjectMapper();
                tree.setData(directories);
                final JSONObject outs = JSONHelper.createJSONObject(Obj.writeValueAsString(tree));
                
                ResponseHelper.writeResponse(params, outs);
            } else if ("list_up_layers".equals(params.getRequiredParam("action"))) {
                //Get directories
                Directories dir = new Directories();
                dir.setData("scenario");
                dir.setLabel("scenario");
                dir.setIcon(null);
                directories.add(dir);

                //Get layers
                ArrayList<Directories> layers = getUPLayers(user_id, params.getRequiredParam("id"));
                dir.setChildren(layers);

                JSONArray out = new JSONArray();
                for (Directories index : directories) {
                    //Convert to Json Object
                    ObjectMapper Obj = new ObjectMapper();
                    final JSONObject json = JSONHelper.createJSONObject(Obj.writeValueAsString(index));
                    out.put(json);
                }
                ObjectMapper Obj = new ObjectMapper();
                tree.setData(directories);
                final JSONObject outs = JSONHelper.createJSONObject(Obj.writeValueAsString(tree));
                
                ResponseHelper.writeResponse(params, outs);
            } else if ("list_columns".equals(params.getRequiredParam("action"))) {
                Layers layers = new Layers();
                layers.setColumns(getColumns(params.getRequiredParam("layer_id")));
                ObjectMapper Obj = new ObjectMapper();
                final JSONObject json = JSONHelper.createJSONObject(Obj.writeValueAsString(layers));
                ResponseHelper.writeResponse(params, json);
            } else if ("list_up_columns".equals(params.getRequiredParam("action"))) {
                UPFieldsList layers=getUPColumns(params.getRequiredParam("layer_id"));
                ObjectMapper Obj = new ObjectMapper();
                final JSONObject json = JSONHelper.createJSONObject(Obj.writeValueAsString(layers));
                ResponseHelper.writeResponse(params, json);
            }
            if ("list_study_areas".equals(params.getRequiredParam("action"))) {
                //Get directories
                JSONArray out = new JSONArray();
                ArrayList<StudyAreaUP> response = getStudyAreas();
                for (StudyAreaUP index : response) {
                    //Convert to Json Object
                    ObjectMapper Obj = new ObjectMapper();
                    final JSONObject json = JSONHelper.createJSONObject(Obj.writeValueAsString(index));
                    out.put(json);
                }
                ResponseHelper.writeResponse(params, out);
            }
        } catch (Exception e) {
            errorMsg = errorMsg + e.getMessage();
            log.error(e, errorMsg);
        }
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        
        String errorMsg = "Layers UP get ";
        try {
            params.requireLoggedInUser();
            ArrayList<String> roles = new UPTRoles().handleGet(params,params.getUser());
            if (!roles.contains("uptadmin") && !roles.contains("uptuser") ){
                throw new Exception("User privilege is not enough for this action");
            }
            
            PostStatus status = null;
            if ("copy_data".equals(params.getRequiredParam("action"))) {
                if (params.getRequiredParam("layerUPName") != null
                        && params.getRequiredParam("layerName") != null
                        && params.getRequiredParam("tableUP") != null
                        && params.getRequiredParam("table") != null
                        && params.getRequiredParam("scenarioId") != null) {
                    if (null == params.getRequiredParam("layerUPName")) {
                        status = this.getAmenities(params.getRequiredParam("layerUPName"),
                                params.getRequiredParam("layerName"),
                                params.getRequest().getParameterValues("tableUP"),
                                params.getRequest().getParameterValues("table"),
                                params.getRequiredParam("scenarioId"));
                        status.message += params.getRequiredParam("scenarioId");
                    } else {
                        switch (params.getRequiredParam("layerUPName")) {
                            case "mmu":
                                status = this.setMmu(
                                        params.getRequiredParam("layerUPName"),
                                        params.getRequiredParam("layerName"),
                                        params.getRequest().getParameterValues("tableUP"),
                                        params.getRequest().getParameterValues("table"),
                                        params.getRequiredParam("scenarioId")
                                );
                                status.message += params.getRequiredParam("scenarioId");
                                break;
                            case "mmu_info":
                                status = this.getMmuInfo(
                                        params.getRequiredParam("layerUPName"),
                                        params.getRequiredParam("layerName"),
                                        params.getRequest().getParameterValues("tableUP"),
                                        params.getRequest().getParameterValues("table"),
                                        params.getRequiredParam("scenarioId")
                                );
                                status.message += params.getRequiredParam("scenarioId");
                                break;
                            case "transit":
                                status = this.getTransit(params.getRequiredParam("layerUPName"),
                                        params.getRequiredParam("layerName"),
                                        params.getRequest().getParameterValues("tableUP"),
                                        params.getRequest().getParameterValues("table"),
                                        params.getRequiredParam("scenarioId"));
                                status.message += params.getRequiredParam("scenarioId");
                                break;
                            case "roads":
                                status = this.getRoads(params.getRequiredParam("layerUPName"),
                                        params.getRequiredParam("layerName"),
                                        params.getRequest().getParameterValues("tableUP"),
                                        params.getRequest().getParameterValues("table"),
                                        params.getRequiredParam("scenarioId"));
                                status.message += params.getRequiredParam("scenarioId");
                                break;
                            case "jobs":
                                status = this.getJobs(params.getRequiredParam("layerUPName"),
                                        params.getRequiredParam("layerName"),
                                        params.getRequest().getParameterValues("tableUP"),
                                        params.getRequest().getParameterValues("table"),
                                        params.getRequiredParam("scenarioId"));
                                status.message += params.getRequiredParam("scenarioId");
                                break;
                            case "footprint":
                                status = this.getFootprint(params.getRequiredParam("layerUPName"),
                                        params.getRequiredParam("layerName"),
                                        params.getRequest().getParameterValues("tableUP"),
                                        params.getRequest().getParameterValues("table"),
                                        params.getRequiredParam("scenarioId"));
                                status.message += " "+params.getRequiredParam("scenarioId");
                                break;
                            case "risk":
                                status = this.getRisk(params.getRequiredParam("layerUPName"),
                                        params.getRequiredParam("layerName"),
                                        params.getRequest().getParameterValues("tableUP"),
                                        params.getRequest().getParameterValues("table"),
                                        params.getRequiredParam("scenarioId"));
                                status.message += params.getRequiredParam("scenarioId");
                                break;
                            default:
                                status = this.getAmenities(params.getRequiredParam("layerUPName"),
                                        params.getRequiredParam("layerName"),
                                        params.getRequest().getParameterValues("tableUP"),
                                        params.getRequest().getParameterValues("table"),
                                        params.getRequiredParam("scenarioId"));
                                status.message += params.getRequiredParam("scenarioId");
                                break;
                        }
                        
                    }
                }
            }
            ObjectMapper Obj = new ObjectMapper();
            final JSONObject outs;
            errorMsg = Obj.writeValueAsString(status);
            outs = JSONHelper.createJSONObject(Obj.writeValueAsString(status));
            if (status.status.equals("Error")){
                ResponseHelper.writeError(params,Obj.writeValueAsString(status),500);
            }else{
                ResponseHelper.writeResponse(params, outs);
            }
        } catch (Exception e) {
            errorMsg = errorMsg + e.getMessage();
            log.error(e, errorMsg);
        }
    }

    @Override
    public void handlePut(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
        throw new ActionParamsException("Notify there was something wrong with the params");
    }

    @Override
    public void handleDelete(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
        throw new ActionDeniedException("Not deleting anything");
    }

    private void getUserParams(User user, ActionParameters params) throws ActionParamsException {
    }

    private ArrayList<Directories> getLayers() {
        String errorMsg = "getLayers";
        ArrayList<Directories> children = new ArrayList<Directories>();
        try (
                Connection connection = DriverManager.getConnection(
                        upURL,
                        upUser,
                        upPassword);
                PreparedStatement statement = connection.prepareStatement(
                    "with user_layers as(\n" +
                    "    select user_layer.id ,\n" +
                    "    layer_name \n" +
                    "    from user_layer\n" +
                    "    left join upt_user_layer_scope on upt_user_layer_scope.user_layer_id=user_layer.id\n" +
                    "    where (user_layer.uuid=? or upt_user_layer_scope.is_public=1) and lower(layer_name) not like '%buffer%' and lower(layer_name) not like '%distance%'\n" +
                    ")\n" +
                    "select id,layer_name\n" +
                    "from user_layers"
                );) {
                        //"select id,layer_name from user_layer where uuid=? and lower(layer_name) not like '%buffer%' and lower(layer_name) not like '%distance%'");) {
            statement.setString(1, user_uuid);
            
            boolean status = statement.execute();
            if (status) {
                ResultSet data = statement.getResultSet();
                while (data.next()) {
                    Directories child = new Directories();
                    child.setData(data.getString("id"));
                    child.setLabel(data.getString("layer_name"));
                    child.setExpandedIcon(null);
                    child.setCollapsedIcon(null);
                    child.setType("layer");
                    children.add(child);
                }
                return children;
            }
            return children;
        } catch (SQLException e) {
            errorMsg = errorMsg + e.toString();
            log.error(e, errorMsg);
        }
        return children;
    }

    private ArrayList<Directories> getUPLayers(Long user_id, String scenario_id) {
        String errorMsg = "getUPLayers";
        ArrayList<Directories> children = new ArrayList<Directories>();
        try (
                Connection connection = DriverManager.getConnection(
                        upURL,
                        upUser,
                        upPassword);) {
            Statement statement = connection.createStatement();
            ResultSet data = statement.executeQuery("select up_modules_translation.name from up_modules_translation\n"
                    + " inner join up_scenario_modules on up_modules_translation.id=up_scenario_modules.module\n"
                    + " inner join up_scenario on up_scenario.id=up_scenario_modules.scenario\n"
                    + " where up_scenario.id=" + Integer.parseInt(scenario_id));

            List<String> modules = new ArrayList<>();

            while (data.next()) {
                modules.add(data.getString("name"));
            };

            ResponseEntity<List<TableInfo>> returns = null;
            RestTemplate restTemplate = new RestTemplate();

            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl("http://" + upwsHost + ":" + upwsPort + "/layers/")
                    .queryParam("modules", modules)
                    .queryParam("scenario", Integer.parseInt(scenario_id));

            returns = restTemplate.exchange(
                    uriBuilder.toUriString(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<TableInfo>>() {
            }
            );

            List<TableInfo> res = returns.getBody();
            Statement statement2 = connection.createStatement();
            ResultSet tablesLabel = statement2.executeQuery("SELECT distinct name, label\n" +
                "	FROM public.up_layers"+
                "       where language='english' ");

            List<String> tables = new ArrayList<>();
            List<String> labels = new ArrayList<>();

            while (tablesLabel.next()) {
                tables.add(tablesLabel.getString("name"));
                labels.add(tablesLabel.getString("label"));
            };

            for (TableInfo table : res) {
                String label=table.name;
                for(String tab:tables){
                    if(tab.equals(table.name)){
                        label=labels.get(tables.indexOf(tab))+" ("+table.value.intValue()+")";
                        break;
                    }
                }
                Directories child = new Directories();
                child.setData(table.name);
                child.setLabel(label);
                child.setExpandedIcon(null);
                child.setCollapsedIcon(null);
                child.setType("layer");
                children.add(child);
            }
            return children;
        } catch (SQLException e) {
            errorMsg = errorMsg + e.toString();
            log.error(e, errorMsg);
        }
        return children;
    }

    private ArrayList<String> getColumns(String id) {
        String errorMsg = "getLayers";
        ArrayList<String> layers = new ArrayList<String>();
        try (
                Connection connection = DriverManager.getConnection(
                        upURL,
                        upUser,
                        upPassword);) {
            Statement statement = connection.createStatement();

            ResultSet data = statement.executeQuery(
                    "with cols as("
                    + " select id,fields "
                    + " from user_layer "
                    + " where id=" + id
                    + " ) "
                    + " select name "
                    + " from cols,json_populate_recordset(null::record,cols.fields) as(name text) "
                    + " where name !='the_geom' "
                    + " union all "
                    + " select 'geometry';");
            while (data.next()) {
                layers.add(data.getString("name"));
            }
            return layers;
        } catch (SQLException e) {
            errorMsg = errorMsg + e.toString();
            log.error(e, errorMsg);
        }
        return layers;
    }

    private UPFieldsList getUPColumns(String table) {
        String errorMsg = "getUPLayers";
        ArrayList<String> res=null;
        UPFieldsList columns_labeled=new UPFieldsList();
        columns_labeled.upFields = new ArrayList<UPFields>();
        try(Connection connection = DriverManager.getConnection(
                        upURL,
                        upUser,
                        upPassword);){
            ResponseEntity<ArrayList<String>> returns = null;
            RestTemplate restTemplate = new RestTemplate();

            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl("http://" + upwsHost + ":" + upwsPort + "/layer_columns/")
                    .queryParam("table", table);

            returns = restTemplate.exchange(
                    uriBuilder.toUriString(),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<ArrayList<String>>() {}
            );

            res = returns.getBody();
            
            for (String col : res) {
                errorMsg+=" "+col;
                PreparedStatement statement = connection.prepareStatement(
                        "INSERT INTO public.up_layers_fields(up_layers_id, name, label, language)\n" +
                                "VALUES ((select id from up_layers where name=? and language=? limit 1), ?, ?, ?)"
                                + " on conflict(up_layers_id, name,language) do nothing;"
                );
                statement.setString(1, table);
                statement.setString(2, "english");
                statement.setString(3, col);
                statement.setString(4, col);
                statement.setString(5, "english");
                errorMsg+=" "+statement.toString();
                statement.execute();
            }
            
            PreparedStatement statement2 = connection.prepareStatement(
                "SELECT name, label\n" +
                " FROM public.up_layers_fields\n"+ 
                " where language=? and up_layers_id=(select id from up_layers where name=? and language=? limit 1); "
            );
            statement2.setString(1, "english");
            statement2.setString(2, table);
            statement2.setString(3, "english");
            ResultSet columnssLabel = statement2.executeQuery();
            while (columnssLabel.next()) {
                errorMsg+=" 2 "+columnssLabel.getString("name");
                boolean is_available=false;
                for (String col : res) {
                    if (col.equals(columnssLabel.getString("name"))){
                        is_available=true;
                        break;
                    }
                }
                if(is_available){
                    UPFields column_labeled = new UPFields();
                    column_labeled.name=columnssLabel.getString("name");
                    column_labeled.label=columnssLabel.getString("label");
                    //column_labeled.f_type=columnssLabel.getString("type");
                    columns_labeled.upFields.add(column_labeled);
                }
            };
            
        }catch(Exception e){
            errorMsg = errorMsg + e.toString();
            log.error(e, errorMsg);
        }
        return columns_labeled;
    }

    private ArrayList<StudyAreaUP> getStudyAreas() {
        String errorMsg = "getStudyAreas";
        ArrayList<StudyAreaUP> layers = new ArrayList<StudyAreaUP>();
        try (
                Connection connection = DriverManager.getConnection(
                        upURL,
                        upUser,
                        upPassword);) {
            PreparedStatement statement = connection.prepareStatement("with user_layers as(\n" +
                    "    select case when upt_user_layer_scope.id is null then 0 else upt_user_layer_scope.id end as id,\n" +
                    "    layer_name ,\n" +
                    "    case when is_public is null then 0 else is_public end as is_public\n" +
                    "    ,wkt\n" +
                    "    from user_layer\n" +
                    "    left join upt_user_layer_scope on upt_user_layer_scope.user_layer_id=user_layer.id\n" +
                    "    where user_layer.uuid=? or upt_user_layer_scope.is_public=1\n" +
                    ")\n" +
                    "select id,layer_name\n" +
                    "from user_layers\n");
            statement.setString(1, user_uuid);
            ResultSet data = statement.executeQuery();
            while (data.next()) {
                StudyAreaUP child = new StudyAreaUP();
                child.setId("" + data.getInt("id"));
                child.setName(data.getString("layer_name"));
                layers.add(child);
            }
            
            return layers;
        } catch (SQLException e) {
            errorMsg = errorMsg + e.toString();
            log.error(e, errorMsg);
        }
        return layers;
    }

    private PostStatus getFootprint(String layerUP, String layer, String[] tableup, String[] table, String scenarioId) {
        PostStatus postStatus = new PostStatus();
        String values = "";
        String tableUP[] = new String[tableup.length + 1];
        tableUP[tableup.length] = "scenario";
        System.arraycopy(tableup, 0, tableUP, 0, tableup.length);
        for (int i = 0; i < tableUP.length; i++) {
            switch (tableUP[i]) {
                case "name":
                case "value":
                    values += " trim(both '\"' from CAST(property_json->'" + table[i] + "' AS text)) as " + tableUP[i];
                    break;
                case "location":
                    values += " st_astext(st_transform(st_setsrid(" + table[i] + "," + upProjection + "),4326)) as " + tableUP[i];
                    break;
                case "scenario":
                    values += scenarioId + " as "+ tableUP[i];
                    break;
                default:
                    values += " trim(both '\"' from CAST(property_json->'" + table[i] + "' AS text))  as " + tableUP[i];
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
                        upPassword)) {

            Statement statement = connection.createStatement();
            query = "select distinct " + values.replaceAll(",,", ",") + " from user_layer\n"
                    + " inner join user_layer_data on user_layer.id = user_layer_data.user_layer_id\n"
                    + " where user_layer.id=" + layer;
            ResultSet data = statement.executeQuery(query);

            ArrayList<FootprintUP> data_in = new ArrayList<>();

            while (data.next()) {
                Object o = new FootprintUP();
                Class<?> c = o.getClass();
                for (int i = 0; i < tableUP.length; i++) {
                    try {
                        Field f = c.getDeclaredField(tableUP[i]);
                        f.setAccessible(true);
                        if (tableUP[i].equals("scenario")) {
                            Integer val = (Integer) data.getInt(tableUP[i]);
                            f.set(o, val);
                        } else if (tableUP[i].equals("location")) {
                            f.set(o, data.getString(tableUP[i]));
                        } else if (tableUP[i].equals("footprint_id")) {
                        } else if (tableUP[i].equals("name")) {
                            f.set(o, data.getString(tableUP[i]));
                        } else if (tableUP[i].equals("value")) {
                            f.set(o, data.getFloat(tableUP[i]));
                        }
                    } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException | SQLException e) {
                        log.error(e, errorMsg);
                    }
                }
                log.debug(o, "reflection");
                data_in.add((FootprintUP) o);
                //return postStatus;
            }
            Tables<FootprintUP> final_data = new Tables<FootprintUP>(data_in);

            RestTemplate restTemplate = new RestTemplate();
            postStatus = restTemplate.postForObject("http://" + upwsHost + ":" + upwsPort + "/footprint/", final_data, PostStatus.class);
            return postStatus;
        } catch (SQLException ex) {
            for (Throwable e : ex) {
                if (e instanceof SQLException) {

                    e.printStackTrace(System.err);
                    errorMsg += "SQLState: " + ((SQLException) e).getSQLState();

                    errorMsg += "Error Code: " + ((SQLException) e).getErrorCode();

                    errorMsg += "Message: " + e.getMessage();
                }
            }
            log.error(ex, errorMsg + query);
        } catch (Exception e) {
            log.error(e, errorMsg + query);
        }
        postStatus.status = "Error";
        postStatus.message = errorMsg +" "+ query;
        return postStatus;
    }

    private PostStatus getRisk(String layerUP, String layer, String[] tableup, String[] table, String scenarioId) {
        PostStatus postStatus = new PostStatus();
        String values = "";
        String tableUP[] = new String[tableup.length + 2];
        tableUP[tableup.length] = "scenario";
        tableUP[tableup.length+1] = "oskari_code";
        System.arraycopy(tableup, 0, tableUP, 0, tableup.length);
        for (int i = 0; i < tableUP.length; i++) {
            switch (tableUP[i]) {
                case "fclass":
                    values += " trim(both '\"' from CAST(property_json->'" + table[i] + "' AS text)) as " + tableUP[i];
                    break;
                case "location":
                    values += " st_astext(st_transform(st_setsrid(" + table[i] + "," + upProjection + "),4326)) as " + table[i];
                    break;
                case "scenario":
                    values += scenarioId + " as scenario";
                    break;
                case "oskari_code":
                    values += " user_layer_data.id as oskari_code";
                    break;
                default:
                    values += " trim(both '\"' from CAST(property_json->'" + table[i] + "' AS text))  as " + table[i];
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
                        upPassword)) {

            Statement statement = connection.createStatement();
            query = "select distinct " + values.replaceAll(",,", ",") + " from user_layer\n"
                    + " inner join user_layer_data on user_layer.id = user_layer_data.user_layer_id\n"
                    + " where user_layer.id=" + layer;
            ResultSet data = statement.executeQuery(query);

            ArrayList<RiskUP> data_in = new ArrayList<>();

            while (data.next()) {
                Object o = new RiskUP();
                Class<?> c = o.getClass();
                for (int i = 0; i < tableUP.length; i++) {
                    try {
                        Field f = c.getDeclaredField(tableUP[i]);
                        f.setAccessible(true);
                        if (tableUP[i].equals("scenario")) {
                            Integer val = (Integer) data.getInt(tableUP[i]);
                            f.set(o, val);
                        }else if (tableUP[i].equals("oskari_code")) {
                            Long val = (Long) data.getLong(tableUP[i]);
                            f.set(o, val);
                        } else if (tableUP[i].equals("location")) {
                            f.set(o, data.getString(table[i]));
                        } else if (tableUP[i].equals("risk_id")) {
                        } else if (tableUP[i].equals("fclass")) {
                            f.set(o, data.getString(tableUP[i]));
                        } else if (tableUP[i].equals("value")) {
                            f.set(o, data.getFloat(tableUP[i]));
                        } else {
                        }
                    } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException | SQLException e) {
                        log.error(e, errorMsg);
                    }
                }

                log.debug(o, "reflection");
                data_in.add((RiskUP) o);
                //return postStatus;
            }
            Tables<RiskUP> final_data = new Tables<RiskUP>(data_in);

            RestTemplate restTemplate = new RestTemplate();
            postStatus = restTemplate.postForObject("http://" + upwsHost + ":" + upwsPort + "/risk/", final_data, PostStatus.class);
            return postStatus;
        } catch (SQLException ex) {
            for (Throwable e : ex) {
                if (e instanceof SQLException) {

                    e.printStackTrace(System.err);
                    errorMsg += "SQLState: " + ((SQLException) e).getSQLState();

                    errorMsg += "Error Code: " + ((SQLException) e).getErrorCode();

                    errorMsg += "Message: " + e.getMessage();
                }
            }
            log.error(ex, errorMsg + query);
        } catch (Exception e) {
            log.error(e, errorMsg + query);
        }
        postStatus.status = "Error";
        postStatus.message = errorMsg + query;
        return postStatus;
    }

    private PostStatus getTransit(String layerUP, String layer, String[] tableup, String[] table, String scenarioId) {
        PostStatus postStatus = new PostStatus();
        String values = "";
        String tableUP[] = new String[tableup.length + 2];
        System.arraycopy(tableup, 0, tableUP, 0, tableup.length);
        tableUP[tableup.length] = "scenario";
        tableUP[tableup.length+1] = "oskari_code";
        try{
            for (int i = 0; i < tableUP.length; i++) {
                switch (tableUP[i]) {
                    case "location":
                        values += "st_astext(st_transform(st_setsrid(" + table[i] + "," + upProjection + "),4326)) as " + tableUP[i];
                        break;
                    case "scenario":
                        values += scenarioId + " as "+ tableUP[i];
                        break;
                    case "oskari_code":
                            values += " user_layer_data.id as "+ tableUP[i];
                        break;
                    default:
                        values += " trim(both '\"' from CAST(property_json->'" + table[i] + "' AS text))  as " + tableUP[i];
                        break;
                }
                if (i < tableUP.length - 1) {
                    values += ",";
                } else {
                    values = values.replaceAll(",$", "");
                }
            }
        }catch(Exception e) {
            postStatus.message += e.toString();
        }
        String errorMsg = "";
        String query = "";
        try (
                Connection connection = DriverManager.getConnection(
                        upURL,
                        upUser,
                        upPassword)) {

            Statement statement = connection.createStatement();
            query = "select distinct " + values + " from user_layer\n"
                    + " inner join user_layer_data on user_layer.id = user_layer_data.user_layer_id\n"
                    + " where user_layer.id=" + layer;
            ResultSet data = statement.executeQuery(query);

            ArrayList<TransitUP> data_in = new ArrayList<>();

            while (data.next()) {
                Object o = new TransitUP();
                Class<?> c = o.getClass();
                for (int i = 0; i < tableUP.length; i++) {
                    try {
                        Field f = c.getDeclaredField(tableUP[i]);
                        f.setAccessible(true);
                        if (!tableUP[i].equals("scenario") && !tableUP[i].equals("transit_id")&& !tableUP[i].equals("oskari_code")) {
                            f.set(o, data.getString(tableUP[i]));
                        } else if (tableUP[i].equals("scenario")) {
                            Integer val = (Integer) data.getInt(tableUP[i]);
                            f.set(o, val);
                        }else if (tableUP[i].equals("oskari_code")) {
                            Long val = (Long) data.getLong(tableUP[i]);
                            f.set(o, val);
                        } else if (tableUP[i].equals("location")) {
                            f.set(o, data.getString(tableUP[i]));
                        } else if (tableUP[i].equals("transit_id")) {
                        }
                    } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException | SQLException e) {
                        log.error(e, errorMsg);
                    }
                }
                log.debug(o, "reflection");
                data_in.add((TransitUP) o);
                
            }
            Tables<TransitUP> final_data = new Tables<TransitUP>(data_in);

            RestTemplate restTemplate = new RestTemplate();
            postStatus = restTemplate.postForObject("http://" + upwsHost + ":" + upwsPort + "/transit/", final_data, PostStatus.class);
            return postStatus;
        } catch (SQLException ex) {
            for (Throwable e : ex) {
                if (e instanceof SQLException) {

                    e.printStackTrace(System.err);
                    errorMsg += "SQLState: " + ((SQLException) e).getSQLState();

                    errorMsg += "Error Code: " + ((SQLException) e).getErrorCode();

                    errorMsg += "Message: " + e.getMessage();
                }
            }
            log.error(ex, errorMsg + query);
        } catch (Exception e) {
            log.error(e, errorMsg + query);
        }
        postStatus.status = "Error";
        postStatus.message = errorMsg + query;
        return postStatus;
    }

    private PostStatus getRoads(String layerUP, String layer, String[] tableup, String[] table, String scenarioId) {
        PostStatus postStatus = new PostStatus();
        String values = "";
        String tableUP[] = new String[tableup.length + 2];
        System.arraycopy(tableup, 0, tableUP, 0, tableup.length);
        tableUP[tableup.length] = "scenario";
        tableUP[tableup.length+1] = "oskari_code";
        try{
            for (int i = 0; i < tableUP.length; i++) {
                switch (tableUP[i]) {
                    case "location":
                        values += "st_astext(st_transform(st_setsrid(" + table[i] + "," + upProjection + "),4326)) as " + tableUP[i];
                        break;
                    case "scenario":
                        values += scenarioId + " as "+ tableUP[i];
                        break;
                    case "oskari_code":
                        values += " user_layer_data.id as "+ tableUP[i];
                        break;
                    default:
                        values += " trim(both '\"' from CAST(property_json->'" + table[i] + "' AS text))  as " + tableUP[i];
                        break;
                }
                if (i < tableUP.length - 1) {
                    values += ",";
                } else {
                    values = values.replaceAll(",$", "");
                }
            }
        }catch(Exception e) {
            postStatus.message += e.toString();
        }
        String errorMsg = "";
        String query = "";
        try (
                Connection connection = DriverManager.getConnection(
                        upURL,
                        upUser,
                        upPassword)) {

            Statement statement = connection.createStatement();
            query = "select distinct " + values + " from user_layer\n"
                    + " inner join user_layer_data on user_layer.id = user_layer_data.user_layer_id\n"
                    + " where user_layer.id=" + layer;
            ResultSet data = statement.executeQuery(query);

            ArrayList<UPRoads> data_in = new ArrayList<>();

            while (data.next()) {
                Object o = new UPRoads();
                Class<?> c = o.getClass();
                for (int i = 0; i < tableUP.length; i++) {
                    try {
                        Field f = c.getDeclaredField(tableUP[i]);
                        f.setAccessible(true);
                        if (!tableUP[i].equals("scenario") && !tableUP[i].equals("roads_id") && !tableUP[i].equals("oskari_code")) {
                            f.set(o, data.getString(tableUP[i]));
                        } else if (tableUP[i].equals("scenario")) {
                            Integer val = (Integer) data.getInt(tableUP[i]);
                            f.set(o, val);
                        }else if (tableUP[i].equals("oskari_code")) {
                            Long val = (Long)data.getLong(tableUP[i]);
                            f.set(o, val);
                        } else if (tableUP[i].equals("location")) {
                            f.set(o, data.getString(tableUP[i]));
                        } else if (tableUP[i].equals("roads_id")) {
                        }
                    } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException | SQLException e) {
                        log.error(e, errorMsg);
                    }
                }
                log.debug(o, "reflection");
                data_in.add((UPRoads) o);
                
            }
            Tables<UPRoads> final_data = new Tables<UPRoads>(data_in);

            RestTemplate restTemplate = new RestTemplate();
            postStatus = restTemplate.postForObject("http://" + upwsHost + ":" + upwsPort + "/roads/", final_data, PostStatus.class);
            return postStatus;
        } catch (SQLException ex) {
            for (Throwable e : ex) {
                if (e instanceof SQLException) {

                    e.printStackTrace(System.err);
                    errorMsg += "SQLState: " + ((SQLException) e).getSQLState();

                    errorMsg += "Error Code: " + ((SQLException) e).getErrorCode();

                    errorMsg += "Message: " + e.getMessage();
                }
            }
            log.error(ex, errorMsg + query);
        } catch (Exception e) {
            log.error(e, errorMsg + query);
        }
        postStatus.status = "Error";
        postStatus.message = errorMsg + query;
        return postStatus;
    }

    private PostStatus getJobs(String layerUP, String layer, String[] tableup, String[] table, String scenarioId) {
        PostStatus postStatus = new PostStatus();
        String values = " ";
        String tableUP[] = new String[tableup.length + 2];
        System.arraycopy(tableup, 0, tableUP, 0, tableup.length);
        tableUP[tableup.length] = "scenario";
        tableUP[tableup.length+1] = "oskari_code";
        try{
            for (int i = 0; i < tableUP.length; i++) {
                switch (tableUP[i]) {
                    case "location":
                        values += "st_astext(st_transform(st_setsrid(" + table[i] + "," + upProjection + "),4326)) as " + tableUP[i];
                        break;
                    case "scenario":
                        values += scenarioId + " as "+ tableUP[i];
                        break;
                    case "oskari_code":
                            values += " user_layer_data.id as "+ tableUP[i];
                        break;
                    default:
                        values += " trim(both '\"' from CAST(property_json->'" + table[i] + "' AS text))  as " + tableUP[i];
                        break;
                }
                if (i < tableUP.length - 1) {
                    values += ",";
                } else {
                    values = values.replaceAll(",$", "");
                }
            }
        }catch(Exception e) {
            postStatus.message += e.toString();
        }
        String errorMsg = "";
        String query = "";
        try (
                Connection connection = DriverManager.getConnection(
                        upURL,
                        upUser,
                        upPassword)) {

            Statement statement = connection.createStatement();
            query = "select distinct " + values + " from user_layer\n"
                    + " inner join user_layer_data on user_layer.id = user_layer_data.user_layer_id\n"
                    + " where user_layer.id=" + layer;
            ResultSet data = statement.executeQuery(query);

            ArrayList<UPJobs> data_in = new ArrayList<>();

            while (data.next()) {
                Object o = new UPJobs();
                Class<?> c = o.getClass();
                for (int i = 0; i < tableUP.length; i++) {
                    try {
                        Field f = c.getDeclaredField(tableUP[i]);
                        f.setAccessible(true);
                        if (!tableUP[i].equals("scenario") && !tableUP[i].equals("jobs_id")&& !tableUP[i].equals("oskari_code")) {
                            f.set(o, data.getString(tableUP[i]));
                        } else if (tableUP[i].equals("scenario")) {
                            Integer val = (Integer) data.getInt(tableUP[i]);
                            f.set(o, val);
                        }else if (tableUP[i].equals("oskari_code")) {
                            Long val = (Long) data.getLong(tableUP[i]);
                            f.set(o, val);
                        }else if (tableUP[i].equals("location")) {
                            f.set(o, data.getString(tableUP[i]));
                        } else if (tableUP[i].equals("jobs_id")) {
                        }
                    } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException | SQLException e) {
                        log.error(e, errorMsg);
                    }
                }
                log.debug(o, "reflection");
                data_in.add((UPJobs) o);
                //return postStatus;
            }
            Tables<UPJobs> final_data = new Tables<UPJobs>(data_in);

            RestTemplate restTemplate = new RestTemplate();
            postStatus = restTemplate.postForObject("http://" + upwsHost + ":" + upwsPort + "/jobs/", final_data, PostStatus.class);
            return postStatus;
        } catch (SQLException ex) {
            for (Throwable e : ex) {
                if (e instanceof SQLException) {

                    e.printStackTrace(System.err);
                    errorMsg += "SQLState: " + ((SQLException) e).getSQLState();

                    errorMsg += "Error Code: " + ((SQLException) e).getErrorCode();

                    errorMsg += "Message: " + e.getMessage();
                }
            }
            log.error(ex, errorMsg + query);
        } catch (Exception e) {
            log.error(e, errorMsg + query);
        }
        postStatus.status = "Error";
        postStatus.message = errorMsg + query;
        return postStatus;
    }

    private PostStatus getAmenities(String layerUP, String layer, String[] tableup, String[] table, String scenarioId) {
        String tableUP[] = new String[tableup.length + 2];
        System.arraycopy(tableup, 0, tableUP, 0, tableup.length);
        tableUP[tableup.length] = "scenario";
        tableUP[tableup.length+1] = "oskari_code";

        PostStatus postStatus = new PostStatus();
        String values = " ";
        for (int i = 0; i < tableUP.length; i++) {
            switch (tableUP[i]) {
                case "location":
                    values += " st_astext(st_transform(st_setsrid(" + table[i] + "," + upProjection + "),4326)) as " + tableUP[i];
                    break;
                case "scenario":
                    values += scenarioId + " as "+tableUP[i];
                    break;
                case "oskari_code":
                    values += " user_layer_data.id as "+tableUP[i];
                    break;
                default:
                    values += " trim(both '\"' from CAST(property_json->'" + table[i] + "' AS text))  as " + tableUP[i];
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
                        upPassword)) {

            Statement statement = connection.createStatement();
            query = "select distinct " + values + " from user_layer\n"
                    + " inner join user_layer_data on user_layer.id = user_layer_data.user_layer_id\n"
                    + " where user_layer.id=" + layer;
            ResultSet data = statement.executeQuery(query);

            ArrayList<Amenities> data_in = new ArrayList<>();

            while (data.next()) {
                Object o = new Amenities();
                Class<?> c = o.getClass();
                for (int i = 0; i < tableUP.length; i++) {
                    postStatus.message += " "+tableUP[i];
                    try {
                        Field f = c.getDeclaredField(tableUP[i]);
                        f.setAccessible(true);
                        if (!tableUP[i].equals("scenario") && !tableUP[i].equals("amenities_id") && !tableUP[i].equals("oskari_code")) {
                            f.set(o, data.getString(tableUP[i]));
                        } else if (tableUP[i].equals("scenario")) {
                            Integer val = (Integer) data.getInt(tableUP[i]);
                            f.set(o, val);
                        }else if (tableUP[i].equals("oskari_code")) {
                            Long val = (Long) data.getLong(tableUP[i]);
                            f.set(o, val);
                        } else if (tableUP[i].equals("location")) {
                            f.set(o, data.getString(tableUP[i]));
                        } else if (tableUP[i].equals("amenities_id")) {
                        }
                    } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException | SQLException e) {
                        log.error(e, errorMsg);
                    }
                }
                data_in.add((Amenities) o);
            }
            Tables<Amenities> final_data = new Tables<Amenities>(data_in);

            RestTemplate restTemplate = new RestTemplate();
            postStatus = restTemplate.postForObject("http://" + upwsHost + ":" + upwsPort + "/amenities/", final_data, PostStatus.class);
            return postStatus;
        } catch (SQLException ex) {
            for (Throwable e : ex) {
                if (e instanceof SQLException) {

                    e.printStackTrace(System.err);
                    errorMsg += "SQLState: " + ((SQLException) e).getSQLState();

                    errorMsg += "Error Code: " + ((SQLException) e).getErrorCode();

                    errorMsg += "Message: " + e.getMessage();
                }
            }
            log.error(ex, errorMsg + query);
        } catch (Exception e) {
            log.error(e, errorMsg + query);
        }
        postStatus.status = "Error";
        postStatus.message += errorMsg + query;
        return postStatus;
    }

    private PostStatus setMmu(String layerUP, String layer, String[] tableup, String[] table, String scenarioId) {
        PostStatus postStatus = new PostStatus();
        String values = "";
        String tableUP[] = new String[tableup.length + 2];
        System.arraycopy(tableup, 0, tableUP, 0, tableup.length);
        tableUP[tableup.length] = "scenario";
        tableUP[tableup.length+1] = "oskari_code";


        for (int i = 0; i < tableUP.length; i++) {
            switch (tableUP[i]) {
                case "location":
                    values += " st_astext(st_transform(st_setsrid(" + table[i] + "," + upProjection + "),4326)) as " + tableUP[i];
                    break;
                case "scenario":
                    values += scenarioId + " as "+tableUP[i];
                    break;
                case "oskari_code":
                    values += " user_layer_data.id as "+tableUP[i];
                    break;
                default:
                    values += " trim(both '\"' from CAST(property_json->'" + table[i] + "' AS text))  as " + tableUP[i];
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
                        upPassword)) {

            Statement statement = connection.createStatement();
            query = "select distinct " + values + " from user_layer\n"
                    + " inner join user_layer_data on user_layer.id = user_layer_data.user_layer_id\n"
                    + " where user_layer.id=" + layer;
            ResultSet data = statement.executeQuery(query);

            ArrayList<MmuUP> data_in = new ArrayList<>();

            while (data.next()) {
                Object o = new MmuUP();
                Class<?> c = o.getClass();
                for (int i = 0; i < tableUP.length; i++) {
                    try {
                        Field f = c.getDeclaredField(tableUP[i]);
                        f.setAccessible(true);
                        if (!tableUP[i].equals("scenario") && !tableUP[i].equals("mmu_id")) {
                            f.set(o, data.getString(table[i]));
                        } else if (tableUP[i].equals("scenario")) {
                            Integer val = (Integer) data.getInt(tableUP[i]);
                            f.set(o, val);
                        }else if (tableUP[i].equals("oskari_code")) {
                            Long val = (Long) data.getLong(tableUP[i]);
                            f.set(o, val);
                        } else if (tableUP[i].equals("location")) {
                            f.set(o, data.getString(tableUP[i]));
                        } else if (tableUP[i].equals("mmu_id")) {
                        }
                    } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException | SQLException e) {
                        log.error(e, errorMsg);
                    }
                }
                data_in.add((MmuUP) o);
                //return postStatus;
            }
            Tables<MmuUP> final_data = new Tables<MmuUP>(data_in);

            RestTemplate restTemplate = new RestTemplate();
            postStatus = restTemplate.postForObject("http://" + upwsHost + ":" + upwsPort + "/mmu/", final_data, PostStatus.class);
            return postStatus;
        } catch (SQLException ex) {
            for (Throwable e : ex) {
                if (e instanceof SQLException) {

                    e.printStackTrace(System.err);
                    errorMsg += "SQLState: " + ((SQLException) e).getSQLState();

                    errorMsg += "Error Code: " + ((SQLException) e).getErrorCode();

                    errorMsg += "Message: " + e.getMessage();
                    postStatus.message += errorMsg;
                }
            }
            log.error(ex, errorMsg + query);
            postStatus.message += errorMsg;
        } catch (Exception e) {
            log.error(e, errorMsg + query);
            postStatus.message += errorMsg;
        }
        postStatus.status = "Error";
        postStatus.message += errorMsg + query;
        return postStatus;
    }

    private PostStatus getMmuInfo(String layerUP, String layer, String[] tableUP, String[] table, String scenarioId) {
        PostStatus postStatus = new PostStatus();
        String values = "";
        try {
            for (int i = 0; i < tableUP.length; i++) {
                if (tableUP[i].equals("name")) {
                    values += " '" + table[i] + "' as name, ";
                    values += " trim(both '\"' from CAST(property_json->'" + table[i] + "' AS text))::double precision as value";
                } else if (tableUP[i].equals("value")) {
                } else if (tableUP[i].equals("mmu")) {
                    values += " trim(both '\"' from CAST(property_json->'" + table[i] + "' AS text))::integer as mmu"; //" user_layer_data." + table[i] + " as mmu";
                } else {
                    values += table[i];
                }
                if (i < tableUP.length - 1) {
                    values += ",";
                } else {
                    values = values.replaceAll(",$", "");
                }
            }
        } catch (Exception e) {
            log.debug(postStatus, e.toString());
        }
        String errorMsg = "getMmuInfo";
        String query = "";
        List<MmuIdUP> mmus_deb = null;
        try (
                Connection connection = DriverManager.getConnection(
                        upURL,
                        upUser,
                        upPassword)) {

            Statement statement = connection.createStatement();
            query = "select distinct " + values + " from user_layer\n"
                    + " inner join user_layer_data on user_layer.id = user_layer_data.user_layer_id\n"
                    + " where user_layer.id=" + layer;
            ResultSet data = statement.executeQuery(query);
            List<MmuIdUP> mmus = getMmuCodes(scenarioId);
            mmus_deb = mmus;
            ArrayList<MmuInfoUP> data_in = new ArrayList<>();

            while (data.next()) {
                Object o = new MmuInfoUP();
                Class<?> c = o.getClass();
                for (int i = 0; i < tableUP.length; i++) {
                    try {
                        Field f = c.getDeclaredField(tableUP[i]);
                        f.setAccessible(true);
                        if (tableUP[i].equals("mmu")) {
                            Integer mmu_id = getMmuId(mmus, data.getInt(tableUP[i]));
                            f.set(o, mmu_id);
                            errorMsg += tableUP[i] + ":" + mmu_id + " " + data.getString(tableUP[i]) + " ";
                        } else if (tableUP[i].equals("name")) {
                            String scen = data.getString(tableUP[i]);
                            f.set(o, scen);
                            errorMsg += tableUP[i] + ":" + scen + " ";
                        } else if (tableUP[i].equals("value")) {
                            Float val = data.getFloat(tableUP[i]);
                            f.set(o, val);
                            errorMsg += tableUP[i] + ":" + val + " ";
                        }
                    } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException | SQLException e) {
                        errorMsg += " loop " + tableUP[i];
                        log.error(o, "table error: " + tableUP[i] + " " + errorMsg);
                    }
                }
                data_in.add((MmuInfoUP) o);
            }
            Tables<MmuInfoUP> final_data = new Tables<MmuInfoUP>(data_in);

            RestTemplate restTemplate = new RestTemplate();
            postStatus = restTemplate.postForObject("http://" + upwsHost + ":" + upwsPort + "/mmu_info/", final_data, PostStatus.class);
            return postStatus;
        } catch (SQLException ex) {
            for (Throwable e : ex) {
                if (e instanceof SQLException) {

                    e.printStackTrace(System.err);
                    errorMsg += "SQLState: " + ((SQLException) e).getSQLState();

                    errorMsg += "Error Code: " + ((SQLException) e).getErrorCode();

                    errorMsg += "Message: " + e.getMessage();
                }
            }
            log.error(ex, errorMsg + "-->" + query);
        } catch (Exception e) {
            log.error(mmus_deb, errorMsg + "++>" + query);
        }
        postStatus.status = "Error";
        postStatus.message = errorMsg + "//>" + query;
        return postStatus;
    }

    private Integer getMmuId(List<MmuIdUP> mmus, Integer mmu_code) {
        log.debug(mmus, mmu_code);
        for (MmuIdUP mmu : mmus) {
            if (Objects.equals(mmu.mmu_code, mmu_code)) {
                return mmu.mmu_id;
            } else {
                log.debug(mmu, mmu.mmu_code, mmu_code, "different");
            }
        }
        return -1;
    }

    private List<MmuIdUP> getMmuCodes(String table) {
        String errorMsg = "getMmuCodes";
        ResponseEntity<List<MmuIdUP>> returns = null;
        RestTemplate restTemplate = new RestTemplate();

        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl("http://" + upwsHost + ":" + upwsPort + "/mmu/")
                .queryParam("scenario", table);

        returns = restTemplate.exchange(
                uriBuilder.toUriString(),
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<MmuIdUP>>() {
        }
        );
        List<MmuIdUP> res = returns.getBody();
        return res;
    }
}

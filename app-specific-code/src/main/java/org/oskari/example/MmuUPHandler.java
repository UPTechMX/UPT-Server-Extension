package org.oskari.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.springframework.web.client.RestTemplate;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionDeniedException;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.User;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;

@OskariActionRoute("MmuUPHandler")
public class MmuUPHandler extends RestActionHandler {

    private static  String upURL;
    private static  String upUser;
    private static  String upPassword;

    private static  String upwsHost;
    private static  String upwsPort;
    private static  String upProjection ;
    private static final Logger log = LogFactory.getLogger(MmuUPHandler.class);

    public void preProcess(ActionParameters params) throws ActionException {
        // common method called for all request methods
        log.info(params.getUser(), "accessing route", getName());
        PropertyUtil.loadProperties("/oskari-ext.properties");
        upURL=PropertyUtil.get("up.db.URL");
        upUser=PropertyUtil.get("up.db.user");
        upPassword=PropertyUtil.get("up.db.password");

        upwsHost=PropertyUtil.get("upws.db.host");
        upwsPort=PropertyUtil.get("upws.db.port");
        upProjection = PropertyUtil.get("oskari.native.srs").substring(PropertyUtil.get("oskari.native.srs").indexOf(":")+1);
    }

    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
        throw new ActionException("This will be logged including stack trace");
    }

    @Override
    public void handlePost(ActionParameters params) throws ActionException {
        String errorMsg = "Scenario UP get ";
        try {
            params.requireLoggedInUser();
            ArrayList<String> roles = new UPTRoles().handleGet(params,params.getUser());
            if (!roles.contains("uptadmin") && !roles.contains("uptuser") ){
                throw new Exception("User privilege is not enough for this action");
            }
            
            Long user_id = params.getUser().getId();
            RestTemplate restTemplate = new RestTemplate();
            List<MmuUP> mmus;
            mmus=evaluateScenario(params);
            MmuInfoArrayUP data=new MmuInfoArrayUP(mmus);

            MmuInfoArrayUP returns = restTemplate.postForObject("http://"+upwsHost+":"+upwsPort+"/mmu/", data, MmuInfoArrayUP.class);
            
            ResponseHelper.writeResponse(params,returns);
        } catch (Exception e) {
            errorMsg = errorMsg + e.getMessage();
            log.error(e, errorMsg);
        }
        throw new ActionException("This will be logged including stack trace");
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

    protected List<MmuUP> evaluateScenario(ActionParameters params) {
        List<MmuUP> mmus = new ArrayList<>();
        try (
                Connection connection = DriverManager.getConnection(
                        upURL,
                        upUser,
                        upPassword)) {
            Statement statement = connection.createStatement();
            ResultSet mmu = statement.executeQuery("SELECT user_layer.id as scenario_id, feature_id as _code, st_astext(st_transform(st_setsrid(geometry,"+ upProjection +"),4326)) as location\n"
                    + "FROM public.user_layer_data\n"
                    + "inner join user_layer on user_layer.id=user_layer_data.user_layer_id\n"
                    + "where user_layer.id=2");
            while (mmu.next()) {
                ResultSet mmu_pop = statement.executeQuery("SELECT feature_id as mmu_code,'population' as name,user_layer_data.property_json->'population' as value\n"
                        + "FROM public.user_layer_data\n"
                        + "inner join user_layer on user_layer.id=user_layer_data.user_layer_id\n"
                        + "where user_layer.id=2 and feature_id=" + mmu.getString("mmu_code"));
                ResultSet mmu_area = statement.executeQuery("SELECT feature_id as mmu_code,'area' as name,user_layer_data.property_json->'area' as value\n"
                        + "FROM public.user_layer_data\n"
                        + "inner join user_layer on user_layer.id=user_layer_data.user_layer_id\n"
                        + "where user_layer.id=2 and feature_id =" + mmu.getString("mmu_code"));

                MmuUP data = new MmuUP();
                data.setLocation(mmu.getString("location"));
                data.setMmuCode(mmu.getString("mmu_code"));
                data.setScenarioId(mmu.getInt("scenario_id"));

                MmuInfoUP pop = new MmuInfoUP();
                pop.setName(mmu_pop.getString("name"));
                pop.setValue(mmu_pop.getFloat("value"));

                MmuInfoUP area = new MmuInfoUP();
                area.setName(mmu_area.getString("name"));
                area.setValue(mmu_area.getFloat("value"));
                
                data.setMmuInfo(pop);
                data.setMmuInfo(area);
                
                mmus.add(data);
            }
            return mmus;
        } catch (SQLException e) {
            return mmus;
        }
    }

}

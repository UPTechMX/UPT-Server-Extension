package org.oskari.example;

import fi.nls.oskari.control.*;
import fi.nls.oskari.util.PropertyUtil;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.ResponseHelper;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.ActionParamsException;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.domain.User;
import java.io.File;
import java.util.ArrayList;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;


@OskariActionRoute("AdminUPHandler")
public class AdminUPHandler extends RestActionHandler {
    private static final Logger log = LogFactory.getLogger(AdminUPHandler.class);
    private static final String PROPERTY_USERLAYER_MAX_FILE_SIZE_MB = "userlayer.max.filesize.mb";
    private static final int KB = 1024 * 1024;
    private static final int MB = 1024 * KB;

    // Store files smaller than 128kb in memory instead of writing them to disk
    private static final int MAX_SIZE_MEMORY = 128 * KB;
    private static final int MAX_RETRY_RANDOM_UUID = 100;
    private final DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory(MAX_SIZE_MEMORY, null);
    private final int userlayerMaxFileSize = PropertyUtil.getOptional(PROPERTY_USERLAYER_MAX_FILE_SIZE_MB, 10) * MB;
    
    private static  String upURL;
    private static  String upUser;
    private static  String upPassword;

    private static  String upwsHost;
    private static  String upwsPort;

    public void preProcess(ActionParameters params) throws ActionException {
        // common method called for all request methods
        log.info(params.getUser(), "accessing route", getName());
        PropertyUtil.loadProperties("/oskari-ext.properties");
        upURL=PropertyUtil.get("up.db.URL");
        upUser=PropertyUtil.get("up.db.user");
        upPassword=PropertyUtil.get("up.db.password");

        upwsHost=PropertyUtil.get("upws.db.host");
        upwsPort=PropertyUtil.get("upws.db.port");
    }
    @Override
    public void handleGet(ActionParameters params) throws ActionException {
        params.requireLoggedInUser();
    }

    
    @Override
    public void handlePost(ActionParameters params) throws ActionException {
      
        String errorMsg="Results UP post ";
        try {
            params.requireLoggedInUser();
            ArrayList<String> roles = new UPTRoles().handleGet(params,params.getUser());
            if (!roles.contains("uptadmin") && !roles.contains("uptuser") ){
                throw new Exception("User privilege is not enough for this action");
            }
            //Upload file
            HttpServletRequest request=params.getRequest();
            request.setCharacterEncoding("UTF-8");
            
            
            FileItem zipFile = null;
            FileSystemResource indicator=null;
            File file=null;
            if (ServletFileUpload.isMultipartContent(request)) {

                ServletFileUpload upload = new ServletFileUpload(diskFileItemFactory);
                upload.setSizeMax(userlayerMaxFileSize);
                List<FileItem> fileItems=upload.parseRequest(request);
                if (fileItems != null && fileItems.size() > 0) {
                    for (FileItem item : fileItems) {
                        if (!item.isFormField()) {
                            String fileName = new File(item.getName()).getName();
                            String filePath = File.separator+"tmp" + File.separator + fileName;
                                file = new File(filePath);
                                item.write(file);
                        }
                    }
                }
                
                
                zipFile = fileItems.stream()
                    .filter(f -> !f.isFormField())
                    .findAny() // If there are more files we'll get the zip or fail miserably
                    .orElseThrow(() -> new ActionParamsException("No file entries in FormData"));
                
                indicator= new FileSystemResource( fileItems.toString());
                
            }
            //Send file
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            MultiValueMap<String, Object> body= new LinkedMultiValueMap<>();
            body.add("file", indicator);
            
            HttpEntity<MultiValueMap<String, Object>> requestEntity= new HttpEntity<>(body, headers);
            
            String serverUrl = "http://"+upwsHost+":"+upwsPort+"/indicator/";
            
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(serverUrl, requestEntity, String.class);
            ResponseHelper.writeResponse(params,response);
            
            
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


}


    
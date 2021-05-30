package org.oskari.example.up;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fi.nls.oskari.annotation.OskariActionRoute;
import fi.nls.oskari.control.ActionException;
import fi.nls.oskari.control.ActionParameters;
import fi.nls.oskari.control.RestActionHandler;
import fi.nls.oskari.log.LogFactory;
import fi.nls.oskari.log.Logger;
import fi.nls.oskari.util.JSONHelper;
import fi.nls.oskari.util.PropertyUtil;
import fi.nls.oskari.util.ResponseHelper;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.FilenameUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.oskari.example.IndicatorUP;
import org.oskari.example.PostStatus;
import org.oskari.example.UPTRoles;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@OskariActionRoute("indicators_installer")
public class UPCalculusModulesInstallerHandler extends RestActionHandler {
  private static String upURL;
  private static String upUser;
  private static String upPassword;
  private static String upwsHost;
  private static String upwsPort;
  private JSONArray errors;
  private ObjectMapper Obj;

  private static final String PROPERTY_USERLAYER_MAX_FILE_SIZE_MB =
    "userlayer.max.filesize.mb";

  private static final Charset[] POSSIBLE_CHARSETS_USED_IN_ZIP_FILE_NAMES = {
    StandardCharsets.UTF_8,
    StandardCharsets.ISO_8859_1,
    Charset.forName("CP437"),
    Charset.forName("CP866"),
  };

  private static final int KB = 1024 * 1024;
  private static final int MB = 1024 * KB;

  // Store files smaller than 128kb in memory instead of writing them to disk
  private static final int MAX_SIZE_MEMORY = 128 * KB;

  private final DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory(
    MAX_SIZE_MEMORY,
    null
  );
  private final int userlayerMaxFileSize =
    PropertyUtil.getOptional(PROPERTY_USERLAYER_MAX_FILE_SIZE_MB, 10) * MB;

  private static final Logger log = LogFactory.getLogger(
    UPCalculusModulesInstallerHandler.class
  );

  @Override
  public void preProcess(ActionParameters params) throws ActionException {
    // common method called for all request methods
    log.info(params.getUser(), "accessing route", getName());
    PropertyUtil.loadProperties("/oskari-ext.properties");
    upURL = PropertyUtil.get("up.db.URL");
    upUser = PropertyUtil.get("up.db.user");
    upPassword = PropertyUtil.get("up.db.password");

    upwsHost = PropertyUtil.get("upws.db.host");
    upwsPort = PropertyUtil.get("upws.db.port");
    errors = new JSONArray();
    Obj = new ObjectMapper();
  }

  @Override
  public void handleGet(ActionParameters params) throws ActionException {
    try (
      Connection connection = DriverManager.getConnection(
        upURL,
        upUser,
        upPassword
      );
    ) {
      params.requireLoggedInUser();
      ArrayList<String> roles = new UPTRoles()
      .handleGet(params, params.getUser());
      if (!roles.contains("uptadmin") && !roles.contains("uptuser")) {
        throw new Exception("User privilege is not enough for this action");
      }

      ResponseEntity<List<IndicatorUP>> returns = null;
      RestTemplate restTemplate = new RestTemplate();
      returns =
        restTemplate.exchange(
          "http://" + upwsHost + ":" + upwsPort + "/indicator/",
          HttpMethod.GET,
          null,
          new ParameterizedTypeReference<List<IndicatorUP>>() {}
        );

      Long user_id = params.getUser().getId();
      List<IndicatorUP> responseFromUP = returns.getBody();

      PreparedStatement statement = connection.prepareStatement(
        "SELECT id, name, label, tooltip\n" +
        "	FROM public.up_modules_translation where language=?\n" +
        "order by label asc;"
      );
      statement.setString(1, "english");
      ResultSet indicators = statement.executeQuery();
      JSONArray response = new JSONArray();
      ObjectMapper Obj = new ObjectMapper();
      while (indicators.next()) {
        IndicatorUP indicator = new IndicatorUP();
        indicator.name = indicators.getString("name");
        indicator.label = indicators.getString("label");
        indicator.description = indicators.getString("tooltip");
        indicator.id = indicators.getInt("id");
        //Update dependencies field
        for (IndicatorUP index : responseFromUP) {
          if (
            index != null &&
            indicator.name != null &&
            index.module.equals(indicator.name)
          ) {
            //Update dependencies field
            String[] deps;
            deps =
              index
                .dependencies.replace("[", "")
                .replace("]", "")
                .replaceAll("\"", "")
                .split(",");
            for (String dependency : deps) {
              if (dependency.equals(indicator.name)) {
                index.dependencies =
                  index.dependencies.replaceAll(dependency, indicator.label);
              }
            }
            indicator.dependencies = index.dependencies;
            break;
          }
        }
        response.put(
          JSONHelper.createJSONObject(Obj.writeValueAsString(indicator))
        );
      }
      ResponseHelper.writeResponse(params, response);
    } catch (JsonProcessingException ex) {
      java
        .util.logging.Logger.getLogger(
          UPCalculusModulesInstallerHandler.class.getName()
        )
        .log(Level.SEVERE, null, ex);
      ResponseHelper.writeError(params, ex.toString());
    } catch (Exception e) {
      java
        .util.logging.Logger.getLogger(
          UPCalculusModulesInstallerHandler.class.getName()
        )
        .log(Level.SEVERE, null, e);
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
          .util.logging.Logger.getLogger(
            UPCalculusModulesInstallerHandler.class.getName()
          )
          .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java
          .util.logging.Logger.getLogger(
            UPCalculusModulesInstallerHandler.class.getName()
          )
          .log(Level.SEVERE, null, ex);
      }
    }
  }

  @Override
  public void handlePost(ActionParameters params) throws ActionException {
    String errorMsg = "Assumptions post";

    List<FileItem> fileItems = getFileItems(params.getRequest());

    try {
      params.requireLoggedInUser();
      for (FileItem indicator : fileItems) {
        File memory = new File(FilenameUtils.getName(indicator.getName()));
        indicator.write(memory);

        RestTemplate restTemplate = new RestTemplateBuilder().build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
        ContentDisposition contentDisposition = ContentDisposition
          .builder("form-data")
          .name("file")
          .filename(FilenameUtils.getName(indicator.getName()))
          .build();

        fileMap.add(
          HttpHeaders.CONTENT_DISPOSITION,
          contentDisposition.toString()
        );
        HttpEntity<byte[]> fileEntity = new HttpEntity<>(
          indicator.get(),
          fileMap
        );

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileEntity);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(
          body,
          headers
        );
        try {
          ResponseEntity<String> response = restTemplate.exchange(
            "http://" + upwsHost + ":" + upwsPort + "/indicator/",
            HttpMethod.POST,
            requestEntity,
            String.class
          );
        } catch (Exception e) {
          try {
            errors.put(
              JSONHelper.createJSONObject(
                Obj.writeValueAsString(new PostStatus("Error", e.toString()))
              )
            );
            ResponseHelper.writeError(
              null,
              "",
              500,
              new JSONObject().put("Errors", errors)
            );
          } catch (JsonProcessingException ex) {
            java
              .util.logging.Logger.getLogger(
                UPCalculusModulesInstallerHandler.class.getName()
              )
              .log(Level.SEVERE, null, ex);
          } catch (JSONException ex) {
            java
              .util.logging.Logger.getLogger(
                UPCalculusModulesInstallerHandler.class.getName()
              )
              .log(Level.SEVERE, null, ex);
          }
        }
      }
      getIndicatorsInstalled();
      getIndicatorsTables();
    } catch (Exception e) {
      java
        .util.logging.Logger.getLogger(UPAssumptionsHandler.class.getName())
        .log(Level.SEVERE, null, e);
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
          )
        );
        ResponseHelper.writeError(
          null,
          "",
          500,
          new JSONObject().put("Errors", errors)
        );
      } catch (JsonProcessingException ex) {
        java
          .util.logging.Logger.getLogger(
            UPCalculusModulesInstallerHandler.class.getName()
          )
          .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java
          .util.logging.Logger.getLogger(
            UPCalculusModulesInstallerHandler.class.getName()
          )
          .log(Level.SEVERE, null, ex);
      }
    }
  }

  @Override
  public void handlePut(ActionParameters params) throws ActionException {
    try (
      Connection connection = DriverManager.getConnection(
        upURL,
        upUser,
        upPassword
      );
      PreparedStatement statement = connection.prepareStatement(
        "UPDATE public.up_modules_translation\n" +
        "SET language=?, name=?, label=?, tooltip=?\n" +
        "WHERE id=?;"
      );
    ) {
      params.requireLoggedInUser();
      statement.setString(1, "english");
      statement.setString(2, params.getRequiredParam("name"));
      statement.setString(3, params.getRequiredParam("label"));
      statement.setString(4, params.getRequiredParam("description"));
      statement.setInt(5, Integer.parseInt(params.getRequiredParam("id")));
      statement.execute();
    } catch (SQLException ex) {
      java
        .util.logging.Logger.getLogger(
          UPCalculusModulesInstallerHandler.class.getName()
        )
        .log(Level.SEVERE, null, ex);
      ResponseHelper.writeError(params, ex.toString());
    }
  }

  @Override
  public void handleDelete(ActionParameters params) throws ActionException {
    try (
      Connection connection = DriverManager.getConnection(
        upURL,
        upUser,
        upPassword
      );
      PreparedStatement statement = connection.prepareStatement(
        "DELETE FROM public.up_modules_translation\n" + "	WHERE id=?;"
      );
    ) {
      params.requireLoggedInUser();
      statement.setInt(1, Integer.parseInt(params.getRequiredParam("id")));
      deleteInstalledIndicator(params.getRequiredParam("name"));
      statement.execute();
    } catch (SQLException ex) {
      java
        .util.logging.Logger.getLogger(
          UPCalculusModulesInstallerHandler.class.getName()
        )
        .log(Level.SEVERE, null, ex);
      ResponseHelper.writeError(params, ex.toString());
    }
  }

  private List<FileItem> getFileItems(HttpServletRequest request)
    throws ActionException {
    try {
      request.setCharacterEncoding("UTF-8");
      ServletFileUpload upload = new ServletFileUpload(diskFileItemFactory);
      upload.setSizeMax(userlayerMaxFileSize);
      return upload.parseRequest(request);
    } catch (UnsupportedEncodingException | FileUploadException e) {
      throw new ActionException("Failed to read request", e);
    }
  }

  private void getIndicatorsInstalled() {
    String errorMsg = "Scenario UP get ";
    //IndicatorsUP returns=new IndicatorsUP();
    ResponseEntity<List<IndicatorUP>> returns = null;
    try {
      RestTemplate restTemplate = new RestTemplate();
      returns =
        restTemplate.exchange(
          "http://" + upwsHost + ":" + upwsPort + "/indicator/",
          HttpMethod.GET,
          null,
          new ParameterizedTypeReference<List<IndicatorUP>>() {}
        );
      List<IndicatorUP> response = returns.getBody();

      try (
        Connection connection = DriverManager.getConnection(
          upURL,
          upUser,
          upPassword
        );
      ) {
        for (IndicatorUP index : response) {
          PreparedStatement statement = connection.prepareStatement(
            "INSERT INTO public.up_modules_translation(\n" +
            "	language, name, label, tooltip)\n" +
            "	VALUES (?, ?, ?, ?)\n" +
            "on conflict (language, name)\n" +
            "do nothing;"
          );
          statement.setString(1, "english");
          statement.setString(2, index.getName());
          statement.setString(3, index.getModule());
          statement.setString(4, index.getDescription());

          statement.execute();
        }
      }
    } catch (Exception e) {
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
          )
        );
        ResponseHelper.writeError(
          null,
          "",
          500,
          new JSONObject().put("Errors", errors)
        );
      } catch (JsonProcessingException ex) {
        java
          .util.logging.Logger.getLogger(
            UPCalculusModulesInstallerHandler.class.getName()
          )
          .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java
          .util.logging.Logger.getLogger(
            UPCalculusModulesInstallerHandler.class.getName()
          )
          .log(Level.SEVERE, null, ex);
      }
      log.error(e, errorMsg);
    }
  }

  private void getIndicatorsTables() {
    String errorMsg = "Scenario UP get ";
    try {
      ResponseEntity<List<String>> returns = null;
      RestTemplate restTemplate = new RestTemplate();

      UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(
        "http://" + upwsHost + ":" + upwsPort + "/all_layers"
      );

      returns =
        restTemplate.exchange(
          uriBuilder.toUriString(),
          HttpMethod.GET,
          null,
          new ParameterizedTypeReference<List<String>>() {}
        );

      List<String> res = returns.getBody();

      try (
        Connection connection = DriverManager.getConnection(
          upURL,
          upUser,
          upPassword
        );
      ) {
        PreparedStatement statement = connection.prepareStatement(
          "INSERT INTO public.up_layers(\n" +
          "	language, name,label)\n" +
          "	VALUES (?, ?,?)" +
          "on conflict(language,name) do nothing;"
        );
        for (String table : res) {
          statement.setString(1, "english");
          statement.setString(2, table);
          statement.setString(3, table);
          statement.execute();
        }
      }
    } catch (Exception e) {
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
          )
        );
        ResponseHelper.writeError(
          null,
          "",
          500,
          new JSONObject().put("Errors", errors)
        );
      } catch (JsonProcessingException ex) {
        java
          .util.logging.Logger.getLogger(
            UPCalculusModulesInstallerHandler.class.getName()
          )
          .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java
          .util.logging.Logger.getLogger(
            UPCalculusModulesInstallerHandler.class.getName()
          )
          .log(Level.SEVERE, null, ex);
      }
      errorMsg = errorMsg + e.getMessage();
      log.error(e, errorMsg);
    }
  }

  private void deleteInstalledIndicator(String module) {
    try {
      Map<String, String> params = new HashMap<String, String>();
      params.put("module", module);

      RestTemplate restTemplate = new RestTemplate();
      restTemplate.delete(
        "http://" + upwsHost + ":" + upwsPort + "/indicator/{module}",
        params
      );
    } catch (Exception e) {
      try {
        errors.put(
          JSONHelper.createJSONObject(
            Obj.writeValueAsString(new PostStatus("Error", e.toString()))
          )
        );
        ResponseHelper.writeError(
          null,
          "",
          500,
          new JSONObject().put("Errors", errors)
        );
      } catch (JsonProcessingException ex) {
        java
          .util.logging.Logger.getLogger(
            UPCalculusModulesInstallerHandler.class.getName()
          )
          .log(Level.SEVERE, null, ex);
      } catch (JSONException ex) {
        java
          .util.logging.Logger.getLogger(
            UPCalculusModulesInstallerHandler.class.getName()
          )
          .log(Level.SEVERE, null, ex);
      }
    }
  }
}

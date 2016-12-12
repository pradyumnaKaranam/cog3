/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.research.cogams.tikaingestion;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.Request;
import okhttp3.RequestBody;

import org.apache.commons.io.IOUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ibm.watson.developer_cloud.document_conversion.v1.DocumentConversion;
import com.ibm.watson.developer_cloud.document_conversion.v1.model.Answers;
import com.ibm.watson.developer_cloud.document_conversion.v1.util.ConversionTarget;
import com.ibm.watson.developer_cloud.http.HttpHeaders;
import com.ibm.watson.developer_cloud.http.HttpMediaType;
import com.ibm.watson.developer_cloud.http.RequestBuilder;
import com.ibm.watson.developer_cloud.http.ServiceCall;
import com.ibm.watson.developer_cloud.util.ResponseConverterUtils;

/**
 *
 * @author sampath
 */
public class ExtendedDocumentConversion extends DocumentConversion {
    private static final String CONVERSION_TARGET = "conversion_target";
    private static final String CONVERT_DOCUMENT = "convert_document";
    private static final String CONVERT_DOCUMENT_PATH = "/v1/" + CONVERT_DOCUMENT;
    private static final JsonObject EMPTY_CONFIG = new JsonParser().parse("{}").getAsJsonObject();
    private static final JsonObject CUSTOM_CONFIG = new JsonParser().parse("{ \"conversion_target\":\"ANSWER_UNITS\","+
        "\"answer_units\": {" +
          "\"selector_tags\": [\"h1\",\"h2\",\"h3\",\"h4\",\"h5\",\"h6\"],"+
          "\"output_media_types\": [\"text/plain\",\"text/html\"]"+
            "}}"
    ).getAsJsonObject();

    private final String versionDate;

    public ExtendedDocumentConversion(String versionDate) {
        super(versionDate);
        this.versionDate = versionDate;
    }

    public ExtendedDocumentConversion(String versionDate, String username, String password) {
        super(versionDate, username, password);
        this.versionDate = versionDate;

        
    }
    public ServiceCall<Answers> convertDocumentToAnswer(InputStream f,String contentType) throws IOException {
    Request request = createConversionRequest(IOUtils.toByteArray(f), contentType,ConversionTarget.ANSWER_UNITS, CUSTOM_CONFIG);
    return createServiceCall(request, ResponseConverterUtils.getObject(Answers.class));
  }
  public ServiceCall<Answers> convertDocumentToAnswer(String urlString,String contentType) {
    Request request = createConversionRequest(urlString.getBytes(StandardCharsets.UTF_8), contentType,ConversionTarget.ANSWER_UNITS, CUSTOM_CONFIG);
    return createServiceCall(request, ResponseConverterUtils.getObject(Answers.class));
  }
   private Request createConversionRequest(final byte[] document,final String contentType,
                                          final ConversionTarget conversionTarget, final JsonObject customConfig) {

    

    JsonObject config = null;

    if (customConfig != null){
      config = customConfig;
     // System.out.print("Doc Conversion Config: " + config.toString());
    }
    else
      config = EMPTY_CONFIG;

    final JsonObject configJson = new JsonObject();
    // Do this since we shouldn't mutate customConfig
    for (Map.Entry<String, JsonElement> entry : config.entrySet()) {
      configJson.add(entry.getKey(), entry.getValue());
    }
    // Add or override the conversion target
    //configJson.addProperty(CONVERSION_TARGET, conversionTarget.toString());

    final MediaType mType = MediaType.parse(contentType);
    final RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
        .addPart(Headers.of(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"config\""),
            RequestBody.create(HttpMediaType.JSON, configJson.toString()))
        .addPart(Headers.of(HttpHeaders.CONTENT_DISPOSITION, "form-data; name=\"file\""),
            RequestBody.create(mType, document))
        .build();

    return RequestBuilder.post(CONVERT_DOCUMENT_PATH).query(VERSION, versionDate).body(body).build();
  }
}

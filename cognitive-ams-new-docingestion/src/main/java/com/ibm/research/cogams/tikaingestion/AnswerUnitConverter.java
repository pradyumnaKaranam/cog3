/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.research.cogams.tikaingestion;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.ibm.research.cogams.docingestion.DocumentStream;
import com.ibm.watson.developer_cloud.document_conversion.v1.DocumentConversion;
import com.ibm.watson.developer_cloud.document_conversion.v1.model.Answers;

/**
 *
 * @author sampath
 */
public class AnswerUnitConverter {
    private String username;
    private String password;

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
    
    public AnswerUnitConverter() throws ParseException{
            
            Map<String, String> env = System.getenv();
            
            
            String VCAP_SERVICES = System.getenv("VCAP_SERVICES");
            if(VCAP_SERVICES != null){
                System.out.print("Using Cloud Credentials..");
                JSONParser parser = new JSONParser();
                JSONObject vcap;
                try {
                    vcap = (JSONObject) parser.parse(VCAP_SERVICES);
                    JSONArray docConversion = (JSONArray) vcap.get("document_conversion");
                    JSONObject docConversionInstance = (JSONObject) docConversion.get(0);
                    JSONObject docConversionCredentials = (JSONObject) docConversionInstance.get("credentials"); 
                    this.username = (String) docConversionCredentials.get("username"); 
                    this.password = (String) docConversionCredentials.get("password");
                } catch (ParseException ex) {
                    Logger.getLogger(AnswerUnitConverter.class.getName()).log(Level.SEVERE, null, ex);
                    throw ex;
                }
            }
            else {
                username = "2f464300-c952-4873-a7de-835419d40e02";
                password = "bQDQBLIAaDWR";
            }
            
        }  
    
    public String chunk(String urlString) throws IOException{
        ExtendedDocumentConversion service = new ExtendedDocumentConversion(DocumentConversion.VERSION_DATE_2015_12_01);
        service.setUsernameAndPassword(username, password);

        Answers htmlToAnswers = service.convertDocumentToAnswer(urlString,"text/html").execute();
       // System.out.print(htmlToAnswers);

        return htmlToAnswers.toString();
    }
    public String chunk(DocumentStream doc) throws IOException{
        ExtendedDocumentConversion service = new ExtendedDocumentConversion(DocumentConversion.VERSION_DATE_2015_12_01);
        service.setUsernameAndPassword(username, password);

        Answers htmlToAnswers = service.convertDocumentToAnswer(doc.getInputStream(),doc.getMediaType().toString()).execute();
       // System.out.print(htmlToAnswers);

        return htmlToAnswers.toString();
    }
        
}

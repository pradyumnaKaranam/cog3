/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.research.cogams.docstorage;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.LoggerFactory;

import com.cloudant.client.api.ClientBuilder;
import com.cloudant.client.api.CloudantClient;
import com.cloudant.client.api.Database;
import com.ibm.research.cogams.docingestion.AppLevelGlobals;

/**
 *
 * @author sampath
 */
public class CloundantIntfc {
        private String username;
        private String password;
        private String url;
        CloudantClient client;
        
		private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CloundantIntfc.class);

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getUrl() {
            return url;
        }
        
        
       
        public CloundantIntfc() throws ParseException, MalformedURLException{
            
            Map<String, String> env = System.getenv();
            if(AppLevelGlobals.getInstance().isOnCloud() || AppLevelGlobals.getInstance().useCloudant()){
                String VCAP_SERVICES = System.getenv("VCAP_SERVICES");
                if(VCAP_SERVICES!=null) {
	                System.out.print("Using Cloud Credentials..");
	                JSONParser parser = new JSONParser();
	                JSONObject vcap;
	                try {
	                    vcap = (JSONObject) parser.parse(VCAP_SERVICES);
	                    JSONArray cloudant = (JSONArray) vcap.get("cloudantNoSQLDB");
	                    JSONObject cloudantInstance = (JSONObject) cloudant.get(0);
	                    JSONObject cloudantCredentials = (JSONObject) cloudantInstance.get("credentials"); 
	                    this.url = (String) cloudantCredentials.get("url"); 
	                    this.username = (String) cloudantCredentials.get("username"); 
	                    this.password = (String) cloudantCredentials.get("password");
	                } catch (ParseException ex) {
	                    Logger.getLogger(CloundantIntfc.class.getName()).log(Level.SEVERE, null, ex);
	                    throw ex;
	                }
                }
                else {
                	logger.warn("Using local cloudant credentials...");
                	this.url = "https://5387a852-872a-4b3d-8652-744264d2cef6-bluemix:0b998a7fa1ae5faca8bb562800a6df2a45715b026b6c1c486bd7d5c5462ec67f@5387a852-872a-4b3d-8652-744264d2cef6-bluemix.cloudant.com";
                    this.username = "5387a852-872a-4b3d-8652-744264d2cef6-bluemix";
                    this.password = "0b998a7fa1ae5faca8bb562800a6df2a45715b026b6c1c486bd7d5c5462ec67f";
                }
                client = ClientBuilder.url(new URL(url))
                        .username(username)
                        .password(password)
                        .build();
            }
            else {
                 url = AppLevelGlobals.getInstance().getCouchDBURL();//"http://127.0.0.1:5984";
                 client = ClientBuilder.url(new URL(url))
                            .build();
            }
             
        }
        
       
        CloudantClient getClient() throws MalformedURLException{
            
            return client;

        }
        //creates if it is not present
        Database getDB(String dbname){
            
            //create if it doesnt already exist, the second true
            Database db;
            db = client.database(dbname, false);
            return db;
        }
        
        Boolean isDBExists(String name){
            List<String> databases = client.getAllDbs();
            //logger.info(databases.toString());
            return (databases.contains(name));
        }
        void printAllDBs(){
            // Get a List of all the databases this Cloudant account
            List<String> databases = client.getAllDbs();
            System.out.println("All my databases : " + databases.size());
            for ( String db : databases ) {
                System.out.println(db);
            }
        }
        public List<String> getAllDBs(){
        	List<String> databases = client.getAllDbs();
			return databases;
        }
        public void createDB(String name){
        		 if(!isDBExists(name))
        			 client.createDB(name);
        }
        public void deleteDB(String name){
        		if(isDBExists(name))
        			client.deleteDB(name);
        } 
}


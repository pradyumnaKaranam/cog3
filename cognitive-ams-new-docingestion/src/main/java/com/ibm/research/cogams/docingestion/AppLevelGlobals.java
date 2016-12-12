/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.research.cogams.docingestion;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.LoggerFactory;


/**
 * This class is container for global functions and parameters specified through application_config.properties
 * For every new parameter in application_config.properties, add a function here. 
 * @author sampath
 */
public class AppLevelGlobals {
        private static final org.slf4j.Logger logger = LoggerFactory.getLogger(AppLevelGlobals.class);
        private Properties appProperties;
        private static AppLevelGlobals singletonInstance;
        private static String APP_HOME;
        String SOLR_URL = "SOLR_URL";
        String COUCHDB_URL = "COUCHDB_URL";
        String IMAGE_DIR = "IMAGE_DIR";
        String FILEUPLOAD_DIR = "FILEUPLOAD_DIR";
        String PDFMINER_URL = "PDFMINER_URL";
        String GENSIM_URL = "GENSIM_URL";
        String ALCHEMY_API_KEY="ALCHEMY_API_KEY";
        String USE_CLOUDANT="USE_CLOUDANT";
        String CLOUD_MODE="CLOUD_MODE";
        String APP_HOME_PROPERTY = "DOCINGESTION_APP_HOME";
        String IMAGE_DIR_URL = "IMAGE_DIR_URL";
        
    private AppLevelGlobals(){
        try {
        	if(APP_HOME == null) throw new IOException("APP_HOME is not specified");
        	
            appProperties = new Properties();
            logger.info("loading properties from {}",APP_HOME);

            File propertiesFile = new File(getAppHomeDirPath()+"/" + "application_config.properties");
            appProperties.load(new FileReader(propertiesFile));

            Properties logProperties = new Properties();
            propertiesFile = new File(getAppHomeDirPath()+"/" + "log4j2.properties");
            logProperties.load(new FileReader(propertiesFile));
            PropertyConfigurator.configure(logProperties);
            logger.debug("Applevel Global Properties:\n {} ",getPropertyAsString(appProperties));

           // SolrIndexSearch.SOLR_URL=appProperties.getProperty("SOLR_URL");
           // FSDocStorage.imageDir=appProperties.getProperty("IMAGE_DIR");
        } catch (IOException ex) {
            logger.error("Exception occured ", ex);
        }
        
    }

    /**
     *
     */
    public static void init(String app_properties_file_path){
        APP_HOME = app_properties_file_path;
        singletonInstance = new AppLevelGlobals();
    }
    
    public static String getAppHomeDirPath(){
    	return APP_HOME;
    }
    
    public static String getPropertyAsString(Properties prop) {    
    	  StringWriter writer = new StringWriter();
    	  prop.list(new PrintWriter(writer));
    	  return writer.getBuffer().toString();
    	}

    public String getParameter(String parameter_name) {
    	String parm = appProperties.getProperty(parameter_name);
    	/*if(parm==null) throw new IOException(
    					String.format("Applevel parameter %s is not defined in application_config.properties",parameter_name));*/
		return parm;
    	
    }
    /**
     *
     * @return
     * @throws IOException 
     */
    public static AppLevelGlobals getInstance(){
        if(singletonInstance==null)
            logger.error("Docingestion app needs to be initialized, AppLevelGlobal Instance is Null..");
            return singletonInstance;
    }

    /**
     *
     * @return
     * @ 
     */
    public String getSOLRURL() {
        String solrUrl  = getParameter(SOLR_URL);
        return solrUrl;
    }

    /**
     *
     * @return
     * @ 
     */
    public String getIMAGEDIR() {
        String imageDir = getParameter(IMAGE_DIR);
        return imageDir;
    }
    /**
     *
     * @return
     * @ 
     */
    public String getFileUploadDIR() {
        String fDir = getParameter(FILEUPLOAD_DIR);
        return fDir;
    }
    
    public String getCouchDBURL() {
        String couchDBURL = getParameter(COUCHDB_URL);
        
        return couchDBURL;
    }
    public String getImageFilesPath(){
    	String imgDirUrl = getParameter(IMAGE_DIR_URL);
        return imgDirUrl;
    }
    public String getPDFMinerURL() {
        String pdfminerURL = getParameter(PDFMINER_URL);
        return pdfminerURL;
    }
    public String getGenSIMURL() {
        String gensimURL = getParameter(GENSIM_URL);
        return gensimURL;
    }
    public String getAlchemyAPIKey() {
        String alchemyAPIKey = getParameter(ALCHEMY_API_KEY);
        return alchemyAPIKey;
    }
  
    public Boolean useCloudant() {
        String useCloudant = getParameter(USE_CLOUDANT);
        if(useCloudant !=null && useCloudant.toLowerCase().equals("true")){
        	return true;
        }
        return false;
    }
    /**
     *
     * @return
     * @ 
     */
    public boolean isOnCloud() {
    	 Boolean onCloud = System.getenv("VCAP_SERVICES") != null;
    	 String onCloudStr = getParameter(CLOUD_MODE);
    	 return(onCloud || onCloudStr.toLowerCase().equals(true));
    }
    
}

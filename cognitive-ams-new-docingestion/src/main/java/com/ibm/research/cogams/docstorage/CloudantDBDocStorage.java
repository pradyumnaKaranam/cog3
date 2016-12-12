/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.research.cogams.docstorage;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;

import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudant.client.api.Database;
import com.cloudant.client.api.model.Response;



// Using the tika slf4j binding


public class CloudantDBDocStorage implements DocStorageIntfc{
    private static final Logger logger = LoggerFactory.getLogger(CloudantDBDocStorage.class);

    String dbName;
    Database db;
    CloundantIntfc dbcon;
    String extractDir;
    String url;
    
    public class DocFileRecord  {
    	private String docFileName;
    	private String _id = null;
    	private String _rev = null;
    	public DocFileRecord(String filename){
    		this.docFileName=filename;
    		// intentionally duplicating to accomodate changes later
    		this._id=filename;
    		logger.debug("instantiating  file {} with id {}",docFileName,_id);
    	}
    	public String getDocFileName() {
			return docFileName;
		}
    	
    	public void setDocFileName(String docFileName) {
			this.docFileName = docFileName;
		}
    	@Override
    	public String toString() {
    		return "{ id: " + _id + ",\nrev: " + _rev + ",\ndocFileName: " + docFileName + "\n}";
    	}
    	public String get_id() {
			return _id;
		}
    	public String get_rev() {
			return _rev;
		}
    	
    }
    public CloudantDBDocStorage(String projID) throws ParseException, MalformedURLException {

       dbName = projID;
       dbcon = new CloundantIntfc();
       url = dbcon.getUrl()+"/"+dbName;
       if(dbcon.isDBExists(dbName)){
           db = dbcon.getDB(dbName);
       }
       else
    	   db=null;
      // dbcon.printAllDBs();

    }
    public static Boolean doesProjectExist(String projName) throws MalformedURLException, ParseException{
    	return(new CloundantIntfc().isDBExists(projName));
    }
    @Override
    public InputStream findImage(String name){
        return db.find(name);
    }
    @Override
    public String storeImage(InputStream inputStream, String name, String contentType,String docFileName) {
    		String id;
    		String rev=null;
    		if(!db.contains(docFileName)){
    			DocFileRecord data = new DocFileRecord(docFileName);
    			Response response = db.save(data);
    			 id = response.getId();
    			 rev = response.getRev();
    			 logger.debug("Created  file {} with id {}",docFileName,id);
    		}
    		else {
    			DocFileRecord data = db.find(DocFileRecord.class,docFileName);
    			id = data.get_id();
    			rev = data.get_rev();
    		}
    		//logger.debug("Created Image {} from file {} with id {}",name,docFileName,id);
	logger.debug("Created Image {} from file {} with id {}", new Object[]{name,docFileName,id});
            Response response = db.saveAttachment(inputStream, name, contentType,id,rev);
            
            return(response.getId());
            //System.out.print(response.toString() +" for "+name + "("+contentType+")");
    }
    
    
    @Override
    public String getLink(){
        return url;
    }
    @Override
     public String storeStringFile(String fileContent, String name) {
            byte[] bytesToDB = fileContent.getBytes();
            ByteArrayInputStream bytesIn = new ByteArrayInputStream(bytesToDB);
            Response response = db.saveAttachment(bytesIn, name, "text/html");
            return(response.getId());
            //System.out.print(response.toString() +" for "+name + "("+contentType+")");
    }
    @Override
    public void deleteImages(String fileName){
    	if(db.contains(fileName)){
    		DocFileRecord data = db.find(DocFileRecord.class,fileName);
			db.remove(data.get_id(), data.get_rev());
		}
    }
    @Override
    public boolean contains(String fileName){
    	return db.contains(fileName);
    }

    @Override
    public void deleteDB() {
        logger.debug("Deleting document database for project: {}", dbName);
        dbcon.deleteDB(dbName);
    }
    @Override
    public void createDB() {
        logger.debug("Creating document database for project: {}", dbName);
        dbcon.createDB(dbName);
    }

	
   
}
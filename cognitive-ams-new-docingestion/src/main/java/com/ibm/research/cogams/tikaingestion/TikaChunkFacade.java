package com.ibm.research.cogams.tikaingestion;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.List;
import java.util.logging.Level;

import javax.servlet.http.Part;

import org.apache.tika.exception.TikaException;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ibm.research.cogams.docanalyzer.DocumentAnalyzer;
import com.ibm.research.cogams.docanalyzer.DocumentAnalyzerFactory;
import com.ibm.research.cogams.docingestion.AppLevelGlobals;
import com.ibm.research.cogams.docingestion.DocumentStream;
import com.ibm.research.cogams.docstorage.DocStorageFactory;
import com.ibm.research.cogams.docstorage.DocStorageIntfc;
import com.ibm.research.cogams.docstorage.MongoIntfc;

public class TikaChunkFacade {
	 	private static final Logger logger = LoggerFactory.getLogger(TikaChunkFacade.class);
	    
	    String projID;
	   
	    public TikaChunkFacade(String projID) throws IOException, ParseException{
	       //convert project name to a valid project name for solrcore and cloudant
	       this.projID = projID;
	    }
	    
	    public static void initAppProperties(String propertiesFilePath){
	    	AppLevelGlobals.init(propertiesFilePath);
	    }
	    
	    public static String convertToValidProjectName(String projID){
	    	//couchdb needs only lowercase 
	    	String validName = projID.toLowerCase();
	    	return validName;
	    }
	    
	    public static List<String> getAllProjects() throws MalformedURLException, ParseException{
	    	return MongoIntfc.getInstance().getProjectNames();
	    } 
	    
	    public static Boolean doesProjectExist(String projID) throws MalformedURLException, ParseException{
	    	return MongoIntfc.getInstance().doesProjectExist(projID);
	    }
	    
	    public static void createProject(String projID) throws Exception{
	    	try {
	    		DocStorageIntfc docS;
				docS = DocStorageFactory.createDocStorageInstance(projID);
				docS.createDB();
				MongoIntfc.getInstance().addProject(projID);
			} catch(Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	        
	    }
	    
	    public static void deleteProject(String projID){
	    	DocStorageIntfc docS;
			try {
				docS = DocStorageFactory.createDocStorageInstance(projID);
				docS.deleteDB();
				DocumentStream.deleteFromFS(projID);
				MongoIntfc.getInstance().deleteProject(projID);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    public String chunk(Part filePart) throws IOException, TikaException, Exception {
        		DocumentStream doc  = getDocumentFromFilePart(filePart);
	            String chunks = chunk(doc);
	            return chunks;
	    }
	    
	    public String chunk(String filePath) throws FileNotFoundException, IOException, TikaException, Exception{
	        FileInputStream fis = null;
	        String []filePathElements = filePath.split("/");
	        String fileName = filePathElements[filePathElements.length-1];
	        fis = new FileInputStream(filePath);
	        DocumentStream doc = new DocumentStream(fis,fileName,projID);
	        String chunks  = chunk(doc);
	        return chunks;
	    }
	     public String chunkFile(File file) {
	        FileInputStream fis = null;
	        String fileName = file.getName();
	        try {
	            fis = new FileInputStream(file);
	        } catch (FileNotFoundException ex) {
	            java.util.logging.Logger.getLogger(TikaChunkFacade.class.getName()).log(Level.SEVERE, null, ex);
	            return null;
	        }
	        DocumentStream doc;
	        try {
	            doc = new DocumentStream(fis,fileName,projID);
	        } catch (IOException | TikaException ex) {
	            java.util.logging.Logger.getLogger(TikaChunkFacade.class.getName()).log(Level.SEVERE, null, ex);
	            return null;
	        }
	        String chunks;
	        try {
	            chunks = chunk(doc);
	            return chunks;

	        } catch (MalformedURLException ex) {
	            java.util.logging.Logger.getLogger(TikaChunkFacade.class.getName()).log(Level.SEVERE, null, ex);
	            return null;
	        } catch (Exception ex) {
	            java.util.logging.Logger.getLogger(TikaChunkFacade.class.getName()).log(Level.SEVERE, null, ex);
	            return null;
	        }
	    }
	    
	    protected String chunk(DocumentStream doc) throws ParseException, MalformedURLException, Exception{
	            DocumentAnalyzer docAnalyzer = DocumentAnalyzerFactory.createDocumentAnalyzer(doc);
	            doc.storeToFS();
	            // Pass the file to tika -> get HTML -> modify image embeddings -> enhance HTML (topics,heading qualification)
	            String chunks = docAnalyzer.chunk(doc);
	            
	            return chunks;
	        
	    }
	    
	   
	  
	    public void deleteDocument(String fileName) throws IOException{
	    	DocumentStream.deleteFromFS(projID, fileName);
	    	try {
				DocStorageIntfc docStorage = DocStorageFactory.createDocStorageInstance(projID);
				DocumentStream.deleteFromFS(projID,fileName);
				if(docStorage.contains(fileName))
					docStorage.deleteImages(fileName);
				else 
					throw new IOException("Document "+ fileName + " in project " + projID + " doesnot exist"); 
			} catch (MalformedURLException | ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	    
	    public DocumentStream getDocumentFromFilePart(Part filePart) throws IOException, TikaException{
	    	InputStream fileContent = null;
            fileContent = filePart.getInputStream();
            DocumentStream doc = new DocumentStream(fileContent,filePart.getSubmittedFileName(),projID);
            return doc;
	    }

	    public String convertToHTML(Part filePart) {
	        try {
	            
	        	DocumentStream doc  = getDocumentFromFilePart(filePart);
	            DocumentAnalyzer docAnalyzer = DocumentAnalyzerFactory.createDocumentAnalyzer(doc);
	            
	            // Pass the file to tika -> get HTML -> modify image embeddings -> enhance HTML (topics,heading qualification)
	            String enhancedHTML = docAnalyzer.convertToHTML(doc);
	            
	            return enhancedHTML;
	            
	        } catch (TikaException ex) {
	            java.util.logging.Logger.getLogger(TikaChunkFacade.class.getName()).log(Level.SEVERE, null, ex);
	        } catch (IOException ex) {
	            java.util.logging.Logger.getLogger(TikaChunkFacade.class.getName()).log(Level.SEVERE, null, ex);
	        } catch (Exception ex) {
	            java.util.logging.Logger.getLogger(TikaChunkFacade.class.getName()).log(Level.SEVERE, null, ex);
	        }
	        return "";
	    }
	    
	    public String getTextFromDoc(Part filePart) throws Exception{
        	DocumentStream doc  = getDocumentFromFilePart(filePart);
        	DocumentAnalyzer docAnalyzer = DocumentAnalyzerFactory.createDocumentAnalyzer(doc);
        	return docAnalyzer.getText(doc);

	    }
		
}

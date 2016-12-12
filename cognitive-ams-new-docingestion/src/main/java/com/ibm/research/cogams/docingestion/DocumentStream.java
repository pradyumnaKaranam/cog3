/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.research.cogams.docingestion;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sampath
 */
public class DocumentStream {
	
	private static final Logger logger = LoggerFactory.getLogger(DocumentStream.class);
    byte [] input;
    MediaType mType;
    String fileName;
    String projID;
    UUID docID;
    
    /**
     *
     * @param f
     * @param fileName
     * @param projID
     */
    public DocumentStream(InputStream f,String fileName,String projID) throws IOException, TikaException{
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            org.apache.commons.io.IOUtils.copy(f, baos);
        } catch (IOException ex) {
        	logger.error("Exception occurred while created documentstream",ex);
        }
        input = baos.toByteArray();
        this.projID=projID;
        this.docID = UUID.randomUUID();
        this.fileName=fileName;
        mType = detectContent();
        
    }
    private MediaType detectContent() throws TikaException, IOException {
        TikaConfig tika = new TikaConfig();
        MediaType mimeType = tika.getDetector().detect(
        TikaInputStream.get(getInputStream()), new Metadata());
        return mimeType;
    }
    public static void deleteFromFS(String projID){
    	String uploadDir = AppLevelGlobals.getInstance().getFileUploadDIR();
    	if(!AppLevelGlobals.getInstance().isOnCloud() && uploadDir!=null){
	    	File imgdir = new File(uploadDir+"/"+projID);
			if(imgdir.exists()){
				String[]entries = imgdir.list();
				for(String s: entries){
					//delete file directories with images
					deleteFromFS(projID,s);
				}
				imgdir.delete();
			}
    	}
    }
    public static boolean deleteFromFS(String projID, String fileName) {
		String uploadDir = AppLevelGlobals.getInstance().getFileUploadDIR();

    	if(!AppLevelGlobals.getInstance().isOnCloud() && uploadDir!=null){
			String filePath = uploadDir+"/"+projID+"/"+fileName;
			Path fileToDeletePath = Paths.get(filePath);
			try {
				boolean result = Files.deleteIfExists(fileToDeletePath);
				return result;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}
    	}
    	else {
    		return false;
    	}
    	
    }
    public Boolean storeToFS() throws IOException{
        if(!AppLevelGlobals.getInstance().isOnCloud() && AppLevelGlobals.getInstance().getFileUploadDIR() !=null ){
            String uploadDir = AppLevelGlobals.getInstance().getFileUploadDIR();
            String id = UUID.randomUUID().toString();
            String nOutFileName = projID+"/"+fileName;
            //String nOutFileName = name.substring(0,name.indexOf('.'))+"_"+id+name.substring(name.indexOf('.'));

            File outputFile = new File(uploadDir, FilenameUtils.normalize(nOutFileName));
            File parent = outputFile.getParentFile();
            if (!parent.exists()) {
                if (!parent.mkdirs()) {
                    try {
                        throw new IOException("unable to create directory \"" + parent + "\"");
                    } catch (IOException ex) {
                    	logger.error("Exception occurred while while storing document to FS",ex);
                        return false;
                    }
                }
            }
            logger.debug("Stored file: {}",outputFile.toPath());
            Files.copy(getInputStream(), outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        }
        return false;
        
    }
    
    
    
    /**
     *
     * @return
     */
    public InputStream getInputStream(){
        return new ByteArrayInputStream(input);
    }

    /**
     *
     * @return
     */
    public MediaType getMediaType() {
        return mType;
    }
    
    /**
     *
     * @return
     */
    public String getProjID() {
        return projID;
    }
    
    
    public String getType(){
        return mType.toString();
    }
    /**
     *
     * @param mType
     */
    public void setMediaType(MediaType mType) {
        this.mType = mType;
    }

    /**
     *
     * @return
     */
    public String getDocID() {
        return docID.toString();
    }

    /**
     *
     * @return
     */
    public String getFileName(){
        return fileName;
    }
    
    public Boolean isPDF(){
        
        return("application/pdf".equals(mType.toString()));
    }
    public Boolean isZIP(){
        
        return("application/zip".equals(mType.toString()));
    }
    public Boolean isDOC(){
        return("application/msword".equals(mType.toString()));
    }
    public Boolean isDOCX(){
        return("application/vnd.openxmlformats-officedocument.wordprocessingml.document".equals(mType.toString()));
    }
    public Boolean isPPTX(){
        
        return("application/vnd.openxmlformats-officedocument.presentationml.presentation".equals(mType.toString()));
                
    }
    public Boolean isPPT(){
        return("application/vnd.ms-powerpoint".equals(mType.toString()));
    }
    public Boolean isHTML(){
    	return("text/html".equals(mType.toString()));
    }
    
}

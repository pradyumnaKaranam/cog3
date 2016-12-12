package com.ibm.research.cogams.docstorage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;

public class MongDBDocStorage {
	private static final Logger logger = LoggerFactory.getLogger(MongDBDocStorage.class);
	private String username;
	private String password;
	MongoClient client;
	private String host;
	private int port;
	private String dbName;
	DB db;
	GridFS docsFS;
	GridFS imagesFS;
	public MongDBDocStorage(String projID){
		host = "localhost";
		port = 27017;
		dbName = projID;
		//String uri = String.format("mongodb://%s:%s@%s:%d/%s", username,password,host,port,dbName);
		String uri = String.format("mongodb://%s:%d", host,port);
		logger.debug("Mongo URI: {}",uri);
		client = new MongoClient(new MongoClientURI(uri));
		db = client.getDB(dbName);
		docsFS = new GridFS(db,"documents");
		imagesFS = new GridFS(db,"images");
	}
	
	public void storeFile(InputStream in,String filename){
		GridFSInputFile gfsFile = docsFS.createFile(in);
		gfsFile.setFilename(filename);
		gfsFile.save();
	}
	public void getFile(String filename) throws IOException{
		String dummyLocation = "/Users/sampath/files/tmp";
		GridFSDBFile gfsFile = docsFS.findOne(filename);
		InputStream i = gfsFile.getInputStream();
		File targetFile = new File(dummyLocation,filename);
	    OutputStream outStream = new FileOutputStream(targetFile);
	    IOUtils.copy(i,outStream);
	}
	public void deleteFile(String filename) {
		GridFSDBFile gfsFile = docsFS.findOne(filename);
		docsFS.remove(gfsFile);
	}
	public void listFiles(){
		DBCursor cursor = docsFS.getFileList();
		while (cursor.hasNext()) {
			System.out.println(cursor.next());
		}
	}
	
}

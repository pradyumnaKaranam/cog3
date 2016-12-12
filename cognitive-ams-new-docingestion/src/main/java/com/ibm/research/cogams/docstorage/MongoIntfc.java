package com.ibm.research.cogams.docstorage;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
public class MongoIntfc {
	
	private static final Logger logger = LoggerFactory.getLogger(MongoIntfc.class);
	private static MongoIntfc intfc;
	private String username;
	private String password;
	MongoClient client;
	private String host;
	private int port;
	private String dbName;
	MongoDatabase db;
	MongoCollection<Document> projectsCollection;
	public MongoIntfc(){
		host = "localhost";
		port = 27017;
		dbName = "docingestion";
		//String uri = String.format("mongodb://%s:%s@%s:%d/%s", username,password,host,port,dbName);
		String uri = String.format("mongodb://%s:%d", host,port);
		logger.debug("Mongo URI: {}",uri);
		client = new MongoClient(new MongoClientURI(uri));
		db = client.getDatabase(dbName);
		if(db.getCollection("projects")==null){
			db.createCollection("projects");
		}
		projectsCollection = db.getCollection("projects");
	}
	public static MongoIntfc getInstance(){
		if(intfc == null) intfc = new MongoIntfc();
		return intfc;
	}
	public void addProject(String projID){
		if(!doesProjectExist(projID)){
			Document dbObj = new Document();
			dbObj.append("name", projID);
			dbObj.append("createdDate", new Date());
			projectsCollection.insertOne(dbObj);
		}
	}
	
	public List<String> getProjectNames(){
		ArrayList<String> oList = new ArrayList<String>();
		MongoCursor<Document> iter = projectsCollection.find().iterator();
		while(iter.hasNext()){
			Document d = iter.next();
			oList.add(d.getString("name"));
		}
		return oList;
	}
	
	public Document getProject(String projID){
		Document dbObj = new Document();
		dbObj.append("name", projID);
		MongoCursor<Document> cur = projectsCollection.find(dbObj).limit(1).iterator();
		if(cur.hasNext()) return cur.next();
		else return null;
	}
	public boolean doesProjectExist(String projID){
		return getProject(projID)!=null;
	}
	
	public void deleteProject(String projID){
		Document dbObj = new Document();
		dbObj.append("name", projID);
		projectsCollection.findOneAndDelete(dbObj);
	}
	/*
	public static void testGridFS(){
		String newFileName = "my-image";
		File imageFile = new File("/users/victor/images/image.png");
		GridFS gfsPhoto = new GridFS(db, "photo");
		GridFSInputFile gfsFile = gfsPhoto.createFile(imageFile);
		gfsFile.setFilename(newFileName);
		gfsFile.save();
	}
	*/
	
}

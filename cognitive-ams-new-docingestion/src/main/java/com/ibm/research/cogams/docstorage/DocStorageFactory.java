package com.ibm.research.cogams.docstorage;

public class DocStorageFactory {
	
	static public DocStorageIntfc createDocStorageInstance(String projID) throws Exception {
		DocStorageIntfc docStorage= new FSDocStorage(projID);
		return docStorage;
	}

}

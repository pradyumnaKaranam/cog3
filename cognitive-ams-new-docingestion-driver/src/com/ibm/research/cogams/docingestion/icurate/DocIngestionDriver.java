package com.ibm.research.cogams.docingestion.icurate;

import java.io.IOException;

import javax.sql.DataSource;

import com.ibm.research.cogassist.common.CogAssist;
import com.ibm.research.cogassist.common.DatabaseManager;
import com.ibm.research.cogassist.json.JSONArray;
import com.ibm.research.cogassist.json.JSONObject;


public class DocIngestionDriver {	
	public static void ingestDocs (Integer accountId, String accountName, String language) throws IOException  
	{
		JSONObject res = ChunkDocuments.processDocuments(accountId, accountName,language);
		if (res.getInt("parsed")>0)
			IndexDocuments.indexDocuments(accountId, accountName);
                
		JSONArray rejected = res.getJSONArray("rejected");
		JSONArray accepted = res.getJSONArray("accepted");
		try {
		DataSource ds = DatabaseManager.getDataSource();
		for(int i=0; i<accepted.length(); i++)
		{
			String filename = accepted.getJSONObject(i).getString("filename");
			String status = "COMPLETED";
			CogAssist.updateDocumentStatus(ds, accountName, status, filename);
		}
		for(int i=0; i<rejected.length(); i++)
		{
			String filename = rejected.getJSONObject(i).getString("filename");
			String status = "FAILED";
			String reason = rejected.getJSONObject(i).getString("reason");
			CogAssist.updateDocumentStatus(ds, accountName, status, filename, reason);
		}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		try {
			DocIngestionDriver.ingestDocs(216, "ABC", "en");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}

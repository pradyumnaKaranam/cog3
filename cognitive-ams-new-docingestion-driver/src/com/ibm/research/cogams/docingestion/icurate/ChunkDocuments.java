package com.ibm.research.cogams.docingestion.icurate;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.ibm.research.cogams.docingestion.icurate.common.Constants;
import com.ibm.research.cogassist.common.CogAssist;
import com.ibm.research.cogassist.docingestion.DocIngestion;
import com.ibm.research.cogassist.json.JSONArray;
import com.ibm.research.cogassist.json.JSONObject;
import com.ibm.research.cogassist.utils.Utils;

public class ChunkDocuments {
	
	public static JSONObject processDocuments(Integer accountId,
			String accountName, String language) throws IOException {
		if (Constants.isoCodeLangMap.containsKey(language)) {
			return chunkDocuments(accountId, accountName, language);
		} else {
			return getErrorJSONObject("Language not supported");
		}

	}

	private static JSONObject getErrorJSONObject(String errorMessage) {
		return new JSONObject().put("error", errorMessage);
	}

	public static JSONObject chunkDocuments(Integer accountId,
			String accountName, String language) throws IOException {
		String dumplocation = CogAssist.getDumpLocationName(
				accountId.toString(), language);
		String projlocation = DocIngestion.getProjectLocation(accountId
				.toString());
		String csvFile = DocIngestion.getCsvDataPath(accountId.toString());
		if (dumplocation == null || projlocation == null) {
			System.out.println("Could not identify dumplocation for account : "
					+ accountName);
			return getErrorJSONObject("Could not identify dumplocation for account : "
					+ accountName);

		}
		if (!new File(dumplocation).isDirectory()) {
			System.out.println("No file dump to process : " + dumplocation);
			return getErrorJSONObject("No file dump to process : "
					+ dumplocation);

		}

		String rejectedListFile = projlocation + "/rejected.txt";
		String parsedFileLocation = projlocation + "/parsedFiles";
		// String xmlLocation = projlocation+"/index_xmls";

		// Delete the previously existing rejected.txt and parsedFiles documents
		File rejectedListFilePath = new File(rejectedListFile);
		File parseFileDir = new File(parsedFileLocation);
		File csvFilePath = new File(csvFile);

		if (rejectedListFilePath.exists()) {
			System.out.println(" Delete Rejected.txt .."
					+ rejectedListFilePath.getAbsolutePath());
			FileUtils.deleteQuietly(rejectedListFilePath);
		}
		if (parseFileDir.exists()) {
			System.out.println(" Delete Parsed Files Dir .."
					+ parseFileDir.getAbsolutePath());
			FileUtils.deleteDirectory(parseFileDir);
		}
		if (csvFilePath.exists()) {
			System.out.println(" Delete data.csv .."
					+ csvFilePath.getAbsolutePath());
			FileUtils.deleteQuietly(csvFilePath);
		}

		JSONObject finalObj = CreateCommonXML.processDump(accountName,
				accountId, dumplocation, csvFile, language);
		JSONArray accepted = finalObj.getJSONArray("accepted");
		JSONArray rejected = finalObj.getJSONArray("rejected");
		int total = accepted.length() + rejected.length();

		/*
		 * JSONArray retJSArray=null; retJSArray = CSVtoXML.createXML(csvFile,
		 * xmlLocation, dumplocation); JSONObject finalObj = new JSONObject();
		 * finalObj.put("rejected", rejected); finalObj.put("data", retJSArray);
		 * return finalObj;
		 */

		Utils.writeFile(rejectedListFile, rejected.toString());
		int parsed = (total - rejected.length());
		System.out.println("Total Documents = " + total + ", Parsed = "
				+ parsed + ", Rejected = " + rejected.length());
		return new JSONObject().put("total", total).put("parsed", parsed)
				.put("rejected", rejected).put("accepted", accepted);
	}

	public static void main(String[] args) {
		try {
			ChunkDocuments.chunkDocuments(1, "docingest", "en");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
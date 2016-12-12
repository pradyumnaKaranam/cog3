package com.ibm.research.cogams.docingestion.icurate;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.simple.parser.ParseException;

import com.ibm.research.cogams.docingestion.icurate.common.Constants;
import com.ibm.research.cogams.tikaingestion.TikaChunkFacade;
import com.ibm.research.cogassist.common.CogAssist;
import com.ibm.research.cogassist.docingestion.DocIngestion;
import com.ibm.research.cogassist.json.JSONArray;
import com.ibm.research.cogassist.json.JSONObject;
import com.ibm.research.cogassist.utils.Utils;

public class CreateCommonXML {

	public static void main(String[] args) {
		String docFolder = "/Users/senthil/test-doc-ingestion";
		String csvFile = "/Users/senthil/test.csv";
		CreateCommonXML.processDump("docingest", 1, docFolder, csvFile, "es");
	}

	public static JSONObject processDump(String projName, int projID,
			String docFolder, String csvFile, String language) {
		File docFolderHandle = new File(docFolder);
		List<File> filesToBeProcessed = Utils
				.getNestedFilesFromFolder(docFolderHandle);

		JSONObject finalObj = new JSONObject();
		JSONArray accepted = new JSONArray();
		JSONArray rejected = new JSONArray();
		TikaChunkFacade.initAppProperties(CogAssist.getHomeDirectoryName());
		System.out.println("Begin Chunking.....");
		for (File docFile : filesToBeProcessed) {
			try {
				System.out
						.println("com.ibm.research.cogams.docingestion.icurate.CreateCommonXML.processDump(): "
								+ docFile.getAbsolutePath()
								+ "-"
								+ docFile.getName() + "-" + projID);

				TikaChunkFacade tf = new TikaChunkFacade(projName);
				String chunks = tf.chunkFile(docFile);

				if (chunks == null) {
					rejected.add(new JSONObject()
							.put("filename", docFile.getName())
							.put("reason",
									"Document Parsing Error / May not be a supported format"));
					System.out
							.println("com.ibm.research.cogams.docingestion.icurate.CreateCommonXML.processDump(): Error while ingesting "
									+ docFile.getName());
				} else {
					accepted.add(new JSONObject().put("filename",
							docFile.getName()));
					System.out
							.println("com.ibm.research.cogams.docingestion.icurate.CreateCommonXML.processDump(): Successful ingested "
									+ docFile.getName());
					try {
						writeCSVFileForChunks(projName, projID,
								docFile.getName(), chunks, csvFile, language);
					} catch (Exception ex) {
						Logger.getLogger(CreateCommonXML.class.getName()).log(
								Level.SEVERE, null, ex);

					}

				}

			} catch (IOException | ParseException ex) {
				Logger.getLogger(CreateCommonXML.class.getName()).log(
						Level.SEVERE, null, ex);
				rejected.add(new JSONObject()
						.put("filename", docFile.getName())
						.put("reason",
								"Document Parsing Error / May not be a supported format"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		System.out.println("End Chunking");
		return finalObj.put("accepted", accepted).put("rejected", rejected);
	}

	public static void writeCSVFileForChunks(String projName, int projID,
			String docName, String chunks, String csvFile, String language)
			throws Exception {
		String csvOutput = String
				.format("DOCUMENT_ID,CHUNK_ID,PROJECT_ID,CHUNK_URL,DOCUMENT_URL,LANGUAGE,ISO_CODE,BREADCRUMB_%s,FILENAME_%s,CHUNKTEXT_%s,HIFITEXT_%s\n",
						language, language, language, language);
		org.json.JSONObject jsonObject = new org.json.JSONObject(chunks);
		org.json.JSONArray chunksArray = jsonObject
				.getJSONArray("answer_units");
		System.out.println("Total # of chunks:" + chunksArray.length());

		for (int i = 0; i < chunksArray.length(); i++) {
			String FILENAME = docName;
			int DOCUMENT_ID = docName.hashCode();
			int CHUNK_ID = i + 1;
			int PROJECT_ID = projID;
			String CHUNK_URL = DocIngestion.getNodeAppDocLocation(projName)
					+ File.separator + language + File.separator;
			String DOCUMENT_URL = DocIngestion.getNodeAppDocLocation(projName)
					+ File.separator + language + File.separator;
			String LANGUAGE = "English";
			if (Constants.isoCodeLangMap.containsKey(language))
				LANGUAGE = Constants.isoCodeLangMap.get(language);
			String ISO_CODE = language;
			org.json.JSONObject iObj = chunksArray.getJSONObject(i);
			String BREADCRUMB = iObj.getString("title").replaceAll("-", " ");
			BREADCRUMB = BREADCRUMB.replaceAll("[\\u2018\\u2019]", "'")
					.replaceAll("[\\u201C\\u201D]", "\"");
			BREADCRUMB = BREADCRUMB.replaceAll("\"", "\\\\\"");
			BREADCRUMB = BREADCRUMB.replaceAll(",", " ");
			String CHUNKTEXT = iObj.getJSONArray("content").getJSONObject((1))
					.getString("text");
			CHUNKTEXT = CHUNKTEXT.replaceAll("[\\u2018\\u2019]", "'")
					.replaceAll("[\\u201C\\u201D]", "\"");
			CHUNKTEXT = CHUNKTEXT.replaceAll("\"", "\\\\\"");
			CHUNKTEXT = CHUNKTEXT.replaceAll(",", " ");

			String HIFITEXT = iObj.getJSONArray("content").getJSONObject((0))
					.getString("text");
			// System.out.println(" HiFiText "+i+":"+HIFITEXT);
			HIFITEXT = HIFITEXT.replaceAll("[\\u2018\\u2019]", "'").replaceAll(
					"[\\u201C\\u201D]", "\"");
			HIFITEXT = HIFITEXT.replaceAll("\"", "\\\\\"");
			HIFITEXT = HIFITEXT.replaceAll(",", "&#44");

			CHUNKTEXT = String.format("\"%s\"", CHUNKTEXT);
			HIFITEXT = String.format("\"%s\"", HIFITEXT);
			BREADCRUMB = String.format("\"%s\"", BREADCRUMB);
			if (CHUNKTEXT.length() > 2 && HIFITEXT.length() > 2)
				csvOutput += DOCUMENT_ID + "," + DOCUMENT_ID + "_" + CHUNK_ID
						+ "," + PROJECT_ID + "," + CHUNK_URL + ","
						+ DOCUMENT_URL + "," + LANGUAGE + "," + ISO_CODE + ","
						+ BREADCRUMB + "," + FILENAME + "," + CHUNKTEXT + ","
						+ HIFITEXT + "\n";

		}
		Utils.writeStringToFile(csvFile, csvOutput);

	}

}

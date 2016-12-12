package com.ibm.research.cogams.docingestion.icurate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;

import com.ibm.research.cogams.docingestion.icurate.common.Constants;
import com.ibm.research.cogams.docingestion.icurate.utils.ServerUtils;
import com.ibm.research.cogassist.common.CogAssist;
import com.ibm.research.cogassist.common.DatabaseManager;
import com.ibm.research.cogassist.docingestion.DocIngestion;
import com.ibm.research.cogassist.utils.CsvReader;


public class IndexDocuments {

	public static void indexDocuments(Integer accountId, String accountName) throws IOException {

		// For a give account - first go and fetch the csv file from the server
		// location

		String csvFile = DocIngestion.getCsvDataPath(accountId.toString());
		// TODO : Do some validation on the CSV to make sure it is amenable for
		// indexing into SOLR Index

		String SOLR_CORE = accountName + "_DOCS";
		String solrURL = DocIngestion.getProperty(DocIngestion.DB_SOLR_URL) + "/" + SOLR_CORE;
		// this needs to be changed by checking the
		// project table to see if the document URL
		// already exist or not
		boolean append = isSOLRCOREExist(accountName, SOLR_CORE);

		// Get the HTTP SOLR Server connecting to the core (if not, then create
		// the core)
		SolrServer server = null;
		String userName = "";
		String password = "";
		try {

			server = ServerUtils.initHTTPSolrServer(
					DocIngestion.getProperty(DocIngestion.SOLR_URL),
					Constants.DEFAULT_INDEX_NAME, SOLR_CORE, userName,
					password, append);

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Parse the CSV line - by - line
		// Create the SOLR Input document (with 10k docs as limit)
		// Index it into the SOLR Core and commit

		CsvReader csvReader = null;
		SolrInputDocument doc = null;
		List<SolrInputDocument> documents = new ArrayList<SolrInputDocument>();

		System.out.println("Creating SOLR Input Documents....");
		try {
			csvReader = new CsvReader(csvFile);
			csvReader.setEscapeMode(2);
			String[] headers = null;
			int i = 0;
			
			while (csvReader.readRecord()) {

				if (i == 0) {
					// reader the header column (for the solr schema fields)
					headers = csvReader.getValues();
					i++;
				} else {
					// get the row
					String[] values = csvReader.getValues();

					// some cases -- check if the header row is repeating, skip this entry 
					if(values[0].equalsIgnoreCase("DOCUMENT_ID"))
						continue;
					int index = 0;
					String documentUrl = "";
					String iso_code = "";
					for (int j = 0; j < values.length; j++) {
						if (index == 0) {
							// create a new input document for every row
							doc = new SolrInputDocument();
						}
						index++;
						if (values[j] != null && !values[j].equals("")) {
							// if value for that cell exist then add it to the
							// document
							if(headers[j].equals("DOCUMENT_URL")){
								documentUrl = values[j];
							}
						    if(headers[j].equals("ISO_CODE")){
								iso_code = values[j];
							}
						    
						    if(!headers[j].equals("DOCUMENT_URL")){
								doc.addField(headers[j], values[j]);
							}
							
						}	
					}
					// add the document url alone (if it exist)
					doc.addField("DOCUMENT_URL", "documents" + File.separator + accountName + File.separator + iso_code + documentUrl);
					
					// add the document to the list
					documents.add(doc);


					// if the list size is > 10K, then index the documents into
					// SOLR and clear the list
					if (documents.size() >= 10000) {
						System.out.println("Indexing Documents ..."
								+ documents.size());
						server.add(documents);
						server.commit();
						documents.clear();
					}

				}// end of else

			}// end of while

		} catch (FileNotFoundException e) {
			System.out.println(" CSV File not found");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println(" Error while Indexing Documents ");
			e.printStackTrace();
		} catch (SolrServerException e) {
			System.out.println(" Error while Indexing Documents ");
			e.printStackTrace();
		} finally {
			if (csvReader != null)
				csvReader.close();
		}

		System.out
				.println("Adding the final remaining documents to server and committing...");
		// adding the final reamining documents to SOLR index
		if (documents.size() > 0) {
			System.out.println(documents.get(0));
			System.out.println("Indexing Documents ..." + documents.size());
			try {
				server.add(documents);
				server.commit();
			} catch (SolrServerException | IOException e) {
				System.out.println(" Error while Indexing Documents ");
				e.printStackTrace();
			}

		}

		if (!append) {			
			updateSOLRURLForAccount(accountName, solrURL);
		}
	}

	private static void updateSOLRURLForAccount(String accountName,
			String solrURL) {
		try {
			DataSource ds = DatabaseManager.getDataSource();
			CogAssist.updateSearchUrlForAccount(ds, accountName, solrURL);
		} catch (Exception e) {
			System.out.println("Error while updating database");
			e.printStackTrace();
		}

	}

	private static boolean isSOLRCOREExist(String accountName, String SOLR_CORE) {
		try {
			DataSource ds = DatabaseManager.getDataSource();
			String url = CogAssist.checkSearchUrlForAccount(ds, accountName);

			if (url == null || url.equalsIgnoreCase("null") || url.equals("")) {
				System.out.println("*****returning false");
				return false;
			} else
				return true;
		} catch (Exception e) {
			System.out.println("Error while accessing database");
			e.printStackTrace();
			return false;
		}
	}

	public static void main(String[] args) {
		try {
			IndexDocuments.indexDocuments(1, "Aribus");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
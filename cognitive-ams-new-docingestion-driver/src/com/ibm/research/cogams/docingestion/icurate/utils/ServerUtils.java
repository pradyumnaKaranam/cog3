package com.ibm.research.cogams.docingestion.icurate.utils;

import java.io.File;
import java.io.IOException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.FileUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.core.CoreContainer;

public class ServerUtils {
	
	static CoreContainer container = null;

	public static void shutDownEmbeddedServer() {
		container.shutdown();
	}

	/*public static SolrServer initHTTPSolrServerUsingTemplateIndex(
			String solr_url, String accountName, String userId,
			String password, boolean append, String templateIndexName)
			throws HttpException, IOException {
		if (!append) {

			createFromTemplateSolrIndex(solr_url, accountName, userId,
					password, templateIndexName);

			// loadDefaultSolrIndex(solr_url, accountName, userId, password);
		}

		SolrServer server = new HttpSolrServer(solr_url + "/" + accountName);

		return server;
	}*/

	public static SolrServer initHTTPSolrServer(String solr_url,
			String solr_index, String accountName, String userId,
			String password, boolean append) {
		
		if (!append) {
			String solr_home = System.getProperty("solr.solr.home");
			
			try {
				FileUtils.copyDirectory(new File(solr_home + File.separator
						+ solr_index), new File(solr_home + File.separator
						+ accountName));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			loadDefaultSolrIndex(solr_url, accountName, userId, password);
		}

		SolrServer server = new HttpSolrServer(solr_url + "/" + accountName);

		return server;

	}

	/*private static void createFromTemplateSolrIndex(
			String defaultEmbeddedSolrUrl, String accountName, String userId,
			String password, String templateIndexName) throws HttpException,
			IOException {

		HttpClient client = new HttpClient();

		String solrHome = System.getProperty("solr.solr.home");

		// make the initial get to get the JSESSION cookie
		// http://localhost:8080/solr/admin/cores?action=CREATE&instanceDir=453&name=453
		GetMethod get = new GetMethod(defaultEmbeddedSolrUrl
				+ "/admin/cores?action=CREATE&instanceDir=" + accountName
				+ "&name=" + accountName + "&config=" + solrHome + "/"
				+ templateIndexName + "/conf/solrconfig.xml&schema=" + solrHome
				+ "/" + templateIndexName + "/conf/schema.xml");

		int responseCode = client.executeMethod(get);

		get.releaseConnection();

		System.out.println("Response code from CREATE SOLR Index "
				+ responseCode);

		if (userId != null && !userId.equals("")) {

			// authorize
			PostMethod post = new PostMethod(defaultEmbeddedSolrUrl
					+ "/j_security_check");
			NameValuePair[] data = { new NameValuePair("j_username", userId),
					new NameValuePair("j_password", password) };
			post.setRequestBody(data);
			client.executeMethod(post);
			post.releaseConnection();

			// resubmit the original request
			client.executeMethod(get);
			String response = get.getResponseBodyAsString();
			get.releaseConnection();
			System.out.println(response);
		}

	}*/

	private static void loadDefaultSolrIndex(String defaultEmbeddedSolrUrl,
			String accountName, String userId, String password) {

		try {

			HttpClient client = new HttpClient();

			// make the initial get to get the JSESSION cookie
			// http://localhost:8080/solr/admin/cores?action=CREATE&instanceDir=453&name=453
			GetMethod get = new GetMethod(defaultEmbeddedSolrUrl
					+ "/admin/cores?action=CREATE&instanceDir=" + accountName
					+ "&name=" + accountName + "&config=solrconfig.xml&schema=schema.xml");

			int responseCode = client.executeMethod(get);

			get.releaseConnection();

			System.out.println("Response code from CREATE SOLR Index "
					+ responseCode);

			if (userId != null && !userId.equals("")) {

				// authorize
				PostMethod post = new PostMethod(defaultEmbeddedSolrUrl
						+ "/j_security_check");
				NameValuePair[] data = {
						new NameValuePair("j_username", userId),
						new NameValuePair("j_password", password) };
				post.setRequestBody(data);
				client.executeMethod(post);
				post.releaseConnection();

				// resubmit the original request
				client.executeMethod(get);
				String response = get.getResponseBodyAsString();
				get.releaseConnection();
				System.out.println(response);
			}
		} catch (HttpException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}

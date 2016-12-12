package com.ibm.research.cogams.docingestion.icurate.utils;

public class SOLRSchema {

	public static final String DOCUMENT_ID = "DOCUMENT_ID";
	public static final String CHUNK_ID = "CHUNK_ID";
	public static final String PROJECT_ID = "PROJECT_ID";
	public static final String CHUNK_URL = "CHUNK_URL";
	public static final String DOCUMENT_URL = "DOCUMENT_URL";

	public static final String LANGUAGE = "LANGUAGE";
	public static final String ISO_CODE = "ISO_CODE";
	
	/* These three fields does not exist in SOLR but exist as <FIELDNAME>_<ISO_CODE>
	 * for e.g BREADCRUMB_en, FILENAME_en and FULLTEXT_EN
	 * */
    public static final String BREADCRUMB = "BREADCRUMB";
	public static final String FILENAME = "FILENAME";
	public static final String FULLTEXT = "FULLTEXT";
	

}

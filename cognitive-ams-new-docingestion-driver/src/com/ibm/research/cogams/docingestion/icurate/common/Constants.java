package com.ibm.research.cogams.docingestion.icurate.common;

import java.util.HashMap;

/**
 * Class containing a list of constants
 * 
 * @author costel.crihana
 *
 */
public class Constants {
	// File types
	public static final String FILE_EXTENSION_DOC = "DOC";
	public static final String FILE_EXTENSION_DOCX = "DOCX";
	public static final String FILE_EXTENSION_XLS = "XLS";
	public static final String FILE_EXTENSION_XLSX = "XLSX";
	public static final String FILE_EXTENSION_PPT = "PPT";
	public static final String FILE_EXTENSION_PPTX = "PPTX";
	public static final String FILE_EXTENSION_PDF = "PDF";
	public static final String FILE_EXTENSION_ZIP = "ZIP";
	public static final String FILE_EXTENSION_TXT = "TXT";
	public static final String FILE_EXTENSION_CSV = "CSV";

	// SOLR Constants
	public static final String DEFAULT_INDEX_NAME = "doc_ingestion";

	public static final HashMap<String, String> isoCodeLangMap = new HashMap<String, String>();
	static {
		isoCodeLangMap.put("en", "English");
		isoCodeLangMap.put("es", "Spanish");
		isoCodeLangMap.put("pt", "Portuguese");
		isoCodeLangMap.put("fr", "French");
		isoCodeLangMap.put("de", "German");
		isoCodeLangMap.put("ja", "Japanese");
		isoCodeLangMap.put("it", "Italian");
		isoCodeLangMap.put("br", "Brazilian");
	}
}

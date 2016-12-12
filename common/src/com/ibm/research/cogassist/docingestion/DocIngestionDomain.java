package com.ibm.research.cogassist.docingestion;

import com.ibm.research.cogassist.common.Domain;

public class DocIngestionDomain extends Domain {
	public static final String NAME = "doc_ingestion";

	@Override
	public String getName() {
		return NAME;
	}
	
	@Override 
	public String toString() {
		return NAME;
	}
	

	@Override
	public String getDisplayName() {
		return "Document Ingestion";
	}

	@Override
	public String getLocalizationResourceName() {
		return "doc-ingestion";
	}

	@Override
	public String getWebResourceClassName() {
		return "com.ibm.research.cogassist.docingestion.DocIngestionResource";
	}

	@Override
	public String getTaskProcessorClassName() {
		return "com.ibm.research.cogams.docingestion.icurate.DocIngestionProcessor";
	}
	

}

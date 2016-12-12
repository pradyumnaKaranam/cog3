package com.ibm.research.cogassist.kg;

import com.ibm.research.cogassist.common.Domain;

public class KgCreationDomain  extends Domain  {
public static final String NAME = "kg_creation";
	
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
		return "Knowledge Graph Creation";
	}
	
	public static Domain getInstance() {
		return Domain.valueOf(NAME);
	}
	
	@Override
	public String getLocalizationResourceName() {
		return "kg-creation";
	}

	@Override
	public String getWebResourceClassName() {
		return "com.ibm.research.cogassist.kg.creation.KgCreationResource";
	}

	@Override
	public String getTaskProcessorClassName() {
		return "com.ibm.research.cogassist.kg.creation.KgCreationProcessor";
	}
	
}

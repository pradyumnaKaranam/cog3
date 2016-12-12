/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.research.cogams.docanalyzer;

import com.ibm.research.cogams.docingestion.DocumentStream;
import com.ibm.research.cogams.tikaingestion.AnswerUnitConverter;

/**
 *
 * @author sampath
 */
public class WDCDocAnalyzer implements DocumentAnalyzer {
    String projID;
    public WDCDocAnalyzer(String projID) {
        this.projID = projID;
    }

    @Override
    public String getFormatType() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String chunk(DocumentStream doc) throws Exception {
        AnswerUnitConverter ansConverter = new  AnswerUnitConverter();
        return ansConverter.chunk(doc);
    }

    @Override
    public String convertToHTML(DocumentStream is) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

	@Override
	public String getText(DocumentStream doc) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

   
    
}

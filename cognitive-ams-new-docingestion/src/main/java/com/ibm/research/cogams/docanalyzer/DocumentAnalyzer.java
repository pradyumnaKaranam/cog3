/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.research.cogams.docanalyzer;

import com.ibm.research.cogams.docingestion.DocumentStream;

/**
 *
 * @author sampath
 */
public interface DocumentAnalyzer {
    
    /**
     *
     * @return
     */
    String getFormatType();

    /**
     *
     * @param is
     * @return
     * @throws Exception
     */
    String chunk(DocumentStream is) throws Exception;
    String convertToHTML(DocumentStream is) throws Exception;

	String getText(DocumentStream doc) throws Exception;
    
}

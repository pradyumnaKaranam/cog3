/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.research.cogams.docanalyzer;

import java.net.MalformedURLException;

import org.json.simple.parser.ParseException;

import com.ibm.research.cogams.docingestion.DocumentStream;

/**
 *
 * @author sampath
 */
public class DocumentAnalyzerFactory {
    
    /**
     *
     * @param c
     * @return
     */
    public static DocumentAnalyzer createDocumentAnalyzer(DocumentStream c) throws ParseException, MalformedURLException{
        if(c.isPDF()){
            //return(new TikaDocAnalyzer());
            return(new PDFAnalyzer(c.getProjID()));
        }
        if(c.isHTML()){
        	return(new WDCDocAnalyzer(c.getProjID()));
        }
        else{
            return(new TikaDocAnalyzer(c.getProjID()));
        }
        
    }
}
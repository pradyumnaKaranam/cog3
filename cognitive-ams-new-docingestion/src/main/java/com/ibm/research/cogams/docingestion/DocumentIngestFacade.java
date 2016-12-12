/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.research.cogams.docingestion;

import javax.servlet.http.Part;

/**
 *
 * @author sampath
 */
public interface DocumentIngestFacade {
    /**
     *
     * @param filePart
     * @param projID
     * @return
     * @throws Exception 
     */
    public Boolean ingest(Part filePart) throws Exception;
    public Boolean ingest(String filePath) throws Exception;
    /**
     *
     * @param queryString
     * @param projID
     * @return
     */
    public String search(String queryString);
    
    public String convertToHTML(Part filePart);

    
}

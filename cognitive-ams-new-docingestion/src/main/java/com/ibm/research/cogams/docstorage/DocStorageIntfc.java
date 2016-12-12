/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.research.cogams.docstorage;

import java.io.InputStream;

/**
 *
 * @author sampath
 */
public interface DocStorageIntfc {
    public InputStream findImage(String name);
    public String storeImage(InputStream inputStream, String name, String contentType,String docFileName);
    public void deleteImages(String name);
    public String getLink();
    public String storeStringFile(String fileContent, String name);
    public void deleteDB();
    public void createDB();
	public boolean contains(String fileName);
}

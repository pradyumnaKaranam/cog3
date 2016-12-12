/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.research.cogams.docstorage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.poi.poifs.filesystem.DirectoryEntry;
import org.apache.poi.poifs.filesystem.DocumentEntry;
import org.apache.poi.poifs.filesystem.DocumentInputStream;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.tika.io.TikaInputStream;

import com.ibm.research.cogams.docingestion.AppLevelGlobals;

/**
 *
 * @author sampath
 */
public class FSDocStorage implements DocStorageIntfc {
	public String imageDir; 
	private String projID;
	public FSDocStorage(String projID){
		this.projID=projID;
		imageDir = AppLevelGlobals.getInstance().getIMAGEDIR()+"/"+projID;
	}
	@Override
	public InputStream findImage(String name) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public String storeImage(InputStream imageInputStream, String name, String contentType,String docFileName) {
		//  String id = UUID.randomUUID().toString();
		String id = docFileName;
		String nOutFileName = id+"/"+name;
		//String nOutFileName = name.substring(0,name.indexOf('.'))+"_"+id+name.substring(name.indexOf('.'));

		File outputFile = new File(imageDir, FilenameUtils.normalize(nOutFileName));
		File parent = outputFile.getParentFile();
		if (!parent.exists()) {
			if (!parent.mkdirs()) {
				try {
					throw new IOException("unable to create directory \"" + parent + "\"");
				} catch (IOException ex) {
					Logger.getLogger(FSDocStorage.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
		// System.out.println("Extracting '"+name+"' ("+contentType+") to " + outputFile);

		try (FileOutputStream os = new FileOutputStream(outputFile)) {
			if (imageInputStream instanceof TikaInputStream) {
				TikaInputStream tin = (TikaInputStream) imageInputStream;
				if (tin.getOpenContainer() != null && tin.getOpenContainer() instanceof DirectoryEntry) {
					POIFSFileSystem fs = new POIFSFileSystem();
					copy((DirectoryEntry) tin.getOpenContainer(), fs.getRoot());
					fs.writeFilesystem(os);
				} else {
					IOUtils.copy(imageInputStream, os);
				}
			} else {
				IOUtils.copy(imageInputStream, os);
			}
		} catch (Exception e) {
			//
			// being a CLI program messages should go to the stderr too
			//
			String msg = String.format(
					Locale.ROOT,
					"Ignoring unexpected exception trying to save embedded file %s (%s)",
					name,
					e.getMessage()
					);
			System.err.println(msg);
		}
		return id; //To change body of generated methods, choose Tools | Templates.
	}
	protected void copy(DirectoryEntry sourceDir, DirectoryEntry destDir)
			throws IOException {
		for (org.apache.poi.poifs.filesystem.Entry entry : sourceDir) {
			if (entry instanceof DirectoryEntry) {
				// Need to recurse
				DirectoryEntry newDir = destDir.createDirectory(entry.getName());
				copy((DirectoryEntry) entry, newDir);
			} else {
				// Copy entry
				try (InputStream contents =
						new DocumentInputStream((DocumentEntry) entry)) {
					destDir.createDocument(entry.getName(), contents);
				}
			}
		}
	}
	@Override
	public String getLink() {
		String imgFilePath = AppLevelGlobals.getInstance().getImageFilesPath();
		return imgFilePath+"/"+projID;
	}

	@Override
	public String storeStringFile(String fileContent, String name) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public void deleteDB() {
		File imgdir = new File(imageDir);
		if(imgdir.exists()){
			String[]entries = imgdir.list();
			for(String s: entries){
				//delete file directories with images
				deleteImages(s);
			}
			imgdir.delete();
		}
	}

	@Override
	public void createDB() {
		File imgdir = new File(imageDir);
		if(!imgdir.exists()){
			imgdir.mkdir();
		}
	}
	@Override
	public void deleteImages(String filename) {
		File fileDir = new File(imageDir,filename);
		if(fileDir.exists()){
			String[]fentries = fileDir.list();
			for(String fs: fentries){
				File fsFile = new File(fileDir.getPath(),fs);
				fsFile.delete();
			}
			fileDir.delete();
		}

	}
	@Override
	public boolean contains(String filename) {
		File fileDir = new File(imageDir,filename);
		return(fileDir.exists());
	}
	

}

package com.ibm.research.cogassist.qprocessor.util;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;


/**
 * A class-loader which loads classes from any <tt>jar</tt> file 
 * in a given directory.
 * 
 * @author Rohan Padhye
 *
 */
public class JarDirectoryClassLoader extends URLClassLoader {
	
	/** 
	 * Creates a new class loader which will search for classes
	 * within all <tt>jar</tt> files in a given directory.
	 */
	public JarDirectoryClassLoader(File directory) {
		super(getJarUrls(directory), JarDirectoryClassLoader.class.getClassLoader());		
	}
	
	private static URL[] getJarUrls(File directory) {
		// Ensure this is a directory
		if (directory.isDirectory() == false) {
			throw new IllegalArgumentException(directory + " is not a directory.");
		}
		
		// Make a filter that only accepts Jars
		FilenameFilter jarFilter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar");				
			}
		};
		
		// Get Jars
		List<URL> jars = new ArrayList<URL>();
		for (File jar : directory.listFiles(jarFilter)) {
			try {
				URL jarUrl = jar.toURI().toURL();
				jars.add(jarUrl);
			} catch (MalformedURLException e) {
				// Ignore and continue
			}
		}
		return jars.toArray(new URL[0]);
	}
	
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.research.cogams.docingestion;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.hwpf.usermodel.Paragraph;
import org.apache.poi.hwpf.usermodel.Range;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;

/**
 *
 * @author Anush
 */
public class FileFormattedChecker {

    /**
     *
     */
    public static final String FILE_EXTENSION_DOC = "DOC";

    /**
     *
     */
    public static final String FILE_EXTENSION_DOCX = "DOCX";

    /**
     *
     */
    public static final String FILE_EXTENSION_PPT = "PPT";

    /**
     *
     */
    public static final String FILE_EXTENSION_PPTX = "PPTX";
    
    /**
     *
     * @param theFileName
     * @return
     * @throws Exception
     */
    public static String getFileExtension(String theFileName) throws Exception {
        int theExtensionDelimiterPosition = theFileName.lastIndexOf(".");
        if (theExtensionDelimiterPosition == -1) {
            return "";
        }
        String fileExtension = theFileName.substring(theExtensionDelimiterPosition + 1, theFileName.length()).toUpperCase();
        return fileExtension;
    }
    
    /**
     *
     * @param styles
     * @return
     */
    public static boolean checkFormatted(List<Integer> styles){
        boolean isFormatted = false;
        
        if(styles.isEmpty()) return isFormatted;
        
        int occurrences = Collections.frequency(styles, 0);
        if (styles.size() - occurrences > 2)
            isFormatted = true;
        return isFormatted;
    }
    
    /**
     *
     * @param document
     * @return
     * @throws IOException
     */
    public static boolean isDocFormatted(HWPFDocument document) throws IOException{        
        Range r = document.getRange ();
        int lenParagraph = r.numParagraphs();
        List<Integer> styles = new ArrayList<>();
        for (int i = 0; i < lenParagraph; i++){
            Paragraph p = r.getParagraph(i);
            if (p.text().trim().isEmpty())
        	 continue; 
            styles.add((int) p.getStyleIndex());
        }
        return checkFormatted(styles);
    }
    
    /**
     *
     * @param document
     * @return
     * @throws IOException
     * @throws InvalidFormatException
     */
    public static boolean isDocxFormatted(XWPFDocument document) throws IOException, InvalidFormatException{        
        List<XWPFParagraph> paraList=document.getParagraphs();
  	Iterator<XWPFParagraph> Iterpara=paraList.iterator();
        XWPFParagraph paragraph = null;
        List<Integer> styles = new ArrayList<>();
        while (Iterpara.hasNext()) {
            paragraph = Iterpara.next(); 
            if (paragraph.getText().trim().length() == 0)
                continue;        
            String styleName = paragraph.getStyle();
            if (styleName!= null && styleName.startsWith ("Heading"))
                styles.add(Integer.parseInt(styleName.split("Heading")[1]));
            else
                styles.add(0);                                       
        }        
        return checkFormatted(styles);
    }
    
    /**
     *
     * @param filePath
     * @return
     * @throws IOException
     * @throws Exception
     */
    public static boolean isFileFormatted(String filePath) throws IOException, Exception{
        String fileExtension = getFileExtension(filePath);
        Boolean isFormatted = false;
        
        if (fileExtension.equalsIgnoreCase(FILE_EXTENSION_DOC)) {
            System.out.println("...doc processing...."); 
            HWPFDocument document = new HWPFDocument(new FileInputStream(filePath));
            isFormatted = isDocFormatted(document);
        }
        
        if (fileExtension.equalsIgnoreCase(FILE_EXTENSION_DOCX)) {
            System.out.println("...docx processing....");
            XWPFDocument document = new XWPFDocument(new FileInputStream(filePath));
            isFormatted = isDocxFormatted(document);
        }
        
        return isFormatted;
    }
    
    // only callable function

    /**
     *
     * @param fileStream
     * @param fileExtension
     * @return
     * @throws IOException
     * @throws Exception
     */
    public static boolean isFileFormatted(FileInputStream fileStream, String fileExtension) throws IOException, Exception{
        Boolean isFormatted = false;
        
        if (fileExtension.equalsIgnoreCase(FILE_EXTENSION_DOC)) {
            System.out.println("...doc processing...."); 
            HWPFDocument document = new HWPFDocument(fileStream);
            isFormatted = isDocFormatted(document);
        }
        
        if (fileExtension.equalsIgnoreCase(FILE_EXTENSION_DOCX)) {
            System.out.println("...docx processing....");
            XWPFDocument document = new XWPFDocument(fileStream);
            isFormatted = isDocxFormatted(document);
        }
        
        return isFormatted;
    }
}

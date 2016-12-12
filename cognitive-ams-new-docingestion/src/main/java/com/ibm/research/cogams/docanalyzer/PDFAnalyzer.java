/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.research.cogams.docanalyzer;

import static java.lang.Integer.max;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import org.apache.poi.util.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.LoggerFactory;

import com.ibm.research.cogams.docingestion.AppLevelGlobals;
import com.ibm.research.cogams.docingestion.DocumentStream;
import com.ibm.research.cogams.tikaingestion.AnswerUnitConverter;

/**
 *
 * @author sampath
 */
public class PDFAnalyzer implements DocumentAnalyzer{
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(PDFAnalyzer.class);
    public  static String pdfminerURL = "http://pdfminer.mybluemix.net/convert";   
    String projID;
    Document doc;
    public PDFAnalyzer(String projID){
        this.projID = projID;
    }
    @Override
    public String getFormatType() {
        return ("pdf");
    }

    @Override
    public String chunk(DocumentStream is) throws Exception {
        //String html = convertToHTML(is);
        AnswerUnitConverter ansConverter = new  AnswerUnitConverter();
        return ansConverter.chunk(is);
    }

    @Override
    public String convertToHTML(DocumentStream is) throws Exception {
        String oHTML = getHTML(is.getInputStream());
        doc = Jsoup.parse(oHTML);
        removeEmptyHTML();
        modifySpans();
        
        return doc.toString();
    }
    
    public void removeEmptyHTML(){
        // Names of the elements to remove if empty
        Set<String> removable = new HashSet<String>();
        removable.add("span");
        removable.add("div");
        // For each element in the cleaned document
        for(Element el: doc.getAllElements()) {

           if(el.children().isEmpty() && !el.hasText()) {
               // Element is empty, check if should be removed
               if(removable.contains(el.tagName())) el.remove();
           }
        }
    }
    private class SpanStyleList{
        ArrayList<SpanStyle> spanList;
        Set<Integer> fontSizeSet;
        int maxFont;
        int sum;
        public ArrayList<SpanStyle> getSpanList(){
            return spanList;
        }
        public Set<Integer> getFontSizeSet(){
            return fontSizeSet;
        }
        public SpanStyleList(){
            spanList = new ArrayList<>();
            fontSizeSet = new HashSet<>();
            maxFont = 0;
            sum = 0;
        }
        void add(SpanStyle s){
            spanList.add(s);
            maxFont = max(maxFont,s.getFontSize());
            sum = sum+s.getFontSize();
            fontSizeSet.add(s.getFontSize());
            
        }
        int getMaxFont(){
            return maxFont;
        }
        int getAverageFont(){
            return sum/spanList.size();
        }
         
    }
    private class SpanStyle {
        String fontStyle;
        int fontSize;
        Element span;
        public SpanStyle(Element span){
            this.span = span;
            String style = span.attr("style");
            String [] subStyles = style.split(";");
            String fontSizeStr = subStyles[subStyles.length-1];
            fontStyle = (subStyles[0].split(":"))[1];
            fontSizeStr = (fontSizeStr.split(":"))[1];
            fontSize =  Integer.parseInt(fontSizeStr.substring(0, fontSizeStr.length()-2));
        }
        public Element getSpan(){
            return span;
        }
        public String getFontStyle() {
            return fontStyle;
        }
        public void setFontStyle(String fontStyle) {
            this.fontStyle = fontStyle;
        }
        public int getFontSize() {
            return fontSize;
        }
        public void setFontSize(int fontSize) {
            this.fontSize = fontSize;
        }
    }
  
    public void modifySpans(){
        SpanStyleList sList = new SpanStyleList();
        String curStyle = "";
        Element curSpan = null;
        for(Element el: doc.select("span")) {
            if(curStyle.equals(el.attr("style"))){
                curSpan.append(" " + el.html());
                el.remove();
            }
            else {
                curStyle = el.attr("style");
                curSpan = el;
            }
        }
        for(Element el: doc.select("span")) {

            SpanStyle s = new SpanStyle(el);
            sList.add(s);
            logger.trace("Font Style: {} Font Size: {}",s.getFontStyle(),s.getFontSize());
        }
        ArrayList<SpanStyle> styleList = sList.getSpanList();
        List sortedList = new ArrayList(sList.getFontSizeSet());
        Collections.sort(sortedList);
        for(SpanStyle es: styleList){
            if(es.getFontSize()> sList.getAverageFont()){
                int pos = sortedList.size()-sortedList.indexOf(es.getFontSize())+1;
                Element span = es.getSpan();
                span.tagName("h"+pos);
            }
           
        }
       
        for(Object i: sortedList){
            logger.debug("Font Size: {} ",i);
        }
        logger.debug("Average FontSize {} ", sList.getAverageFont());
        
        
        
        
    }
    
    public  String getHTML(InputStream f) throws IOException{
             final MediaType MEDIA_TYPE_PDF = MediaType.parse("application/pdf");
            final OkHttpClient client = new OkHttpClient();


            final RequestBody body;
            body = new MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("file","testfile",
                            RequestBody.create(MEDIA_TYPE_PDF,IOUtils.toByteArray(f))
                    )
                    .build();
            
            Request request = new Request.Builder().url(AppLevelGlobals.getInstance().getPDFMinerURL())
                                .post(body)
                                .build();
            System.out.println(request.body().contentType());

            Response response = client.newCall(request).execute();
            //if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

           // System.out.println(response.body().string());
            return response.body().string();
        }
	@Override
	public String getText(DocumentStream doc) throws Exception {
		String oHTML = getHTML(doc.getInputStream());
        String  text= Jsoup.parse(oHTML).select("body").text();
        return text;
	}
    
}

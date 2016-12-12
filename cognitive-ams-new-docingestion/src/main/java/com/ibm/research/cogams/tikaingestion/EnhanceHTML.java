/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.research.cogams.tikaingestion;

import static java.lang.Integer.parseInt;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Stack;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
//import org.jsoup.select.Elements;

/**
 *
 * @author sampath
 */
public class EnhanceHTML {
    String input;
    Document doc;
    String separator = "::";
    private class stackElement {
        public int level;
        public String text;
        public stackElement(Element e) {
            level = parseInt(e.tagName().substring(1));
            this.text = e.text();
        }
        public stackElement(int level, String text) {
            this.level = level;
            this.text = text;
        }
    }
    public EnhanceHTML(String fileContent){
        input = fileContent;
    }
    public String enhance(){
        input  = input.replaceAll("&ndash;","-");
        doc = Jsoup.parse(input);
        removeHeaderFooterDivsFromDocs();
        addBordersToTable();
        addTopicHeadings();
        qualifyHeadings();
        addH1TagsForPPTOutput();
        return doc.toString();
    }
    private void removeHeaderFooterDivsFromDocs(){
   	 Elements slideFooter = doc.select("div.footer,div.header");
        slideFooter.remove();
   }
    private void addH1TagsForPPTOutput(){
        //remove slide-footer statements
        Elements slideFooter = doc.select("p.slide-footer,div.slide-master-content");
        slideFooter.remove();
        
        //add h1 tags
        Elements slideContentTags = doc.select("div.slide-content");
        int count = 0;
        for(Element e: slideContentTags){
            count ++;
            e.prepend("<h1> Slide # " + count + "</h1");
        }
    }
    private void addBordersToTable(){
        Elements elems = doc.select("table");
        //bootstrap.js classes
        elems.addClass("table table-bordered");
    }
   
    private void addTopicHeadings(){
        Elements elems = doc.select("h0,h1,h2,h3,h4,h5,h6,h7");
        Stack<stackElement> s = new Stack<>();
        
        for(Element e: elems){
            
            //System.out.println(e.tagName()+"-"+e.text()+"-"+e.id());
            int level = parseInt(e.tagName().substring(1));
            String text = e.text();
            
            if(s.empty()){
                s.push(new stackElement(e));
                String anchor = text.replaceAll("-", " ").replaceAll(" ", "");
                //System.out.println(level+":"+anchor);
                e.prepend("<a name=\""+anchor+"\"></a>");
            }
            else {
                stackElement se = s.peek();
                if(level > se.level){
                    String qualifier = se.text+separator;
                    s.push(new stackElement(level,qualifier+text));
                    String anchor;
                    anchor = s.peek().text.replaceAll("-", " ").replaceAll(" ", "");
                    //System.out.println(anchor);

                    e.prepend("<a name=\""+anchor+"\"></a>");
                }
                else{
                    while(!s.isEmpty() && s.peek().level >= level){
                        s.pop();
                    }
                
                    if(s.empty()){
                        s.push(new stackElement(e));
                        String anchor = text.replaceAll("-", " ").replaceAll(" ", "");
                       // System.out.println(anchor);
                        e.prepend("<a name=\""+anchor+"\"></a>");
                    }
                    else {
                        String qualifier = s.peek().text+separator;
                        s.push(new stackElement(level,qualifier+text));
                        String anchor;
                        anchor = s.peek().text.replaceAll("-", " ").replaceAll(" ", "");
                                   //     System.out.println(anchor);

                        e.prepend("<a name=\""+anchor+"\"></a>");

                    }
                }
            }
            
        }
        
    }
    private void qualifyHeadings(){
        Elements elems = doc.select("h0,h1,h2,h3,h4,h5,h6,h7");
        Stack<stackElement> s = new Stack<>();
        
        for(Element e: elems){
            //System.out.println(e.tagName()+"-"+e.text()+"-"+e.id());
            int level = parseInt(e.tagName().substring(1));
            String text = e.text();
            
            if(s.empty()){
                s.push(new stackElement(e));
            }
            else {
                stackElement se = s.peek();
                if(level > se.level){
                    String qualifier = se.text+separator;
                    s.push(new stackElement(level,qualifier+text));
                    e.text(s.peek().text);
                }
                else{
                    while(!s.isEmpty() && s.peek().level >= level){
                        s.pop();
                    }
                
                    if(s.empty()){
                        s.push(new stackElement(e));
                        
                    }
                    else {
                        String qualifier = s.peek().text+separator;
                        s.push(new stackElement(level,qualifier+text));
                        e.text(s.peek().text);

                    }
                }
            }
            
        }
    }
    public static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
    
    
}

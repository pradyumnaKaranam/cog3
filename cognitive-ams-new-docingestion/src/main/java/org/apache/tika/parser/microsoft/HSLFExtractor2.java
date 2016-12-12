/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.tika.parser.microsoft;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.hslf.model.OLEShape;
import org.apache.poi.hslf.usermodel.HSLFMasterSheet;
import org.apache.poi.hslf.usermodel.HSLFObjectData;
import org.apache.poi.hslf.usermodel.HSLFPictureData;
import org.apache.poi.hslf.usermodel.HSLFPictureShape;
import org.apache.poi.hslf.usermodel.HSLFShape;
import org.apache.poi.hslf.usermodel.HSLFSlide;
import org.apache.poi.hslf.usermodel.HSLFSlideShow;
import org.apache.poi.hslf.usermodel.HSLFTable;
import org.apache.poi.hslf.usermodel.HSLFTableCell;
import org.apache.poi.hslf.usermodel.HSLFTextParagraph;
import org.apache.poi.hslf.usermodel.HSLFTextRun;
import org.apache.poi.hslf.usermodel.HSLFTextShape;
import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.XHTMLContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

public class HSLFExtractor2 extends AbstractPOIFSExtractor {
    public HSLFExtractor2(ParseContext context) {
        super(context);
    }

    protected void parse(
            NPOIFSFileSystem filesystem, XHTMLContentHandler xhtml)
            throws IOException, SAXException, TikaException {
        parse(filesystem.getRoot(), xhtml);
    }

    protected void parse(
            DirectoryNode root, XHTMLContentHandler xhtml)
            throws IOException, SAXException, TikaException {
        HSLFSlideShow ss = new HSLFSlideShow(root);
        List<HSLFSlide> _slides = ss.getSlides();
        

        xhtml.startElement("div", "class", "SlideShow");
        int count = 0;
      /* Iterate over slides and extract text */
        for (HSLFSlide slide : _slides) {
            

            xhtml.startElement("div", "class", "slide-content");
            extractSlideAsImage(xhtml,slide,ss);

            for (HSLFShape shape : slide.getShapes()) {
                if (shape instanceof HSLFTable) {
                    extractTableText(xhtml, (HSLFTable) shape);
                }
                if (shape instanceof HSLFTextShape) {
                    extractText(xhtml,(HSLFTextShape) shape);
                }
                if (shape instanceof HSLFPictureShape) {
                    count++;
                    extractPicture(xhtml,(HSLFPictureShape) shape,"image"+count);
                }
                
            }
            /*
            // Slide text
            {
                xhtml.startElement("div", "class", "slide-content");

                textRunsToText(xhtml, slide.getTextParagraphs());

                xhtml.endElement("div");
            }
            // Table text
            for (HSLFShape shape : slide.getShapes()) {
                if (shape instanceof HSLFTable) {
                    extractTableText(xhtml, (HSLFTable) shape);
                }
            }
            */
            // Now any embedded resources
            handleSlideEmbeddedResources(slide, xhtml);
            // Slide complete
            xhtml.endElement("div");
        }

        // All slides done
        xhtml.endElement("div");
      

        xhtml.endElement("div");
    }

    private void extractMaster(XHTMLContentHandler xhtml, HSLFMasterSheet master) throws SAXException {
        if (master == null) {
            return;
        }
        List<HSLFShape> shapes = master.getShapes();
        if (shapes == null || shapes.isEmpty()) {
            return;
        }

        xhtml.startElement("div", "class", "slide-master-content");
        for (HSLFShape shape : shapes) {
            if (shape != null && !HSLFMasterSheet.isPlaceholder(shape)) {
                if (shape instanceof HSLFTextShape) {
                    HSLFTextShape tsh = (HSLFTextShape) shape;
                    String text = tsh.getText();
                    if (text != null) {
                        xhtml.element("p", text);
                    }
                }
            }
        }
        xhtml.endElement("div");
    }

    private void extractTableText(XHTMLContentHandler xhtml, HSLFTable shape) throws SAXException {
        xhtml.startElement("table");
        for (int row = 0; row < shape.getNumberOfRows(); row++) {
            xhtml.startElement("tr");
            for (int col = 0; col < shape.getNumberOfColumns(); col++) {
                HSLFTableCell cell = shape.getCell(row, col);
                //insert empty string for empty cell if cell is null
                String txt = "";
                if (cell != null) {
                    txt = cell.getText();
                }
                xhtml.element("td", txt);
            }
            xhtml.endElement("tr");
        }
        xhtml.endElement("table");
    }
    private void extractSlideAsImage(XHTMLContentHandler xhtml, HSLFSlide slide,HSLFSlideShow ss) throws IOException, SAXException, TikaException{
        //create image for this slide
        //getting the dimensions and size of the slide 
        Dimension pgsize = ss.getPageSize();
        BufferedImage img = new BufferedImage(pgsize.width, pgsize.height,BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = img.createGraphics();
        
        //clear the drawing area
        graphics.setPaint(Color.white);
        graphics.fill(new Rectangle2D.Float(0, 0, pgsize.width, pgsize.height));
        slide.draw(graphics);
         //creating an image file as output
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        //FileOutputStream out = new FileOutputStream("/Users/sampath/slides/slide_"+slide.getSlideNumber()+".png");
        javax.imageio.ImageIO.write(img, "png", b);
        ByteArrayInputStream bin = new ByteArrayInputStream(b.toByteArray());
        String imgName = "slide_"+slide.getSlideNumber();
        String extension = ".png";
        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute("", "src", "src", "CDATA", "embedded:" + imgName+extension);
        attr.addAttribute("", "alt", "alt", "CDATA", imgName+extension);
        xhtml.startElement("img", attr);
        xhtml.endElement("img");
        handleEmbeddedResource(
            TikaInputStream.get(b.toByteArray()), imgName, null,
            "image/png", xhtml, false);
        //ss.write(out);
    }
    private void extractPicture(XHTMLContentHandler xhtml, HSLFPictureShape shape, String picName) throws IOException, SAXException, TikaException{
        HSLFPictureData picData = shape.getPictureData();
        String mediaType;

        switch (picData.getType()) {
            case EMF:
                mediaType = "application/x-emf";
                break;
            case WMF:
                mediaType = "application/x-msmetafile";
                break;
            case DIB:
                mediaType = "image/bmp";
                break;
            default:
                mediaType = picData.getContentType();
                break;
        }
        String[] types = mediaType.split("/");
        TikaConfig config = TikaConfig.getDefaultConfig();
         // Output the img tag
         MediaType md = new MediaType(types[0],types[1]);
          String extension = config.getMimeRepository().forName(
                            mediaType).getExtension();
        AttributesImpl attr = new AttributesImpl();
        attr.addAttribute("", "src", "src", "CDATA", "embedded:" + picName+extension);
        attr.addAttribute("", "alt", "alt", "CDATA", picName+extension);
        xhtml.startElement("img", attr);
        xhtml.endElement("img");
        handleEmbeddedResource(
            TikaInputStream.get(picData.getData()), picName, null,
            mediaType, xhtml, false);
            
    }
    private void extractText(XHTMLContentHandler xhtml, HSLFTextShape shape) throws SAXException {
        List<HSLFTextParagraph> run = shape.getTextParagraphs();
        if (run == null) {
            return;
        }

        // Leaving in wisdom from TIKA-712 for easy revert.
        // Avoid boiler-plate text on the master slide (0
        // = TextHeaderAtom.TITLE_TYPE, 1 = TextHeaderAtom.BODY_TYPE):
        //if (!isMaster || (run.getRunType() != 0 && run.getRunType() != 1)) {

        boolean isBullet = false;
        for (HSLFTextParagraph htp : run) {
            boolean nextBullet = htp.isBullet();
            // TODO: identify bullet/list type
            if (isBullet != nextBullet) {
                isBullet = nextBullet;
                if (isBullet) {
                    xhtml.startElement("ul");
                } else {
                    xhtml.endElement("ul");
                }
            }

            List<HSLFTextRun> textRuns = htp.getTextRuns();
            String firstLine = removePBreak(textRuns.get(0).getRawText());
            boolean showBullet = (isBullet && (textRuns.size() > 1 || !"".equals(firstLine)));
            String paraTag = showBullet ? "li" : "p";

            xhtml.startElement(paraTag);
            for (HSLFTextRun htr : textRuns) {
                String line = htr.getRawText();
                if (line != null) {
                    boolean isfirst = true;
                    for (String fragment : line.split("\\u000b")) {
                        if (!isfirst) {
                            xhtml.startElement("br");
                            xhtml.endElement("br");
                        }
                        isfirst = false;
                        xhtml.characters(removePBreak(fragment));
                    }
                    if (line.endsWith("\u000b")) {
                        xhtml.startElement("br");
                        xhtml.endElement("br");
                    }
                }
            }
            xhtml.endElement(paraTag);
        }
        if (isBullet) {
            xhtml.endElement("ul");
        }
    }
    private void textRunsToText(XHTMLContentHandler xhtml, List<List<HSLFTextParagraph>> paragraphsList) throws SAXException {
        if (paragraphsList == null) {
            return;
        }

        for (List<HSLFTextParagraph> run : paragraphsList) {
            // Leaving in wisdom from TIKA-712 for easy revert.
            // Avoid boiler-plate text on the master slide (0
            // = TextHeaderAtom.TITLE_TYPE, 1 = TextHeaderAtom.BODY_TYPE):
            //if (!isMaster || (run.getRunType() != 0 && run.getRunType() != 1)) {

            boolean isBullet = false;
            for (HSLFTextParagraph htp : run) {
                boolean nextBullet = htp.isBullet();
                // TODO: identify bullet/list type
                if (isBullet != nextBullet) {
                    isBullet = nextBullet;
                    if (isBullet) {
                        xhtml.startElement("ul");
                    } else {
                        xhtml.endElement("ul");
                    }
                }

                List<HSLFTextRun> textRuns = htp.getTextRuns();
                String firstLine = removePBreak(textRuns.get(0).getRawText());
                boolean showBullet = (isBullet && (textRuns.size() > 1 || !"".equals(firstLine)));
                String paraTag = showBullet ? "li" : "p";

                xhtml.startElement(paraTag);
                for (HSLFTextRun htr : textRuns) {
                    String line = htr.getRawText();
                    if (line != null) {
                        boolean isfirst = true;
                        for (String fragment : line.split("\\u000b")) {
                            if (!isfirst) {
                                xhtml.startElement("br");
                                xhtml.endElement("br");
                            }
                            isfirst = false;
                            xhtml.characters(removePBreak(fragment));
                        }
                        if (line.endsWith("\u000b")) {
                            xhtml.startElement("br");
                            xhtml.endElement("br");
                        }
                    }
                }
                xhtml.endElement(paraTag);
            }
            if (isBullet) {
                xhtml.endElement("ul");
            }
        }
    }

    // remove trailing paragraph break
    private static String removePBreak(String fragment) {
        // the last text run of a text paragraph contains the paragraph break (\r)
        // line breaks (\\u000b) can happen more often
        return fragment.replaceFirst("\\r$", "");
    }

    private void handleSlideEmbeddedPictures(HSLFSlideShow slideshow, XHTMLContentHandler xhtml)
            throws TikaException, SAXException, IOException {
        for (HSLFPictureData pic : slideshow.getPictureData()) {
            String mediaType;

            switch (pic.getType()) {
                case EMF:
                    mediaType = "application/x-emf";
                    break;
                case WMF:
                    mediaType = "application/x-msmetafile";
                    break;
                case DIB:
                    mediaType = "image/bmp";
                    break;
                default:
                    mediaType = pic.getContentType();
                    break;
            }
            
            handleEmbeddedResource(
                    TikaInputStream.get(pic.getData()), null, null,
                    mediaType, xhtml, false);
            
        }
    }

    private void handleSlideEmbeddedResources(HSLFSlide slide, XHTMLContentHandler xhtml)
            throws TikaException, SAXException, IOException {
        List<HSLFShape> shapes;
        try {
            shapes = slide.getShapes();
        } catch (NullPointerException e) {
            // Sometimes HSLF hits problems
            // Please open POI bugs for any you come across!
            return;
        }

        for (HSLFShape shape : shapes) {
            if (shape instanceof OLEShape) {
                OLEShape oleShape = (OLEShape) shape;
                HSLFObjectData data = null;
                try {
                    data = oleShape.getObjectData();
                } catch (NullPointerException e) {
                /* getObjectData throws NPE some times. */
                }

                if (data != null) {
                    String objID = Integer.toString(oleShape.getObjectID());

                    // Embedded Object: add a <div
                    // class="embedded" id="X"/> so consumer can see where
                    // in the main text each embedded document
                    // occurred:
                    AttributesImpl attributes = new AttributesImpl();
                    attributes.addAttribute("", "class", "class", "CDATA", "embedded");
                    attributes.addAttribute("", "id", "id", "CDATA", objID);
                    xhtml.startElement("div", attributes);
                    xhtml.endElement("div");

                    try (TikaInputStream stream = TikaInputStream.get(data.getData())) {
                        String mediaType = null;
                        if ("Excel.Chart.8".equals(oleShape.getProgID())) {
                            mediaType = "application/vnd.ms-excel";
                        }
                       /* handleEmbeddedResource(
                                stream, objID, objID,
                                mediaType, xhtml, false);
                        */
                    }
                }
            }
        }
    }
}

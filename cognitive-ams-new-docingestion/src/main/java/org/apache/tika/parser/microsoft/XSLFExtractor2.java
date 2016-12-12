/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.tika.parser.microsoft;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.namespace.QName;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.hpsf.ClassID;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.openxml4j.opc.PackagePartName;
import org.apache.poi.openxml4j.opc.PackageRelationship;
import org.apache.poi.openxml4j.opc.PackagingURIHelper;
import org.apache.poi.openxml4j.opc.TargetMode;
import org.apache.poi.sl.usermodel.Placeholder;
import org.apache.poi.xslf.extractor.XSLFPowerPointExtractor;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFCommentAuthors;
import org.apache.poi.xslf.usermodel.XSLFGraphicFrame;
import org.apache.poi.xslf.usermodel.XSLFGroupShape;
import org.apache.poi.xslf.usermodel.XSLFPictureData;
import org.apache.poi.xslf.usermodel.XSLFPictureShape;
import org.apache.poi.xslf.usermodel.XSLFRelation;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFSlideShow;
import org.apache.poi.xslf.usermodel.XSLFTable;
import org.apache.poi.xslf.usermodel.XSLFTableCell;
import org.apache.poi.xslf.usermodel.XSLFTableRow;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.exception.TikaException;
import org.apache.tika.extractor.EmbeddedDocumentExtractor;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.microsoft.ooxml.AbstractOOXMLExtractor;
import org.apache.tika.sax.XHTMLContentHandler;
import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.openxmlformats.schemas.presentationml.x2006.main.CTPicture;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideIdList;
import org.openxmlformats.schemas.presentationml.x2006.main.CTSlideIdListEntry;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;
/**
 *
 * @author sampath
 */
public class XSLFExtractor2 extends AbstractOOXMLExtractor {
    private final EmbeddedDocumentExtractor docExtractor;
    private int picCount;
    public XSLFExtractor2(ParseContext context, XSLFPowerPointExtractor extractor) {
        super(context, extractor);
        docExtractor = context.get(EmbeddedDocumentExtractor.class);
    }
    
    public void getXHTML(
            ContentHandler handler, Metadata metadata, ParseContext context)
            throws SAXException, XmlException, IOException, TikaException {
        XHTMLContentHandler xhtml = new XHTMLContentHandler(handler, metadata);
        xhtml.startDocument();

        buildXHTML(xhtml);

       
        xhtml.endDocument();
    }
      /**
     * @see org.apache.poi.xslf.extractor.XSLFPowerPointExtractor#getText()
     */
    @Override
    protected void buildXHTML(XHTMLContentHandler xhtml) throws SAXException, IOException {
        XMLSlideShow slideShow = (XMLSlideShow) extractor.getDocument();
        XSLFCommentAuthors commentAuthors = slideShow.getCommentAuthors();

        List<XSLFSlide> slides = slideShow.getSlides();
        picCount = 0;
        for (XSLFSlide slide : slides) {
            try {
                extractSlideAsImage(xhtml,slide,slideShow);
                String slideDesc;
                if (slide.getPackagePart() != null && slide.getPackagePart().getPartName() != null) {
                    slideDesc = getJustFileName(slide.getPackagePart().getPartName().toString());
                    slideDesc += "_";
                } else {
                    slideDesc = null;
                }
                
                // slide content
                xhtml.startElement("div", "class", "slide-content");
                extractContent(slide.getShapes(), false, xhtml, slideDesc);
                xhtml.endElement("div");
            } catch (TikaException ex) {
                Logger.getLogger(XSLFExtractor2.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    private void extractContent(List<? extends XSLFShape> shapes, boolean skipPlaceholders, XHTMLContentHandler xhtml, String slideDesc)
            throws SAXException, IOException, TikaException {
        for (XSLFShape sh : shapes) {
            if (sh instanceof XSLFTextShape) {
                XSLFTextShape txt = (XSLFTextShape) sh;
                Placeholder ph = txt.getTextType();
                if (skipPlaceholders && ph != null) {
                    continue;
                }
                for (XSLFTextParagraph p : txt.getTextParagraphs()) {
                    xhtml.element("p", p.getText());
                }
            } else if (sh instanceof XSLFGroupShape) {
                // recurse into groups of shapes
                XSLFGroupShape group = (XSLFGroupShape) sh;
                extractContent(group.getShapes(), skipPlaceholders, xhtml, slideDesc);
            } else if (sh instanceof XSLFTable) {
                //unlike tables in Word, ppt/x can't have recursive tables...I don't think
                extractTable((XSLFTable)sh, xhtml);
            } else if (sh instanceof XSLFGraphicFrame) {
                XSLFGraphicFrame frame = (XSLFGraphicFrame) sh;
                XmlObject[] sp = frame.getXmlObject().selectPath(
                        "declare namespace p='http://schemas.openxmlformats.org/presentationml/2006/main' .//*/p:oleObj");
                if (sp != null) {
                    for (XmlObject emb : sp) {
                        XmlObject relIDAtt = emb.selectAttribute(new QName("http://schemas.openxmlformats.org/officeDocument/2006/relationships", "id"));
                        if (relIDAtt != null) {
                            String relID = relIDAtt.getDomNode().getNodeValue();
                            if (slideDesc != null) {
                                relID = slideDesc + relID;
                            }
                            AttributesImpl attributes = new AttributesImpl();
                            attributes.addAttribute("", "class", "class", "CDATA", "embedded");
                            attributes.addAttribute("", "id", "id", "CDATA", relID);
                            xhtml.startElement("div", attributes);
                            xhtml.endElement("div");
                        }
                    }
                }
            } else if (sh instanceof XSLFPictureShape) {
                if (!skipPlaceholders && (sh.getXmlObject() instanceof CTPicture)) {
                    CTPicture ctPic = ((CTPicture) sh.getXmlObject());
                    if (ctPic.getBlipFill() != null && ctPic.getBlipFill().getBlip() != null) {
                        String relID = ctPic.getBlipFill().getBlip().getEmbed();
                        if (relID != null) {
                            if (slideDesc != null) {
                                relID = slideDesc + relID;
                            }
                            extractPicture(xhtml,(XSLFPictureShape)sh,"image"+relID);
                        }
                    }
                }
            }
        }
    }
 private void extractPicture(XHTMLContentHandler xhtml, XSLFPictureShape shape, String picName) throws IOException, SAXException, TikaException{
        XSLFPictureData picData = shape.getPictureData();
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
    private void extractTable(XSLFTable tbl, XHTMLContentHandler xhtml) throws SAXException {
        xhtml.startElement("table");
        for (XSLFTableRow row : tbl) {
            xhtml.startElement("tr");
            List<XSLFTableCell> cells = row.getCells();
            for (XSLFTableCell c : row.getCells()) {
                xhtml.startElement("td");
                xhtml.characters(c.getText());
                xhtml.endElement("td");
            }
            xhtml.endElement("tr");
        }
        xhtml.endElement("table");

    }

    /**
     * In PowerPoint files, slides have things embedded in them,
     * and slide drawings which have the images
     * @throws org.apache.tika.exception.TikaException
     */
    @Override
    protected List<PackagePart> getMainDocumentParts() throws TikaException {
        List<PackagePart> parts = new ArrayList<>();
        XSLFSlideShow document = null;
        try {
            document = new XSLFSlideShow(extractor.getPackage());
        } catch (Exception e) {
            throw new TikaException(e.getMessage()); // Shouldn't happen
        }

        CTSlideIdList ctSlideIdList = document.getSlideReferences();
        if (ctSlideIdList != null) {
            for (int i = 0; i < ctSlideIdList.sizeOfSldIdArray(); i++) {
                CTSlideIdListEntry ctSlide = ctSlideIdList.getSldIdArray(i);
                // Add the slide
                PackagePart slidePart;
                try {
                    slidePart = document.getSlidePart(ctSlide);
                } catch (IOException e) {
                    throw new TikaException("Broken OOXML file", e);
                } catch (XmlException xe) {
                    throw new TikaException("Broken OOXML file", xe);
                }
                parts.add(slidePart);

                // If it has drawings, return those too
                try {
                    for (PackageRelationship rel : slidePart.getRelationshipsByType(XSLFRelation.VML_DRAWING.getRelation())) {
                        if (rel.getTargetMode() == TargetMode.INTERNAL) {
                            PackagePartName relName = PackagingURIHelper.createPartName(rel.getTargetURI());
                            parts.add(rel.getPackage().getPart(relName));
                        }
                    }
                } catch (InvalidFormatException e) {
                    throw new TikaException("Broken OOXML file", e);
                }
            }
        }
        return parts;
    }
    
    protected void handleEmbeddedResource(TikaInputStream resource, String filename,
                                          String relationshipID, String mediaType, XHTMLContentHandler xhtml,
                                          boolean outputHtml)
            throws IOException, SAXException, TikaException {
        handleEmbeddedResource(resource, filename, relationshipID, null, mediaType, xhtml, outputHtml);
    }

    protected void handleEmbeddedResource(TikaInputStream resource, String filename,
                                          String relationshipID, ClassID storageClassID, String mediaType, XHTMLContentHandler xhtml,
                                          boolean outputHtml)
            throws IOException, SAXException, TikaException {
        try {
            Metadata metadata = new Metadata();
            if (filename != null) {
                metadata.set(Metadata.TIKA_MIME_FILE, filename);
                metadata.set(Metadata.RESOURCE_NAME_KEY, filename);
            }
            if (relationshipID != null) {
                metadata.set(Metadata.EMBEDDED_RELATIONSHIP_ID, relationshipID);
            }
            if (storageClassID != null) {
                metadata.set(Metadata.EMBEDDED_STORAGE_CLASS_ID, storageClassID.toString());
            }
            if (mediaType != null) {
                metadata.set(Metadata.CONTENT_TYPE, mediaType);
            }

            if (docExtractor.shouldParseEmbedded(metadata)) {
                docExtractor.parseEmbedded(resource, xhtml, metadata, outputHtml);
            }
        } finally {
            resource.close();
        }
    }
    private void extractSlideAsImage(XHTMLContentHandler xhtml, XSLFSlide slide,XMLSlideShow ss) throws IOException, SAXException, TikaException{
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

    
}

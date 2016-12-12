/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ibm.research.cogams.docanalyzer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.tika.config.TikaConfig;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.exception.TikaException;
import org.apache.tika.extractor.EmbeddedDocumentExtractor;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.mime.MimeTypeException;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.pdf.PDFParserConfig;
import org.apache.tika.sax.ExpandedTitleContentHandler;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.slf4j.LoggerFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.ibm.research.cogams.docingestion.DocumentStream;
import com.ibm.research.cogams.docstorage.DocStorageFactory;
import com.ibm.research.cogams.docstorage.DocStorageIntfc;
import com.ibm.research.cogams.tikaingestion.AlchemyIntfc;
import com.ibm.research.cogams.tikaingestion.AnswerUnitConverter;
import com.ibm.research.cogams.tikaingestion.EnhanceHTML;

/**
 *
 * @author sampath
 */
public class TikaDocAnalyzer implements DocumentAnalyzer {
	
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(TikaDocAnalyzer.class);

	// String extractDir;
	ParseContext context;
	DefaultDetector detector;
	DocStorageIntfc docStore;
	AnswerUnitConverter ansConverter;
	AlchemyIntfc alchemyIntfc;

	@Override
	public String chunk(DocumentStream content)
			throws IOException, TransformerConfigurationException, SAXException, TikaException, ParseException {

		String enhancedHTML = convertToHTML(content);
		// Pass enhanced HTML to doc conversion service and get the chunks

		logger.info("File: {} HTML is sent to document conversion service..", content.getFileName());

		String jsonUnits = ansConverter.chunk(enhancedHTML);
//		logger.info("File: {} Adding entities and keywords .. ", content.getFileName());
//		jsonUnits = alchemyIntfc.addHighLights(jsonUnits);
		logger.info(" File: {} Chunking is successful", content.getFileName());
		return jsonUnits;

	}

	private TikaConfig getTikaConfigFromFile() throws TikaException, IOException, SAXException {
		// Get file from resources folder
		InputStream is = this.getClass().getResourceAsStream("/tikaConfig.xml");
		TikaConfig tikaConfig = new TikaConfig(is);
		return tikaConfig;
	}
	
	public String getText(DocumentStream content) throws TransformerConfigurationException, SAXException, TikaException, IOException{
		String html = convertToHTML(content);
		String text = Jsoup.parse(html).select("body").text();
		return text;
	}

	public String convertToHTML(DocumentStream content)
			throws TransformerConfigurationException, SAXException, TikaException, IOException {
		try {
			OutputStream output = new ByteArrayOutputStream();
			Metadata metadata = new Metadata();
			ContentHandler handler = new ExpandedTitleContentHandler(
					getTransformerHandler(output, "html", null, false));
			AutoDetectParser parser = new AutoDetectParser(getTikaConfigFromFile());
			PDFParserConfig config = new PDFParserConfig();
			config.setExtractInlineImages(true);
			config.setExtractUniqueInlineImagesOnly(false);

			context.set(org.apache.tika.parser.pdf.PDFParserConfig.class, config);
			FileEmbeddedDocumentExtractor documentExtractor = new FileEmbeddedDocumentExtractor(content.getFileName());
			context.set(EmbeddedDocumentExtractor.class, documentExtractor);

			parser.parse(content.getInputStream(), handler, metadata, context);

			String outputString = output.toString();
			outputString = replaceImageEmbeddings(outputString,documentExtractor.getImageNameIDMap());
			EnhanceHTML eh = new EnhanceHTML(outputString);
			outputString = eh.enhance();

			return (outputString);
			// String id =
			// docStore.storeStringFile(enhancedString,"fileContent");
			// return docStore.getLink()+"/"+id+"/fileContent";
		} catch (TransformerConfigurationException | SAXException | TikaException | IOException ex) {
			Logger.getLogger(TikaDocAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
			throw ex;
		}
	}

	// creates a document in couchdb with filename and related images links
	// this is used to remove file related data when file is deleted. 
	private String createFileDataInProject(String fileName){
		return "";
	}
	private String replaceImageEmbeddings(String input,HashMap<String, String> imageNameIDMap) {

		// replace all embedded:<image name> to new name with id attached
		for (Entry<String, String> entry : imageNameIDMap.entrySet()) {
			String key = entry.getKey();
			String value = entry.getValue();
			input = input.replaceAll("embedded:" + key, docStore.getLink() + "/" + value + "/" + key);
		}
		return input;
	}

	public TikaDocAnalyzer(String projID) throws ParseException, MalformedURLException {

		try {
			this.docStore = DocStorageFactory.createDocStorageInstance(projID);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		context = new ParseContext();
		detector = new DefaultDetector();
		
		ansConverter = new AnswerUnitConverter();
        alchemyIntfc = new AlchemyIntfc();

	}

	private static TransformerHandler getTransformerHandler(OutputStream output, String method, String encoding,
			boolean prettyPrint) throws TransformerConfigurationException {
		SAXTransformerFactory factory = (SAXTransformerFactory) SAXTransformerFactory.newInstance();
		TransformerHandler handler = factory.newTransformerHandler();
		handler.getTransformer().setOutputProperty(OutputKeys.METHOD, method);
		handler.getTransformer().setOutputProperty(OutputKeys.INDENT, prettyPrint ? "yes" : "no");
		if (encoding != null) {
			handler.getTransformer().setOutputProperty(OutputKeys.ENCODING, encoding);
		}
		handler.setResult(new StreamResult(output));
		return handler;
	}

	@Override
	public String getFormatType() {
		throw new UnsupportedOperationException("Not supported yet."); // To
																		// change
																		// body
																		// of
																		// generated
																		// methods,
																		// choose
																		// Tools
																		// |
																		// Templates.
	}

	private class FileEmbeddedDocumentExtractor implements EmbeddedDocumentExtractor {

		private int count = 0;
		HashMap<String, String> imageNameIDMap;
		private String docFileName;
		public String getDocFileName() {
			return docFileName;
		}	
		public FileEmbeddedDocumentExtractor(String docFileName){
			this.docFileName = docFileName;
			imageNameIDMap = new HashMap<String, String>();
			
			//create document in couchdb to store image attachements
			
		}
		public void setDocFileName(String docFileName) {
			this.docFileName = docFileName;
			
		}
		public HashMap<String, String> getImageNameIDMap() {
			return imageNameIDMap;
		}
		private final TikaConfig config = TikaConfig.getDefaultConfig();

		@Override
		public boolean shouldParseEmbedded(Metadata metadata) {
			return true;
		}

		@Override
		public void parseEmbedded(InputStream inputStream, ContentHandler contentHandler, Metadata metadata,
				boolean outputHtml) throws SAXException, IOException {

			String name = metadata.get(Metadata.RESOURCE_NAME_KEY);

			if (name == null) {
				name = "file" + count++;
			}

			MediaType contentType = detector.detect(inputStream, metadata);

			if (name.indexOf('.') == -1 && contentType != null) {
				try {
					name += config.getMimeRepository().forName(contentType.toString()).getExtension();
				} catch (MimeTypeException e) {
					e.printStackTrace();
				}
			}

			/*
			 * String relID = metadata.get(Metadata.EMBEDDED_RELATIONSHIP_ID);
			 * if (relID != null && !name.startsWith(relID)) { name = relID +
			 * "_" + name; }
			 */

			if (!name.contains(".vs")) {
				if(name.contains("emf")||name.contains(".wmf")||name.contains("bmp")){
					//convert
				}
				String id = docStore.storeImage(inputStream, name, contentType.toString(),docFileName);
				//logger.debug("Stored: {}/{}/{}", docStore.getLink(), id, name);
logger.debug("Stored: {}/{}/{}", new Object[]{docStore.getLink(), id, name});
				imageNameIDMap.put(name, id);
			} else {
				logger.debug("Unsupported image file: {},{}", name,contentType.toString());
			}

			// *****Very important logic to put images into a file.

		}
	}

}

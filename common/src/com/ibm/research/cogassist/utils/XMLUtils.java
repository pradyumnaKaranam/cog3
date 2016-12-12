package com.ibm.research.cogassist.utils;

/**
 * 
 * @author monikgup
 *
 */

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLUtils {
	public static Document readXmlDOM(String filename) throws ParserConfigurationException, SAXException, IOException {
		File fXmlFile = new File(filename);
		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder;

		dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(fXmlFile);
		return doc;

	}

	public static Document createXMLDOM(String rootElementName, List<String> rootAttrs,
			List<String> rootAttrVals) throws ParserConfigurationException{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		DOMImplementation impl = builder.getDOMImplementation();

		Document doc = impl.createDocument(null,null,null);
		Element rootElt = doc.createElement(rootElementName);
		for(int i = 0; i < rootAttrs.size(); i++){
			rootElt.setAttribute(rootAttrs.get(i),rootAttrVals.get(i));
		}
		doc.appendChild(rootElt);
		return doc;
	}

	static public String toString(Document document) {
		String result = null;
		if (document != null) {
			StringWriter strWtr = new StringWriter();
			StreamResult strResult = new StreamResult(strWtr);
			TransformerFactory tfac = TransformerFactory.newInstance();
			try {
				Transformer t = tfac.newTransformer();
				// t.setOutputProperty(OutputKeys.ENCODING, "iso-8859-1");
				t.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
				t.setOutputProperty(OutputKeys.INDENT, "yes");
				t.setOutputProperty(OutputKeys.METHOD, "xml"); // xml, html,
				// text
				//				t.setOutputProperty(OutputKeys.CDATA_SECTION_ELEMENTS,
				//						"col,dataSource");
				t.setOutputProperty(
						"{http://xml.apache.org/xslt}indent-amount", "4");
				t.transform(new DOMSource(document.getDocumentElement()),
						strResult);
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("XML.toString(Document): " + e);
			}
			result = strResult.getWriter().toString();
		}
		return result;
	}// toString()

	public static void createNewXMLFile(String rootName, List<String> rootAttr, 
			List<String> rootAttrValues, String filename){
		Document doc;
		try {
			doc = createXMLDOM(rootName,rootAttr,rootAttrValues);
			CAFileUtils.writeFile(filename, toString(doc));
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String evaluateXPathToString(Document doc, String xpathExpr){
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		XPathExpression expr;
		try {
			expr = xpath.compile(xpathExpr);
			String result = (String) expr.evaluate(doc, XPathConstants.STRING);
			return result;
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static Node evaluateXPathToNode(Document doc, String xpathExpr){
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		XPathExpression expr;
		try {
			expr = xpath.compile(xpathExpr);
			Node result = (Node) expr.evaluate(doc, XPathConstants.NODE);
			return result;
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	public static NodeList evaluateXPathToNodeList(Document doc, String xpathExpr){
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		XPathExpression expr;
		try {
			expr = xpath.compile(xpathExpr);
			NodeList result = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
			return result;
		} catch (XPathExpressionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}

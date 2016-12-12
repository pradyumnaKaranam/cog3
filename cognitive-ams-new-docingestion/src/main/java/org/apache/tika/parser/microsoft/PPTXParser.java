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
package org.apache.tika.parser.microsoft;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.io.input.CloseShieldInputStream;
import org.apache.poi.POIXMLDocument;
import org.apache.poi.POIXMLTextExtractor;
import org.apache.poi.extractor.ExtractorFactory;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.xslf.extractor.XSLFPowerPointExtractor;
import org.apache.tika.exception.TikaException;
import org.apache.tika.io.TikaInputStream;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.AbstractParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.microsoft.ooxml.OOXMLExtractor;
import org.apache.tika.parser.pkg.ZipContainerDetector;
import org.apache.xmlbeans.XmlException;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Figures out the correct {@link OOXMLExtractor} for the supplied document and
 * returns it.
 */
public class PPTXParser extends AbstractParser {

    public void parse(
            InputStream stream, ContentHandler baseHandler,
            Metadata metadata, ParseContext context)
            throws IOException, SAXException, TikaException {
        Locale locale = context.get(Locale.class, Locale.getDefault());
        ExtractorFactory.setThreadPrefersEventExtractors(true);

        try {
            OOXMLExtractor extractor;
            OPCPackage pkg;

            // Locate or Open the OPCPackage for the file
            TikaInputStream tis = TikaInputStream.cast(stream);
            if (tis != null && tis.getOpenContainer() instanceof OPCPackage) {
                pkg = (OPCPackage) tis.getOpenContainer();
            } else if (tis != null && tis.hasFile()) {
                pkg = OPCPackage.open(tis.getFile().getPath(), PackageAccess.READ);
                tis.setOpenContainer(pkg);
            } else {
                InputStream shield = new CloseShieldInputStream(stream);
                pkg = OPCPackage.open(shield);
            }

            // Get the type, and ensure it's one we handle
            MediaType type = ZipContainerDetector.detectOfficeOpenXML(pkg);
            
            metadata.set(Metadata.CONTENT_TYPE, type.toString());

            // Have the appropriate OOXML text extractor picked
            POIXMLTextExtractor poiExtractor = ExtractorFactory.createExtractor(pkg);

            POIXMLDocument document = poiExtractor.getDocument();
            
            extractor = new XSLFExtractor2(
                        context, (XSLFPowerPointExtractor) poiExtractor);
            

            // Get the bulk of the metadata first, so that it's accessible during
            //  parsing if desired by the client (see TIKA-1109)
            extractor.getMetadataExtractor().extract(metadata);

            // Extract the text, along with any in-document metadata
            extractor.getXHTML(baseHandler, metadata, context);
        } catch (IllegalArgumentException e) {
            if (e.getMessage() != null &&
                    e.getMessage().startsWith("No supported documents found")) {
                throw new TikaException(
                        "TIKA-418: RuntimeException while getting content"
                                + " for thmx and xps file types", e);
            } else {
                throw new TikaException("Error creating OOXML extractor", e);
            }
        } catch (InvalidFormatException e) {
            throw new TikaException("Error creating OOXML extractor", e);
        } catch (OpenXML4JException e) {
            throw new TikaException("Error creating OOXML extractor", e);
        } catch (XmlException e) {
            throw new TikaException("Error creating OOXML extractor", e);

        }
    }

     private static final Set<MediaType> SUPPORTED_TYPES =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
                    MediaType.application("vnd.openxmlformats-officedocument.presentationml.presentation")
            )));

    @Override
    public Set<MediaType> getSupportedTypes(ParseContext context) {
        return SUPPORTED_TYPES;
    }
}

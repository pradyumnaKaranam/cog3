/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.apache.tika.parser.microsoft;


import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.poi.poifs.filesystem.DirectoryNode;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.sax.XHTMLContentHandler;
import org.xml.sax.SAXException;

/**
 *
 * @author sampath
 */
@SuppressWarnings("serial")
public class PPTParser extends OfficeParser {
    private static final Set<MediaType> SUPPORTED_TYPES =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
                    MediaType.application("vnd.ms-powerpoint")
            )));

    @Override
    public Set<MediaType> getSupportedTypes(ParseContext context) {
        return SUPPORTED_TYPES;
    }
    

    
    protected void parse(
            DirectoryNode root, ParseContext context, Metadata metadata, XHTMLContentHandler xhtml)
            throws IOException, SAXException, TikaException {
            
            new HSLFExtractor2(context).parse(root, xhtml);
        
    }

    
    
}

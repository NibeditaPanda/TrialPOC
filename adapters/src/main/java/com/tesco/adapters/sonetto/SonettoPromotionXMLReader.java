package com.tesco.adapters.sonetto;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.FileInputStream;
import java.io.IOException;

public class SonettoPromotionXMLReader {
    private final SAXParser parser;
    private String filePath;
    private DefaultHandler handler;

    public SonettoPromotionXMLReader(String filePath, DefaultHandler handler) throws ParserConfigurationException, SAXException {
        this.filePath = filePath;
        this.handler = handler;
        parser = SAXParserFactory.newInstance().newSAXParser();
    }

    public void read() throws IOException, SAXException {
        InputSource is = new InputSource(new FileInputStream(this.filePath));
        is.setEncoding("UTF-8");
        parser.parse(filePath, handler);
    }

}

package soya.framework.transform.transformers;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import soya.framework.transform.TemplateBased;
import soya.framework.transform.TransformerException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public class XsltTransformer implements XmlDataTransformer, TemplateBased {
    private final String url;

    protected TransformerFactory transformerFactory;
    protected javax.xml.transform.Transformer transformer;

    protected DocumentBuilderFactory factory;
    protected DocumentBuilder builder;

    public XsltTransformer(String url) {
        this.url = url;
        try {
            transformerFactory = TransformerFactory.newInstance();

            InputStream is = getClass().getClassLoader().getResourceAsStream(url);
            StreamSource style = new StreamSource(is);

            transformer = transformerFactory.newTransformer(style);

            factory = DocumentBuilderFactory.newInstance();
            //factory.setNamespaceAware(true);
            builder = factory.newDocumentBuilder();


        } catch (NullPointerException e) {
            throw new IllegalArgumentException("Resource not found: " + url);

        } catch (TransformerConfigurationException | ParserConfigurationException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public OutputStream transform(InputStream src) throws TransformerException {
        try {
            Document document = builder.parse(src);
            DOMSource source = new DOMSource(document);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            StreamResult result = new StreamResult(bos);
            transformer.transform(source, result);

            return bos;

        } catch (SAXException | IOException | javax.xml.transform.TransformerException e) {
            throw new TransformerException(e);

        }
    }
}

package de.domschmidt.koku.carddav.helper;

import de.domschmidt.koku.carddav.DAVConstants;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.QName;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

public final class DavUtils {

    public static String toXmlString(final Document result) throws IOException {
        StringWriter out = new StringWriter();
        OutputFormat format = new OutputFormat();
        format.setLineSeparator("\n");
        format.setOmitEncoding(true);
        XMLWriter writer = new XMLWriter(out, format);
        writer.setEscapeText(false);
        writer.write(result);
        writer.flush();
        return out.toString();
    }

    public static List<Element> parseRequestedDavProps(final Document saxDocument) {
        final Element root = saxDocument.getRootElement();
        final Element prop = root.element(new QName("prop", Namespace.get(DAVConstants.DAV_NAMESPACE)));
        return prop.elements();
    }

    public static Element attachMultiStatusResponse(final Document document) {
        Element root = document.addElement("d:multistatus");
        root.addNamespace("d", DAVConstants.DAV_NAMESPACE);
        root.addNamespace("card", DAVConstants.CARDDAV_NAMESPACE);
        return root;
    }

}

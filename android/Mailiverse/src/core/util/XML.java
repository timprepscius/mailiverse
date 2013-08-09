package core.util;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XML 
{
	public static int ELEMENT_NODE = Node.ELEMENT_NODE;
	
	public static Object parse(String xml) throws Exception
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));

		return doc;
	}
	
	public static Object[] getElementsByTagName(Object doc, String tag) throws Exception
	{
		NodeList contents;
		
		if (doc instanceof Document)
			contents = ((Document)doc).getElementsByTagName(tag);
		else
			contents = ((Element) doc).getElementsByTagName(tag);
		
		Object[] result = new Object[contents.getLength()];
		
		for (int i=0; i<contents.getLength(); ++i)
			result[i] = contents.item(i);
		
		return result;
	}

	public static short getNodeType(Object currentNode) 
	{
		return ((Node) currentNode).getNodeType();
	}

	public static String textOf(Object keyNode) 
	{
		return ((Element)keyNode).getTextContent();
	}
	
}

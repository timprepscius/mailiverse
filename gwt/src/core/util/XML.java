/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package core.util;

import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

public class XML 
{
	public static int ELEMENT_NODE = Node.ELEMENT_NODE;

	public static Object parse(String result)
	{
		Document doc = XMLParser.parse(result);
		return doc;
	}
	
	public static Object[] getElementsByTagName(Object doc, String tag) throws Exception
	{
		NodeList contents = null;
		if (doc instanceof Element)
			contents = ((Element) doc).getElementsByTagName(tag);
		else
		if (doc instanceof Document)
			contents = ((Document) doc).getElementsByTagName(tag);

		Object[] result = new Object[contents.getLength()];
		
		for (int i=0; i<contents.getLength(); ++i)
			result[i] = contents.item(i);
		
		return result;
	}

	public static short getNodeType(Object currentNode) 
	{
		return ((Node) currentNode).getNodeType();
	}

	public static String textOf(Object node) 
	{
		return ((Node) node).getFirstChild().getNodeValue();
	}
}

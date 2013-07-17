/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.ArrayList;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;

import core.util.Characters;
import core.util.LogNull;
import core.util.Pair;
import core.util.Strings;

@Export()
public class Body implements Exportable
{
	static LogNull log = new LogNull(Body.class);
	
	String text, html;
	
	public Body ()
	{
	}
	
	public Body(Body body)
	{
		this.text = body.text;
		this.html = body.html;
	}

	public boolean hasText()
	{		
		return text != null;
	}
	
	public String getText()
	{
		return text;
	}

	public void setText(String text) 
	{
		this.text = text;
	}
	
	public boolean hasHTML()
	{		
		return html != null;
	}
	
	public String getHTML()
	{
		return html;
	}
	
	/*
	static class Patterns {
		static Pattern 
			removeStartHtml = Pattern.compile(".*<\\s*+html.*?>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
			removeHeadBlock = Pattern.compile(".*<\\s*+\\/head.*?>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
			removeScripting = Pattern.compile("<\\s*+script[\\s>]++.*?<\\s*+\\/script.*?>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
			removeBeforeBody1 = Pattern.compile(".*\\<body\\>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
			removeBeforeBody2 = Pattern.compile(".*\\<body\\s", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
			removeAfterBody = Pattern.compile("<\\s*+\\/body[\\s>]++.*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL),
			removeEndHtml = Pattern.compile("<\\s*+\\/html[\\s>]++.*", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
	};
	*/
	public String getStrippedHTML ()
	{
		return stripHTML(html);
	}
	
	public String getStrippedText ()
	{
		return stripHTML(text);
	}
	
	/* too slow
	public String stripHTML (String html)
	{
		if (html == null)
			return null;

		log.debug("stripHTML");

		// remove the <html> tag
		html = Patterns.removeStartHtml.matcher(html).replaceFirst("");
		log.debug("after removeStartHtml");
		
		// try to remove the head block
		html = Patterns.removeHeadBlock.matcher(html).replaceFirst("");
		log.debug("after removeHeadBlock");
		
		// remove scripting if possible
		html = Patterns.removeScripting.matcher(html).replaceAll("");
		log.debug("after removeScripting");
		
		// remove before the body
		html = Patterns.removeBeforeBody1.matcher(html).replaceFirst("<div>");
		log.debug("after removeBeforeBody1");

		html = Patterns.removeBeforeBody2.matcher(html).replaceFirst("<div ");
		log.debug("after removeBeforeBody2");
		
		// remove after the body
		html = Patterns.removeAfterBody.matcher(html).replaceFirst("</div>");
		log.debug("after removeAfterBody");
		
		// remove the html tag ender and everyhting aftewards
		html = Patterns.removeEndHtml.matcher(html).replaceAll("");
		log.debug("after removeEndHtml");
		
		return html;
	}
	*/

	/* maybe for another day
	public String stripHTML (String html) throws Exception
	{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setValidating(false);
		dbf.setNamespaceAware(true);
		dbf.setIgnoringComments(false);
		dbf.setIgnoringElementContentWhitespace(false);
		dbf.setExpandEntityReferences(false);
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(new StringInputStream(html));
	}
	*/
	
	protected static Pair<Integer, Integer> findTag(boolean last, String html, String tag, Pair<Integer, Integer> search)
	{
		// adkjfkasdflk asdlkasd < html > ojwefijoweiofjoiwjfwe
		// find: "html"
		
		Pair<Integer,Integer> range = Pair.create(search.first, search.second);
		int pos = -1;
		
		while (true)
		{
			
			if (last)
			{
				if (pos != -1)
					range.second = pos-1;
				
				pos = html.lastIndexOf(tag, range.second);
				if (pos == -1 || pos < range.first)
					return null;
			}
			else
			{
				if (pos != -1)
					range.first = pos + tag.length();
				
				if (html.length() <= range.first + tag.length())
					return null;
				
				pos = html.indexOf(tag, range.first);
				if (pos == -1 || pos > range.second)
					return null;
			}
			
			// search backwards for <
			boolean restart = false;
			int i;
			for (i=pos-1; i>=0; i--)
			{
				char c = html.charAt(i);
				if (c=='<')
					break;
				else
				// wan't our tag signal for restart search
				if (!Characters.isWhitespace(c))
				{
					restart = true;
					break;
				}
			}
			
			// restart the search if signaled
			if (restart)
				continue;
			
			if (i == -1)
				return null;
			
			int j;
			for (j=pos+tag.length(); j<html.length(); ++j)
			{
				char c = html.charAt(j);
				if (c=='>')
					break;
			}
			
			// restart the search
			if (j == html.length())
				continue;
			
			log.debug("found",tag,i,j);
			
			return Pair.create(i,j+1);
		}
	}
	
	protected static void andLeftRange (Pair<Integer, Integer> range, Pair<Integer,Integer> tag)
	{
		if (tag == null)
			return;
		
		if (tag.second > range.first)
			range.first = tag.second;
	}
	
	protected static void andRightRange (Pair<Integer, Integer> range, Pair<Integer,Integer> tag)
	{
		if (tag == null)
			return;

		if (tag.first < range.second)
			range.second = tag.first;
	}
	
	public static String stripHTML (String html)
	{
		if (html == null)
			return null;
		
		String lower = html.toLowerCase();
		Pair<Integer,Integer> range = Pair.create(0, html.length());
		Pair<Integer,Integer> tag;
		
		//-----------------
		
		tag = findTag(true, lower, "body", range);
		andLeftRange(range,tag);

		tag = findTag(true, lower, "html", range);
		andLeftRange(range,tag);

		tag = findTag(true, lower, "/head", range);
		andLeftRange(range,tag);
		
		//----------------

		tag = findTag(false, lower, "/body", range);
		andRightRange(range,tag);

		tag = findTag(false, lower, "/html", range);
		andRightRange(range,tag);
		
		//----------------
		String result = html.substring(range.first, range.second);
		log.debug("stripHTML", result);
		return result;
	}

	/*
	public static void main (String[] args)
	{
		String html = "kjhasdfkjakjd <nothtml> < html > <nothtml> < body > woeifjweijf <nothtml> </body> </nothtml> </html> <nothtml>";
		System.out.println(stripHTML(html));
	}
	*/
	
	public void setHTML(String html)
	{
		this.html = html;
	}
	
	public String calculateBrief ()
	{
		if (text == null)
			return null;
		
		String content = calculateTextWithoutReply();
		int length = Math.min(content.length(), 256);
		String brief = content.substring(0, length).replace("\n", " ");
		
		return brief;
	}
	
	public String calculateReply ()
	{
		if (text == null)
			return "";
		
		try
		{
			ArrayList<String> lines = new ArrayList<String>();
			BufferedReader r = new BufferedReader(new StringReader(text));
			String line;
			while ((line = r.readLine()) != null)
			{
				lines.add("> " + line);
			}
	
			return Strings.concat(lines.iterator(), "\n").trim();
		}
		catch (Exception e)
		{
			return "Failed to calculate reply";
		}
	}
	
	public boolean isProbablyReplyHeader (String s)
	{
		return (s.endsWith(":") && s.contains("On"));
	}
	
	public boolean isPossiblyQuoteBeginning (String s)
	{
		return s.startsWith("--") || s.startsWith("==");
	}
	
	public String calculateTextWithoutReply ()
	{
		if (text == null)
			return "";
		
		try
		{
			ArrayList<String> lines = new ArrayList<String>();
			BufferedReader r = new BufferedReader(new StringReader(text));
			String line;
			
			boolean alreadyFoundReply = false;
			int possiblyFoundQuote = -1;
			while ((line = r.readLine()) != null)
			{
				if (line.startsWith(">"))
				{
					if (!alreadyFoundReply)
					{	
						alreadyFoundReply = true;
						
						for (int i=0; i<5; ++i)
						{
							int index = (lines.size()-i)-1;
							if (index < 0)
								break;
							
							if (isProbablyReplyHeader(lines.get(index)))
							{
								assert(index >=0 );
								while (lines.size() > index)
									lines.remove(lines.size()-1);
								
								break;
							}
						}
					}
				}
				else
				{
					lines.add(line);

					if (isPossiblyQuoteBeginning(line))
						possiblyFoundQuote = lines.size()-1;
				}
			}
			
			while (!lines.isEmpty() && lines.get(lines.size()-1).trim().isEmpty())
				lines.remove(lines.size()-1);
		
			if (possiblyFoundQuote >= 0 && possiblyFoundQuote > (lines.size()-6))
			{
				while (lines.size() > possiblyFoundQuote)
					lines.remove(lines.size()-1);
			}
			
			return Strings.concat(lines.iterator(), "\n").trim();
		}
		catch (Exception e)
		{
			return e.toString();
		}
	}
}

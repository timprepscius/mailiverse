/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_Body_h__
#define __mailiverse_mail_Body_h__

#include "mailiverse/Types.h"
#include "mailiverse/utilities/SmartPtr.h"
#include "mailiverse/utilities/Strings.h"
#include "mailiverse/Exception.h"
#include "mailiverse/utilities/Log.h"

namespace mailiverse {
namespace mail {
namespace model {

class Body {

protected:
	StringPtr text;
	StringPtr html;
	
public:
	Body (String *_text, String *_html) :
		text(_text),
		html(_html)
	{
		
	}
	
	Body () {}
	
	bool hasText () const
	{
		return text;
	}
	
	String *getText () const
	{
		return text;
	}
	
	void setText(String *text)
	{
		this->text = text;
	}
	
	bool hasHTML () const
	{
		return html;
	}
	
	String *getHTML () const
	{
		return html;
	}
	
	void setHTML (String *html)
	{
		this->html = html;
	}
	
	StringPtr getHTMLStripped ()
	{
		return encapsulateHTML(stripHTML(html));
	}

	StringPtr getTextHTMLified ()
	{
		String text = calculateHTMLifiedText();
		return encapsulateHTML(&text);
	}
	
	static StringPtr encapsulateHTML(String *_html)
	{
		if (!_html)
			return NULL;
	
		String &html = *_html;	

/*		
		const String _prefix = 
			"<div>";
		const String _postfix = 
			"</div>"
			"<script>"
			"	var e = document.getElementById('___RANDOM___');"
			"	var sections = e.getElementsByTagName('blockquote');"
			"	for(var i = 0; i < sections.length; ++i) {"
			"		var s = sections[i];"
			"		var a = document.createElement('a');"
			"		a.setAttribute('href', 'javascript:void');"
			"		var js ="
			"			'if(this.innerText==\\'-show quoted-\\')' +"
			"			'{  this.innerText=\\'-hide quoted-\\'; this.nextSibling.style.display=\\'block\\'; } else' +"
			"			'{  this.innerText=\\'-show quoted-\\'; this.nextSibling.style.display=\\'none\\'; }';"
			"		s.setAttribute('onclick', js);"
			"		a.appendChild(document.createTextNode('-show quoted-'));"
			"		s.parentNode.insertBefore(a,s);"
			"	}"
			"</script>";
		
		String result = 
			_prefix + html + _postfix;
			
*/

		String result = html;

		utilities::ireplaceAll(
			result, 
			"<blockquote", 
			"<div class='__showQuote__' onclick=\""
			"	if(this.innerText=='-show quoted-')"
			"	{  this.innerText='-hide quoted-'; this.nextSibling.style.display='block'; __propagateHeightChange__(); } else"
			"	{  this.innerText='-show quoted-'; this.nextSibling.style.display='none'; __propagateHeightChange__(); }"
			"	\">-show quoted-</div><blockquote"
		);
		
		LogDebug(mailiverse::mail::model::Body::encapsulateHTML, result);
		
		return new String(result);
	}

	const static Pair<int,int> NO_TAG;

	static Pair<int, int> findTag(bool last, const String &html, const String &tag, const Pair<int, int> &search)
	{
		// adkjfkasdflk asdlkasd < html > ojwefijoweiofjoiwjfwe
		// find: "html"

		Pair<int,int> range(search.first, search.second);
		int pos = -1;

		while (true)
		{
			if (last)
			{
				if (pos != -1)
					range.second = pos-1;

				pos = html.rfind(tag, range.second);
				if (pos == -1 || pos < range.first)
					return NO_TAG;
			}
			else
			{
				if (pos != -1)
					range.first = pos + tag.length();

				if (html.length() <= range.first + tag.length())
					return NO_TAG;

				pos = html.find(tag, range.first);
				if (pos == -1 || pos > range.second)
					return NO_TAG;
			}

			// search backwards for <
			bool restart = false;
			int i;
			for (i=pos-1; i>=0; i--)
			{
				char c = html[i];
				if (c=='<')
					break;
				else
				// wan't our tag signal for restart search
				if (!utilities::isWhitespace(c))
				{
					restart = true;
					break;
				}
			}

			// restart the search if signaled
			if (restart)
				continue;

			if (i == -1)
				return NO_TAG;

			int j;
			for (j=pos+tag.length(); j<html.length(); ++j)
			{
				char c = html[j];
				if (c=='>')
					break;
			}

			// restart the search
			if (j == html.length())
				continue;

			return Pair<int,int>(i,j+1);
		}
	}

	static void andLeftRange (Pair<int, int> &range, const Pair<int,int> &tag)
	{
		if (tag == NO_TAG)
			return;

		if (tag.second > range.first)
			range.first = tag.second;
	}

	static void andRightRange (Pair<int, int> &range, const Pair<int,int> &tag)
	{
		if (tag == NO_TAG)
			return;

		if (tag.first < range.second)
			range.second = tag.first;
	}

	static StringPtr stripHTML (String *html)
	{
		if (html == NULL)
			return NULL;

		String lower = utilities::toLowerCase(*html);
		Pair<int,int> range = Pair<int,int>(0, html->length());
		Pair<int,int> tag;

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
		String result = html->substr(range.first, range.second - range.first);
		return new String(result);
	}

	StringPtr calculateBrief ()
	{
		if (!text)
			return NULL;

		String content = calculateTextWithoutReply();
		int length = std::min((int)content.length(), 256);
		content = content.substr(0, length);
		utilities::replaceAll(content, "\n", " ");

		return new String(content);
	}

	String calculateReply ()
	{
		if (!text)
			return "";

		List<String> result;
		std::list<String> lines = utilities::splitLines(*text);

		for (auto &i : lines)
			result.add("> " + i);

		return String(utilities::trim(utilities::join(result, "\n")));
	}

	bool isProbablyReplyHeader (const String &s)
	{
		return (utilities::endsWith(s,":") && utilities::contains(s,"On"));
	}

	bool isPossiblyQuoteBeginning (const String &s)
	{
		return utilities::startsWith(s, "--") || utilities::startsWith(s,"==");
	}

	String calculateHTMLifiedText ()
	{
		std::vector<String> lines = utilities::splitLinesV(*text);

		// need to put elsewherez
		const int MAX_LINES_TO_SEARCH_FOR_QUOTE = 6;
		String BQB = "___BLOCK_QUOTE_BEGIN___";
		String BQE = "___BLOCK_QUOTE_END___";
		
		int possiblyFoundQuote = -1;

		Vector<int> commentStack;
		commentStack.push_back(0);
		
		int lineNumber= 0;
		for (String &line : lines)
		{
			int commentIndicatorCount = 0;
			int pos = 0;
			while (utilities::firstNonWhiteSpace(line,">",pos))
				commentIndicatorCount++;
				
			while (commentIndicatorCount < commentStack.back())
			{
				lines[lineNumber-1] += BQE;
				commentStack.pop_back();
			}
			if (commentIndicatorCount > commentStack.back())
			{
				commentStack.push_back(commentIndicatorCount);
				line = BQB + line;
			}
			
			if (isPossiblyQuoteBeginning(line))
				possiblyFoundQuote = lineNumber;
				
			lineNumber++;
		}

		while (!lines.empty() && utilities::trim(lines.back()).empty())
			lines.erase(lines.end()-1);

		if (possiblyFoundQuote >= 0 && possiblyFoundQuote > (lines.size()-MAX_LINES_TO_SEARCH_FOR_QUOTE))
		{
			lines[possiblyFoundQuote] = BQB + lines[possiblyFoundQuote];
			lines.back() += BQE; 
		}

		String final = utilities::trim(utilities::join(lines, "\n"));
		
		// this is really crappy
		// and I know it
		utilities::replaceAll(final,"&", "&amp;");
		utilities::replaceAll(final,"<", "&lt;");
		utilities::replaceAll(final,">", "&gt;");
		utilities::replaceAll(final,"\n", "<br>");
		utilities::replaceAll(final,BQB,"<blockquote>");
		utilities::replaceAll(final,BQE,"</blockquote>");
		return final;
	}

	String calculateTextWithoutReply ()
	{
		try
		{
			Vector<String> lines;
			std::list<String> reader = utilities::splitLines(*text);

			bool alreadyFoundReply = false;
			int possiblyFoundQuote = -1;
			for (auto &line : reader)
			{
				if (utilities::startsWith(line,">"))
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
									lines.removeIndex(lines.size()-1);

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

			while (!lines.empty() && utilities::trim(lines.get(lines.size()-1)).empty())
				lines.removeIndex(lines.size()-1);

			if (possiblyFoundQuote >= 0 && possiblyFoundQuote > (lines.size()-6))
			{
				while (lines.size() > possiblyFoundQuote)
					lines.removeIndex(lines.size()-1);
			}

			return utilities::trim(utilities::join(lines, "\n"));
		}
		catch (const Exception &e)
		{
			return e.what();
		}
	}


} ;

DECLARE_SMARTPTR(Body);

} // namespace
} // namespace
} // namespace

#endif

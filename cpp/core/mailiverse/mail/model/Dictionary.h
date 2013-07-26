/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_model_Dictionary_h__
#define __mailiverse_mail_model_Dictionary_h__

#include "mailiverse/utilities/Strings.h"
#include "mailiverse/utilities/Algorithm.h"
#include "mailiverse/utilities/SmartPtr.h"
#include "mailiverse/utilities/Log.h"
#include "mailiverse/Types.h"
#include "Library.h"
#include "Record.h"

namespace mailiverse {
namespace mail {
namespace model {

class Mail;

class Dictionary
{
public:
	struct Word {
		const char *ptr;
		short length;
		int usage;
	} ;
	
	struct WordComparator {
		bool operator ()(const Word &left, const Word &right)
		{
			int l = std::min(left.length, right.length);
			int c = strncmp(left.ptr, right.ptr, l);
			
			if (c == 0 && right.length != left.length)
				c = (right.length > left.length) ? -1 : 1;
				
			return c < 0;
		}
		
		bool equals(const Word &left, const Word &right)
		{
			if (left.length != right.length)
				return false;
				
			return strncmp(left.ptr, right.ptr, left.length)==0;
		}
		
		bool startsWith(const Word &left, const Word &right)
		{
			return utilities::startsWith(left.ptr, left.length, right.ptr, right.length);
		}
	} ;
	
	static WordComparator wordComparator;

protected:
	bool realized;
	Vector<Word> vocabulary;
	std::vector<String> sources;
	utilities::EmptyMonitor monitor;
	
	void addSource (const String &_source)
	{
		utilities::EmptyMonitor::Writer writer(monitor);
	
		sources.push_back(_source);
		realized = false;
	}
	
	void realizeSourceNoLock(const String &source)
	{
		std::vector<Word> insertions;
	
		int i=0;
		const char *str = source.data();
		while (i < source.length())
		{
			int colon = source.find(':', i);
			int comma = source.find(',', colon);
			if (comma == -1)
				comma = source.length(); // an imaginary comma at the end

			static char buffer[256];
			strncpy(buffer, (str+colon)+1,(comma-colon)-1);
			buffer[(comma-colon)-1]=0;
			
			Word word = { str + i, (short)(colon - i),atoi(buffer) };

			auto j = std::lower_bound(vocabulary.begin(), vocabulary.end(), word, wordComparator);
			if (j!=vocabulary.end() && wordComparator.equals(*j, word))
				j->usage += word.usage;
			else
				insertions.push_back(word);
			
			i = comma+1;
		}
		
		for (Word word : insertions)
			vocabulary.push_back(word);
		
		std::sort(vocabulary.begin(), vocabulary.end(), wordComparator);
	}
	
	void realize ()
	{
		utilities::EmptyMonitor::Writer writer(monitor);

		if (realized)
			return;
			
		vocabulary.clear();
		for (auto &source : sources)
			realizeSourceNoLock(source);
			
		realized = true;
	}

	void consolidate()
	{
		realize();
		
		utilities::EmptyMonitor::Writer writer(monitor);

		bool first = true;
		std::ostringstream ss;
		for (Word word : vocabulary)
		{
			if (!first)
				ss << ",";
				
			ss << std::string(word.ptr, word.length);
			ss << ":";
			ss << word.usage;

			first = false;
		}
		
		sources.clear();
		vocabulary.clear();
		addSource(ss.str());
	}
	
	bool contains (const Word &word)
	{
		return std::binary_search(vocabulary.begin(), vocabulary.end(), word, wordComparator);
	}
	
public:
	Dictionary() 
	{
	}

	Dictionary (const String &filter)
	{
		add(filter);
	}

	Dictionary (Mail *mail)
	{
		add (mail);
	}
	
	virtual ~Dictionary() 
	{
	}

	const String &serialize ()
	{
		consolidate();
		return sources.front();
	}
	
	void deserialize(const String &text)
	{
		addSource(text);
	}

	Dictionary *add (const String &text)
	{
		std::list<std::string> tokens = utilities::tokenize(utilities::toLowerCase(text));
		
		std::map<std::string, int> counts;
		bool first = true;
		for (auto &token : tokens)
		{
			if (counts.find(token)==counts.end())
				counts[token] = 1;
			else
				counts[token]++;
		}
		
		std::ostringstream ss;
		for (auto &i : counts)
		{
			if (!first)
				ss << ",";
				
			ss << i.first << ":" << i.second;
			first = false;
		}
		
		addSource(ss.str());
	
		return this;
	}
	
	Dictionary *add (Mail *mail);

	bool matches (Dictionary *filter)
	{
		realize();
		filter->realize();
		
		utilities::EmptyMonitor::Reader reader(monitor);
		
		const String Q = "\"";

		bool found = true;
		
		Word exacter;
		
		for (auto &i : filter->vocabulary)
		{
			Word *match = &i;

			bool exact = 
				(utilities::startsWith(match->ptr, match->length, Q.c_str(), Q.length()) && 
				 utilities::endsWith(match->ptr, match->length, Q.c_str(), Q.length()));

			if (exact)
			{
				exacter = *match;
				exacter.ptr+=1;
				exacter.length-=2;
				match = &exacter;
			}

			LogDebug (mailiverse::model::Dictionary::matches, "M: " << std::string(match->ptr,match->length));

			auto j = std::lower_bound(
				vocabulary.begin(), vocabulary.end(), 
				*match, 
				wordComparator
			);

			if (exact)
			{
				found = (j != vocabulary.end()) && wordComparator(*match,*j)==0;
			}
			else
			{
				found = (j != vocabulary.end()) && wordComparator.startsWith(*j, *match);
			}
			
			LogDebug (mailiverse::model::Dictionary::matches, "C: " << (j!=vocabulary.end() ? std::string(j->ptr, j->length) : std::string()));

			if (found)
				break;
		}

		return found;
	}
	
	//-----------------------------------------------------------------
	
	void add (Dictionary *dictionary)
	{
	}
	
	void subtract(Dictionary *dictionary) 
	{
	}
	
	bool bayesianMatches(Dictionary *dictionary) 
	{
		return false;
	}
	
};

DECLARE_SMARTPTR(Dictionary);

} /* namespace model */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* __mailiverse_mail_model_accumulator_h__ */

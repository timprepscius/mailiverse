/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "Strings.h"
#include <boost/date_time/posix_time/posix_time.hpp>
#include <boost/algorithm/string/finder.hpp>
#include <boost/algorithm/string/replace.hpp>
#include <boost/algorithm/string.hpp>
#include <boost/algorithm/string/compare.hpp>


using namespace mailiverse::utilities;
using namespace mailiverse;

std::time_t posixTimeToTimeT(const boost::posix_time::ptime& pt)
{
    boost::posix_time::ptime timet_start(boost::gregorian::date(1970,1,1));
    boost::posix_time::time_duration diff = pt - timet_start;
    return diff.ticks()/boost::posix_time::time_duration::rep_type::ticks_per_second;
}

time_t utilities::parseDate (const std::string &date, const std::string &format, bool zoneOnEnd)
{
	std::istringstream iss(date);
	iss.imbue(std::locale(std::locale::classic(),new boost::posix_time::time_input_facet(format)));
	boost::posix_time::ptime pt;
	iss >> pt;
	
	time_t t = posixTimeToTimeT(pt);

	if (zoneOnEnd)
	{
		std::string zone;
		iss >> zone;
		t -= (fromString<int>(zone)/100) * 60 * 60;
	}
	
//	std::cerr << "\nconverted \n\t" << date << " to \n\t" << ctime(&t) << std::endl;
	
	return t;
}

std::list<std::string> utilities::tokenize(const std::string &_text)
{
	std::string text = _text;
	boost::to_lower(text);

	std::list<std::string> results;
	boost::char_separator<char> sep(" \t\n\r!#$%^&*,./;'\"[]|=+:()?-><");
	boost::tokenizer< boost::char_separator<char> > tokens(text, sep);
	for (auto &term : tokens)
		results.push_back(std::string(term));

	return results;
}

void utilities::replaceAll(std::string& str, const std::string& from, const std::string& to)
{
    size_t start_pos = 0;
    while((start_pos < str.size()) && ((start_pos = str.find(from, start_pos)) != std::string::npos))
	{
        str.replace(start_pos, from.length(), to);
        start_pos += to.length(); // In case 'to' contains 'from', like replacing 'x' with 'yx'
    }
}

// this is lame and I know it
void utilities::ireplaceAll(std::string& strC, const std::string& fromC, const std::string& to)
{
	std::string str = strC;
	std::string from = fromC;

	boost::to_lower(str);
	boost::to_lower(from);

    size_t start_pos = 0;
    while((start_pos < str.size()) && ((start_pos = str.find(from, start_pos)) != std::string::npos))
	{
        str.replace(start_pos, from.length(), to);
        strC.replace(start_pos, from.length(), to);
        start_pos += to.length(); // In case 'to' contains 'from', like replacing 'x' with 'yx'
    }
}

bool utilities::firstNonWhiteSpace(const std::string &s, const std::string &find, int &pos)
{
	if (pos >= s.size())
		return false;
		
	const char *notOf = " \t\n\r";
	pos = s.find_first_not_of(notOf, pos);
	
	bool result = pos!=-1 && find.find(s[pos])!=-1;
	pos ++;
	
	return result;
}


std::string utilities::trimQuotes(const std::string &s)
{
	std::string n = s;
	boost::algorithm::trim_if(n, boost::is_any_of("\'\""));
	return n;
}

std::string utilities::trim (const std::string &s)
{
	std::string n = s;
	boost::trim(n);
	return n;
}

std::list<std::string> utilities::splitLines(const std::string &s)
{
	std::list<std::string> lines;
	std::istringstream iss(s);

	std::string line;
	while (std::getline(iss, line))
	{
		boost::trim_right_if(line, boost::is_any_of("\n\r"));
		lines.push_back(line);
	}
	return lines;
}

std::vector<std::string> utilities::splitLinesV(const std::string &s)
{
	std::vector<std::string> lines;
	std::istringstream iss(s);

	std::string line;
	while (std::getline(iss, line))
	{
		boost::trim_right_if(line, boost::is_any_of("\n\r"));
		lines.push_back(line);
	}
	return lines;
}

std::vector<std::string> utilities::split(const std::string &s, const std::string &del)
{
	std::vector<std::string> words;
	boost::algorithm::split(words, s, boost::is_any_of(del));
	return words;
}

bool utilities::isWhitespace (char c)
{
	static std::string w(" \t\r\n");
	return w.find(c)!=-1;
}


bool utilities::startsWith (const std::string &lhs, const std::string &rhs)
{
	return boost::algorithm::starts_with(lhs, rhs);
}

bool utilities::startsWith (const char *lhs, const char *rhs)
{
	return boost::algorithm::starts_with(lhs, rhs);
}

bool utilities::startsWith (const char *lhs, int ll, const char *rhs, int rl)
{
	if (ll < rl)
		return false;
		
	return strncmp(lhs, rhs, rl)==0;
}

bool utilities::endsWith (const std::string &lhs, const std::string &rhs)
{
	return boost::algorithm::ends_with(lhs, rhs);
}

bool utilities::endsWith (const char *lhs, const char *rhs)
{
	return boost::algorithm::ends_with(lhs, rhs);
}

bool utilities::endsWith (const char *lhs, int ll, const char *rhs, int rl)
{
	if (ll < rl)
		return false;
		
	return strncmp(lhs+ll-rl, rhs, rl)==0;
}


bool utilities::contains (const std::string &lhs, const std::string &token)
{
	return lhs.find(token)!=-1;
}

bool lower_test (char l, char r) {
  return (std::tolower(l) == std::tolower(r));
}

bool utilities::icontains(const std::string & lhs, const std::string & token) 
{
	std::string::const_iterator fpos = std::search(lhs.begin(), lhs.end(), token.begin(), token.end(), lower_test);
	
	if (fpos != lhs.end())
		return true;
		
	return false;
}

bool utilities::matches (const std::string &lhs, const std::string &rhs)
{
	assert(false);
	return lhs==rhs;
}

std::string utilities::toLowerCase (const std::string &s)
{
	std::string n = s;
	boost::algorithm::to_lower(n);
	return n;
}

std::string utilities::toUpperCase (const std::string &s)
{
	std::string n = s;
	boost::algorithm::to_upper(n);
	return n;
}

std::wstring utilities::convert (const std::string &text)
{
        wchar_t *ws = new wchar_t[text.size()+1];
        size_t length = mbstowcs(ws, text.c_str(), text.size());

        std::wstring wst;
        if (length != -1)
        {
                ws[length] = 0;
                wst = ws;
        }

        delete[] ws;
        return wst;
}

std::string utilities::convert (const std::wstring &text)
{
        char *cs = new char[(text.size()+1)*4];
        size_t length = wcstombs(cs, text.c_str(), text.size());

        std::string cst;
        if (length != -1)
        {
                cs[length] = 0;
                cst = std::string(cs, length);
        }

        delete[] cs;
        return cst;
}

wchar_t utilities::convert (char c)
{
        wchar_t ws[2];
        char s[2];
        s[0] = c;
        s[1] = 0;

        mbstowcs(ws, s, 1);
        return ws[0];
}

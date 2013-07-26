/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */


#ifndef __Util_Strings_h__
#define __Util_Strings_h__

#include <string>
#include <time.h>
#include <sstream>
#include <list>
#include <vector>

namespace mailiverse {
namespace utilities {

std::wstring convert (const std::string &);
std::string convert (const std::wstring &);
wchar_t convert (char);

time_t parseDate (const std::string &date, const std::string &format, bool zoneOnEnd);

template<typename T>
inline T fromString (const std::string &str)
{
	std::istringstream iss(str);
	T t;
	iss >> t;
	return t;
}

template<typename T>
inline std::string toString (const T &t)
{
	std::ostringstream oss;
	oss << t;
	return oss.str();
}

std::string trimQuotes (const std::string &);

std::string trim (const std::string &s);

std::vector<std::string> split(const std::string &s, const std::string &del);

void replaceAll(std::string& str, const std::string& from, const std::string& to);
void ireplaceAll(std::string& str, const std::string& from, const std::string& to);

bool firstNonWhiteSpace(const std::string &s, const std::string &find, int &pos);

bool isWhitespace (char c);

template<typename T>
std::string join (const T &t, const std::string &del)
{
	std::ostringstream ss;

	bool first = true;
	for (auto &i : t)
	{
		if (!first)
			ss << del;

		ss << i;
		first = false;
	}

	return ss.str();
}

template<typename T>
std::string joinToString (const T &t, const std::string &del)
{
	std::ostringstream ss;

	bool first = true;
	for (auto &i : t)
	{
		if (!first)
			ss << del;

		ss << i->toString();
		first = false;
	}

	return ss.str();
}

std::list<std::string> splitLines (const std::string &s);
std::vector<std::string> splitLinesV (const std::string &s);

bool startsWith (const std::string &lhs, const std::string &rhs);
bool startsWith (const char *lhs, const char *rhs);
bool startsWith (const char *lhs, int ll, const char *rhs, int rl);

bool endsWith (const char *lhs, const char *rhs);
bool endsWith (const char *lhs, int ll, const char *rhs, int rl);
bool endsWith (const std::string &lhs, const std::string &rhs);

bool icontains (const std::string &lhs, const std::string &token);
bool contains (const std::string &lhs, const std::string &token);

bool matches (const std::string &lhs, const std::string &rhs);
std::list<std::string> tokenize (const std::string &rhs);

std::string toLowerCase (const std::string &s);
std::string toUpperCase (const std::string &s);

inline int str_find(const char *s, char del, int offset=0)
{
	char *result = strchr(s+offset, del);
	if (result == NULL)
		return -1;
	return (int)(result - s);
}

class Tokenizer
{
protected:
	const char *s;
	char del;
	
	int size;
	int left;
	int right;
	
public:
	
	Tokenizer (const char *_s, int _size, char _del) :
		s(_s), left(0), del(_del)
	{
		right = str_find(s, del);
		size = _size;
	}
	
	bool next (std::string &_s)
	{
		int _left,_size;
		
		bool result = next(_left, _size);
		if (result)
			_s = std::string(s+_left, _size); 
			
		return result;
	}
	
	bool next (int &_left, int &_size)
	{
		if (left != -1)
		{
			if (right == -1)
			{
				_left = left;
				_size = size - left;
				left = right;
			}
			else
			{
				_left = left;
				_size = right-left;
				
				left = right + 1;
				if (left < size)
					right = str_find(s, del, left);
				else
					left = -1;
			}
			
			return true;
		}
		
		return false;
	}
} ;

} // namespace
} // namespace

#endif

/******************************************************************************

Copyright (c) 2009-2010, Terry Caton
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright 
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the projecct nor the names of its contributors 
      may be used to endorse or promote products derived from this software 
      without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

******************************************************************************/



#pragma once

#include "elements.h"
#include <iostream>
#include <vector>
#include <set>

namespace json
{

class SimpleMemStream
{
private:
	const char *begin, *end, *pos;
public:
	SimpleMemStream(const char *_begin, const char *_end) :
		begin(_begin), end(_end), pos(_begin)
	{
	}
	
	SimpleMemStream(const char *_begin, int size) :
		begin(_begin), end(_begin + size), pos(_begin)
	{
	}
	
	SimpleMemStream(const std::string &s)
	{
		int size = s.size();
		begin = s.c_str();
		end = begin + size;
		pos = begin;
	}
	
	int get()
	{
		if (pos < end)
			return *pos++;
		return -1;
	}
	
	bool eof()
	{
		return pos == end;
	}
	
	int peek()
	{
		if (pos < end)
			return *pos;
			
		return -1;
	}
	
	int tellg()
	{
		return (pos-end);
	}
	
	void setg (int _pos)
	{
		pos = begin + _pos;
		if (pos > end)
			pos = end;
	}
};

template<typename istream=SimpleMemStream>
class Reader
{
public:
   // this structure will be reported in one of the exceptions defined below
   struct Location
   {
      Location() :
	   m_nLine(0),
	   m_nLineOffset(0),
	   m_nDocOffset(0)
	{}

      unsigned int m_nLine;       // document line, zero-indexed
      unsigned int m_nLineOffset; // character offset from beginning of line, zero indexed
      unsigned int m_nDocOffset;  // character offset from entire document, zero indexed
   };

   // thrown during the first phase of reading. generally catches low-level problems such
   //  as errant characters or corrupt/incomplete documents
   class ScanException : public Exception
   {
   public:
      ScanException(const std::string& sMessage, const Reader::Location& locError) :
         Exception(sMessage),
         m_locError(locError) {}

      Reader::Location m_locError;
   };

   // thrown during the second phase of reading. generally catches higher-level problems such
   //  as missing commas or brackets
   class ParseException : public Exception
   {
   public:
      ParseException(const std::string& sMessage, const Reader::Location& locTokenBegin, const Reader::Location& locTokenEnd) :
         Exception(sMessage),
         m_locTokenBegin(locTokenBegin),
         m_locTokenEnd(locTokenEnd) {}

      Reader::Location m_locTokenBegin;
      Reader::Location m_locTokenEnd;
   };


   // if you know what the document looks like, call one of these...
   static void Read(Object& o, istream& istr) { Read_i(o, istr); }
   static void Read(Array& o, istream& istr) { Read_i(o, istr); }
   static void Read(String& o, istream& istr) { Read_i(o, istr); }
   static void Read(Number& o, istream& istr) { Read_i(o, istr); }
   static void Read(Boolean& o, istream& istr) { Read_i(o, istr); }
   static void Read(Null& o, istream& istr) { Read_i(o, istr); }

   // ...otherwise, if you don't know, call this & visit it
   static void Read(UnknownElement& elementRoot, istream& istr);

private:
   struct Token
   {
      enum Type
      {
         TOKEN_OBJECT_BEGIN,  //    {
         TOKEN_OBJECT_END,    //    }
         TOKEN_ARRAY_BEGIN,   //    [
         TOKEN_ARRAY_END,     //    ]
         TOKEN_NEXT_ELEMENT,  //    ,
         TOKEN_MEMBER_ASSIGN, //    :
         TOKEN_STRING,        //    "xxx"
         TOKEN_NUMBER,        //    [+/-]000.000[e[+/-]000]
         TOKEN_BOOLEAN,       //    true -or- false
         TOKEN_NULL,          //    null
      };

      Type nType;
      String::ValueType sValue;
	  Number::ValueType nValue;

      // for malformed file debugging
      Reader::Location locBegin;
      Reader::Location locEnd;
   };

	class InputStream // would be cool if we could inherit from istream & override "get"
	{
	public:
	   InputStream(istream& iStr) :
		  m_iStr(iStr) {}

	   // protect access to the input stream, so we can keeep track of document/line offsets
	   char Get()
		{
		   assert(m_iStr.eof() == false); // enforce reading of only valid stream data 
		   char c = m_iStr.get();
		   
		   ++m_Location.m_nDocOffset;
		   if (c == '\n') {
			  ++m_Location.m_nLine;
			  m_Location.m_nLineOffset = 0;
		   }
		   else {
			  ++m_Location.m_nLineOffset;
		   }

		   return c;
		}
		
	   char Peek() {
		  assert(m_iStr.eof() == false); // enforce reading of only valid stream data 
		  return m_iStr.peek();
	   }

	   bool EOS() {
		  m_iStr.peek(); // apparently eof flag isn't set until a character read is attempted. whatever.
		  return m_iStr.eof();
	   }
	   
	   int TellG () {
			return m_iStr.tellg();
	   }
	   
	   int SetG (int pos) {
		m_iStr.setg(pos);
	   }

	   const Location& GetLocation() const { return m_Location; }

	private:
	   istream& m_iStr;
	   Location m_Location;
	};

   typedef std::vector<Token> Tokens;

   class TokenStream
   {
	public:
	   TokenStream(const Tokens& tokens):
	   m_Tokens(tokens),
	   m_itCurrent(tokens.begin())
	{}

	   const Token& Peek()
		{
			if (EOS())
			{
			  const Token& lastToken = *m_Tokens.rbegin();
			  std::string sMessage = "Unexpected end of token stream";
			  throw ParseException(sMessage, lastToken.locBegin, lastToken.locEnd); // nowhere to point to
			}
			return *(m_itCurrent); 
		}
	   const Token& Get()
		{
		   const Token& token = Peek();
		   ++m_itCurrent;
		   return token;
		}
		
	   bool EOS() const
	   {
		   return m_itCurrent == m_Tokens.end(); 
		}

	private:
	   const Tokens& m_Tokens;
	   typename Tokens::const_iterator m_itCurrent;
	};
   
   std::string buffer;
   std::set<char> numericChars;

   template <typename ElementTypeT>   
   static void Read_i(ElementTypeT& element, istream& istr);

   // scanning istream into token sequence
   void Scan(Tokens& tokens, InputStream& inputStream);

   void EatWhiteSpace(InputStream& inputStream);
   const std::string &MatchString(InputStream& inputStream);
   Number::ValueType MatchNumber(InputStream& inputStream);
   const std::string &MatchExpectedString(InputStream& inputStream, const std::string& sExpected);

   // parsing token sequence into element structure
   void Parse(UnknownElement& element, TokenStream& tokenStream);
   void Parse(Object& object, TokenStream& tokenStream);
   void Parse(Array& array, TokenStream& tokenStream);
   void Parse(String& string, TokenStream& tokenStream);
   void Parse(Number& number, TokenStream& tokenStream);
   void Parse(Boolean& boolean, TokenStream& tokenStream);
   void Parse(Null& null, TokenStream& tokenStream);

	unsigned int decodeUnicodeCodePoint (InputStream& inputStream);
	std::string codePointToUTF8(unsigned int cp);

   const std::string& MatchExpectedToken(typename Token::Type nExpected, TokenStream& tokenStream)
   {
	   const Token& token = tokenStream.Get();
	   if (token.nType != nExpected)
	   {
		  std::string sMessage = std::string("Unexpected token: ") + token.sValue;
		  throw ParseException(sMessage, token.locBegin, token.locEnd);
	   }

	   return token.sValue;
	}
	
	Reader ()
	{
		const char sNumericChars[] = "0123456789.eE-+";
		numericChars.insert(sNumericChars, sNumericChars + sizeof(sNumericChars));
		buffer.reserve(1024);
	}

};


} // End namespace


//#include "reader.inl"

/******************************************************************************

Copyright (c) 2009-2010, Terry Caton
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright 
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the projecct nor the names of its contributors 
      may be used to endorse or promote products derived from this software 
      without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

******************************************************************************/

#include <cassert>
#include <set>
#include <sstream>

/*  

TODO:
* better documentation
* unicode character decoding

*/

namespace json
{

template<typename istream>
inline istream& operator >> (istream& istr, UnknownElement& elementRoot) {
   Reader<istream>::Read(elementRoot, istr);
   return istr;
}

//////////////////////
// Reader::InputStream
///////////////////
// Reader (finally)


template <typename istream>
template <typename ElementTypeT>   
void Reader<istream>::Read_i(ElementTypeT& element, istream& istr)
{
   Reader reader;

   Tokens tokens;
   InputStream inputStream(istr);
   reader.Scan(tokens, inputStream);

   TokenStream tokenStream(tokens);
   reader.Parse(element, tokenStream);

   if (tokenStream.EOS() == false)
   {
      const Token& token = tokenStream.Peek();
      std::string sMessage = std::string("Expected End of token stream; found ") + token.sValue;
      throw ParseException(sMessage, token.locBegin, token.locEnd);
   }
}

template <typename istream>
inline void Reader<istream>::Scan(Tokens& tokens, InputStream& inputStream)
{
   while (EatWhiteSpace(inputStream),              // ignore any leading white space...
          inputStream.EOS() == false) // ...before checking for EOS
   {
      // if all goes well, we'll create a token each pass
      Token token;
      token.locBegin = inputStream.GetLocation();

      // gives us null-terminated string
      char sChar = inputStream.Peek();
      switch (sChar)
      {
         case '{':
            token.sValue = MatchExpectedString(inputStream, "{");
            token.nType = Token::TOKEN_OBJECT_BEGIN;
            break;

         case '}':
            token.sValue = MatchExpectedString(inputStream, "}");
            token.nType = Token::TOKEN_OBJECT_END;
            break;

         case '[':
            token.sValue = MatchExpectedString(inputStream, "[");
            token.nType = Token::TOKEN_ARRAY_BEGIN;
            break;

         case ']':
            token.sValue = MatchExpectedString(inputStream, "]");
            token.nType = Token::TOKEN_ARRAY_END;
            break;

         case ',':
            token.sValue = MatchExpectedString(inputStream, ",");
            token.nType = Token::TOKEN_NEXT_ELEMENT;
            break;

         case ':':
            token.sValue = MatchExpectedString(inputStream, ":");
            token.nType = Token::TOKEN_MEMBER_ASSIGN;
            break;

         case '"':
            token.sValue = MatchString(inputStream);
            token.nType = Token::TOKEN_STRING;
            break;

         case '-':
         case '0':
         case '1':
         case '2':
         case '3':
         case '4':
         case '5':
         case '6':
         case '7':
         case '8':
         case '9':
            token.nValue = MatchNumber(inputStream);
            token.nType = Token::TOKEN_NUMBER;
            break;

         case 't':
            token.sValue = MatchExpectedString(inputStream, "true");
            token.nType = Token::TOKEN_BOOLEAN;
            break;

         case 'f':
            token.sValue = MatchExpectedString(inputStream, "false");
            token.nType = Token::TOKEN_BOOLEAN;
            break;

         case 'n':
            token.sValue = MatchExpectedString(inputStream, "null");
            token.nType = Token::TOKEN_NULL;
            break;

         default:
         {
            std::string sErrorMessage = std::string("Unexpected character in stream: ") + sChar;
            throw ScanException(sErrorMessage, inputStream.GetLocation());
         }
      }

      token.locEnd = inputStream.GetLocation();
      tokens.push_back(token);
   }
}


template <typename istream>
inline void Reader<istream>::EatWhiteSpace(InputStream& inputStream)
{
   while (inputStream.EOS() == false && 
          ::isspace(inputStream.Peek()))
      inputStream.Get();
}

template <typename istream>
inline const std::string &Reader<istream>::MatchExpectedString(InputStream& inputStream, const std::string& sExpected)
{
   std::string::const_iterator it(sExpected.begin()),
                               itEnd(sExpected.end());
   for ( ; it != itEnd; ++it) {
      if (inputStream.EOS() ||      // did we reach the end before finding what we're looking for...
          inputStream.Get() != *it) // ...or did we find something different?
      {
         std::string sMessage = std::string("Expected string: ") + sExpected;
         throw ScanException(sMessage, inputStream.GetLocation());
      }
   }

   // all's well if we made it here
   return sExpected;
}

template <typename istream>
inline unsigned int Reader<istream>::decodeUnicodeCodePoint (InputStream& inputStream)
{
	unsigned int v_[2];
	unsigned int &unicode = v_[0];
	unsigned int &surrogate = v_[1];

	int n;
	for (n=0; n<2; n++)
	{
		unsigned int &v = v_[n];
		v = 0;
		
		for (int i=3; i>=0 && !inputStream.EOS(); --i)
		{
			char c = inputStream.Get();
			unsigned int ord = 0;
			if ('0' <= c && c <= '9')
				ord = c - '0';
			else
			if ('A' <= c && c <= 'F')
				ord = 10 + (c - 'A');
			else
			if ('a' <= c && c <= 'f')
				ord = 10 + (c - 'a');
				
			v |= ord << (4 * i);
		}
		
		bool hasSurrogate = false;
		if (v >= 0xD800 && v <= 0xDBFF)
		{
			int pos = inputStream.TellG();
			
			if (!inputStream.Get() != '\\' || !inputStream.Get() != 'u')
				inputStream.SetG(pos);
			else
				hasSurrogate = true;
		}
		
		if (!hasSurrogate)
			break;
	}

	if (n == 2)
		unicode = 0x10000 + ((unicode & 0x3FF) << 10) + (surrogate & 0x3FF);
		
	return unicode;
}

template <typename istream>
inline std::string Reader<istream>::codePointToUTF8(unsigned int cp)
{
   std::string result;
   
   // based on description from http://en.wikipedia.org/wiki/UTF-8

   if (cp <= 0x7f) 
   {
      result.resize(1);
      result[0] = static_cast<char>(cp);
   } 
   else if (cp <= 0x7FF) 
   {
      result.resize(2);
      result[1] = static_cast<char>(0x80 | (0x3f & cp));
      result[0] = static_cast<char>(0xC0 | (0x1f & (cp >> 6)));
   } 
   else if (cp <= 0xFFFF) 
   {
      result.resize(3);
      result[2] = static_cast<char>(0x80 | (0x3f & cp));
      result[1] = 0x80 | static_cast<char>((0x3f & (cp >> 6)));
      result[0] = 0xE0 | static_cast<char>((0xf & (cp >> 12)));
   }
   else if (cp <= 0x10FFFF) 
   {
      result.resize(4);
      result[3] = static_cast<char>(0x80 | (0x3f & cp));
      result[2] = static_cast<char>(0x80 | (0x3f & (cp >> 6)));
      result[1] = static_cast<char>(0x80 | (0x3f & (cp >> 12)));
      result[0] = static_cast<char>(0xF0 | (0x7 & (cp >> 18)));
   }

   return result;
}

template <typename istream>
inline const std::string &Reader<istream>::MatchString(InputStream& inputStream)
{
   MatchExpectedString(inputStream, "\"");

   buffer.resize(0);
   std::string &string = buffer;
   
   while (inputStream.EOS() == false &&
          inputStream.Peek() != '"')
   {
      char c = inputStream.Get();

      // escape?
      if (c == '\\' &&
          inputStream.EOS() == false) // shouldn't have reached the end yet
      {
         c = inputStream.Get();
         switch (c) {
            case '/':      string.push_back('/');     break;
            case '"':      string.push_back('"');     break;
            case '\\':     string.push_back('\\');    break;
            case 'b':      string.push_back('\b');    break;
            case 'f':      string.push_back('\f');    break;
            case 'n':      string.push_back('\n');    break;
            case 'r':      string.push_back('\r');    break;
            case 't':      string.push_back('\t');    break;
            case 'u': 
			{
				string += codePointToUTF8(decodeUnicodeCodePoint(inputStream));
			}
			break;
            default: {
               std::string sMessage = std::string("Unrecognized escape sequence found in string: \\") + c;
               throw ScanException(sMessage, inputStream.GetLocation());
            }
         }
      }
      else {
         string.push_back(c);
      }
   }

   // eat the last '"' that we just peeked
   MatchExpectedString(inputStream, "\"");

   // all's well if we made it here
   return buffer;
}


template <typename istream>
inline Number::ValueType Reader<istream>::MatchNumber(InputStream& inputStream)
{
	const int MAX_BUF=255;
	char buffer[MAX_BUF+1];
	char p = 0;
	
   while (p<MAX_BUF && inputStream.EOS() == false &&
          numericChars.find(inputStream.Peek()) != numericChars.end())
   {
	buffer[p++] = inputStream.Get();
   }
   buffer[p] = 0;
   
#ifdef JSON_USE_STRING_AS_NUMBER
	return json::NumberString(buffer);
#else
   int base=0;
   char *end;
   return strtol(buffer,&end, base);
#endif
}

template <typename istream>
inline void Reader<istream>::Parse(UnknownElement& element, Reader::TokenStream& tokenStream) 
{
   const Token& token = tokenStream.Peek();
   switch (token.nType) {
      case Token::TOKEN_OBJECT_BEGIN:
      {
         // implicit non-const cast will perform conversion for us (if necessary)
         Object& object = element;
         Parse(object, tokenStream);
         break;
      }

      case Token::TOKEN_ARRAY_BEGIN:
      {
         Array& array = element;
         Parse(array, tokenStream);
         break;
      }

      case Token::TOKEN_STRING:
      {
         String& string = element;
         Parse(string, tokenStream);
         break;
      }

      case Token::TOKEN_NUMBER:
      {
         Number& number = element;
         Parse(number, tokenStream);
         break;
      }

      case Token::TOKEN_BOOLEAN:
      {
         Boolean& boolean = element;
         Parse(boolean, tokenStream);
         break;
      }

      case Token::TOKEN_NULL:
      {
         Null& null = element;
         Parse(null, tokenStream);
         break;
      }

      default:
      {
         std::string sMessage = std::string("Unexpected token: ") + token.sValue;
         throw ParseException(sMessage, token.locBegin, token.locEnd);
      }
   }
}


template <typename istream>
inline void Reader<istream>::Parse(Object& object, Reader::TokenStream& tokenStream)
{
   MatchExpectedToken(Token::TOKEN_OBJECT_BEGIN, tokenStream);

   bool bContinue = (tokenStream.EOS() == false &&
                     tokenStream.Peek().nType != Token::TOKEN_OBJECT_END);
   while (bContinue)
   {
      Object::Member member;

      // first the member name. save the token in case we have to throw an exception
      const Token& tokenName = tokenStream.Peek();
      member.name = MatchExpectedToken(Token::TOKEN_STRING, tokenStream);

      // ...then the key/value separator...
      MatchExpectedToken(Token::TOKEN_MEMBER_ASSIGN, tokenStream);

      // ...then the value itself (can be anything).
      Parse(member.element, tokenStream);

      // try adding it to the object (this could throw)
      try
      {
         object.Insert(member);
      }
      catch (Exception&)
      {
         // must be a duplicate name
         std::string sMessage = std::string("Duplicate object member token: ") + member.name; 
         throw ParseException(sMessage, tokenName.locBegin, tokenName.locEnd);
      }

      bContinue = (tokenStream.EOS() == false &&
                   tokenStream.Peek().nType == Token::TOKEN_NEXT_ELEMENT);
      if (bContinue)
         MatchExpectedToken(Token::TOKEN_NEXT_ELEMENT, tokenStream);
   }

   MatchExpectedToken(Token::TOKEN_OBJECT_END, tokenStream);
}

template <typename istream>
inline void Reader<istream>::Parse(Array& array, Reader::TokenStream& tokenStream)
{
   MatchExpectedToken(Token::TOKEN_ARRAY_BEGIN, tokenStream);

   bool bContinue = (tokenStream.EOS() == false &&
                     tokenStream.Peek().nType != Token::TOKEN_ARRAY_END);
   while (bContinue)
   {
      // ...what's next? could be anything
      Array::iterator itElement = array.Insert(UnknownElement());
      UnknownElement& element = *itElement;
      Parse(element, tokenStream);

      bContinue = (tokenStream.EOS() == false &&
                   tokenStream.Peek().nType == Token::TOKEN_NEXT_ELEMENT);
      if (bContinue)
         MatchExpectedToken(Token::TOKEN_NEXT_ELEMENT, tokenStream);
   }

   MatchExpectedToken(Token::TOKEN_ARRAY_END, tokenStream);
}

template <typename istream>
inline void Reader<istream>::Parse(String& string, Reader::TokenStream& tokenStream)
{
   string = MatchExpectedToken(Token::TOKEN_STRING, tokenStream);
}


template <typename istream>
inline void Reader<istream>::Parse(Number& number, Reader::TokenStream& tokenStream)
{
/*
#ifdef JSON_USE_STRING_AS_NUMBER
   const std::string& sValue = MatchExpectedToken(Token::TOKEN_NUMBER, tokenStream);
	
//   std::istringstream iStr(sValue);
   Number::ValueType dValue;
//   iStr >> dValue;

	int base=0;
	char *end;
	const char *s = sValue.c_str();
	dValue = strtol(s, &end, base);

   // did we consume all characters in the token?
   if (*end != '\0')
//   if (iStr.eof() == false)
   {
 //     char c = iStr.peek();
	char c = *end;
      std::string sMessage = std::string("Unexpected character in NUMBER token: ") + c;
      throw ParseException(sMessage, currentToken.locBegin, currentToken.locEnd);
   }

   number = dValue;
#else
*/
   const Token& currentToken = tokenStream.Get(); // might need this later for throwing exception
	number = currentToken.nValue;
//#endif
}


template <typename istream>
inline void Reader<istream>::Parse(Boolean& boolean, Reader::TokenStream& tokenStream)
{
   const std::string& sValue = MatchExpectedToken(Token::TOKEN_BOOLEAN, tokenStream);
   boolean = (sValue == "true" ? true : false);
}


template <typename istream>
inline void Reader<istream>::Parse(Null&, Reader::TokenStream& tokenStream)
{
   MatchExpectedToken(Token::TOKEN_NULL, tokenStream);
}


} // End namespace






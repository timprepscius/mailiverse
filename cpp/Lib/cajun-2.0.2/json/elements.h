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

#include <deque>
#include <list>
#include <string>
#include <stdexcept>
#include <sstream>

/*  

TODO:
* better documentation (doxygen?)
* Unicode support
* parent element accessors

*/

namespace json
{

namespace Version
{
   enum { MAJOR = 2 };
   enum { MINOR = 0 };
   enum {ENGINEERING = 2 };
}

/////////////////////////////////////////////////
// forward declarations (more info further below)


class Visitor;
class ConstVisitor;

template <typename ValueTypeT>
class TrivialType_T;

#define JSON_USE_STRING_AS_NUMBER

#ifdef JSON_USE_STRING_AS_NUMBER
	struct NumberString : public std::string 
	{
		typedef std::string Super;

		template<typename T>
		NumberString(const T &v)
		{
			std::ostringstream ss;
			ss << v;
			Super::operator=(std::string(ss.str()));
		}
		
		NumberString(const std::string &s) : Super(s) {}

		NumberString () {}
		
		template<typename T>
		operator T() const {
			std::istringstream ss(*this);
			T v;
			ss >> v;
			return v;
		};
	};

	typedef TrivialType_T<NumberString> Number;
#else
	typedef TrivialType_T<long> Number;
#endif

typedef TrivialType_T<bool> Boolean;
typedef TrivialType_T<std::string> String;

class Object;
class Array;
class Null;



/////////////////////////////////////////////////////////////////////////
// Exception - base class for all JSON-related runtime errors

class Exception : public std::runtime_error
{
public:
   Exception(const std::string& sMessage);
};




/////////////////////////////////////////////////////////////////////////
// UnknownElement - provides a typesafe surrogate for any of the JSON-
//  sanctioned element types. This class allows the Array and Object
//  class to effectively contain a heterogeneous set of child elements.
// The cast operators provide convenient implicit downcasting, while
//  preserving dynamic type safety by throwing an exception during a
//  a bad cast. 
// The object & array element index operators (operators [std::string]
//  and [size_t]) provide convenient, quick access to child elements.
//  They are a logical extension of the cast operators. These child
//  element accesses can be chained together, allowing the following
//  (when document structure is well-known):
//  String str = objInvoices[1]["Customer"]["Company"];


class UnknownElement
{
public:
   UnknownElement();
   UnknownElement(const UnknownElement& unknown);
   UnknownElement(const Object& object);
   UnknownElement(const Array& array);
   UnknownElement(const Number& number);
   UnknownElement(const Boolean& boolean);
   UnknownElement(const String& string);
   UnknownElement(const Null& null);

   ~UnknownElement();

   UnknownElement& operator = (const UnknownElement& unknown);

   // implicit cast to actual element type. throws on failure
   operator const Object& () const;
   operator const Array& () const;
   operator const Number& () const;
   operator const Boolean& () const;
   operator const String& () const;
   operator const Null& () const;

   // implicit cast to actual element type. *converts* on failure, and always returns success
   operator Object& ();
   operator Array& ();
   operator Number& ();
   operator Boolean& ();
   operator String& ();
   operator Null& ();

   // provides quick access to children when real element type is object
   UnknownElement& operator[] (const std::string& key);
   const UnknownElement& operator[] (const std::string& key) const;

   // provides quick access to children when real element type is array
   UnknownElement& operator[] (size_t index);
   const UnknownElement& operator[] (size_t index) const;

   // implements visitor pattern
   void Accept(ConstVisitor& visitor) const;
   void Accept(Visitor& visitor);

   // tests equality. first checks type, then value if possible
   bool operator == (const UnknownElement& element) const;

private:
   class Imp;

   template <typename ElementTypeT>
   class Imp_T;

   class CastVisitor;
   class ConstCastVisitor;
   
   template <typename ElementTypeT>
   class CastVisitor_T;

   template <typename ElementTypeT>
   class ConstCastVisitor_T;

   template <typename ElementTypeT>
   const ElementTypeT& CastTo() const;

   template <typename ElementTypeT>
   ElementTypeT& ConvertTo();

   Imp* m_pImp;
};


/////////////////////////////////////////////////////////////////////////////////
// Array - mimics std::deque<UnknownElement>. The array contents are effectively 
//  heterogeneous thanks to the ElementUnknown class. push_back has been replaced 
//  by more generic insert functions.

class Array
{
public:
   typedef std::deque<UnknownElement> Elements;
   typedef Elements::iterator iterator;
   typedef Elements::const_iterator const_iterator;

   iterator Begin();
   iterator End();
   const_iterator Begin() const;
   const_iterator End() const;
   
   iterator Insert(const UnknownElement& element, iterator itWhere);
   iterator Insert(const UnknownElement& element);
   iterator Erase(iterator itWhere);
   void Resize(size_t newSize);
   void Clear();

   size_t Size() const;
   bool Empty() const;

   UnknownElement& operator[] (size_t index);
   const UnknownElement& operator[] (size_t index) const;

   bool operator == (const Array& array) const;

private:
   Elements m_Elements;
};


/////////////////////////////////////////////////////////////////////////////////
// Object - mimics std::map<std::string, UnknownElement>. The member value 
//  contents are effectively heterogeneous thanks to the UnknownElement class

class Object
{
public:
   struct Member {
      Member(const std::string& nameIn = std::string(), const UnknownElement& elementIn = UnknownElement());

      bool operator == (const Member& member) const;

      std::string name;
      UnknownElement element;
   };

   typedef std::list<Member> Members; // map faster, but does not preserve order
   typedef Members::iterator iterator;
   typedef Members::const_iterator const_iterator;

   bool operator == (const Object& object) const;

   iterator Begin();
   iterator End();
   const_iterator Begin() const;
   const_iterator End() const;

   size_t Size() const;
   bool Empty() const;

   iterator Find(const std::string& name);
   const_iterator Find(const std::string& name) const;

   iterator Insert(const Member& member);
   iterator Insert(const Member& member, iterator itWhere);
   iterator Erase(iterator itWhere);
   void Clear();

   UnknownElement& operator [](const std::string& name);
   const UnknownElement& operator [](const std::string& name) const;

private:
   class Finder;

   Members m_Members;
};


/////////////////////////////////////////////////////////////////////////////////
// TrivialType_T - class template for encapsulates a simple data type, such as
//  a string, number, or boolean. Provides implicit const & noncost cast operators
//  for that type, allowing "DataTypeT type = trivialType;"


template <typename DataTypeT>
class TrivialType_T
{
public:
   typedef DataTypeT ValueType;

   TrivialType_T(const DataTypeT& t = DataTypeT());

   operator DataTypeT&();
   operator const DataTypeT&() const;

   template<typename T>
   T as() const
   {
	return m_tValue;
   }

   template<typename T>
   T as()
   {
	return m_tValue;
   }
   
   DataTypeT& Value();
   const DataTypeT& Value() const;

   bool operator == (const TrivialType_T<DataTypeT>& trivial) const;

private:
   DataTypeT m_tValue;
};



/////////////////////////////////////////////////////////////////////////////////
// Null - doesn't do much of anything but satisfy the JSON spec. It is the default
//  element type of UnknownElement

class Null
{
public:
   bool operator == (const Null& trivial) const;
};


} // End namespace


//#include "elements.inl"


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

#include "visitor.h"
#include "reader.h"
#include <cassert>
#include <algorithm>
#include <map>

/*  

TODO:
* better documentation

*/

namespace json
{


inline Exception::Exception(const std::string& sMessage) :
   std::runtime_error(sMessage) {}


/////////////////////////
// UnknownElement members

class UnknownElement::Imp
{
public:
   virtual ~Imp() {}
   virtual Imp* Clone() const = 0;

   virtual bool Compare(const Imp& imp) const = 0;

   virtual void Accept(ConstVisitor& visitor) const = 0;
   virtual void Accept(Visitor& visitor) = 0;
};


template <typename ElementTypeT>
class UnknownElement::Imp_T : public UnknownElement::Imp
{
public:
   Imp_T(const ElementTypeT& element) : m_Element(element) {}
   virtual Imp* Clone() const { return new Imp_T<ElementTypeT>(*this); }

   virtual void Accept(ConstVisitor& visitor) const { visitor.Visit(m_Element); }
   virtual void Accept(Visitor& visitor) { visitor.Visit(m_Element); }

   virtual bool Compare(const Imp& imp) const
   {
      ConstCastVisitor_T<ElementTypeT> castVisitor;
      imp.Accept(castVisitor);
      return castVisitor.m_pElement &&
             m_Element == *castVisitor.m_pElement;
   }

private:
   ElementTypeT m_Element;
};


class UnknownElement::ConstCastVisitor : public ConstVisitor
{
   virtual void Visit(const Array& array) {}
   virtual void Visit(const Object& object) {}
   virtual void Visit(const Number& number) {}
   virtual void Visit(const String& string) {}
   virtual void Visit(const Boolean& boolean) {}
   virtual void Visit(const Null& null) {}
};

template <typename ElementTypeT>
class UnknownElement::ConstCastVisitor_T : public ConstCastVisitor
{
public:
   ConstCastVisitor_T() : m_pElement(0) {}
   virtual void Visit(const ElementTypeT& element) { m_pElement = &element; } // we don't know what this is, but it overrides one of the base's no-op functions
   const ElementTypeT* m_pElement;
};


class UnknownElement::CastVisitor : public Visitor
{
   virtual void Visit(Array& array) {}
   virtual void Visit(Object& object) {}
   virtual void Visit(Number& number) {}
   virtual void Visit(String& string) {}
   virtual void Visit(Boolean& boolean) {}
   virtual void Visit(Null& null) {}
};

template <typename ElementTypeT>
class UnknownElement::CastVisitor_T : public CastVisitor
{
public:
   CastVisitor_T() : m_pElement(0) {}
   virtual void Visit(ElementTypeT& element) { m_pElement = &element; } // we don't know what this is, but it overrides one of the base's no-op functions
   ElementTypeT* m_pElement;
};




inline UnknownElement::UnknownElement() :                               m_pImp( new Imp_T<Null>( Null() ) ) {}
inline UnknownElement::UnknownElement(const UnknownElement& unknown) :  m_pImp( unknown.m_pImp->Clone()) {}
inline UnknownElement::UnknownElement(const Object& object) :           m_pImp( new Imp_T<Object>(object) ) {}
inline UnknownElement::UnknownElement(const Array& array) :             m_pImp( new Imp_T<Array>(array) ) {}
inline UnknownElement::UnknownElement(const Number& number) :           m_pImp( new Imp_T<Number>(number) ) {}
inline UnknownElement::UnknownElement(const Boolean& boolean) :         m_pImp( new Imp_T<Boolean>(boolean) ) {}
inline UnknownElement::UnknownElement(const String& string) :           m_pImp( new Imp_T<String>(string) ) {}
inline UnknownElement::UnknownElement(const Null& null) :               m_pImp( new Imp_T<Null>(null) ) {}

inline UnknownElement::~UnknownElement()   { delete m_pImp; }

inline UnknownElement::operator const Object& () const    { return CastTo<Object>(); }
inline UnknownElement::operator const Array& () const     { return CastTo<Array>(); }
inline UnknownElement::operator const Number& () const    { return CastTo<Number>(); }
inline UnknownElement::operator const Boolean& () const   { return CastTo<Boolean>(); }
inline UnknownElement::operator const String& () const    { return CastTo<String>(); }
inline UnknownElement::operator const Null& () const      { return CastTo<Null>(); }

inline UnknownElement::operator Object& ()    { return ConvertTo<Object>(); }
inline UnknownElement::operator Array& ()     { return ConvertTo<Array>(); }
inline UnknownElement::operator Number& ()    { return ConvertTo<Number>(); }
inline UnknownElement::operator Boolean& ()   { return ConvertTo<Boolean>(); }
inline UnknownElement::operator String& ()    { return ConvertTo<String>(); }
inline UnknownElement::operator Null& ()      { return ConvertTo<Null>(); }

inline UnknownElement& UnknownElement::operator = (const UnknownElement& unknown) 
{
   // always check for this
   if (&unknown != this)
   {
      // we might be copying from a subtree of ourselves. delete the old imp
      //  only after the clone operation is complete. yes, this could be made 
      //  more efficient, but isn't worth the complexity
      Imp* pOldImp = m_pImp;
      m_pImp = unknown.m_pImp->Clone();
      delete pOldImp;
   }

   return *this;
}

inline UnknownElement& UnknownElement::operator[] (const std::string& key)
{
   // the people want an object. make us one if we aren't already
   Object& object = ConvertTo<Object>();
   return object[key];
}

inline const UnknownElement& UnknownElement::operator[] (const std::string& key) const
{
   // throws if we aren't an object
   const Object& object = CastTo<Object>();
   return object[key];
}

inline UnknownElement& UnknownElement::operator[] (size_t index)
{
   // the people want an array. make us one if we aren't already
   Array& array = ConvertTo<Array>();
   return array[index];
}

inline const UnknownElement& UnknownElement::operator[] (size_t index) const
{
   // throws if we aren't an array
   const Array& array = CastTo<Array>();
   return array[index];
}


template <typename ElementTypeT>
const ElementTypeT& UnknownElement::CastTo() const
{
   ConstCastVisitor_T<ElementTypeT> castVisitor;
   m_pImp->Accept(castVisitor);
   if (castVisitor.m_pElement == 0)
      throw Exception("Bad cast");
   return *castVisitor.m_pElement;
}



template <typename ElementTypeT>
ElementTypeT& UnknownElement::ConvertTo() 
{
   CastVisitor_T<ElementTypeT> castVisitor;
   m_pImp->Accept(castVisitor);
   if (castVisitor.m_pElement == 0)
   {
      // we're not the right type. fix it & try again
      *this = ElementTypeT();
      m_pImp->Accept(castVisitor);
   }

   return *castVisitor.m_pElement;
}


inline void UnknownElement::Accept(ConstVisitor& visitor) const { m_pImp->Accept(visitor); }
inline void UnknownElement::Accept(Visitor& visitor)            { m_pImp->Accept(visitor); }


inline bool UnknownElement::operator == (const UnknownElement& element) const
{
   return m_pImp->Compare(*element.m_pImp);
}



//////////////////
// Object members


inline Object::Member::Member(const std::string& nameIn, const UnknownElement& elementIn) :
   name(nameIn), element(elementIn) {}

inline bool Object::Member::operator == (const Member& member) const 
{
   return name == member.name &&
          element == member.element;
}

class Object::Finder : public std::unary_function<Object::Member, bool>
{
public:
   Finder(const std::string& name) : m_name(name) {}
   bool operator () (const Object::Member& member) {
      return member.name == m_name;
   }

private:
   std::string m_name;
};



inline Object::iterator Object::Begin() { return m_Members.begin(); }
inline Object::iterator Object::End() { return m_Members.end(); }
inline Object::const_iterator Object::Begin() const { return m_Members.begin(); }
inline Object::const_iterator Object::End() const { return m_Members.end(); }

inline size_t Object::Size() const { return m_Members.size(); }
inline bool Object::Empty() const { return m_Members.empty(); }

inline Object::iterator Object::Find(const std::string& name) 
{
   return std::find_if(m_Members.begin(), m_Members.end(), Finder(name));
}

inline Object::const_iterator Object::Find(const std::string& name) const 
{
   return std::find_if(m_Members.begin(), m_Members.end(), Finder(name));
}

inline Object::iterator Object::Insert(const Member& member)
{
   return Insert(member, End());
}

inline Object::iterator Object::Insert(const Member& member, iterator itWhere)
{
   iterator it = Find(member.name);
   if (it != m_Members.end())
      throw Exception(std::string("Object member already exists: ") + member.name);

   it = m_Members.insert(itWhere, member);
   return it;
}

inline Object::iterator Object::Erase(iterator itWhere) 
{
   return m_Members.erase(itWhere);
}

inline UnknownElement& Object::operator [](const std::string& name)
{

   iterator it = Find(name);
   if (it == m_Members.end())
   {
      Member member(name);
      it = Insert(member, End());
   }
   return it->element;      
}

inline const UnknownElement& Object::operator [](const std::string& name) const 
{
   const_iterator it = Find(name);
   if (it == End())
      throw Exception(std::string("Object member not found: ") + name);
   return it->element;
}

inline void Object::Clear() 
{
   m_Members.clear(); 
}

inline bool Object::operator == (const Object& object) const 
{
   return m_Members == object.m_Members;
}


/////////////////
// Array members

inline Array::iterator Array::Begin()  { return m_Elements.begin(); }
inline Array::iterator Array::End()    { return m_Elements.end(); }
inline Array::const_iterator Array::Begin() const  { return m_Elements.begin(); }
inline Array::const_iterator Array::End() const    { return m_Elements.end(); }

inline Array::iterator Array::Insert(const UnknownElement& element, iterator itWhere)
{ 
   return m_Elements.insert(itWhere, element);
}

inline Array::iterator Array::Insert(const UnknownElement& element)
{
   return Insert(element, End());
}

inline Array::iterator Array::Erase(iterator itWhere)
{ 
   return m_Elements.erase(itWhere);
}

inline void Array::Resize(size_t newSize)
{
   m_Elements.resize(newSize);
}

inline size_t Array::Size() const  { return m_Elements.size(); }
inline bool Array::Empty() const   { return m_Elements.empty(); }

inline UnknownElement& Array::operator[] (size_t index)
{
   size_t nMinSize = index + 1; // zero indexed
   if (m_Elements.size() < nMinSize)
      m_Elements.resize(nMinSize);
   return m_Elements[index]; 
}

inline const UnknownElement& Array::operator[] (size_t index) const 
{
   if (index >= m_Elements.size())
      throw Exception("Array out of bounds");
   return m_Elements[index]; 
}

inline void Array::Clear() {
   m_Elements.clear();
}

inline bool Array::operator == (const Array& array) const
{
   return m_Elements == array.m_Elements;
}


////////////////////////
// TrivialType_T members

template <typename DataTypeT>
TrivialType_T<DataTypeT>::TrivialType_T(const DataTypeT& t) :
   m_tValue(t) {}

template <typename DataTypeT>
TrivialType_T<DataTypeT>::operator DataTypeT&()
{
  return Value(); 
}

template <typename DataTypeT>
TrivialType_T<DataTypeT>::operator const DataTypeT&() const
{
   return Value(); 
}

template <typename DataTypeT>
DataTypeT& TrivialType_T<DataTypeT>::Value()
{
   return m_tValue; 
}

template <typename DataTypeT>
const DataTypeT& TrivialType_T<DataTypeT>::Value() const
{
   return m_tValue; 
}

template <typename DataTypeT>
bool TrivialType_T<DataTypeT>::operator == (const TrivialType_T<DataTypeT>& trivial) const
{
   return m_tValue == trivial.m_tValue;
}



//////////////////
// Null members

inline bool Null::operator == (const Null& trivial) const
{
   return true;
}



} // End namespace











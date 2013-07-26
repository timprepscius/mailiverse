/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef CACHEVALUE_H_
#define CACHEVALUE_H_

#include "mailiverse/utilities/SmartPtr.h"
#include "mailiverse/core/Block.h"

namespace mailiverse {
namespace mail {
namespace cache {

class Value
{
public:
	static const Value DELETED;
	static const Value EMPTY;

protected:
	core::Block value;

public:
	Value (const std::string &rhs) :
		value(rhs)
	{
	}

	Value (const Value &rhs) :
		value(rhs.value)
	{ }

	Value () {}

	Value(const core::Block &_value) :
		value(_value) {}

	virtual ~Value() {}

	int size () const
	{
		return value.size();
	}

//	std::string str () const
//	{
//		return std::string(value.begin(), value.end());
//	}

	const core::Block::Atom *data() const
	{
		return value.data();
	}

	const core::Block &block () const
	{
		return value;
	}

	bool operator ==(const Value &rhs)
	{
		return value == rhs.value;
	}

	bool operator !=(const Value &rhs)
	{
		return value != rhs.value;
	}

};

} /* namespace cache */
} /* namespace mail */
} /* namespace mailiverse */
#endif /* CACHEVALUE_H_ */

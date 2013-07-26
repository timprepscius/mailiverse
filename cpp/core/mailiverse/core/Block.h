/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef BLOCK_H_
#define BLOCK_H_

#include <vector>
#include "mailiverse/utilities/Types.h"
#include "mailiverse/utilities/Strings.h"
#include "mailiverse/utilities/SmartPtr.h"

namespace mailiverse {
namespace core {

class Block : public std::vector<u8>
{
public:
	typedef u8 Atom;

protected:
	typedef std::vector<Atom> Super;

public:
	Block () {}

	Block(const std::vector<Atom> &rhs) :
		Super(rhs) {}

	Block(const Atom *begin, const Atom *end) :
		Super(begin,end) {}
		
	Block(const Atom *begin, int size) :
		Super(begin, begin+size) {}

	Block(int size) :
		Super(size) {}

	Block(const std::string &rhs) :
		Super(rhs.size())
	{
		assign(rhs.begin(), rhs.end());
	}

	Block(int size, int fill)
	{
		assign(size, fill);
	}
};

DECLARE_SMARTPTR(Block);

} /* namespace core */

namespace utilities {

template<>
inline std::string toString (const std::vector<u8> &t)
{
	std::ostringstream oss;
	oss << std::string((char *)t.data(), t.size());
	return oss.str();
}

template<>
inline std::string toString (const core::Block &t)
{
	std::ostringstream oss;
	oss << std::string((char *)t.data(), t.size());
	return oss.str();
}

} // namespace

} /* namespace mailiverse */

#endif /* BLOCK_H_ */

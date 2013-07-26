/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __Utilities_Exception_h__
#define __Utilities_Exception_h__

#include <string>
#include <iostream>

#define DECLARE_EXCEPTION_BASE(e,k) \
	struct e : k \
	{ \
		typedef k Super; \
		e(const std::string &what) : Super(what) { } \
		e() { } \
	} ;\

#define DECLARE_EXCEPTION(e) DECLARE_EXCEPTION_BASE(e, mailiverse::Exception)

namespace mailiverse {

class Exception
{
	private:
		const std::string _what;

	public:
		Exception (const std::string &what) : _what(what) { }
		Exception () { }
		virtual ~Exception () { }

		std::string what() const { return _what; }
} ;

DECLARE_EXCEPTION (NullPointerException);

} // namespace Utilities

inline std::ostream &operator <<(std::ostream &out, mailiverse::Exception &e)
{
	return out << "Exception: " << e.what();
}

#endif


/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_crypt_CryptoException_h__
#define __mailiverse_core_crypt_CryptoException_h__

#include "mailiverse/Exception.h"

namespace mailiverse {
namespace core {
namespace crypt {

class CryptoException : public Exception
{
public:
	typedef Exception Super;

public:
	CryptoException (const std::string &what) : Super(what) {}
	CryptoException () : Super(std::string()) {}
} ;

} // namespace
} // namespace
} // namespace

#endif
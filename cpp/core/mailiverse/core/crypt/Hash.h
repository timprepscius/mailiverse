/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_crypt_Hash_h__
#define __mailiverse_core_crypt_Hash_h__

#include "mailiverse/core/Types.h"
#include "CryptoException.h"
#include <iostream>

namespace mailiverse {
namespace core {
namespace crypt {

class Hash
{
public:
	Hash ();
	virtual ~Hash ();
	
	virtual Block generate (const Block  &) const = 0;
} ;

class HashSha256
{
public:
	HashSha256 ();
	
	Block generate (const Block  &) const;
} ;

class HashMd5
{
public:
	HashMd5 ();
	
	Block generate (const Block  &) const;
} ;

} // namespace 
} // namespace 
} // namespace 

#endif

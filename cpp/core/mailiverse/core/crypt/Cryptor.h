/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_crypt_Cryptor_h__
#define __mailiverse_core_crypt_Cryptor_h__

#include "mailiverse/core/Types.h"
#include "mailiverse/utilities/SmartPtr.h"
#include "CryptoException.h"
#include <iostream>

namespace mailiverse {
namespace core {
namespace crypt {

class Cryptor 
{
public:
	virtual ~Cryptor () {}

	virtual Block encrypt (const Block::Atom *begin, const Block::Atom *end) = 0;
	virtual Block decrypt (const Block::Atom *begin, const Block::Atom *end) = 0;
	
	Block encrypt (const Block &b)
	{
		return encrypt(b.data(), b.data()+b.size());
	}

	Block decrypt (const Block &b)
	{
		return decrypt(b.data(), b.data()+b.size());
	}
	
	Block decrypt (std::istream &is)
	{
		Block block = toBlock(is);
		return decrypt(block);
	}
	
	Block encrypt (std::istream &is)
	{
		Block block = toBlock(is);
		return encrypt(block);
	
	}
} ;

DECLARE_SMARTPTR(Cryptor);

} // namespace crypt
} // namespace core
} // namespace mailiverse

#endif

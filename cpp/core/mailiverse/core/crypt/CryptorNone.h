/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_crypt_CryptorNone_h__
#define __mailiverse_core_crypt_CryptorNone_h__

#include "Cryptor.h"

namespace mailiverse {
namespace core {
namespace crypt {

class CryptorNone : public Cryptor
{
protected:
	Key key;
	IV iv;

public:
	CryptorNone (const Key &key, const IV &iv)
	{
		this->key = key;
		this->iv = iv;
	}
	
	virtual Block encrypt (const Block::Atom *begin, const Block::Atom *end);
	virtual Block decrypt (const Block::Atom *begin, const Block::Atom *end);
} ;

} // namespace
} // namespace
} // namespace

#endif

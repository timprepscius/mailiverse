/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_crypt_CryptorAES_h__
#define __mailiverse_core_crypt_CryptorAES_h__

#include "Cryptor.h"

namespace mailiverse {
namespace core {
namespace crypt {

class CryptorAES : public Cryptor
{
public:
	static const int IV_SIZE_IN_BYTES, KEY_SIZE_IN_BYTES;
	static const IV NullIV;

protected:	
	Key key;
	
	IV newIV();

public:
	CryptorAES (const Key &_key);
	virtual ~CryptorAES ();
	
	virtual Block encrypt (const Block::Atom *begin, const Block::Atom *end);
	virtual Block decrypt (const Block::Atom *begin, const Block::Atom *end);
	
	static Key newKey();
} ;

} // namespace
} // namespace
} // namespace

#endif

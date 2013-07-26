/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_crypt_CryptorAESIV_h__
#define __mailiverse_core_crypt_CryptorAESIV_h__

#include "Cryptor.h"

namespace mailiverse {
namespace core {
namespace crypt {

class CryptorAESIV : public Cryptor
{
protected:
	void *encryptor, *decryptor;
	
public:
	CryptorAESIV (const Key &_key, const IV &_iv);
	virtual ~CryptorAESIV ();
	
	virtual Block encrypt (const Block::Atom *begin, const Block::Atom *end);
	virtual Block decrypt (const Block::Atom *begin, const Block::Atom *end);
} ;

} // namespace
} // namespace
} // namespace

#endif

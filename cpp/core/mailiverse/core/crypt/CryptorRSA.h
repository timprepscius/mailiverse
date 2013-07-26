/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_crypt_CryptorRSA_h__
#define __mailiverse_core_crypt_CryptorRSA_h__

#include "Cryptor.h"

namespace mailiverse {
namespace core {
namespace crypt {

class CryptorRSA : public Cryptor
{
protected:
	Key privateKey, publicKey;
	void *privateKeyImpl, *publicKeyImpl;

public:
	CryptorRSA (const Key &publicKey, const Key &privateKey);
	virtual ~CryptorRSA ();
	
	virtual Block encrypt (const Block::Atom *begin, const Block::Atom *end);
	virtual Block decrypt (const Block::Atom *begin, const Block::Atom *end);
	
	const Key &getPrivateKey() const;
	const Key &getPublicKey() const;
} ;

DECLARE_SMARTPTR(CryptorRSA);

} // namespace
} // namespace
} // namespace

#endif

/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_crypt_KeyPairFromPassword_h___
#define __mailiverse_core_crypt_KeyPairFromPassword_h___

#include "PBE.h"

namespace mailiverse {
namespace core {
namespace crypt {

class KeyPairFromPassword : public Cryptor
{
protected:
	PBEPtr verifier, cryptor;
	
public:
	KeyPairFromPassword (const std::string &password, bool createCryptor=false);
	virtual ~KeyPairFromPassword ();
	
	PBE &getVerifier ()
	{
		return *verifier;
	}
	
	PBE &getCryptor ()
	{
		return *cryptor;
	}
	
	virtual Block encrypt (const Block::Atom *begin, const Block::Atom *end)
	{
		return getCryptor().encrypt(begin, end);
	}
	
	virtual Block decrypt (const Block::Atom *begin, const Block::Atom *end)
	{
		return getCryptor().decrypt(begin, end);
	}
	
} ;

DECLARE_SMARTPTR(KeyPairFromPassword);

} // namespace
} // namespace
} // namespcae

#endif
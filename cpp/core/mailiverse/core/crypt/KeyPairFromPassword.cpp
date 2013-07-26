/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "KeyPairFromPassword.h"
#include "mailiverse/utilities/Log.h"

using namespace mailiverse::core::crypt;

KeyPairFromPassword::KeyPairFromPassword (const std::string &password, bool createCryptor) :
	verifier(0),
	cryptor(0)
{
	LogDebug(mailiverse::core::crypt, "KeyPairFromPassword");
	
	verifier = new PBE(password, PBE::DEFAULT_SALT_VERIFIER, PBE::DEFAULT_ITERATIONS, PBE::DEFAULT_KEYLENGTH);

	if (createCryptor)
		cryptor  = new PBE(password, PBE::DEFAULT_SALT_CRYPTOR, PBE::DEFAULT_ITERATIONS, PBE::DEFAULT_KEYLENGTH);
}
	
KeyPairFromPassword::~KeyPairFromPassword ()
{
	LogDebug(mailiverse::core::crypt, "~KeyPairFromPassword");
}

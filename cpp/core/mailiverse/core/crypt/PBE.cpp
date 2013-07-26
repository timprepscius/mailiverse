/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "PBE.h"
#include "CryptorAES.h"
#include "mailiverse/utilities/Log.h"

#include <botan/botan.h>
#include <botan/pbkdf2.h>
#include <botan/sha2_32.h>

using namespace mailiverse::core::crypt;
using namespace mailiverse::core;
using namespace mailiverse::utilities;
using namespace mailiverse;

const Byte DEFAULT_SALT_CRYPTOR_INITIALIZER[] = { 
	0xc8, 0x73, 0x41, 0x8c,
	0x7e, 0xd8, 0xee, 0x89
} ;

const Salt PBE::DEFAULT_SALT_CRYPTOR = toBlockFromInitializer (DEFAULT_SALT_CRYPTOR_INITIALIZER);

const Byte DEFAULT_SALT_VERIFIER_INITIALIZER[] = {
	0x12, 0x53, 0x14, 0xbb,
	0x7e, 0x97, 0xce, 0x55
} ;

const Salt PBE::DEFAULT_SALT_VERIFIER = toBlockFromInitializer (DEFAULT_SALT_VERIFIER_INITIALIZER);


PBE::PBE (const std::string &password, const Salt &salt, int iterationCount, int keyLength)
{
	Botan::Algorithm_Factory& af = Botan::global_state().algorithm_factory();
	Botan::MessageAuthenticationCode *prf = af.make_mac("HMAC(SHA-256)");
	Botan::PKCS5_PBKDF2 kdf(prf);
	
	Botan::OctetString key = 
		kdf.derive_key(keyLength/8, password, salt.data(), salt.size(), iterationCount);
	
	secretKey.assign(key.begin(), key.end());
	cryptor = new CryptorAES(secretKey);
	
	LogDebug(mailiverse::core::crypt::PBE, "derived: " << toString(toBlockBase64(secretKey)) << " using " << toString(toBlockBase64(salt)) << " password " << password);
}

PBE::~PBE ()
{
}

Block PBE::encrypt(const Block::Atom *begin, const Block::Atom *end)
{
	return cryptor->encrypt(begin, end);
}

Block PBE::decrypt(const Block::Atom *begin, const Block::Atom *end)
{
	return cryptor->decrypt(begin, end);
}

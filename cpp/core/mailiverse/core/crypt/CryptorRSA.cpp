/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "CryptorRSA.h"
#include "CryptorAES.h"
#include "../streams/IO.h"
#include "mailiverse/utilities/Log.h"

#include <botan/botan.h>
#include <sstream>
#include <botan/rsa.h>
#include "../BlockIO.h"
#include <botan/pkcs8.h>
#include "botan/rsa.h"
#include "botan/pubkey.h"
#include "botan/look_pk.h"
#include "mailiverse/utilities/Log.h"
#include "mailiverse/utilities/Streams.h"


using namespace mailiverse::core::crypt;
using namespace mailiverse::core::streams;
using namespace mailiverse::core;
using namespace mailiverse::utilities;
using namespace mailiverse;

using namespace Botan;

CryptorRSA::CryptorRSA (const Key &_publicKey, const Key &_privateKey) :
	privateKey(_privateKey),
	publicKey(_publicKey),
	privateKeyImpl(0),
	publicKeyImpl(0)
{
	LogDebug(mailiverse::core::crypt, "CryptorRSA");

	if (!publicKey.empty())
	{
		Key key = toBlockFromBase64(publicKey);
		Botan::DataSource_Memory dataSource((const unsigned char *)key.data(), (Botan::u32bit)key.size());
		Botan::X509_PublicKey *genericPublicKey = Botan::X509::load_key(dataSource);

		Botan::RSA_PublicKey *rsaPublicKey = 
			dynamic_cast<Botan::RSA_PublicKey *> (
				genericPublicKey
			);
	
		publicKeyImpl = rsaPublicKey;
	}

	if (!privateKey.empty())
	{
		AutoSeeded_RNG rng;
		
		std::string key = 
			"-----BEGIN PRIVATE KEY-----\n" +
			toString(privateKey) +
			"-----END PRIVATE KEY-----";

		Botan::DataSource_Memory dataSource((const unsigned char *)key.data(), (Botan::u32bit)key.size());
		Botan::PKCS8_PrivateKey *genericPrivateKey = Botan::PKCS8::load_key(dataSource, rng);

		Botan::RSA_PrivateKey *rsaPrivateKey = 
			dynamic_cast<Botan::RSA_PrivateKey *> (
				genericPrivateKey
			);
			
		privateKeyImpl = rsaPrivateKey;
	}
}

CryptorRSA::~CryptorRSA()
{
	LogDebug(mailiverse::core::crypt, "~CryptorRSA");
	
	delete (Botan::RSA_PrivateKey *)privateKeyImpl;
	delete (Botan::RSA_PublicKey *)publicKeyImpl;
}

Block CryptorRSA::encrypt (const Block::Atom *begin, const Block::Atom *end)
{
	try
	{
	//	std::string paddingFunction = "EME1(SHA-1)"; // no
	//	std::string paddingFunction = "EME1(SHA-256)"; // no
		std::string paddingFunction = "EME-PKCS1-v1_5"; // yes

		AutoSeeded_RNG rng;
		Botan::RSA_PublicKey *rsaPublicKey = (Botan::RSA_PublicKey *)publicKeyImpl;
		std::auto_ptr<Botan::PK_Encryptor> enc(Botan::get_pk_encryptor(*rsaPublicKey, paddingFunction));
		Botan::SecureVector<Botan::byte> encrypted = enc->encrypt ((const unsigned char *)begin, end-begin, rng);	

		Block out;
		out.assign(encrypted.begin(), encrypted.end());
		return out;
	}
	catch (std::exception &e)
	{
		throw CryptoException(e.what());
	}
}

Block CryptorRSA::decrypt (const Block::Atom *begin, const Block::Atom *end)
{
	try
	{
	//	std::string paddingFunction = "EME1(SHA-1)"; // no
	//	std::string paddingFunction = "EME1(SHA-256)"; // no
		std::string paddingFunction = "EME-PKCS1-v1_5"; // yes

		AutoSeeded_RNG rng;
		Botan::RSA_PrivateKey *rsaPrivateKey = (Botan::RSA_PrivateKey *)privateKeyImpl;
		std::auto_ptr<Botan::PK_Decryptor> dec(Botan::get_pk_decryptor(*rsaPrivateKey, paddingFunction));
		Botan::SecureVector<Botan::byte> decrypted = dec->decrypt((const unsigned char *)begin, end-begin);	

		Block out;
		out.assign(decrypted.begin(), decrypted.end());
		return out;
	}
	catch (std::exception &e)
	{
		throw CryptoException(e.what());
	}
}

const Key &CryptorRSA::getPrivateKey () const
{
	return privateKey;
}

const Key &CryptorRSA::getPublicKey () const
{
	return publicKey;
}

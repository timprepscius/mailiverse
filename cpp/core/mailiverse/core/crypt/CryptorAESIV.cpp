/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "CryptorAESIV.h"
#include <botan/botan.h>
#include <sstream>
#include "mailiverse/utilities/Log.h"
#include "CryptorUtil.h"

using namespace mailiverse::core::crypt;
using namespace mailiverse;

using namespace Botan;

CryptorAESIV::CryptorAESIV (const Key &key, const IV &iv) :
	encryptor(0),
	decryptor(0)
{
	LogDebug(mailiverse::core::crypt, "CryptorAES");

	SymmetricKey _key (OctetString((const byte *)key.data(), key.size()));
	InitializationVector _iv (OctetString((const byte *)iv.data(), iv.size()));
	
	int keySize = key.size();
	if (keySize == 16)
	{
		encryptor = new Pipe(get_cipher("AES-128/CBC/PKCS7", _key, _iv, ENCRYPTION));
		decryptor = new Pipe(get_cipher("AES-128/CBC/PKCS7", _key, _iv, DECRYPTION));
	}
	else
	{
		encryptor = new Pipe(get_cipher("AES-256/CBC/PKCS7", _key, _iv, ENCRYPTION));
		decryptor = new Pipe(get_cipher("AES-256/CBC/PKCS7", _key, _iv, DECRYPTION));
	}
}

CryptorAESIV::~CryptorAESIV()
{
	LogDebug(mailiverse::core::crypt, "~CryptorAES");

	delete (Pipe *)encryptor;
	delete (Pipe *)decryptor;
}

Block CryptorAESIV::encrypt (const Block::Atom *begin, const Block::Atom *end)
{
	try 
	{
		Pipe *pipe = (Pipe *)encryptor;
		pipe->process_msg(begin, end-begin);
		return read_all_as_block(pipe);
	}
	catch (std::exception &e)
	{
		throw CryptoException(e.what());
	}
}

Block CryptorAESIV::decrypt (const Block::Atom *begin, const Block::Atom *end)
{
	try 
	{
		Pipe *pipe = (Pipe *)decryptor;
		pipe->process_msg(begin, end-begin);
		return read_all_as_block(pipe);
	}
	catch (std::exception &e)
	{
		throw CryptoException(e.what());
	}
}
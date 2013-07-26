/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "CryptorAES.h"
#include "CryptorAESIV.h"
#include "mailiverse/core/util/Random.h"

using namespace mailiverse::core::crypt;
using namespace mailiverse::core::util;
using namespace mailiverse::core;
using namespace mailiverse;

const IV CryptorAES::NullIV = toBlockFromFilled(16, 0);
const int CryptorAES::IV_SIZE_IN_BYTES = 16;
const int CryptorAES::KEY_SIZE_IN_BYTES = 256/8;

CryptorAES::CryptorAES (const Key &_key) :
	key(_key)
{
	
}

CryptorAES::~CryptorAES ()
{
}

IV CryptorAES::newIV()
{
	Block b;
	
	Random random;
	for (int i=0; i<IV_SIZE_IN_BYTES; i++)
		b.push_back(random.nextByte());
		
	return IV(b);
}

Key CryptorAES::newKey()
{
	Block b;
	
	Random random;
	for (int i=0; i<KEY_SIZE_IN_BYTES; i++)
		b.push_back(random.nextByte());
		
	return Key(b);
}

Block CryptorAES::encrypt (const Block::Atom *begin, const Block::Atom *end)
{
	core::Block iv = newIV();
	core::Block result = iv;

	CryptorAESIV cryptor(key, iv);
	core::Block encrypted = cryptor.encrypt(begin, end);
	
	result.insert(result.end(), encrypted.begin(), encrypted.end());
	return result;
}

Block CryptorAES::decrypt (const Block::Atom *begin, const Block::Atom *end)
{
	core::Block iv(begin, begin+IV_SIZE_IN_BYTES);
	CryptorAESIV cryptor(key, iv);

	return cryptor.decrypt(begin+IV_SIZE_IN_BYTES, end);
}

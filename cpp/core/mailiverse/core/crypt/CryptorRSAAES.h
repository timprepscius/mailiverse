/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_crypt_CryptorRSAAES_h__
#define __mailiverse_core_crypt_CryptorRSAAES_h__

#include "CryptorRSA.h"
#include "mailiverse/utilities/Types.h"
#include "mailiverse/utilities/Log.h"

namespace mailiverse {
namespace core {
namespace crypt {

class CryptorRSAAES : public Cryptor
{
protected:
	CryptorPtr rsa;

	typedef s8 Version;

	struct Versions {
		static const Version 
			R2012,
			R201303,
			CURRENT;
	};

	static const int MAX_RSA_BLOCK_SIZE = 117;
	
protected:

	Version readVersion (BlockReader &is)
	{
		Version version;
		is.read(version);
		
		if (version < 0 || version > Versions::CURRENT)
			throw Exception("Unknown version");
		
		return version;
	}
	
	void writeVersion (BlockWriter &os)
	{
		os.write((Version)Versions::CURRENT);
	}

	core::Block readEncryptedEmbeddedKey (BlockReader &is, Version version)
	{
		core::Block result;
	
		if (version == Versions::R2012)
		{
			// skip header
			u8 b;
			is.read(b);
			is.read(b);
			is.read(b);
			
			is.read(result);
		}
		else
		if (version == Versions::R201303)
		{
			is.read(result);
		}
		else
			assert(false);
		
		LogDebug(mailiverse::core::crypt::CryptorRSAAES, "readEncryptedEmbeddedKey size " << result.size());
		
		return result;
	}
		
	core::Block decryptMainBlock(BlockReader &is, core::Block &key, Version version)
	{
		core::Block result;
		
		if (version == Versions::R2012)
		{
			CryptorAESIV aes(key, CryptorAES::NullIV);
			result = aes.decrypt(is.begin() + is.pos(), is.end());
		
		}
		else
		if (version == Versions::R201303)
		{
			LogDebug(mailiverse::core::crypt::CryptorRSAAES, "decryptMainBlock size " << is.remaining());

			CryptorAES aes(key);
			result = aes.decrypt(is.begin() + is.pos(), is.end());
		}
		else
			assert(false);

		return result;
	}
	
public:
	CryptorRSAAES (CryptorRSAPtr _rsa) :
		rsa(_rsa)
	{
	
	}
		
	virtual ~CryptorRSAAES ()
	{
	}
	
	virtual Block encrypt (const Block::Atom *begin, const Block::Atom *end)
	{
		Key key = CryptorAES::newKey();
		CryptorAES aes(key);
		core::Block b = aes.encrypt(begin,end);
		
		BlockWriter os;
		os.write(Versions::CURRENT);
		os.write(rsa->encrypt(key));
		os.write((char *)b.data(), b.size());
		
		return os.getBlock();
	}
	
	virtual Block decrypt (const Block::Atom *begin, const Block::Atom *end)
	{
		LogDebug(mailiverse::core::crypt::CryptorRSAAES, "decrypt size " << end-begin);

		BlockReader is(begin, end);
		Version version = readVersion(is);
		core::Block key = rsa->decrypt(readEncryptedEmbeddedKey(is, version));
		return decryptMainBlock(is, key, version);
	}
} ;

DECLARE_SMARTPTR(CryptorRSAAES);

} // namespace
} // namespace
} // namespace

#endif

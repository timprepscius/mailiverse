/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_crypt_CryptorRSAFactory_h__
#define __mailiverse_core_crypt_CryptorRSAFactory_h__

#include "CryptorRSA.h"

namespace mailiverse {
namespace core {
namespace crypt {

class CryptorRSAFactory
{
public:
	static CryptorRSA *create(const store::Environment &e)
	{
		return new
			CryptorRSA(
				Block(
					e.has("RSA-PublicKey") ?
						e.get("RSA-PublicKey") : std::string()
				),
				Block(
					e.has("RSA-PrivateKey") ?
						e.get("RSA-PrivateKey") : std::string()
				)
			);
	}
	
	static CryptorRSA *fromResources(const core::Block &publicKey, const core::Block &privateKey)
	{
		return new CryptorRSA (publicKey, privateKey);
	}
};

} /* namespace cache */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* __mailiverse_core_crypt_CryptorRSAFactory_h__ */

/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_crypt_PBE_h___
#define __mailiverse_core_crypt_PBE_h___

#include "Cryptor.h"

namespace mailiverse {
namespace core {
namespace crypt {

class PBE : public Cryptor
{
public:
	Key secretKey;
	CryptorPtr cryptor;
	
    static const int DEFAULT_ITERATIONS = 131072;
    static const int DEFAULT_KEYLENGTH = 256;

    static const Salt DEFAULT_SALT_CRYPTOR;
    static const Salt DEFAULT_SALT_VERIFIER;

    PBE (const std::string &password, const Salt &salt, int iterationCount, int keyLength);
	virtual ~PBE ();
	
    virtual Block encrypt(const Block::Atom *begin, const Block::Atom *end); 
    virtual Block decrypt(const Block::Atom *begin, const Block::Atom *end);
} ;

DECLARE_SMARTPTR(PBE);

} // namespace
} // namespace
} // namespace

#endif

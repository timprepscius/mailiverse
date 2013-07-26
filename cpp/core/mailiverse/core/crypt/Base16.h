/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_crypt_Base16_h__
#define __mailiverse_core_crypt_Base16_h__

#include "../Block.h"
#include <string>

namespace mailiverse {
namespace core {
namespace crypt {

class Base16
{
public:
	static std::string encode (const Block &b);
	static Block decode (const std::string &str);
	
	static Block encodeBytes (const Block &b);
	static Block decodeBytes (const Block &b);
};

}
}
}

#endif

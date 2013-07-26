/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef BASE64_H_
#define BASE64_H_

#include "../Block.h"
#include <string>

namespace mailiverse {
namespace core {
namespace crypt {

class Base64
{
public:
	static std::string encode (const Block &b);
	static Block decode (const std::string &str);
	
	static Block encodeBytes (const Block &b);
	static Block decodeBytes (const Block &b);
};

} /* namespace crypt */
} /* namespace core */
} /* namespace mailiverse */
#endif /* BASE64_H_ */

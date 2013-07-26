/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_util_InternalResource_h__
#define __mailiverse_core_util_InternalResource_h__

#include "mailiverse/core/Block.h"

namespace mailiverse {
namespace core {
namespace util {

class InternalResource
{
public:
	static core::Block get(const String &package, const String &key);
};

} // namespace
} // namespace
} // namespace

#endif

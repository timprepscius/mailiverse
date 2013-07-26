/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_base_BlockCompression_h__
#define __mailiverse_core_base_BlockCompression_h__

#include "Block.h"
#include "mailiverse/Exception.h"

namespace mailiverse {
namespace core {

Block inflate (const Block &block) throws_ (Exception);
Block deflate (const Block &block) throws_ (Exception);

} // namespace
} // namespace

#endif

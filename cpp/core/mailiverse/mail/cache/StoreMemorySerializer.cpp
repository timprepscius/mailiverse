/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "StoreMemorySerializer.h"
#include "mailiverse/utilities/Types.h"

using namespace mailiverse::mail::cache;
using namespace mailiverse;

const int StoreMemorySerializer::RECORD_OVERHEAD = 3 * sizeof(u32);
const int StoreMemorySerializer::CACHE_OVERHEAD = 1 * sizeof(u32);
const s8 StoreMemorySerializer::VERSION = 1;


/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef Mailiverse_MMPool_h
#define Mailiverse_MMPool_h

#include "MMTypes.h"

#define POOL_START @autoreleasepool  {
#define POOL_STOP }

//#define POOL_START NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
//#define POOL_STOP [pool drain];


#endif

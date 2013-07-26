/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */


#ifndef __mailiverse_utilities_Types_h__
#define __mailiverse_utilities_Types_h__

#include <boost/static_assert.hpp>
#include "Throws.h"

namespace mailiverse {

typedef unsigned char u8;
typedef signed char s8;
typedef unsigned short u16;
typedef signed short s16;
typedef unsigned int u32;
typedef signed int s32;
typedef unsigned long long u64;
typedef signed long long s64;

BOOST_STATIC_ASSERT(sizeof(u8)==1);
BOOST_STATIC_ASSERT(sizeof(s8)==1);
BOOST_STATIC_ASSERT(sizeof(u16)==2);
BOOST_STATIC_ASSERT(sizeof(s16)==2);
BOOST_STATIC_ASSERT(sizeof(u32)==4);
BOOST_STATIC_ASSERT(sizeof(s32)==4);
BOOST_STATIC_ASSERT(sizeof(u64)==8);
BOOST_STATIC_ASSERT(sizeof(s64)==8);

}

#endif /* TYPES_H_ */

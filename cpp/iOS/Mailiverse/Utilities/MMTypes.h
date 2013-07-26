/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_utilities_MMTypes_h__
#define __mailiverse_utilities_MMTypes_h__

inline bool isOSiPhone5 ()
{
	static bool atLeastIOS5 = [[[UIDevice currentDevice] systemVersion] floatValue] >= 5.0;
	return atLeastIOS5;
}

#if defined __IPHONE_OS_VERSION_MIN_REQUIRED
#if __IPHONE_OS_VERSION_MIN_REQUIRED > __IPHONE_4_3
#define __weakptr __weak
#define MM_WEAK weak
#else
#define __weakptr __unsafe_unretained
#define MM_WEAK unsafe_unretained
#endif
#endif

#endif

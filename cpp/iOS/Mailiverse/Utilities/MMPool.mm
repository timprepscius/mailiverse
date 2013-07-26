/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */


#include "MMPool.h"
#include "MMUtilities.h"
#include <map>
#include "mailiverse/utilities/Lock.h"

namespace mailiverse {
namespace utilities {

Mutex autoReleaseThreadPoolsMutex;
std::map<int, NSAutoreleasePool*> autoReleaseThreadPools;

int __onThreadStart__()
{
	Lock lock(autoReleaseThreadPoolsMutex);
	static int _id = 0;
	
	NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
	autoReleaseThreadPools[_id] = pool;
	
	return _id++;
}

void __onThreadEnd__(int idv)
{
	Lock lock(autoReleaseThreadPoolsMutex);
	NSAutoreleasePool *pool = autoReleaseThreadPools[idv];
	autoReleaseThreadPools.erase(idv);
	
	[pool drain];
}

} //
} // 

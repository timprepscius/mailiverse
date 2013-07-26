/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */


#import "MMUtilities.h"
#include "mailiverse/utilities/Log.h"

@implementation MMPtr

+ (id)instantiate:(mailiverse::utilities::SmartPtrBase *)ptr
{
	LogDebug (ui::MMPtr, "alloc " << ptr);
	MMPtr *p = [[MMPtr alloc] init];
	p->ptr = ptr;
	return p;
}

- (void)dealloc
{
	LogDebug (ui::MMPtr, "dealloc " << ptr);
	delete ptr;
}

@end
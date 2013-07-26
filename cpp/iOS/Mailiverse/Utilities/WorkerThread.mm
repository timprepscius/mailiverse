/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#import "WorkerThread.h"
#include "mailiverse/utilities/Log.h"

@interface WorkerThread () {
	NSOperationQueue *queue, *finishing;
}
@end

@implementation WorkerThread

- (id)init
{
	LogDebug(ui::WorkerThread, "init");

	if (self = [super init])
	{
		self->queue = [[NSOperationQueue alloc] init];
		[self->queue setMaxConcurrentOperationCount:1];
	}
	
	return self;
}

- (void)dealloc
{
	LogDebug(ui::WorkerThread, "dealloc");
}

-  (void)performSelector:(SEL)selector withTarget:(id)target withObject:(id)object
{
	NSInvocationOperation *operation = 
		[[NSInvocationOperation alloc] initWithTarget:target selector:selector object:object];
		
	[self->queue addOperation:operation];
}

- (void)invalidate
{
	self->finishing = self->queue;
	self->queue = nil;
}

- (bool)isFinished
{
	return [self->finishing operationCount] == 0;
}

@end
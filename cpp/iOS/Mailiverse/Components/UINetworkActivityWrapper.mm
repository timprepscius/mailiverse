/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */


#import "UINetworkActivityWrapper.h"

@implementation UINetworkActivityWrapper

- (id)init
{
    self = [super init];
    if (self) {
		self->timer = nil;
		self->stack = 0;
    }
    return self;
	
}

- (void)start
{
	[self performSelectorOnMainThread:@selector(startOnMain:) withObject:(id)nil waitUntilDone:NO];
}

- (void)startOnMain:(id)none
{
	LogDebug (ui::UINetworkActivityWrapper,"startMain " << stack);

	if (++stack == 1)
	{
		if (timer)
		{
			[timer invalidate];
			timer = nil;
		}
		else
			[[UIApplication sharedApplication] setNetworkActivityIndicatorVisible:YES];
	}
}

-(void)stop
{
	[self performSelectorOnMainThread:@selector(stopOnMain:) withObject:(id)nil waitUntilDone:NO];
}

-(void)stopOnMain:(id)none
{
	LogDebug (ui::UINetworkActivityWrapper,"stopMain " << stack);

	if (--stack == 0)
	{
		LogDebug (ui::UINetworkActivityWrapper,"stopping ");

		timer = [NSTimer scheduledTimerWithTimeInterval:(NSTimeInterval)0.5
									 target:self
								   selector:@selector(doStop:)
								   userInfo:nil
									repeats:NO];
		
		[[NSRunLoop currentRunLoop] addTimer:timer forMode:NSRunLoopCommonModes]; 
	}
}


- (void)doStop:(id)userInfo
{
	LogDebug (ui::UINetworkActivityWrapper,"doStop ");
	[[UIApplication sharedApplication] setNetworkActivityIndicatorVisible:NO];
	timer = nil;
}

@end
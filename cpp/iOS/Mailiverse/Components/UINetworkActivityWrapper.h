/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#import <UIKit/UIKit.h>

@interface UINetworkActivityWrapper : NSObject {
	NSTimer *timer;
	int stack;
}

- (id)init;
- (void)start;
- (void)stop;

@end

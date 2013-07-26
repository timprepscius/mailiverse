/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#import <UIKit/UIKit.h>

@interface WorkerThread : NSObject

- (void)performSelector:(SEL)selector withTarget:(id)target withObject:(id)object;
- (void)invalidate;
- (bool)isFinished;

@end
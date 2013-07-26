/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#import <UIKit/UIKit.h>
#include "MMTypes.h"

@interface UIToggleBarButtonItem : UIBarButtonItem

@property (assign, nonatomic) BOOL state;
@property (strong, nonatomic) NSObject *user;
@property (nonatomic, unsafe_unretained) SEL toggleSelector;

- (id)initWithTitle:(NSString *)title style:(UIBarButtonItemStyle)style target:(id)target action:(SEL)action user:(id)user;

- (BOOL) click;
- (void) reset;

@end

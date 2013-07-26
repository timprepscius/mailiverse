/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#import <UIKit/UIKit.h>
#include "MMTypes.h"

@interface UIToggleButton : UIButton

@property (strong, nonatomic) IBOutlet UIImage *imageCurrent;
@property (strong, nonatomic) IBOutlet UIImage *imageOn;
@property (strong, nonatomic) IBOutlet UIImage *imageOff;

- (bool) getState;
- (void) setState:(bool)on;
- (bool) toggle;

- (void) initToggle;
- (void) initToggleWithImagesOn:(UIImage *)_imageOn Off:(UIImage *)_imageOff;
- (void) reset;

@end

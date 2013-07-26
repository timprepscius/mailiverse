/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#import "UIToggleButton.h"

@implementation UIToggleButton
@synthesize imageOn;
@synthesize imageOff;
@synthesize imageCurrent;


- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code
    }
    return self;
}

- (void) initToggle
{
	if (imageOn == nil)
	{
		imageOff = self.currentImage;
		imageOn = self.currentBackgroundImage;
		[self setBackgroundImage:nil forState:UIControlStateNormal];
	}
}

- (void) initToggleWithImagesOn:(UIImage *)_imageOn Off:(UIImage *)_imageOff;
{
	bool state = [self getState];
	imageOn = _imageOn;
	imageOff = _imageOff;
	[self setState:state];
}


- (bool) getState
{
	return imageCurrent == imageOn && imageCurrent != nil;
}

- (void) setState:(bool)on
{
	imageCurrent = on ? imageOn : imageOff;
	[self setImage:imageCurrent forState:UIControlStateNormal];
}

- (bool) toggle
{
	bool state = [self getState];
	[self setState:!state];
	return !state;
}

- (void) reset
{
	[self setState:false];
}

@end

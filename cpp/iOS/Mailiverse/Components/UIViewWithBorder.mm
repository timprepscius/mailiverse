/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#import "UIViewWithBorder.h"

@implementation UIViewWithBorder

- (id)initWithFrame:(CGRect)frame
{
    self = [super initWithFrame:frame];
    if (self) {
        // Initialization code
    }
    return self;
}

- (void)layoutSubviews
{
	[self setNeedsDisplay];
	[super layoutSubviews];
}

- (void)drawRect:(CGRect)rect 
{
	// Get the contextRef
	CGContextRef contextRef = UIGraphicsGetCurrentContext();
	
	// Set the border width
	CGContextSetLineWidth(contextRef, 1.0);
	
	// Set the border color to RED
	CGContextSetRGBStrokeColor(contextRef, 0.0, 0.0, 0.0, 0.5);
	
	// Draw the border along the view edge
	CGContextStrokeRect(contextRef, rect);
	
	[super drawRect:rect];
}

@end

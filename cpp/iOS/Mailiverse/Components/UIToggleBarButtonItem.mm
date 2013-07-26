/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#import "UIToggleBarButtonItem.h"
#include "mailiverse/utilities/Log.h"

@implementation UIToggleBarButtonItem 
@synthesize state;
@synthesize toggleSelector;
@synthesize user;

- (id)initWithTitle:(NSString *)title style:(UIBarButtonItemStyle)style target:(id)target action:(SEL)action user:(id)_user
{
	LogDebug(ui::UIToggleBarButtonItem, "init");

	if (self = [super initWithTitle:title style:style target:target action:action])
	{
		self.user = _user;
		self.state = FALSE;
	}
	
	return self;
}

- (id)initWithCoder:(NSCoder *)aDecoder
{
	LogDebug(ui::UIToggleBarButtonItem, "init");

	if (self = [super initWithCoder:aDecoder])
	{
		self.state = FALSE;
		self.user = nil;
	}
	
	return self;
}

- (void)dealloc
{
	LogDebug(ui::UIToggleBarButtonItem, "dealloc");
}

- (BOOL) click
{
	state = !state;
	self.tintColor = 
		state ?
			[UIColor blueColor] :
			nil;
			
	if (toggleSelector)
		[self.target performSelector:toggleSelector withObject:self];
			
	return state;
}

- (void) reset
{
	if (state == FALSE)
		return;
		
	self.state = FALSE;
	self.tintColor = nil;
	
	if (toggleSelector)
		[self.target performSelector:toggleSelector withObject:self];
}

@end

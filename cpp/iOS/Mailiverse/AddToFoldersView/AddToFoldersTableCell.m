/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */


#import "AddToFoldersTableCell.h"

@interface AddToFoldersTableCell ()

@end

@implementation AddToFoldersTableCell
@synthesize toggle;
@synthesize label;

- (id)initWithCoder:(NSCoder *)aDecoder
{
	LogDebug(ui::AddToFoldersTableCell, "init");
	
	self = [super initWithCoder:aDecoder];
	
	if (self)
	{
	}
	
	return self;
}

- (void)postLoadInit
{
	[self.toggle initToggle];
}

@end

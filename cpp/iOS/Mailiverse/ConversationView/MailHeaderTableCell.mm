/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */


#import "MailHeaderTableCell.h"
#import "ConversationViewController.h"
#include "mailiverse/utilities/Log.h"
#include "MailData.h"

@interface MailHeaderTableCell () {
	MailDataPtr data;
}
@end

@implementation MailHeaderTableCell
@synthesize header;
@synthesize date;
@synthesize delegate;
@synthesize brief;

- (void)dealloc
{
	LogDebug(ui::MailLineTableCell, "dealloc");
}

-(void)touchesBegan:(NSSet*)touches withEvent:(UIEvent*)event
{
	UITouch *touch = [touches anyObject];

	if(touch.view == header)
	{
		data->displayMode = MailData::FULL;

		ConversationViewController *_delegate = (ConversationViewController *)delegate;
		[_delegate onDisplayModeChanged:self];
	}
}

- (void)onData:(MailData *)_data
{
	data = _data;
	header.textColor = getColorForIndex(data->color);
	header.text = data->author;
	brief.text = data->brief;
	date.text = data->date;
}

- (void)clear
{
	header.text = @"";
	date.text = @"";
}

@end

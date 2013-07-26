/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */


#import "ConversationTableCell.h"
#include "mailiverse/utilities/Log.h"
#import "FolderViewController.h"

@interface ConversationTableCell () {
	ConversationData *data;
	bool authorBold, selectedCheckBox;
}
@end


@implementation ConversationTableCell
@synthesize delegate;
@synthesize checkbox;
@synthesize date;
@synthesize authors;
@synthesize subject;
@synthesize brief;

- (id)initWithCoder:(NSCoder *)aDecoder
{
	LogDebug(ui::MailHeaderTableCell, "init");
	
	self = [super initWithCoder:aDecoder];
	
	if (self)
	{
		self->authorBold = false;
		self->selectedCheckBox = false;
	}
	
	return self;
}

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
	LogDebug(ui::MailHeaderTableCell, "init");

    self = [super initWithStyle:style reuseIdentifier:reuseIdentifier];
	
    if (self) 
	{
		self->authorBold = false;
		self->selectedCheckBox = false;
    }
    return self;
}

- (void)dealloc
{
	LogDebug(ui::MailHeaderTableCell, "dealloc");
}

- (void)setSelected:(BOOL)selected animated:(BOOL)animated
{
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

- (void)onData:(ConversationData *)_data
{
	data = _data;

	if (data)
	{
		data->dirty = false;
		
		date.text = data->date;
		authors.text = data->authors;
		subject.text = data->subject;
		brief.text = data->brief;
		
		self.userInteractionEnabled = data->isLoaded();
	}
	else
	{
		date.text = nil;
		authors.text = nil;
		subject.text = nil;
		brief.text = nil;
		
		self.userInteractionEnabled = NO;
	}

	[self updateAuthorFont];
	[self updateImage];
}

- (IBAction)onCheckbox:(id)sender 
{
	data->selected = !data->selected;
	[self updateImage];
	
	if (delegate)
	{
		FolderViewController *_delegate = (FolderViewController *)delegate;
		[_delegate onConversationCheckbox:self data:data];
	}
}

- (void)updateAuthorFont
{
	bool dataAuthorBold = data && !data->read;
	
	if (authorBold != dataAuthorBold)
	{
		authors.font = 
			dataAuthorBold ?
				[UIFont boldSystemFontOfSize:16] : 
				[UIFont systemFontOfSize:16];
			
		authorBold = dataAuthorBold;
	}
}

- (void)updateImage
{
	bool dataSelectedCheckBox = data && data->selected;
	if (selectedCheckBox != dataSelectedCheckBox)
	{
		UIImage *image =
			dataSelectedCheckBox ?
				[UIImage imageNamed:@"btn_selected.png"] :
				[UIImage imageNamed:@"btn_unselected.png"];
				
		[checkbox setImage:image forState:UIControlStateNormal];		

		selectedCheckBox = dataSelectedCheckBox;
	}
}

@end

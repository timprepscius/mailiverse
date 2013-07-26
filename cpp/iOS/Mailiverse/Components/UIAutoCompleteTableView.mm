/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#import "UIAutoCompleteTableView.h"
#include "mailiverse/utilities/Log.h"

@interface UIAutoCompleteTableView () {
	NSArray *data;
}

@end

@implementation UIAutoCompleteTableView
@synthesize autoCompleteDelegate;

- (id)initWithCoder:(NSCoder *)aDecoder
{
	LogDebug(ui::UIAutoCompleteTableView, "initWithCoder");

    if (self = [super initWithCoder:aDecoder]) 
	{
		self->data = [[NSMutableArray alloc] init];
		
		self.delegate = self;
		self.dataSource = self;
    }
    return self;
}

- (void)dealloc
{
	LogDebug(ui::UIAutoCompleteTableView, "dealloc");
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
	return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
	return data.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
    static NSString *CellIdentifier = @"Cell";
    UITableViewCell *cell = [tableView dequeueReusableCellWithIdentifier:CellIdentifier];
    
	if (!cell)
	{
		cell = [[UITableViewCell alloc] 
			initWithStyle:UITableViewCellStyleSubtitle 
				reuseIdentifier:CellIdentifier];
	
		cell.backgroundColor = [UIColor lightGrayColor];
	}
	
	NSArray *pair = [data objectAtIndex:indexPath.row];
	cell.textLabel.text = [pair objectAtIndex:0];
//	cell.textLabel.font = [UIFont fontWithName:@"System" size:14];
	cell.detailTextLabel.text = [pair objectAtIndex:1];

    return cell;
}

#pragma mark - Table view delegate

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	[autoCompleteDelegate onSelected:[data objectAtIndex:indexPath.row]];
}

- (void)onKeyChange:(NSString *)key
{
	data = [autoCompleteDelegate valuesFor:key];
	[self reloadData];
}

@end

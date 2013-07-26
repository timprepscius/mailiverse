/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#import "AddToFoldersViewController.h"
#import "AddToFoldersTableCell.h"
#include "MMUtilities.h"
#include "MasterSingleton.h"

using namespace mailiverse::mail::model;
using namespace mailiverse::mail::cache;
using namespace mailiverse::mail;
using namespace mailiverse;

@interface AddToFoldersViewController () {

	Set<ID> ids;
	Vector<FolderFilterPtr> userFolders;
	Vector<bool> selected;
}

@end

@implementation AddToFoldersViewController
@synthesize tableView;
@synthesize loadedTableCell;

+ (id)instantiate
{
	if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) 
	{
		return [[AddToFoldersViewController alloc] initWithNibName:@"AddToFoldersViewController" bundle:nil];
	} 
	else 
	{
		return [[AddToFoldersViewController alloc] initWithNibName:@"AddToFoldersViewController" bundle:nil];
	}
}

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) 
	{
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
}

- (void)viewDidUnload
{
	[self setLoadedTableCell:nil];
	[self setTableView:nil];
	
	ids.clear();
	userFolders.clear();
	selected.clear();
	
    [super viewDidUnload];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

#pragma mark - 
#pragma mark guts

- (void)onData:(Set<ID> &)_ids
{
	self->ids = _ids;
	userFolders.clear();
	for (auto &i : masterSingleton->getIndexer()->getUserFolders())
	{
		if (i->isLoaded())
		{
			FolderFilter *filter = dynamic_cast<FolderFilter *>((Folder *)i);
			userFolders.add(filter);
		}
	}
	
	selected.clear();	
	for (auto &i : userFolders)
		selected.push_back(false);
		
	[tableView reloadData];
}

- (IBAction)onCancel:(id)sender 
{
	[self dismissModalViewControllerAnimated:TRUE];
}

- (IBAction)onSave:(id)sender 
{
	Set<FolderFilterPtr> selectedFolders;
	int j=0;
	for (auto &i : userFolders)
	{
		if (selected[j])
			selectedFolders.add(i);
	}
	
	if (!selectedFolders.empty())
		masterSingleton->getActions()->addToUserFolders(ids, selectedFolders);
		
	[self dismissModalViewControllerAnimated:TRUE];
}

- (IBAction)onCellToggle:(id)sender 
{
	UIToggleButton *button = (UIToggleButton *)sender;
	int row = getRowForButton(tableView, button);
	selected[row] = [button toggle];
}

#pragma mark -
#pragma mark table

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView 
{
	return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section 
{
	return userFolders.size();
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
	return 60.0;
}

// i have no idea why I'm doing this
- (void)tableView:(UITableView *)tableView willDisplayCell:(UITableViewCell *)cell forRowAtIndexPath:(NSIndexPath *)indexPath 
{
    cell.backgroundColor = cell.contentView.backgroundColor;
}

-( UITableViewCell * )tableView:( UITableView * )_tableView cellForRowAtIndexPath:( NSIndexPath * )indexPath
{
    NSString *cellIdentitifier = @"AddToFoldersTableCell";
	
	AddToFoldersTableCell *cell = (AddToFoldersTableCell *)
		loadTableViewCell(
			self, _tableView, cellIdentitifier,
			^(){ id result = loadedTableCell; loadedTableCell = nil; return result; }
		);
		
	Folder *folder = userFolders[indexPath.row];
	[cell postLoadInit];
	cell.label.text = toNSString(folder->getName());
	[cell.toggle setState:false];
	
	return cell;
}

@end

/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */


#import "RevealFoldersViewController.h"
#import "SettingsViewController.h"
#import "AppDelegate.h"

#include "RevealFoldersViewControllerEventListener.h"
#include <mailiverse/mail/manager/Lib.h>
#include "MasterSingleton.h"
#include "FolderData.h"


using namespace mailiverse::mail::model;
using namespace mailiverse::mail;
using namespace mailiverse;

#define SYSTEM_FOLDERS 0
#define USER_FOLDERS 1
#define NUM_FOLDER_SETS 2

@interface RevealFoldersViewController () {
	bool started;
	RevealFoldersViewControllerEventListenerPtr eventListener;
	
	int selected;
	Vector<Vector<FolderPtr>> folderSets;
}

@end

@implementation RevealFoldersViewController

@synthesize tableView;
@synthesize folderViewController;
@synthesize name;
@synthesize email;

+ (id)instantiate
{
	if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) 
	{
		return [[RevealFoldersViewController alloc] initWithNibName:@"RevealFoldersViewController_iPhone" bundle:nil];
	} 
	else 
	{
		return [[RevealFoldersViewController alloc] initWithNibName:@"RevealFoldersViewController_iPhone" bundle:nil];
	}
}

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
		self->started = false;
		self->selected = -1;
		
		folderSets.resize(2);
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.

	[self onSettingsChanged:nil];
}

- (void)viewDidUnload
{
	[self setTableView:nil];
    [self setName:nil];
    [self setEmail:nil];
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

- (IBAction)onLogOut:(id)sender 
{
	AppDelegate *appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
	[appDelegate onLogout:TRUE];
}

- (IBAction)onSettings:(id)sender 
{
	[self presentModalViewController:[SettingsViewController instantiate] animated:TRUE];
}

- (void)onStart
{
	eventListener = new RevealFoldersViewControllerEventListener(self);
}

- (void)onStop
{
	eventListener = NULL;
	
	for (auto &i : folderSets)
		i.clear();
		
	started = false;
}

#pragma mark EventListener

- (void)onInitialized
{
	FolderPtr inbox = masterSingleton->getIndexer()->getInbox();
	[folderViewController setFolder:(Folder *)inbox isSystemFolder:true];
}

- (void)onNewFolder:(MMPtr *)ptr
{
	[self onLoadFolder:ptr];
}

- (void)onLoadFolder:(MMPtr *)ptr
{
	Folder *folder = (Folder *)(ptr->ptr->access());
	if (utilities::endsWith(folder->getName(), ":part"))
		return ;
		
	Vector<List<FolderPtr>> lists;
	lists.push_back (masterSingleton->getIndexer()->getSystemFolders());
	lists.push_back (masterSingleton->getIndexer()->getUserFolders());
	
	for (int i=0; i<NUM_FOLDER_SETS; ++i)
	{
		List<FolderPtr> &list = lists[i];
		Vector<FolderPtr> &folders = folderSets[i];
		
		folders.clear();
		for (auto i : list)
		{
			if (i->isLoaded() && 
				i->getID() != mailiverse::mail::manager::Indexer::KnownFolderIds::RepositoryId)
			{
				folders.push_back(i);
			}
		}
	}

	[tableView reloadData];
}

- (void)onNewConversation:(MMPtr *)ptr
{
}


- (void)onSettingsChanged:(MMPtr *)ptr
{
	name.text = toNSString(masterSingleton->getIdentity()->getName());
	email.text = toNSString(masterSingleton->getIdentity()->getEmail());
}

- (void)onCheckMailFinished
{
}

#pragma mark - TableView

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView 
{
	return 2;
}

- (NSInteger)tableView:(UITableView *)_tableView numberOfRowsInSection:(NSInteger)section 
{
	return folderSets[section].size();
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	LogDebug (ui::RevealFoldersViewController, "tableView:didSelectRowAtIndexPath: " << indexPath.section << " " << indexPath.row);
	
	[folderViewController setFolder:(Folder *)folderSets[indexPath.section][indexPath.row] isSystemFolder:(indexPath.section==0)];
}

-( UITableViewCell * )tableView:( UITableView * )_tableView cellForRowAtIndexPath:( NSIndexPath * )indexPath
{
	LogDebug (ui::RevealFoldersViewController, "tableView:cellForRowAtIndexPath: " << indexPath.section << " " << indexPath.row);

	int section = indexPath.section;
	Vector<FolderPtr> &folders = folderSets[section];

	int row = indexPath.row;
	
    NSString *cellIdentifier = @"FolderTableCell";
	UITableViewCell *cell = (UITableViewCell *)[_tableView dequeueReusableCellWithIdentifier:cellIdentifier];
	if (cell == nil)
		cell = [[UITableViewCell alloc] init];
	
	FolderData *folderData = FolderData::load(folders[row]);
	cell.textLabel.text = folderData->name;
	cell.textLabel.textColor = [UIColor lightGrayColor];
	cell.textLabel.highlightedTextColor = [UIColor lightGrayColor];
	cell.textLabel.font = [UIFont systemFontOfSize:16];

    UIView *selectionColor = [[UIView alloc] init];
    selectionColor.backgroundColor = [UIColor colorWithRed:(0.0/255.0) green:(100.0f/255.0) blue:(200.0f/255.0) alpha:1];
    cell.selectedBackgroundView = selectionColor;
		
	return cell;	
}


@end

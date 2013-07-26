/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#import "FolderViewController.h"
#import "ConversationTableCell.h"
#import "ComposeViewController.h"
#import "ConversationViewController.h"
#import "AddToFoldersViewController.h"
#import "AppDelegate.h"
#include <limits.h>

#include "mailiverse/mail/model/Lib.h"
#include "mailiverse/mail/manager/Lib.h"
#include "mailiverse/utilities/Log.h"
#include "MasterSingleton.h"
#include "MMUtilities.h"
#include "ConversationData.h"
#include "FolderData.h"

#include "FolderViewControllerEventListener.h"

using namespace mailiverse::mail::model;
using namespace mailiverse::mail;
using namespace mailiverse;

const int CHUNK_SIZE=20;

#pragma mark -
#pragma mark FolderViewController


@interface FolderViewController () 
{
	FolderViewControllerEventListenerPtr eventListener;
	bool started, loaded, running, initialized;
	
	bool isSystemFolder;
	FolderPtr folder;
	FolderDataPtr data;
	NSString *searchText;
	bool incrementalSearchRunning;
	manager::FolderQueryPtr folderQuery;

	EGORefreshTableHeaderView *_refreshHeaderView;
	bool _reloading;
}
@end

@implementation FolderViewController
@synthesize menuPopUp;
@synthesize menuSelectionShowPopUp;
@synthesize menuSelectionTrash;
@synthesize menuSelectionAddToFolder;
@synthesize menuSelectionMarkSpam;
@synthesize menuSelection;

@synthesize logoutView;
@synthesize conversationViewController;

@synthesize navbarButtons;
@synthesize navbarButtonsSearch;
@synthesize tableView;
@synthesize cellHeader;
@synthesize searchTableContainer;
@synthesize searchbar;

//----------------------------------------------

#pragma mark -
#pragma mark alloc-dealloc

+ (id)instantiate
{
	if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) 
	{
		return [[FolderViewController alloc] initWithNibName:@"FolderViewController_iPhone" bundle:nil];
	} 
	else 
	{
		return [[FolderViewController alloc] initWithNibName:@"FolderViewController_iPhone" bundle:nil];
	}
}

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
	LogDebug (ui::FolderViewController, "initWithNibName");

    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) 
	{
		self->started = self->loaded = self->running = self->initialized = false;
		self->searchText = @"";
		self->incrementalSearchRunning = false;
    }
	
    return self;
}

- (void)dealloc
{
	LogDebug (ui::FolderViewController, "dealloc");
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
	if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) {
	    return (interfaceOrientation != UIInterfaceOrientationPortraitUpsideDown);
	} else {
	    return YES;
	}
}


#pragma mark initialization

//---------------------------------------------------

- (void)viewDidLoad
{
    [super viewDidLoad];

	LogDebug (ui::FolderViewController, "viewDidLoad");
	[self hackToolbarColor];
	[self hackSlideToRevealWhenWeAreOnIPad];

	[self hackSearchBar];
	[self hackSelectionMenu];
	
	[self hackReplaceTopBar];
	[self hackPullToRefresh];
	[self hackMakeTheTableViewBounceEvenWhenEmpty];

	loaded = true;
	if (started && loaded && initialized)
		[self onRun];
}

- (void)viewDidUnload
{
	LogDebug (ui::FolderViewController, "viewDidUnload");

	[self onPause];
	loaded = false;
	conversationViewController = nil;

	[self setSearchbar:nil];
	[self setTableView:nil];
	[self setLogoutView:nil];
	
	[self setNavbarButtons:nil];
	[self setMenuSelection:nil];
	[self setSearchTableContainer:nil];
	[self setNavbarButtonsSearch:nil];
	[self setMenuSelectionShowPopUp:nil];
	[self setMenuPopUp:nil];
    [self setMenuSelectionTrash:nil];
	[self setMenuSelectionAddToFolder:nil];
    [self setMenuSelectionMarkSpam:nil];
    [super viewDidUnload];
}

- (void)viewDidAppear:(BOOL)animated
{
	LogDebug (ui::FolderViewController, "viewDidAppear");

	[super viewDidAppear:animated];
	[self deselectAndReloadDataForRowOnReturn];
}

- (void)deselectAndReloadDataForRowOnReturn
{
	NSIndexPath *path = tableView.indexPathForSelectedRow;
	
	if (path)
	{
		[tableView deselectRowAtIndexPath:path animated:TRUE];
		[self onPossibleRowDataChange:path.row];
	}
}

- (void)viewWillDisappear:(BOOL)animated
{
	LogDebug (ui::FolderViewController, "viewWillDisappear");
	
	[super viewWillDisappear:animated];
}

//---------------------------------------------------

#pragma mark -
#pragma mark Data Source Loading / Reloading Methods

- (void)onCheckMail
{
    _reloading = YES;
	LogDebug (ui::FolderViewController, "checkMail");
	masterSingleton->getActions()->checkMail();
}

- (void)onCheckMailStep:(NSString *)status
{
    [_refreshHeaderView setStatus:status];
	[self onDataChange];
}

- (void)onCheckMailFinished
{
    //  model should call this when its done loading
    _reloading = NO;
    [self.tableView reloadData];  
    [_refreshHeaderView egoRefreshScrollViewDataSourceDidFinishedLoading:self.tableView];

	[self onDataChange];
}

- (void)onCheckMailFailureNotifyBegin
{
	_refreshHeaderView.backgroundColor = [UIColor colorWithRed:226.0/255.0 green:200.0/255.0 blue:200.0/255.0 alpha:1.0];
	[self performSelector:@selector(onCheckMailFailureNotifyEnd) withObject:nil afterDelay:1.0];
}

- (void)onCheckMailFailureNotifyEnd
{
	_refreshHeaderView.backgroundColor = [UIColor colorWithRed:226.0/255.0 green:231.0/255.0 blue:237.0/255.0 alpha:1.0];
	[self onCheckMailFinished];
}

- (void)onCheckMailFailure
{
	[self onCheckMailFailureNotifyBegin];
}

#pragma mark -
#pragma mark UIScrollViewDelegate Methods

- (void)scrollViewDidScroll:(UIScrollView *)scrollView{ 

    [_refreshHeaderView egoRefreshScrollViewDidScroll:scrollView];
}

- (void)scrollViewDidEndDragging:(UIScrollView *)scrollView willDecelerate:(BOOL)decelerate{

    [_refreshHeaderView egoRefreshScrollViewDidEndDragging:scrollView];

}

#pragma mark -
#pragma mark EGORefreshTableHeaderDelegate Methods

- (void)egoRefreshTableHeaderDidTriggerRefresh:(EGORefreshTableHeaderView*)view
{
    [self onCheckMail];

}

- (BOOL)egoRefreshTableHeaderDataSourceIsLoading:(EGORefreshTableHeaderView*)view{

    return _reloading; // should return if data source model is reloading

}

- (NSDate*)egoRefreshTableHeaderDataSourceLastUpdated:(EGORefreshTableHeaderView*)view{

    return [NSDate date]; // should return date data source was last changed

}

//---------------------------------------------------
#pragma mark -

- (void)setFolder:(Folder*)_folder isSystemFolder:(bool)_isSystemFolder
{
	isSystemFolder = _isSystemFolder;
	folder = (FolderSet *)(Folder *)_folder;
	folderQuery->setFolder(folder);
	data = FolderData::load(folder);
	
	[self resetDisplay];
}

- (void)onDataChange
{
	folderQuery->underlyingResultsChanged();
	[self resetDisplay];
}

- (void)resetDisplay
{
	LogDebug (ui::FolderViewController, "resetDisplay");

	if (folder != NULL)
	{
		[menuSelectionTrash setState:(folder->getID() == manager::Indexer::KnownFolderIds::TrashId)];
		[menuSelectionMarkSpam setState:(folder->getID() == manager::Indexer::KnownFolderIds::SpamId)];
		
		[menuSelectionAddToFolder setState:!isSystemFolder];
		[self refilterDisplay];
	}
	
	NSString *folderTitle = @"Loading...";
	if (folder && folder->isLoaded())
	{
		folderTitle = toNSString(folder->getName());
	}	
		
	self.title = folderTitle;
}

- (void)refilterDisplay
{
	[tableView reloadData];
}

#pragma mark EventListener
//--------------------------------------------------------------------

- (void)onLoadFolder:(MMPtr *)ptr
{
	LogDebug (ui::FolderViewController, "onLoadFolder " << ptr->ptr);
	[self onDataChange];
}

- (void)onPossibleRowDataChange:(int)row
{
	ConversationPtr conversation = folderQuery->results->get(row);
	ConversationData *conversationData = ConversationData::load(conversation);
	if (conversationData->dirty)
	{
		NSIndexPath *indexPath = [NSIndexPath indexPathForRow:row inSection:0];
		if ([tableView cellForRowAtIndexPath:indexPath])
		{
			[tableView 
				reloadRowsAtIndexPaths:
					[NSArray arrayWithObjects:indexPath,nil] 
				withRowAnimation:
					UITableViewRowAnimationNone
			];
		}
	}
}

- (void)onFolderListing:(MMPtr *)ptr
{
	LogDebug (ui::FolderViewController, "onLoadConversation " << ptr->ptr);
	
	int index=-1;
	
	if (folderQuery != NULL)
	{
		ConversationPtr conversation = (Conversation *)ptr->ptr->access();

		utilities::EmptyMonitor::Writer m(folderQuery->monitor);
		auto i = std::find(folderQuery->results->begin(), folderQuery->results->end(), conversation);
		
		if (i!=folderQuery->results->end())
			index = i - folderQuery->results->begin();
	}
	
	if (index != -1)
	{
		LogDebug (ui::FolderViewController, "onLoadConversation has index " << index);
		
		NSIndexPath *indexPath = [NSIndexPath indexPathForRow:index inSection:0];
		if ([tableView cellForRowAtIndexPath:indexPath])
		{
			[tableView 
				reloadRowsAtIndexPaths:
					[NSArray arrayWithObjects:indexPath,nil] 
				withRowAnimation:
					UITableViewRowAnimationNone
			];
		}
	}
	else
	{
		if (ptr->ptr->access() == NULL)
		{
			[tableView reloadData];
		}
	}
}

- (void)onChangedConversation:(MMPtr *)ptr
{
	[self onDataChange];
}

- (void)onNewConversation:(MMPtr *)ptr
{
	[self onDataChange];
}

//----------------------------------------------------------

- (void)onInitialized
{
	initialized = true;
	if (started && loaded && initialized)
		[self onRun];
}

- (void)onStart
{
	LogDebug (ui::FolderViewController, "onStart");

	eventListener = new FolderViewControllerEventListener(self);
	folderQuery = new manager::FolderQuery(masterSingleton, 10);
	started = true;
}

- (void)onRun
{
	LogDebug (ui::FolderViewController, "onRun");

	[self setFolder:(Folder *)masterSingleton->getIndexer()->getInbox() isSystemFolder:true];

	self->running = true;
	[self resetDisplay];
}

- (void)onPause
{
	LogDebug (ui::FolderViewController, "onPause");
	running = false;
	
	if (folderQuery)
		folderQuery->releaseMemory();
}

- (void)onStop
{
	LogDebug (ui::FolderViewController, "onStop");
	
	[self onPause];
	
	data = NULL;
	folderQuery = NULL;
	folder = NULL;
	[tableView reloadData];
	started = false;

	eventListener = NULL;
}

#pragma mark navbar

- (IBAction)navbarButtonsOnCompose:(id)sender 
{
	LogDebug (ui::FolderViewController, "navbarButtonsOnCompose");

	ComposeViewController *composeController = [ComposeViewController instantiate];

	[self hackShowComposeController:composeController];
	[composeController onData:NULL withConversation:NULL];
}

- (IBAction)navbarButtonsOnSearch:(id)sender 
{
	if ([navbarButtonsSearch toggle])
		[self showSearch];
	else
		[self hideSearch];
}

#pragma mark -
#pragma mark searchbar

- (void)startIncrementalSearch
{
	if (!incrementalSearchRunning)
		[self doIncrementalSearch];
}

- (void)doIncrementalSearch
{
	if ([searchText length]>0)
	{
		incrementalSearchRunning = true;

		if (folderQuery->onFilterStep())
		{
			[searchbar startActivity];
		}
		else
		{
			[searchbar finishActivity];
		}
			
		[self performSelector:@selector(doIncrementalSearch) withObject:nil afterDelay:0.25f];
	}
	else
	{
		incrementalSearchRunning = false;
		[searchbar finishActivity];
	}
}

- (void)searchBar:(UISearchBar *)searchBar textDidChange:(NSString *)_searchText
{       
	searchText = _searchText;
	folderQuery->setFilter(searchText.UTF8String);
	[self refilterDisplay];
	[self startIncrementalSearch];
}
        
- (void)searchBarSearchButtonClicked:(UISearchBar *)_searchBar
{
	[searchbar resignFirstResponder];
}

- (void)showSearch
{
	[searchbar becomeFirstResponder];

	[UIView animateWithDuration:0.1 delay:0.0 options:UIViewAnimationOptionCurveEaseInOut 
		animations:^{
			CGRect frame = searchTableContainer.frame;
			frame.size.height = frame.size.height - 44;
			frame.origin.y = 0;
			searchTableContainer.frame = frame;
		} 
		completion:^(BOOL finished) {
		}
	];

}

- (void)hideSearch
{
	[searchbar setText:@""];
	[searchbar resignFirstResponder];
	
	[UIView animateWithDuration:0.1 delay:0.0 options:UIViewAnimationOptionCurveEaseInOut 
		animations:^{
			CGRect frame = searchTableContainer.frame;
			frame.size.height = frame.size.height + 44;
			frame.origin.y = -44;
			searchTableContainer.frame = frame;
		} 
		completion:^(BOOL finished) {
		}
	];

}

// -----------------------------------------------

#pragma mark selection


- (void)hackSelectionMenu
{
	[menuSelectionAddToFolder initToggle];
	[menuSelectionMarkSpam initToggle];
	[menuSelectionTrash initToggle];
	[menuSelectionShowPopUp initToggle];
}

// these need to change

- (void)onConversationCheckbox:(ConversationTableCell *)cell data:(ConversationData *)_data
{
	LogDebug (ui::FolderViewController, "onConversationCheckbox ");
	
	if (_data->selected)
	{
		data->selectedConversations.insert(_data->key);
		if (data->selectedConversations.size()==1)
			[self showSelectionMenu];
	}
	else
	{
		data->selectedConversations.erase(_data->key);
		if (data->selectedConversations.empty())
			[self hideSelectionMenu];
	}
}

- (void)showSelectionMenu
{
	[menuSelectionShowPopUp reset];
	pullUp(true, self.view, menuSelection);
}

- (void)hideSelectionMenu
{
	[self hideMenuPopUp];
	pullUp (false, self.view, menuSelection);
}

- (IBAction)menuSelectionOnClose:(id)sender 
{
	// this needs to change
	data->selectedConversations.clear();
	[self.tableView reloadData];
	[self hideSelectionMenu];
}

- (IBAction)menuSelectionOnTrash:(id)sender 
{
	masterSingleton->getActions()->markAndReindexConversations(
		data->selectedConversations, TransportState::TRASH, 
		folder->getID()!=manager::Indexer::KnownFolderIds::TrashId
	);
	
	[self menuSelectionOnClose:nil];
}

- (IBAction)menuSelectionOnMarkRead:(id)sender 
{
	masterSingleton->getActions()->markConversations(
		data->selectedConversations, TransportState::READ, 
		true
	);
	
	[self menuSelectionOnClose:nil];
}

- (IBAction)menuSelectionOnMarkUnread:(id)sender 
{
	masterSingleton->getActions()->markConversations(
		data->selectedConversations, TransportState::READ, 
		false
	);
	
	[self menuSelectionOnClose:nil];
}

- (IBAction)menuSelectionOnAddToFolder:(id)sender
{
	UIToggleButton *button = sender;
	if ([button getState])
	{
		folder->removeConversations(data->selectedConversations);
		[self menuSelectionOnClose:nil];
		[self onDataChange];
	}
	else
	{
		AddToFoldersViewController *addToFolders = [AddToFoldersViewController instantiate];
		[addToFolders onData:data->selectedConversations];
		[self presentModalViewController:addToFolders animated:TRUE];
		
		[self menuSelectionOnClose:nil];
	}
}

- (IBAction)menuSelectionOnMarkSpam:(id)sender 
{
	masterSingleton->getActions()->markSpam(
		data->selectedConversations, 
		![menuSelectionMarkSpam getState]
	);
	
	[self menuSelectionOnClose:nil];
}

- (IBAction)menuSelectionOnShowPopUp:(id)sender 
{
	if ([menuSelectionShowPopUp toggle])
		[self showMenuPopUp];
	else
		[self hideMenuPopUp];
}

- (void)showMenuPopUp
{
	pullUp (true, self.view, menuPopUp, menuSelection);
}

- (void)hideMenuPopUp
{
	pullUp (false, self.view, menuPopUp, menuSelection);
}

#pragma mark table

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
	LogDebug (ui::FolderViewController, "tableView:cellForRowAtIndexPath: " << indexPath.section << " " << indexPath.row);

	int row = indexPath.row;
	folderQuery->request(row);
	
    NSString *cellIdentitifier = @"ConversationTableCell";
	
	ConversationTableCell *cell = (ConversationTableCell *)
		loadTableViewCell(
			self, _tableView, cellIdentitifier,
			^(){ id result = cellHeader; cellHeader = nil; return result; }
		);
	
	cell.selectionStyle = UITableViewCellSelectionStyleGray;		
	cell.contentView.backgroundColor = cell.backgroundColor;
	cell.delegate = self;
	
	utilities::EmptyMonitor::Writer m(folderQuery->monitor);
	if (*folderQuery->resultsBegin <= row && row < *folderQuery->resultsEnd)
	{
		ConversationPtr conversation = folderQuery->results->get(row);
		ConversationData *conversationData = ConversationData::load(conversation);
		
		conversationData->setSelected(
			data->selectedConversations.contains(
				conversation->getID()
			)
		);
		
		[cell onData:conversationData];
	}
	else
	{
		[cell onData:NULL];
	}
	
	return cell;	
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView 
{
	LogDebug (ui::FolderViewController, "numberOfSectionsInTableView");

	if (!folder)
		return 0;
		
	return 1;
}

- (NSInteger)tableView:(UITableView *)_tableView numberOfRowsInSection:(NSInteger)section 
{
	utilities::EmptyMonitor::Writer m(folderQuery->monitor);

	int result = 
		[searchText isEqualToString:@""] ?
			folder->getNumConversations() :
			folderQuery->results->size();
			
	LogDebug (ui::FolderViewController, "numberOfRowsInSection " << result);
	return result;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
	LogDebug (ui::FolderViewController, "tableView:didSelectRowAtIndexPath: " << indexPath.section << " " << indexPath.row);

	ConversationViewController *_conversationViewController = conversationViewController;
	
	if (!_conversationViewController)
		_conversationViewController = _conversationViewController = [ConversationViewController instantiate];
				
	utilities::EmptyMonitor::Writer m(folderQuery->monitor);
	ConversationPtr conversation = (*folderQuery->results)[indexPath.row];

	[_conversationViewController onData:(Conversation *)conversation];
	
	if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) {
		[self.navigationController pushViewController:_conversationViewController animated:YES];
	}
}

- (IBAction)onForceQuit:(id)sender 
{
	LogDebug (ui::FolderViewController, "onForceQuit");
	exit(0);
}

#pragma -
#pragma mark hacks

- (void)hackPullToRefresh
{
	_refreshHeaderView = [[EGORefreshTableHeaderView alloc] initWithFrame:CGRectMake(0.0f, 0.0f - self.tableView.bounds.size.height, self.view.frame.size.width, self.tableView.bounds.size.height)];
	_refreshHeaderView.delegate = self;
	[self.tableView addSubview:_refreshHeaderView];

	//  update the last update date
	[_refreshHeaderView refreshLastUpdatedDate];
}

- (void)hackMakeTheTableViewBounceEvenWhenEmpty
{
	tableView.bounces = YES;
}

- (void)hackToMakeTheSearchBarHaveADone
{
	for (UIView *searchBarSubview in [searchbar subviews]) {
		if ([searchBarSubview conformsToProtocol:@protocol(UITextInputTraits)]) {

			@try {
				UITextField *tf = (UITextField *)searchBarSubview;

				[tf setReturnKeyType:UIReturnKeyDone];
				tf.enablesReturnKeyAutomatically = NO;
			}
			@catch (NSException * e) {

			}
		}
	}
}

- (UIImage *)hackImageWithColor:(UIColor *)color 
{
    CGRect rect = CGRectMake(0.0f, 0.0f, 1.0f, 1.0f);
    UIGraphicsBeginImageContext(rect.size);
    CGContextRef context = UIGraphicsGetCurrentContext();

    CGContextSetFillColorWithColor(context, [color CGColor]);
    CGContextFillRect(context, rect);

    UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();

    return image;
}

- (void)hackMakeTopBarWhiteBackground
{
	[self.navigationController.navigationBar 
		setBackgroundImage:
		[self hackImageWithColor:[UIColor whiteColor]]
		forBarMetrics:UIBarMetricsDefault
	];
}

- (void)hackRemoveBackgroundFromSearchBar
{
//	[[[searchbar subviews] objectAtIndex:0] removeFromSuperview];
}

/*
- (void)hackToMakeTheSearchBarHaveADone
{
	for (UIView *searchBarSubview in [searchbar subviews]) {
		if ([searchBarSubview conformsToProtocol:@protocol(UITextInputTraits)]) {

			@try {
				UITextField *tf = (UITextField *)searchBarSubview;
				
				[tf setReturnKeyType:UIReturnKeyDone];
				tf.enablesReturnKeyAutomatically = NO;
			}
			@catch (NSException * e) {

			}
		}
	}
}
*/

- (void)hackReplaceTopBar
{
	UIBarButtonItem *customButtons =
		[[UIBarButtonItem alloc] 
			initWithCustomView:navbarButtons
		];
					
	NSMutableArray *array = 
		[[NSMutableArray alloc] initWithObjects:
			customButtons, nil
		];

	addRightBarButtons(self.navigationItem, array);
}

//- (void)hackResetBackgroundOfTopBar
//{
//	[self.navigationController.navigationBar setBackgroundImage:nil forBarMetrics:UIBarMetricsDefault];
//}

- (void)hackSearchBar
{
	[navbarButtonsSearch initToggle];

	searchbar.delegate = self;
	[self hackToMakeTheSearchBarHaveADone];
}

- (void)hackShowComposeController:(ComposeViewController *)composeController
{
	if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) 
	{
		[self.navigationController pushViewController:composeController animated:YES];
	}
	else
	{
		[self presentModalViewController:
			[[UINavigationController alloc] initWithRootViewController:composeController]
		   animated:TRUE];
	}	
}

- (void)hackToolbarColor
{
	self.navigationController.navigationBar.tintColor = [UIColor blackColor];
}

- (void)hackSlideToRevealWhenWeAreOnIPad
{
	if ([[UIDevice currentDevice] userInterfaceIdiom] != UIUserInterfaceIdiomPhone) 
	{
		AppDelegate *appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
		self.navigationItem.leftBarButtonItem = [appDelegate.sidePanelController leftButtonForCenterPanel];
	}
}

@end

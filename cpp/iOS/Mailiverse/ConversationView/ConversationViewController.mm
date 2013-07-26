/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#import "ConversationViewController.h"
#import "MailTableCell.h"
#import "MailHeaderTableCell.h"
#import "AddtoFoldersViewController.h"
#import "ComposeViewController.h"
#import "AppDelegate.h"

#include "mailiverse/mail/manager/Lib.h"
#include "mailiverse/utilities/Log.h"
#include "MMUtilities.h"

#include <boost/algorithm/string.hpp>
#include "MasterSingleton.h"
#include "MailData.h"
#include "ConversationViewControllerEventListener.h"

using namespace mailiverse::mail::model;
using namespace mailiverse::mail;
using namespace mailiverse;

//------------------------------------------------------------


//-------------------------------------------------------------

@interface ConversationViewController () 
{
	ConversationViewControllerEventListenerPtr eventListener;
	mailiverse::mail::model::ConversationPtr conversation;
	Vector<MailPtr> mail;
	
	Map<Mail *, MailTableCell *> fullCellCache;
}
@end

@implementation ConversationViewController

@synthesize navbarButtonsDropdown;
@synthesize subjectTableCell;
@synthesize navbarButtonsTrash;
@synthesize navbarButtonsMarkSpam;
@synthesize subject;
@synthesize tableView;
@synthesize loadedCell;
@synthesize loadedHeaderCell;
@synthesize navbarButtons;
@synthesize navbarDropdown;

//----------------------------------------------------------------

#pragma mark alloc-dealloc


+ (id)instantiate
{
	if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) 
	{
		return [[ConversationViewController alloc] initWithNibName:@"ConversationViewController_iPhone" bundle:nil];
	} 
	else 
	{
		return [[ConversationViewController alloc] initWithNibName:@"ConversationViewController_iPhone" bundle:nil];
	}
}

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
	LogDebug(ui::ConversationViewController, "init");
	return [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
}

- (void)dealloc
{
	LogDebug(ui::ConversationViewController, "dealloc");
}

//----------------------------------------------------------------

#pragma mark initialization

//----------------------------------------------------------------

- (void)onStart
{

}

- (void)onStop
{
	fullCellCache.clear();
	eventListener = NULL;
	conversation = NULL;
	
	[loadedHeaderCell clear];
	mail.clear();
	[tableView reloadData];
}

- (void)viewDidLoad
{
    [super viewDidLoad];	
	[self hackToolbarColor];

	[navbarButtonsDropdown initToggle];
	[navbarButtonsTrash initToggle];
	[navbarButtonsMarkSpam initToggle];
	[self hackSetSubjectCellAsTableHeader];
	[self hackInstallToolbarButtons];
	[self hackInstallSwipeGoesBack];

	[self attemptRun];
}

- (void)viewDidUnload
{
	[self setNavbarButtonsTrash:nil];
    [self setNavbarButtonsMarkSpam:nil];
    [super viewDidUnload];
	
	[self onShutdown];
	
	[self setTableView:nil];
	[self setSubject:nil];
	[self setLoadedCell:nil];
	[self setLoadedHeaderCell:nil];
	
    [self setNavbarButtons:nil];
	[self setNavbarDropdown:nil];
	[self setNavbarButtonsDropdown:nil];
    [self setSubjectTableCell:nil];
}

- (void)viewDidAppear:(BOOL)animated
{
	LogDebug (ui::ConversationViewController, "viewDidAppear");
	
	[super viewDidAppear:animated];	
	[self hackDeselectTableViewCellOnReturn];
}

- (void)onData:(Conversation *)_conversation 
{
	conversation = _conversation;
	[self attemptRun];
}

- (void)attemptRun
{
	if ([self isViewLoaded] && conversation)
		[self onRun];
}

- (void)willRotateToInterfaceOrientation: (UIInterfaceOrientation)toInterfaceOrientation duration: (NSTimeInterval)duration 
{
}

#pragma mark navbar

//-----------------------------------------------------------------

- (IBAction)navbarButtonsOnDropdown:(id)sender 
{
	if ([navbarButtonsDropdown toggle])
		[self showDropdown];
	else
		[self hideDropdown];
}

- (void)showDropdown
{
	pullDown(true, self.view, navbarDropdown);
}

- (void)hideDropdown
{
	pullDown(false, self.view, navbarDropdown);
}

- (IBAction)navbarButtonsOnTrash:(id)sender 
{
	conversation->markState(TransportState::TRASH, [navbarButtonsTrash toggle]);
	masterSingleton->getActions()->reindexConversation(conversation);
}

- (IBAction)navbarButtonsOnMarkUnRead:(id)sender 
{
	conversation->unmarkState(TransportState::READ);
	masterSingleton->getActions()->reindexConversation(conversation);
}

- (IBAction)navbarButtonsOnAddToFolder:(id)sender 
{
	Set<cache::ID> ids;
	ids.add(conversation->getID());
	
	AddToFoldersViewController *addToFolders = [AddToFoldersViewController instantiate];
	[addToFolders onData:ids];
	[self presentModalViewController:addToFolders animated:TRUE];
	
	[self navbarButtonsOnDropdown:nil];
}

- (IBAction)navbarButtonsOnMarkSpam:(id)sender 
{
	UIToggleButton *button = sender;
	Set<cache::ID> ids;
	ids.add(conversation->getID());
	
	masterSingleton->getActions()->markSpam(ids, [button toggle]);
	[self navbarButtonsOnDropdown:nil];
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
	if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone)
		return (interfaceOrientation != UIInterfaceOrientationPortraitUpsideDown);
	else
		return YES;
}

//----------------------------------------------

#pragma mark -
#pragma mark tableView

- (UITableViewCell *)tableView:(UITableView *)_tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath 
{
	Mail *_mail = mail[indexPath.row];
	MailData *data = MailData::load(mail[indexPath.row], conversation);
	
	UITableViewCell *cell;
	
	if (data->displayMode == MailData::SHORT)
	{
		if (fullCellCache.containsKey(_mail))
			fullCellCache.remove(_mail);

		MailTableCell *_cell = 
			(MailTableCell *)loadTableViewCell(
				self, _tableView,
				@"MailHeaderTableCell", 
				^(){ id result = loadedHeaderCell; loadedHeaderCell = nil; return result; }
			);
		_cell.delegate = self;
		[_cell onData:data];
		cell = _cell;
	}
	
	if (data->displayMode == MailData::FULL)
	{
		if (fullCellCache.containsKey(_mail))
		{
			MailTableCell *_cell = fullCellCache.getr(_mail);
			[_cell onRedisplay];
			return _cell;
		}
	
		MailTableCell *_cell = 
			(MailTableCell *)loadTableViewCell(
				self, _tableView,
//				idiomizeXib(@"MailTableCell"), 
				@"MailTableCell_iPhone", 
				^(){ id result = loadedCell; loadedCell = nil; return result; },
				false
			);
			
		fullCellCache.put(_mail, _cell);
		
		_cell.delegate = self;
		[_cell onData:data];
		cell = _cell;
	}

	cell.selectionStyle = UITableViewCellSelectionStyleGray;
	return cell;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath 
{
	MailData *data = MailData::load(mail[indexPath.row], conversation);

	int height =
		data->displayMode == MailData::SHORT ?
			64 : data->height;
	
	height = std::min(2000,height);
	LogDebug (ui::ConversationViewController, "heightForRowAtIndexPath " << indexPath.row << " height " << height);
	return height;
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView 
{
	return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section 
{
	LogDebug(ui::ConversationViewController, "numberOfRowsInSection" << mail.size());
	return mail.size();
}

//--------------------------------------------------------

#pragma mark -

- (void)onRun
{
	[navbarButtonsTrash setState:conversation->getHeader()->hasState(TransportState::TRASH)];
	[navbarButtonsMarkSpam setState:conversation->getHeader()->hasState(TransportState::SPAM)];
	conversation->markState(TransportState::READ);
	
	List<MailPtr> _mail = conversation->getItems();	
	mail.assign(_mail.begin(), _mail.end());

	subject.text = toNSStringPtr(conversation->getHeader()->getSubject());
	eventListener = new ConversationViewControllerEventListener(self);
	
	// calculate the most recent message automatically
	if (!mail.empty())
	{
		MailData *data = MailData::load(mail.back(), conversation);
		data->displayMode = MailData::FULL;
	}

	[tableView reloadData];
	
	[tableView 
		scrollToRowAtIndexPath:[NSIndexPath 
		indexPathForRow:0 inSection:0] 
		atScrollPosition:UITableViewScrollPositionTop 
		animated:FALSE
	];
}

- (void)onShutdown
{
	conversation = NULL;
	mail.clear();
}

- (void)onLoadConversation:(MMPtr *)ptr
{
	if (ptr->ptr->access() == (Conversation *)conversation)
	{
		ConversationPtr _conversation = conversation;
		[self onData:(Conversation *)conversation];
	}
}

- (void)onChangedConversation:(MMPtr *)ptr
{
	LogDebug(ui::ConversationViewController, "onChangedConversation");

	[self onLoadConversation:ptr];
}

- (void)onLoadMail:(MMPtr *)ptr
{
	int I=0;
	for (auto i : mail)
	{
		if ((Mail *)i == ptr->ptr->access())
			break;
			
		I++;
	}
	
	if (I >= mail.size())
		return;
		
	[tableView beginUpdates];
	[tableView reloadRowsAtIndexPaths:
		[NSArray arrayWithObject:[NSIndexPath indexPathForRow:I inSection:0]] 
		withRowAnimation:UITableViewRowAnimationNone
	];
	[tableView endUpdates];
}

- (void)onHeightChange:(UITableViewCell *)cell
{
	LogDebug(ui::ConversationViewController, "onHeightChange");

	[UIView setAnimationsEnabled:NO];
	[tableView beginUpdates];	
	[tableView endUpdates];
	[UIView setAnimationsEnabled:YES];	
}

- (void)onDisplayModeChanged:(UITableViewCell *)cell
{
	LogDebug(ui::ConversationViewController, "onDisplayModeChanged");

	NSIndexPath *index = [tableView indexPathForCell:cell];
	
	[UIView setAnimationsEnabled:NO];
	[tableView reloadRowsAtIndexPaths: 
		[NSArray arrayWithObject:index] 
		withRowAnimation:UITableViewRowAnimationNone
	];
	[UIView setAnimationsEnabled:YES];	
}

//--------------------------------------------------------------------------------
//--------------------------------------------------------------------------------
//--------------------------------------------------------------------------------

- (IBAction)onReplyAll:(id)sender
{
	int row = [self getSelectedRow:sender];
	if (row < 0)
		return;

	MailPtr concerning = mail[row];
	MailPtr _mail = 
		masterSingleton->getActions()->replyToAll(
			conversation, 
			concerning
		);
	
	ComposeViewController *composeController = [ComposeViewController instantiate];
	[self hackShowComposeController:composeController];
	
	[composeController onData:(Mail *)_mail withConversation:(Conversation *)conversation];
}

- (IBAction)onReply:(id)sender
{
	int row = [self getSelectedRow:sender];
	if (row < 0)
		return;

	MailPtr concerning = mail[row];
	MailPtr _mail = masterSingleton->getActions()->replyTo(
		conversation, 
		concerning
	);
	
	id composeController = [ComposeViewController instantiate];
	[self hackShowComposeController:composeController];
	
	[composeController onData:(Mail *)_mail withConversation:(Conversation *)conversation];
}

- (IBAction)onForward:(id)sender
{
	int row = [self getSelectedRow:sender];
	if (row < 0)
		return;

	MailPtr _mail = masterSingleton->getActions()->forward(conversation, mail[row]);
	
	id composeController = [ComposeViewController instantiate];
	[self hackShowComposeController:composeController];
	
	[composeController onData:(Mail *)_mail withConversation:(Conversation *)conversation];
}

- (IBAction)onContinueEditing:(id)sender
{
	int row = [self getSelectedRow:sender];
	if (row < 0)
		return;
		
	MailPtr _mail = mail[row];
	
	id composeController = [ComposeViewController instantiate];
	[self hackShowComposeController:composeController];
	
	[composeController onData:(Mail *)_mail withConversation:(Conversation *)conversation];
}

- (int)getSelectedRow:(id)sender
{
	return getRowForButton(tableView, (UIButton *)sender);
}

#pragma mark -
#pragma mark hacks

- (void)hackToolbarColor
{
	self.navigationController.navigationBar.tintColor = [UIColor blackColor];
}

- (void)hackInstallToolbarButtons
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

- (void)hackDeselectTableViewCellOnReturn
{
	NSIndexPath *path = tableView.indexPathForSelectedRow;
	
	if (path)
		[tableView deselectRowAtIndexPath:path animated:TRUE];
}


- (void)hackInstallSwipeGoesBack
{
	if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) 
	{
		UISwipeGestureRecognizer *leftRecognizer = [[UISwipeGestureRecognizer alloc] initWithTarget:self action:@selector(hackSwipeGoBack:)];
		leftRecognizer.direction = UISwipeGestureRecognizerDirectionRight;
		[leftRecognizer setNumberOfTouchesRequired:1];
		[self.view addGestureRecognizer:leftRecognizer];
	}
}

- (void)hackSwipeGoBack:(UISwipeGestureRecognizer*)gestureRecognizer 
{
	[self.navigationController popViewControllerAnimated:YES];
}

- (void)hackSetSubjectCellAsTableHeader
{
	tableView.tableHeaderView = subjectTableCell;
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


@end

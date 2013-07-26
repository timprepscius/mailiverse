/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */


#import <UIKit/UIKit.h>
#include "MMTypes.h"

#import "UISearchBarWithActivity.h"
#import "ConversationViewController.h"
#import "ConversationTableCell.h"
#import "UIToggleBarButtonItem.h"
#import "LoginViewController.h"
#import "EGORefreshTableHeaderView.h"
#import "UIToggleButton.h"
#include "ConversationData.h"

@interface FolderViewController : UIViewController 
	<UITableViewDataSource, UITableViewDelegate, UITextFieldDelegate, UISearchBarDelegate, 
	EGORefreshTableHeaderDelegate>

@property (strong, nonatomic) IBOutlet UIView *navbarButtons;
@property (MM_WEAK, nonatomic) IBOutlet UIToggleButton *navbarButtonsSearch;

- (IBAction)navbarButtonsOnCompose:(id)sender;
- (IBAction)navbarButtonsOnSearch:(id)sender;

@property (MM_WEAK, nonatomic) IBOutlet UIView *menuSelection;
@property (MM_WEAK, nonatomic) IBOutlet UIToggleButton *menuSelectionShowPopUp;
@property (MM_WEAK, nonatomic) IBOutlet UIToggleButton *menuSelectionTrash;
@property (MM_WEAK, nonatomic) IBOutlet UIToggleButton *menuSelectionAddToFolder;
@property (MM_WEAK, nonatomic) IBOutlet UIToggleButton *menuSelectionMarkSpam;


- (IBAction)menuSelectionOnTrash:(id)sender;
- (IBAction)menuSelectionOnMarkRead:(id)sender;
- (IBAction)menuSelectionOnMarkUnread:(id)sender;
- (IBAction)menuSelectionOnAddToFolder:(id)sender;
- (IBAction)menuSelectionOnMarkSpam:(id)sender;


- (IBAction)menuSelectionOnShowPopUp:(id)sender;
- (IBAction)menuSelectionOnClose:(id)sender;

@property (MM_WEAK, nonatomic) IBOutlet UIView *menuPopUp;

//------------------

@property (MM_WEAK, nonatomic) ConversationViewController *conversationViewController;
@property (nonatomic, strong) IBOutlet UITableViewCell *cellHeader;

@property (MM_WEAK, nonatomic) IBOutlet UIView *searchTableContainer;
@property (MM_WEAK, nonatomic) IBOutlet UITableView *tableView;
@property (MM_WEAK, nonatomic) IBOutlet UISearchBarWithActivity *searchbar;

@property (strong, nonatomic) IBOutlet UIView *logoutView;
- (void)onLogout;
- (IBAction)onForceQuit:(id)sender;

- (void)onConversationCheckbox:(ConversationTableCell *)cell data:(ConversationData *)data;

//---------------------------

+ (id)instantiate;
- (void)setFolder:(mailiverse::mail::model::Folder *)folder isSystemFolder:(bool)isSystemFolder;
- (void)onStart;
- (void)onStop;

@end

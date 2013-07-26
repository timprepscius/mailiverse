/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#import <UIKit/UIKit.h>
#include "MMTypes.h"

#import "UIToggleButton.h"
#import "MailHeaderTableCell.h"
#include <mailiverse/mail/model/Conversation.h>
#include <map>

@interface ConversationViewController : UIViewController 
	<UITableViewDataSource, UITableViewDelegate, UISplitViewControllerDelegate>
{
}

@property (strong, nonatomic) IBOutlet UIView *navbarButtons;
@property (strong, nonatomic) IBOutlet UIView *navbarDropdown;
@property (MM_WEAK, nonatomic) IBOutlet UIToggleButton *navbarButtonsDropdown;
@property (MM_WEAK, nonatomic) IBOutlet UIToggleButton *navbarButtonsTrash;
@property (MM_WEAK, nonatomic) IBOutlet UIToggleButton *navbarButtonsMarkSpam;

- (IBAction)navbarButtonsOnDropdown:(id)sender;
- (IBAction)navbarButtonsOnTrash:(id)sender;
- (IBAction)navbarButtonsOnMarkUnRead:(id)sender;
- (IBAction)navbarButtonsOnAddToFolder:(id)sender;
- (IBAction)navbarButtonsOnMarkSpam:(id)sender;



@property (MM_WEAK, nonatomic) IBOutlet UILabel *subject;

@property (MM_WEAK, nonatomic) IBOutlet UITableView *tableView;
@property (strong, nonatomic) IBOutlet UITableViewCell *subjectTableCell;
@property (nonatomic, strong) IBOutlet UITableViewCell *loadedCell;
@property (nonatomic, strong) IBOutlet MailHeaderTableCell *loadedHeaderCell;


- (void)onDisplayModeChanged:(UITableViewCell *)cell;
- (void)onHeightChange:(UITableViewCell *)cell;

- (void)onStart;
- (void)onStop;
- (void)onData:(mailiverse::mail::model::Conversation *)model;
	
- (IBAction)onReplyAll:(id)sender;
- (IBAction)onForward:(id)sender;
- (IBAction)onReply:(id)sender;
- (IBAction)onContinueEditing:(id)sender;

+ (id)instantiate;

@end

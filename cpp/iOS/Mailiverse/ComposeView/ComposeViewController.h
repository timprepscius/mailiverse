/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#import <UIKit/UIKit.h>
#include "MMTypes.h"
#import "ComposeHeaderLineCell.h"
#import "UIAutoCompleteTableView.h"

#include "mailiverse/mail/model/Mail.h"

@interface ComposeViewController : UITableViewController
	<UITextFieldDelegate, UITextViewDelegate, UIAutoCompleteDelegate, UIActionSheetDelegate>

@property (strong, nonatomic) IBOutlet ComposeHeaderLineCell *loadedTableCell;
@property (strong, nonatomic) IBOutlet UITextView *body;
@property (strong, nonatomic) IBOutlet UITableViewCell *bodyCell;
@property (MM_WEAK, nonatomic) IBOutlet UIView *tableFooter;



- (IBAction)textFieldDidChange:(UITextField *)textField;
- (IBAction)onClose:(id)sender;
- (IBAction)onSend:(id)sender;
- (IBAction)onAdd:(id)sender;

@property (unsafe_unretained, nonatomic) IBOutlet UIAutoCompleteTableView *autoCompleteTableView;
@property (strong, nonatomic) IBOutlet UITableViewCell *autoCompleteTableCell;
+ (id)instantiate;

- (void) onData:(mailiverse::mail::model::Mail *)mail withConversation:(mailiverse::mail::model::Conversation *)conversation;

@end

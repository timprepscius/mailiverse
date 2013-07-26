/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#import <UIKit/UIKit.h>
#include "MMTypes.h"
#include "mailiverse/mail/model/Conversation.h"

@interface AddToFoldersViewController : UIViewController <UITableViewDataSource>

+(AddToFoldersViewController *)instantiate;

@property (MM_WEAK, nonatomic) IBOutlet UITableView *tableView;
@property (strong, nonatomic) IBOutlet UITableViewCell *loadedTableCell;

- (IBAction)onCancel:(id)sender;
- (IBAction)onSave:(id)sender;
- (IBAction)onCellToggle:(id)sender;

- (void)onData:(mailiverse::Set<mailiverse::mail::cache::ID> &) conversations;

@end

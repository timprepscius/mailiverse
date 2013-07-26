/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */


#import <UIKit/UIKit.h>
#include "MMTypes.h"

#include "ConversationData.h"

@interface ConversationTableCell : UITableViewCell

@property (MM_WEAK, nonatomic) id delegate;

@property (MM_WEAK, nonatomic) IBOutlet UIButton *checkbox;
- (IBAction)onCheckbox:(id)sender;

@property (MM_WEAK, nonatomic) IBOutlet UILabel *date;
@property (MM_WEAK, nonatomic) IBOutlet UILabel *authors;
@property (MM_WEAK, nonatomic) IBOutlet UILabel *subject;
@property (MM_WEAK, nonatomic) IBOutlet UILabel *brief;

- (void)onData:(ConversationData *)data;

@end

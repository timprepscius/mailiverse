/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */


#import <UIKit/UIKit.h>
#include "MMTypes.h"

@interface MailHeaderTableCell : UITableViewCell 

- (void) hackMakeAutoButtonOneSegment;
- (void)onData:(struct MailData *)data;

@property (MM_WEAK, nonatomic) IBOutlet UILabel *header;
@property (MM_WEAK, nonatomic) IBOutlet UILabel *date;
@property (MM_WEAK, nonatomic) id delegate;
@property (MM_WEAK, nonatomic) IBOutlet UILabel *brief;

-(void)clear;

@end

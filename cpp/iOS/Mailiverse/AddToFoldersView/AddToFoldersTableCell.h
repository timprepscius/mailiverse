/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#import <UIKit/UIKit.h>
#import "UIToggleButton.h"
#include "MMTypes.h"

@interface AddToFoldersTableCell : UITableViewCell

@property (MM_WEAK, nonatomic) IBOutlet UIToggleButton *toggle;
@property (MM_WEAK, nonatomic) IBOutlet UILabel *label;

- (void)postLoadInit;

@end

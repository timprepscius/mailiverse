/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#import <UIKit/UIKit.h>
#import "UIToggleButton.h"
#include "MMTypes.h"
#include "mailiverse/Types.h"

@interface ComposeHeaderLineCell : UITableViewCell

@property (MM_WEAK, nonatomic) IBOutlet UILabel *label;
@property (MM_WEAK, nonatomic) IBOutlet UITextField *text;
@property (MM_WEAK, nonatomic) IBOutlet UIButton *button;

- (void)onData:(const mailiverse::String &)_text 
	withLabel:(const mailiverse::String &)_label 
	isFirst:(bool)_first 
	isLast:(bool)_last 
	isSubject:(bool)_isSubject;

- (void)onAutoComplete;
- (void)onIdentityChange;

@end

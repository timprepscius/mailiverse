/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */


#import <UIKit/UIKit.h>
#import "UIToggleButton.h"
#include "MMTypes.h"

@interface SettingsViewController : UITableViewController

+(SettingsViewController *)instantiate;

@property (MM_WEAK, nonatomic) IBOutlet UIToggleButton *enableNotifications;
@property (MM_WEAK, nonatomic) IBOutlet UITextField *fullName;
@property (MM_WEAK, nonatomic) IBOutlet UIToggleButton *detailedNotifications;
@property (MM_WEAK, nonatomic) IBOutlet UITextView *signature;

- (IBAction)onSave:(id)sender;
- (IBAction)onCancel:(id)sender;

- (IBAction)onToggle:(id)sender;

- (IBAction)onBackgroundTouched:(id)sender;

@end

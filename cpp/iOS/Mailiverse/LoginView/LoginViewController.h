/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#import <UIKit/UIKit.h>
#import "UIToggleButton.h"

#include "MMTypes.h"

@interface LoginViewController : UIViewController

@property (strong, nonatomic) id folderViewController;
@property (strong, nonatomic) id revealFoldersViewController;

@property (MM_WEAK, nonatomic) IBOutlet UITextField *passwordField;
@property (MM_WEAK, nonatomic) IBOutlet UITextField *nameField;
@property (MM_WEAK, nonatomic) IBOutlet UIButton *loginButton;
@property (MM_WEAK, nonatomic) IBOutlet UILabel *statusLabel;
@property (MM_WEAK, nonatomic) IBOutlet UIImageView *backgroundImage;
@property (MM_WEAK, nonatomic) IBOutlet UIImageView *backgroundImageTop;

- (IBAction)onLoginButton:(id)sender;
- (IBAction)onExit:(id)sender;
- (IBAction)onBackgroundTouched:(id)sender;

+ (id)instantiate;

@end

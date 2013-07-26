/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */


#import <UIKit/UIKit.h>
#import "JASidePanelController.h"
#import "FolderViewController.h"
#import "RevealFoldersViewController.h"
#import "ConversationViewController.h"

@interface AppDelegate : UIResponder <UIApplicationDelegate>

@property (strong, nonatomic) UIWindow *window;

@property (strong, nonatomic) NSString *deviceToken;
@property (strong, nonatomic) UINavigationController *navigationController;
@property (strong, nonatomic) FolderViewController *folderViewController;
@property (strong, nonatomic) ConversationViewController *conversationViewController;
@property (strong, nonatomic) RevealFoldersViewController *revealFoldersViewController;
@property (strong, nonatomic) JASidePanelController *sidePanelController;
@property (strong, nonatomic) LoginViewController *loginViewController;
@property (strong, nonatomic) UIViewController *rootViewController;

- (void)onEnableNotifications:(bool)value;
- (void)onLogout:(BOOL)animated;
- (void)onLogin;

@end

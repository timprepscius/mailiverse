/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */


#import <UIKit/UIKit.h>
#include "MMTypes.h"

#include "mailiverse/mail/model/Lib.h"
#include "FolderViewController.h"

@interface RevealFoldersViewController : UIViewController
	<UITableViewDataSource, UITableViewDelegate>

+ (id)instantiate;

@property (MM_WEAK, nonatomic) IBOutlet UITableView *tableView;
@property (MM_WEAK, nonatomic) FolderViewController *folderViewController;

@property (MM_WEAK, nonatomic) IBOutlet UILabel *name;
@property (MM_WEAK, nonatomic) IBOutlet UILabel *email;


- (IBAction)onLogOut:(id)sender;
- (IBAction)onSettings:(id)sender;


- (void)onStart;
- (void)onStop;
- (void)onInitialized;
- (void)onLoadFolder:(MMPtr *)ptr;
- (void)onNewConversation:(MMPtr *)ptr;
- (void)onCheckMailFinished;

@end

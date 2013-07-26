/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#import <UIKit/UIKit.h>
#import "UIToggleButton.h"
#include "MMTypes.h"

@interface MailTableCell : UITableViewCell <UIWebViewDelegate, UIScrollViewDelegate>

@property (MM_WEAK, nonatomic) UIViewController *delegate;

@property (MM_WEAK, nonatomic) IBOutlet UIView *fullView;
@property (MM_WEAK, nonatomic) IBOutlet UILabel *fullDate;
@property (MM_WEAK, nonatomic) IBOutlet UILabel *fullAuthors;
@property (MM_WEAK, nonatomic) IBOutlet UILabel *fullRecipients;
@property (MM_WEAK, nonatomic) IBOutlet UIWebView *fullHtml;
@property (MM_WEAK, nonatomic) IBOutlet UIToggleButton *fullImages;

@property (MM_WEAK, nonatomic) IBOutlet UIView *fullActions;
@property (MM_WEAK, nonatomic) IBOutlet UIView *draftActions;

@property (MM_WEAK, nonatomic) IBOutlet UIView *touchArea;

- (IBAction)onFullImages:(id)sender;


- (void)onData:(struct MailData *)data;
- (void)onRedisplay;
	
+ (id)instantiate;

@end

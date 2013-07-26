/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#import <UIKit/UIKit.h>

@interface UISearchBarWithActivity : UISearchBar
{
    UIActivityIndicatorView *activityIndicatorView;
}

@property(retain) UIActivityIndicatorView *activityIndicatorView;

- (void)startActivity;  // increments startCount and shows activity indicator
- (void)finishActivity; // decrements startCount and hides activity indicator if 0

@end
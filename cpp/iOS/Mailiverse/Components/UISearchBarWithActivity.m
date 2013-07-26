/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#import "UISearchBarWithActivity.h"


@interface UISearchBarWithActivity () {

	bool started;

}

@end

@implementation UISearchBarWithActivity

@synthesize activityIndicatorView;

- (void)layoutSubviews {
    UITextField *searchField = nil;

    for(UIView* view in self.subviews){
        if([view isKindOfClass:[UITextField class]]){
            searchField= (UITextField *)view;
            break;
        }
    }

    if(searchField) {
        if (!self.activityIndicatorView) {
            UIActivityIndicatorView *taiv = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleGray];
            taiv.center = CGPointMake(searchField.leftView.bounds.origin.x + searchField.leftView.bounds.size.width/2,
                                      searchField.leftView.bounds.origin.y + searchField.leftView.bounds.size.height/2);
            taiv.hidesWhenStopped = YES;
            taiv.backgroundColor = [UIColor whiteColor];
            self.activityIndicatorView = taiv;
			started = false;
			
            [searchField.leftView addSubview:self.activityIndicatorView];
        }
    }

    [super layoutSubviews];
}

- (void)startActivity
{
	if (!started)
	{
		[self.activityIndicatorView startAnimating];
		started = true;
	}
}

- (void)finishActivity 
{
	if (started)
	{
		[self.activityIndicatorView stopAnimating];
		started = false;
	}
}

@end
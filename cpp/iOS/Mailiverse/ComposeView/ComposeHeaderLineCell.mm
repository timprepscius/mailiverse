/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#import "ComposeHeaderLineCell.h"
#include "MMUtilities.h"

using namespace mailiverse::mail::model;
using namespace mailiverse;

@interface ComposeHeaderLineCell () {
	NSString *initialized;
}

@end

@implementation ComposeHeaderLineCell
@synthesize label;
@synthesize text;
@synthesize button;

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
    return (interfaceOrientation == UIInterfaceOrientationPortrait);
}

- (void)onData:(const String &)_text withLabel:(const String &)_label isFirst:(bool)_first isLast:(bool)_last isSubject:(bool)_isSubject
{
	if (initialized == nil)
	{
		text.leftView = label;
		text.rightView = button;
		initialized = @"initialized";
	}
	
	if (_first)
	{
		label.text = toNSString(_label);
		[label sizeToFit];
		text.leftViewMode = UITextFieldViewModeAlways;
	}
	else
	{
		text.leftViewMode = UITextFieldViewModeNever;
	}
	
	text.text = toNSString(_text);
	
	if (!_isSubject)
		[self onIdentityChange];
	
	if (_last && !_isSubject && !_text.empty())
	{
			
		text.rightViewMode = UITextFieldViewModeAlways;
	}
	else
	{
		text.rightViewMode = UITextFieldViewModeNever;
	}
}

- (void)onAutoComplete
{
	text.rightViewMode = UITextFieldViewModeNever;
}

- (void)onIdentityChange
{
	String _text = text.text.UTF8String;
	if (_text.empty() || Identity(_text).hasValidEmail())
		text.backgroundColor = [UIColor whiteColor];
	else
		text.backgroundColor = colorFromRGB(0xFFCCCC);
}


@end

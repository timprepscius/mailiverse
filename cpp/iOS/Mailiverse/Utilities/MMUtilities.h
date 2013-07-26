/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef PirateMailViewer_MMUtilities_h
#define PirateMailViewer_MMUtilities_h

#include "MMTypes.h"
#include "mailiverse/utilities/SmartPtr.h"

#import <UIKit/UIKit.h>
#import "UITransparentToolbar.h"
#include <string>

inline UIColor *colorFromRGB (unsigned int rgbValue)
{
	return 
		[UIColor 
			colorWithRed:((float)((rgbValue & 0xFF0000) >> 16))/255.0 
			green:((float)((rgbValue & 0xFF00) >> 8))/255.0 
			blue:((float)(rgbValue & 0xFF))/255.0 alpha:1.0
		];
}

inline NSIndexPath *getIndexPathForView (UITableView *tableView, UIView *view)
{
	CGRect viewFrame = [view convertRect:view.bounds toView:tableView];
	return [tableView indexPathForRowAtPoint:viewFrame.origin];
}

inline UIScrollView *getWebViewScrollView(UIWebView *webView)
{
	if (isOSiPhone5())
	{
		return webView.scrollView;
	}
	else
	{
		NSArray *sv = [NSArray arrayWithArray:[webView subviews]];
		UIScrollView *webScroller = (UIScrollView *)[sv objectAtIndex:0];
		return webScroller;
	}
}

inline void addRightBarButtons(UINavigationItem *navigationItem, NSArray *buttons)
{
	if (isOSiPhone5())
	{
		navigationItem.rightBarButtonItems = buttons;
	}
	else
	{
		UIToolbar* toolbar = [[UITransparentToolbar alloc]
								initWithFrame:CGRectMake(0, 0, 100, 45)];
		[toolbar setItems:buttons animated:NO];

		UIBarButtonItem *item = [[UIBarButtonItem alloc] initWithCustomView:toolbar];
		navigationItem.rightBarButtonItem = item;
	}
}

inline int getRowForButton (UITableView *tableView, UIView *button)
{
	return getIndexPathForView(tableView,button).row;
}

inline NSString *toNSString(const std::string &s)
{
	return [NSString stringWithUTF8String:s.c_str()];
}

inline NSString *toNSStringPtr(const std::string *s)
{
	return [NSString stringWithUTF8String:(s?s->c_str():"")];
}

inline UITableViewCell *loadTableViewCell (id zelf, UITableView *tableView, NSString *cellIdentifier, id(^get)(), bool setReuse=true)
{
	UITableViewCell *cell = (UITableViewCell *)[tableView dequeueReusableCellWithIdentifier:cellIdentifier];

	if(cell == nil) 
	{
		[[NSBundle mainBundle] loadNibNamed:cellIdentifier owner:zelf options:nil];
		cell = get();
		
		if (setReuse)
			[cell setValue:cellIdentifier forKey:@"reuseIdentifier"];
	}
	
	return cell;
}

inline NSString *idiomizeXib(NSString *name)
{
	return
		([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) ?
			[name stringByAppendingString:@"_iPhone"] :
			[name stringByAppendingString:@"_iPad"];
}

inline void pullUp (
	bool on,
	UIView *zelf, 
	UIView *view, 
	UIView *above = nil, 
	void(^onCompletion)(BOOL finished) = ^(BOOL finished){}
)
{
	if (on)
	{
		[UIView animateWithDuration:0.1 delay:0.0 options:UIViewAnimationOptionCurveEaseInOut 
			animations:^{
				CGRect frame = view.frame;
				
				frame.origin.y = 
					above ?
						(above.frame.origin.y - frame.size.height) :
						(zelf.frame.origin.y + zelf.frame.size.height - frame.size.height);
						
				view.frame = frame;
			} 
			completion:onCompletion
		];
	}
	else
	{
		[UIView animateWithDuration:0.1 delay:0.0 options:UIViewAnimationOptionCurveEaseInOut 
			animations:^{
				CGRect frame = view.frame;
				frame.origin.y = (zelf.frame.origin.y + zelf.frame.size.height);
				view.frame = frame;
			} 
			completion:onCompletion
		];
	
	}
}

inline void pullDown (
	bool on,
	UIView *zelf, 
	UIView *view, 
	UIView *below = nil,
	void(^onCompletion)(BOOL finished) = ^(BOOL finished){}
)
{
	if (on)
	{
		[UIView animateWithDuration:0.1 delay:0.0 options:UIViewAnimationOptionCurveEaseInOut 
			animations:^{
				CGRect frame = view.frame;
				
				frame.origin.y = 
					below ?
						(below.frame.origin.y + below.frame.size.height) :
						(zelf.frame.origin.y);
						
				view.frame = frame;
			} 
			completion:onCompletion
		];
	}
	else
	{
		[UIView animateWithDuration:0.1 delay:0.0 options:UIViewAnimationOptionCurveEaseInOut 
			animations:^{
				CGRect frame = view.frame;				
				frame.origin.y = 0 - frame.size.height;
				view.frame = frame;
			} 
			completion:onCompletion
		];
	
	}
}

inline UIColor *getColor(float r, float g, float b)
{
	return [UIColor colorWithRed:r/255.0f green:g/255.0f blue:b/255.0f alpha:1.0f];
}

inline UIColor *getColorForIndex (int i)
{
	switch (i)
	{
		case 0:
			// dark blue
			return getColor(28.0f,31.0f,158.0f);
		case 1:
			// purple
			return getColor(66.0f,16.0f,120.0f);
		case 2:
			// dark yellow
			return getColor(120.0f,120.0f,16.0f);
		case 3:
			// dark red
			return getColor(120.0f,16.0f,16.0f);
		case 4:
			// really dark blue
			return getColor(11.0f,8.0f,33.0f);
		case 5:
			// dark green
			return getColor(15.0f,64.0f,6.0f);
		case 6:
			// dark light blue
			return getColor(6.0f,42.0f,64.0f);
		case 7:
			// light blue
			return getColor(47.0f,139.0f,196.0f);
	} ;
	
	return getColor(0.0f,0.0f,0.0f);
}

@interface MMPtr : NSObject
{
@public
	mailiverse::utilities::SmartPtrBase *ptr;
}

+ (id)instantiate:(mailiverse::utilities::SmartPtrBase *)ptr;

@end;

#endif

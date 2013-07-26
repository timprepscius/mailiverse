/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#import "MailTableCell.h"
#import "ConversationViewController.h"
#include "mailiverse/utilities/Log.h"
#include "MailData.h"

@interface MailTableCell () {
	MailDataPtr data;

	bool watchingEvents;
	int observeStack;
	int kObservingContentSizeChangesContext;
	
	bool webViewWasInitialized;
	bool initialized;
	bool initializedDisplayPart3;
	
	float zoom;
	float contentHeight;
}

@end

@implementation MailTableCell

const int MailTableCell_ContentOffsetTop = 57;
const int MailTableCell_ContentOffsetBottom = 40;
const int MailTableCell_ContentNonContent = MailTableCell_ContentOffsetTop + MailTableCell_ContentOffsetBottom;

@synthesize delegate;
@synthesize fullView;
@synthesize fullAuthors;
@synthesize fullDate;
@synthesize fullRecipients;
@synthesize fullHtml;
@synthesize fullImages;
@synthesize fullActions;
@synthesize draftActions;

@synthesize touchArea;

- (id)initWithCoder:(NSCoder *)aDecoder
{
	self = [super initWithCoder:aDecoder];
	
	if (self)
	{
		self->watchingEvents = false;
		self->observeStack = 0;
		self->initialized = true;
		self->webViewWasInitialized = false;
		self->initializedDisplayPart3 = false;
	}
	
	return self;
}

- (void)dealloc
{
	[self stopObservingContentSizeChangesInWebView];
	LogDebug(ui::MailTableCell, "dealloc");
}

-(BOOL)webView:(UIWebView *)_webView shouldStartLoadWithRequest:(NSURLRequest *)request navigationType:(UIWebViewNavigationType)navigationType 
{
	LogDebug(ui::MailTableCell, "webView:shouldStartLoadWithRequest " << [[request URL] absoluteString].UTF8String << " " << (int)navigationType);

	NSString *url = [[request URL] absoluteString];
	static NSString *urlPrefix = @"native://";

    if ([url hasPrefix:urlPrefix]) 	
	{
		[self recalculateWebViewSizeAndZoom];
		return NO;
	}
	
	if (navigationType == UIWebViewNavigationTypeLinkClicked) 
	{
		[[UIApplication sharedApplication] openURL:[request URL]];
		return NO;
	}
	return YES;
}

- (void) layoutSubviews
{
	LogDebug(ui::MailTableCell, "layoutSubviews before super: sf.size.height " << self.frame.size.height << " sf.size.width " << self.frame.size.width);

	CGRect sf = self.frame;
	CGRect vf = fullHtml.frame;

	vf.origin.y = MailTableCell_ContentOffsetTop;
	vf.size.height = sf.size.height - (MailTableCell_ContentNonContent);
	fullHtml.frame = vf;
	
	[super layoutSubviews];
}

- (void)onHeightChange:(int)height
{
	if (height != data->height)
	{
		LogDebug(ui::MailTableCell, "propagating height change " << height);
		data->height = height;			

		ConversationViewController *parent = (ConversationViewController *)delegate;
//		[parent performSelectorOnMainThread:@selector(onHeightChange:) withObject:self waitUntilDone:false];
		[parent onHeightChange:self];
	}
}

- (void)onContentHeightChange
{
	LogDebug(ui::MailTableCell, "calculated final height " << (contentHeight + MailTableCell_ContentNonContent));
	
	[self onHeightChange:(contentHeight + MailTableCell_ContentNonContent)];
}

- (void)recalculateWebViewSizeAndZoom
{
//	[self performSelectorOnMainThread:@selector(doRecalculateWebViewSizeAndZoom) withObject:nil waitUntilDone:false];
	[self doRecalculateWebViewSizeAndZoom];
}

- (void)doRecalculateWebViewSizeAndZoom
{
	// have the webview calculate the new height
	CGRect webViewFrame = fullHtml.frame;
	float oldHeight= webViewFrame.size.height;
		
	webViewFrame.size.height = 1;
	fullHtml.frame = webViewFrame;
	CGSize fittingSize = [fullHtml sizeThatFits:CGSizeZero];
	webViewFrame.size.height = oldHeight;
	fullHtml.frame = webViewFrame;	
	
	float height =
		fittingSize.height
//		(int)webView.scrollView.contentSize.height
//		[[webView stringByEvaluatingJavaScriptFromString:@"document.body.scrollHeight"] integerValue]
//		[[webView stringByEvaluatingJavaScriptFromString:@"document.body.offsetHeight"] integerValue]
//		[[webView stringByEvaluatingJavaScriptFromString:@"document.getElementById('__fakeBody__').clientHeight"] integerValue]
	;

	float width =
		getWebViewScrollView(fullHtml).contentSize.width
//		[[webView stringByEvaluatingJavaScriptFromString:@"document.body.scrollWidth"] integerValue]
//		[[webView stringByEvaluatingJavaScriptFromString:@"document.getElementById('__fakeBody__').clientWidth"] integerValue]
	;	
	
	float webViewWidth = webViewFrame.size.width;
	LogDebug (ui::MailTableCell, "width " << width << " height " << height);

	if (width > webViewWidth)
	{
		zoom = webViewWidth / width;
		height = zoom * height;
		width = webViewWidth;

		LogDebug (ui::MailTableCell, "webViewDidFinishLoad zooming to " << zoom);
	}
	else
	{
		zoom = 1.0f;
	}
	
	contentHeight = height; 
	[self hackSetWebViewZoom];
	[self onContentHeightChange];
}

- (void)hackClearWebView
{
	LogDebug(ui::MailTableCell, "hackClearWebView");

//	[[NSURLCache sharedURLCache] removeAllCachedResponses];
//	[webView stringByEvaluatingJavaScriptFromString:@"var body=document.getElementsByTagName('body')[0];body.style.backgroundColor=(body.style.backgroundColor=='')?'white':'';"];
//	[webView stringByEvaluatingJavaScriptFromString:@"document.open();document.close();"];
//	[html stringByEvaluatingJavaScriptFromString:@"document.body.innerHTML = \"\";"];
}

- (void)initializeDisplayPart1
{
	fullAuthors.textColor = getColorForIndex(data->color);
	fullAuthors.text = data->author;
	fullRecipients.text = data->recipients;
	fullDate.text = data->date;
	[fullImages initToggle];
	[fullImages setState:false];
}

- (void)initializeDisplayPart2
{
	NSString *string = nil;
	
	bool isHtml = false;
	if (data->html)
	{
		isHtml = true;
		NSString *str = data->html;
		NSRegularExpression *regex = 
			[NSRegularExpression regularExpressionWithPattern:
				@"(<[\\s\\S]*?img[\\s\\S]*?)src=([\\s\\S]*?>)" options:0 error:NULL];
				
		string = [regex 
			stringByReplacingMatchesInString:str 
			options:0 
			range:NSMakeRange(0, [str length])
			withTemplate:@"$1src='http://localhost/unloadable' fakesrc='http://localhost/unloadable' realsrc=$2"];
	}
	else
	if (data->text)
	{
		string = data->text;
	}

	if (string)
	{
		int deviceWidth = fullHtml.frame.size.width;
		LogDebug(ui::MailTableCell::deviceWidth, "using deviceWidth " << deviceWidth);
		
		bool isPhone = ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone);
		
		NSString *html = 
			[NSString stringWithFormat:
				@"<html>"
				"<head>"
				"	<meta name='viewport' id='meta_viewport' content='width=%d, user-scalable=yes'/>"
				"	<style type=\"text/css\">"
				"		body { padding: 0px; margin: 0px; font-family: 'helvetica'; font-size: %dpx; word-wrap:break-word; padding:0px; margin:0px; }"
//					"		#__fakeBody__ { margin: 0px; padding:0px; }" 
				"		blockquote { display:none; margin:0; padding:0; }"
				"		.__showQuote__ { background-color: #FCFCFC; display: inline-block; border: 1px dotted; }"
				"	</style>"
				"	<script>"
				"		function __doPropagateHeightChange__() { location.href = 'native://event=onHeightChange'; }"
				"		function __propagateHeightChange__() { window.setTimeout(__doPropagateHeightChange__,10); }"
				"		function __enableImages__(v) {"
				"			for(var i=0; i<document.images.length; ++i) {"
				"				var image = document.images[i];"
				"				image.src = v ? image.attributes.realsrc.nodeValue : image.attributes.fakesrc.nodeValue;"
				"			}"
				"		}"
				"	</script>"
				"</head>"
				"<body>"
//					"<div id='__fakeBody__'>"
				"%@"
//					"</div>"
				"</body>"
				"</html>", 
				
				(int)deviceWidth,
				isPhone ? 14 : 9,
				string
		];
		
		LogDebug (ui::MailTableCell::detail, html.UTF8String);
		
		[self hackDoWebView:html];
//		[self hackDoWebViewAfterAMoment:html];
	}
}

- (void)webViewDidFinishLoad:(UIWebView *)webView
{
	LogDebug(ui::MailTableCell, "webViewDidFinishLoad");
	
	if (webViewWasInitialized)
	{
		[self recalculateWebViewSizeAndZoom];
		
		if (!initializedDisplayPart3)
			[self initializeDisplayPart3];
	}	
}

- (void)initializeDisplayPart3
{
	fullActions.hidden = data->draft;
	draftActions.hidden = !data->draft;
}

- (void)initializeDisplay
{
	LogDebug (ui::MailTableCell, "initializeDisplay");

	if (data->isLoaded())
	{
		LogDebug (ui::MailTableCell, "data->isLoaded()");
		if (!initialized || data->dirty)
		{
			LogDebug (ui::MailTableCell, "!initialized || data->dirty");
			data->dirty = false;
			initialized = true;
			
			[self initializeDisplayPart1];
			[self initializeDisplayPart2];
		}
	}
}

- (void)onRedisplay
{
	[self initializeDisplay];
}

- (IBAction)onFullImages:(id)sender 
{
	UIToggleButton *button = sender;
	int value = [button toggle] ? 1 : 0;
	[fullHtml stringByEvaluatingJavaScriptFromString:[NSString stringWithFormat:@"__enableImages__(%d);",value]];
}

- (void)onData:(MailData *)_data 
{
	[self stopObservingContentSizeChangesInWebView];
	
	if (data != _data)
	{
		data = _data;
		zoom = 1.0;
		contentHeight = 0;
		initialized = false;
		webViewWasInitialized = false;
		initializedDisplayPart3 = false;

		[self initializeDisplay];
	}

//	[self startObservingContentSizeChangesInWebView];
}

-(void)touchesBegan:(NSSet*)touches withEvent:(UIEvent*)event
{
	UITouch *touch = [touches anyObject];

	if(touch.view == touchArea)
	{
		data->displayMode = MailData::SHORT;
		
		ConversationViewController *_delegate = (ConversationViewController *)delegate;
		[_delegate onDisplayModeChanged:self];
	}
}

#pragma mark -
#pragma mark hacks

- (void)hackSetWebViewZoom
{
	UIScrollView *sv = getWebViewScrollView(fullHtml);
	sv.bounces = NO;
	
	LogDebug (ui::MailTableCell, "setWebView scale " << sv.zoomScale << " -> " << zoom);
	if (sv.zoomScale != zoom)
		[sv setZoomScale:zoom animated:FALSE];
	
	[sv setNeedsDisplay];
	[sv setNeedsLayout];
	[fullHtml setNeedsLayout];
}

- (void)hackDoWebViewAfterAMoment:(NSString *)html
{
	[self performSelector:@selector(hackDoWebViewOnMainThread:) withObject:html afterDelay:5.0];
}

- (void)hackDoWebViewOnMainThread:(NSString *)html
{
	[self performSelectorOnMainThread:@selector(hackDoWebView:) withObject:html waitUntilDone:NO];
}

- (void)hackDoWebView:(NSString *)html
{
	webViewWasInitialized = true;
	[fullHtml loadHTMLString:html baseURL:nil];
}

#pragma mark scrollview events

// http://stackoverflow.com/questions/13295762/detect-changes-to-uiwebviews-scroll-views-contentsize

- (void)startObservingContentSizeChangesInWebView {
	if (!watchingEvents)
		[getWebViewScrollView(fullHtml) addObserver:self forKeyPath:@"contentSize" options:0 context:&kObservingContentSizeChangesContext];
		
	watchingEvents = true;
}

- (void)stopObservingContentSizeChangesInWebView {
	if (watchingEvents)
		[getWebViewScrollView(fullHtml) removeObserver:self forKeyPath:@"contentSize" context:&kObservingContentSizeChangesContext];
		
	watchingEvents = false;
}

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary *)change context:(void *)context 
{
    if (context == &kObservingContentSizeChangesContext) {
        UIScrollView *scrollView = object;
        NSLog(@"%@ contentSize changed to %@", scrollView, NSStringFromCGSize(scrollView.contentSize));
		
		if (observeStack)
			return;
			
		observeStack ++;
		[self onScrollHeightChange:scrollView.contentSize.height];
		observeStack --;
    } else {
        [super observeValueForKeyPath:keyPath ofObject:object change:change context:context];
    }
}

- (void)onScrollHeightChange:(float)height
{
	LogDebug (ui::MailTableCell, "onScrollHeightChange " << zoom << " scale " << getWebViewScrollView(fullHtml).zoomScale << " " << height);
	
	CGRect webViewFrame = fullHtml.frame;
	float oldHeight= webViewFrame.size.height;
		
	webViewFrame.size.height = 1;
	fullHtml.frame = webViewFrame;
	CGSize fittingSize = [fullHtml sizeThatFits:CGSizeZero];
	webViewFrame.size.height = oldHeight;
	fullHtml.frame = webViewFrame;	
	
	contentHeight = fittingSize.height;

	[self onContentHeightChange];
}

@end

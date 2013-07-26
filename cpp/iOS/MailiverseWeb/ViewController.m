//
//  ViewController.m
//  Mailiverse
//
//  Created by Timothy Prepscius on 1/23/13.
//  Copyright (c) 2013 __MyCompanyName__. All rights reserved.
//

#import "ViewController.h"
#import "NativeMethods.h"

@interface ViewController ()

@end

@implementation ViewController
@synthesize web;

bool alreadyStarted = true;

- (void)signalNativeAvailable
{
	NSString *string = [web stringByEvaluatingJavaScriptFromString:@"__ios_start__()"];
	if ([string isEqualToString:@"Yes"])
	{
		alreadyStarted = true;
	}
	else 
	{
		[self signalNativeAvailableDelay];
	}
}

- (void)signalNativeAvailableDelay
{
	[self performSelector:@selector(signalNativeAvailable) withObject:nil afterDelay:0.25];
}

- (NSString *)readRequest
{
	NSString *data = 
		[web stringByEvaluatingJavaScriptFromString:@"window.mNative.readRequest()"];
		
	if ([data hasPrefix:@"-"])
		return nil;
		
	return [data substringFromIndex:1];
}

- (void)webViewDidFinishLoad:(UIWebView *)webView
{
	if (alreadyStarted)
		return;
		
	[self performSelectorOnMainThread:@selector(signalNativeAvailableDelay) withObject:nil waitUntilDone:FALSE];

	alreadyStarted = true;
}

- (void)writeResponse:(NSString *)response
{
//	NSLog(@"writeResponse %d", [response length]);
	NSString *js = [NSString stringWithFormat:@"window.mNative.writeResponse('%@')",response];

	[web stringByEvaluatingJavaScriptFromString:js];
}

- (void)processRequest:(NSString *)request
{
	NSLog(@"%p processRequest %d", request, [request length]);

	NSError *error = nil;
	NSData *requestData = [request dataUsingEncoding:NSUTF8StringEncoding];
	NSDictionary *requestDictionary = 
		[NSJSONSerialization 
			JSONObjectWithData:requestData
			options: NSJSONReadingMutableContainers 
			error: &error
			];
	
	if (!requestDictionary)
	{
		NSLog(@"%p Malformed request %@", request, request);
		return;
	}

	NSString *cmd = [requestDictionary objectForKey:@"cmd"];
	NSArray *args = [requestDictionary objectForKey:@"args"];
	NSNumber *callback = [requestDictionary objectForKey:@"callback"];
	
	id result = [NativeMethods processRequest:cmd withArgs:args];
	
	if (result == nil)
		return;

	NSDictionary *responseDictionary = 
		[NSMutableDictionary 
			dictionaryWithObjectsAndKeys:result,@"result",callback,@"callback",requestDictionary,@"original",nil];
			
	NSData *responseData = [NSJSONSerialization dataWithJSONObject:responseDictionary options:0 error:&error];
	NSString *response = [[NSString alloc] initWithData:responseData encoding:NSUTF8StringEncoding];
	
//	NSLog(@"%p finished %d",request,[response length]);
	
	[self performSelectorOnMainThread:@selector(writeResponse:) withObject:response waitUntilDone:FALSE];
}

- (void)queryRequest
{
	NSString *request = nil;
	
	while ((request = [self readRequest]) != nil)
	{
		[self performSelectorInBackground:@selector(processRequest:) withObject:request];
	}
}

- (BOOL)webView:(UIWebView *)webView shouldStartLoadWithRequest:(NSURLRequest *)request navigationType:(UIWebViewNavigationType)navigationType {
    NSString *url = [[request URL] absoluteString];
	
	NSString *suffixes[] = { @"/mobile_large.html", @"/mobile_medium.html", @"/mobile_small.html", nil };
	
	int i=0;
	while (suffixes[i] != nil)
	{
		if ([url hasSuffix:suffixes[i]])
			alreadyStarted = false;
			
		i++;
	}
	

    static NSString *urlPrefix = @"native://";
	NSLog(@"shouldStartLoadWithRequest %@", url);

    if ([url hasPrefix:urlPrefix]) {
        NSString *paramsString = [url substringFromIndex:[urlPrefix length]];
        NSArray *paramsArray = [paramsString componentsSeparatedByString:@"&"];
        int paramsAmount = [paramsArray count];

        for (int i = 0; i < paramsAmount; i++) {
            NSArray *keyValuePair = [[paramsArray objectAtIndex:i] componentsSeparatedByString:@"="];
            NSString *key = [keyValuePair objectAtIndex:0];
            NSString *value = nil;
            if ([keyValuePair count] > 1) {
                value = [keyValuePair objectAtIndex:1];
            }

            if (key && [key length] > 0) {
                if (value && [value length] > 0) {
                    if ([key isEqualToString:@"event"]) {
						if ([value isEqualToString:@"onRequest"])
						{
							[self performSelector:@selector(queryRequest) withObject:nil afterDelay:0.01];
						}
                    }
                }
            }
        }

        return NO;
    }
    else {
        return YES;
    }
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	
	[[NSURLCache sharedURLCache] removeAllCachedResponses];
//	[NSClassFromString(@"WebView") performSelector:@selector(_enableRemoteInspector)];
	
	NSString *urlAddress = @"http://white:8000/mobile_large.html";
//	NSString *urlAddress = @"http://websocketstest.com";
	
	//Create a URL object.
	NSURL *url = [NSURL URLWithString:urlAddress];

	//URL Requst Object
	NSURLRequest *request = [NSURLRequest requestWithURL:url];

	[web loadRequest:request];
}

- (void)viewDidUnload
{
	[self setWeb:nil];
    [super viewDidUnload];
    // Release any retained subviews of the main view.
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
	if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) {
	    return (interfaceOrientation != UIInterfaceOrientationPortraitUpsideDown);
	} else {
	    return YES;
	}
}

@end

/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#import "AppDelegate.h"

#import "LoginViewController.h"

#include "mailiverse/Lib.h"
#include "MasterSingleton.h"

Botan::LibraryInitializer *init = 0;

using namespace mailiverse::mail;

@interface AppDelegate() {
	UIAlertView *newMailAlert;
}

@end

@implementation AppDelegate
@synthesize deviceToken;
@synthesize navigationController;
@synthesize folderViewController;
@synthesize revealFoldersViewController;
@synthesize conversationViewController;
@synthesize sidePanelController;
@synthesize loginViewController;
@synthesize rootViewController;

@synthesize window;

- (void)onLogin
{
	if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) 
	{
		[self instantiatePhoneControllers];
		[revealFoldersViewController onStart];
		[folderViewController onStart];

		[self.navigationController pushViewController:sidePanelController animated:TRUE];
	}
	else
	{
		[revealFoldersViewController onStart];
		[folderViewController onStart];
		[loginViewController dismissModalViewControllerAnimated:YES];
		loginViewController = nil;
	}
}

- (void)onLogout:(BOOL)animated
{
	// this is sort of a dirty hack, just need to wait long enough for the enable device notifications to start executing
	[self performSelector:@selector(doLogout:) withObject:[NSNumber numberWithBool:animated] afterDelay:0.5f];
	__onEnableDeviceNotifications__(false);
}

- (void)doLogout:(NSNumber *)_animated
{
	bool animated = [_animated boolValue];
	[conversationViewController onStop];
	[folderViewController onStop];
	[revealFoldersViewController onStop];
	[sidePanelController showCenterPanelAnimated:FALSE];
	masterSingleton = NULL;	
	
//	AppDelegate *appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
//	UIWindow *window = appDelegate.window;
//	[window addSubview:logoutView];

	if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) 
	{
		AppDelegate *appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
		[appDelegate.navigationController popViewControllerAnimated:TRUE];
	}
	else
	{
		loginViewController = [LoginViewController instantiate];
		loginViewController.modalTransitionStyle = UIModalTransitionStyleCrossDissolve;		
		loginViewController.folderViewController = folderViewController;
		loginViewController.revealFoldersViewController = revealFoldersViewController;
		
		[rootViewController presentModalViewController:loginViewController animated:animated];
	}

	SMARTPTR_DEBUG();
	
}

- (void)instantiateLoginPhoneControllers
{
	loginViewController = [LoginViewController instantiate];
	navigationController = [[UINavigationController alloc] initWithRootViewController:loginViewController];
	rootViewController = navigationController;
}

- (void)instantiatePhoneControllers
{
	// folders and reveal
	folderViewController = [FolderViewController instantiate];
	revealFoldersViewController = [RevealFoldersViewController instantiate];
	revealFoldersViewController.folderViewController = folderViewController;

	// side
	sidePanelController = [[JASidePanelController alloc] init];
	sidePanelController.leftPanel = revealFoldersViewController;
	sidePanelController.centerPanel = [[UINavigationController alloc] initWithRootViewController:folderViewController];
	sidePanelController.rightPanel = nil;
}

- (void)instantiatePadControllers
{
	folderViewController = [FolderViewController instantiate];
	conversationViewController = [ConversationViewController instantiate];
	revealFoldersViewController = [RevealFoldersViewController instantiate];

	// link them so they can "start" each other
	folderViewController.conversationViewController = conversationViewController;
	revealFoldersViewController.folderViewController = folderViewController;

	// make the navigation view controllers
	UINavigationController *folderNavigationViewController = 
		[[UINavigationController alloc] initWithRootViewController:folderViewController];

	UINavigationController *conversationNavigationViewController = 
		[[UINavigationController alloc] initWithRootViewController:conversationViewController];
	
	// setup split
	UISplitViewController *splitViewController = [[UISplitViewController alloc] init];
	
	splitViewController.delegate = conversationViewController;
	splitViewController.viewControllers = 
		[NSArray arrayWithObjects:folderNavigationViewController, conversationNavigationViewController, nil];

	// setup side panel
	sidePanelController = [[JASidePanelController alloc] init];
	sidePanelController.leftPanel = revealFoldersViewController;
	sidePanelController.centerPanel = splitViewController;
	sidePanelController.rightPanel = nil;
	
	rootViewController = sidePanelController;
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions
{
	StartLogging();
	ActivateLog(*);
	ActivateLog(ui::MailTableCell);
	ActivateLog(ui::ConversationViewController);
	ActivateLog(ui::UINetworkActivityWrapper);
//	ActivateLog(mailiverse::mail::model::Body::encapsulateHTML);
//	ActivateLog(mailiverse::mail::manager::FolderQuery);
//	ActivateLog(ui::FolderViewController);
//	ActivateLog(mailiverse::mail::serializers::JsonSerializer*);
//	ActivateLog(mailiverse::mail::cache::StoreLibrary);
//	ActivateLog(ui::*);
	
	init = new Botan::LibraryInitializer("thread_safe");
    self.window = [[UIWindow alloc] initWithFrame:[[UIScreen mainScreen] bounds]];
	
	if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) 
	{
		[self instantiateLoginPhoneControllers];
		self.window.rootViewController = rootViewController;
		[self.window makeKeyAndVisible];
	}
	else
	{
		[self instantiatePadControllers];
		self.window.rootViewController = rootViewController;
		[self.window makeKeyAndVisible];
		
		[self doLogout:FALSE];
	}


    return YES;
}

- (void)applicationWillResignActive:(UIApplication *)application
{
	// Sent when the application is about to move from active to inactive state. This can occur for certain types of temporary interruptions (such as an incoming phone call or SMS message) or when the user quits the application and it begins the transition to the background state.
	// Use this method to pause ongoing tasks, disable timers, and throttle down OpenGL ES frame rates. Games should use this method to pause the game.
}

- (void)applicationDidEnterBackground:(UIApplication *)application
{
	// Use this method to release shared resources, save user data, invalidate timers, and store enough application state information to restore your application to its current state in case it is terminated later. 
	// If your application supports background execution, this method is called instead of applicationWillTerminate: when the user quits.
}

- (void)applicationWillEnterForeground:(UIApplication *)application
{
	// Called as part of the transition from the background to the inactive state; here you can undo many of the changes made on entering the background.
}

- (void)applicationDidBecomeActive:(UIApplication *)application
{
	// Restart any tasks that were paused (or not yet started) while the application was inactive. If the application was previously in the background, optionally refresh the user interface.
}

- (void)applicationWillTerminate:(UIApplication *)application
{
	// Called when the application is about to terminate. Save data if appropriate. See also applicationDidEnterBackground:.
}

- (void)onEnableNotifications:(bool)value 
{
	if (value)
	{
		[[UIApplication sharedApplication] registerForRemoteNotificationTypes:(UIRemoteNotificationType) (UIRemoteNotificationTypeAlert | UIRemoteNotificationTypeBadge | UIRemoteNotificationTypeSound)];
	}
	else
	{
		if (masterSingleton)
			masterSingleton->getActions()->enableNotifications();
			
//		[[UIApplication sharedApplication] unregisterForRemoteNotifications];
	}
}

- (void)application:(UIApplication*)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData*)pDeviceToken
{
	NSLog(@"My token is: %@", pDeviceToken);
	self.deviceToken = [NSString stringWithFormat:@"%@",pDeviceToken];

	if (masterSingleton)
		masterSingleton->getActions()->enableNotifications(self.deviceToken.UTF8String);
}
 
- (void)application:(UIApplication*)application didFailToRegisterForRemoteNotificationsWithError:(NSError*)error
{
	NSLog(@"Failed to get token, error: %@", error);
}

void __onEnableDeviceNotifications__(bool notifications)
{
	AppDelegate *appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];  
	[appDelegate onEnableNotifications:notifications];
}

- (void)application:(UIApplication *)application didReceiveRemoteNotification:(NSDictionary *)userInfo 
{    
	UIApplicationState state = [application applicationState];
	if (state == UIApplicationStateActive) 
	{
		NSDictionary *alert = [[userInfo valueForKey:@"aps"] valueForKey:@"alert"];
		if (!alert)
			return;
			
		NSArray *values = [alert valueForKey:@"loc-args"];
		if (!values)
			return;
			
		std::list<std::string> _values;
		_values.push_back("New mail has arrived:");
		for (id v in values)
		{
			NSString *s = v;
			_values.push_back(s.UTF8String);
		}
			
		NSString *message = toNSString(mailiverse::utilities::join(_values, "\n"));

		[self showOnNewMailAlert:message];
	} else {
		//Do stuff that you would do if the application was not active
	}
}

- (void)showOnNewMailAlert:(NSString *)message
{
	[self closeOnNewMailAlert];
	newMailAlert = [[UIAlertView alloc] init];
	UILabel *lblText = [[UILabel alloc] initWithFrame:CGRectMake(0, 30, 300, 66)];
	lblText.text = message;
	lblText.font = [UIFont systemFontOfSize:15.0f];
	lblText.numberOfLines = 3;
	lblText.textAlignment = (UITextAlignment)UITextAlignmentCenter;
	lblText.backgroundColor = [UIColor clearColor];
	lblText.textColor = [UIColor whiteColor];
	lblText.center = CGPointMake(140, 30);
	[newMailAlert addSubview:lblText];
	[newMailAlert show];
	
	[NSTimer scheduledTimerWithTimeInterval:1.0 target:self selector:@selector(closeOnNewMailAlert) userInfo:nil repeats:NO];	
}

-(void)closeOnNewMailAlert {
	if (newMailAlert)
		[newMailAlert dismissWithClickedButtonIndex:0 animated:YES];
		
	newMailAlert = nil;
}

@end

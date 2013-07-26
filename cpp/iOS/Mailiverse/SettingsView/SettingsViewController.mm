/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */


#import "SettingsViewController.h"
#include "AppDelegate.h"
#include "MasterSingleton.h"
#include "mailiverse/Lib.h"
#include "MMUtilities.h"

using namespace mailiverse::mail;
using namespace mailiverse::core::constants;
using namespace mailiverse;

@interface SettingsViewController ()

@end

@implementation SettingsViewController
@synthesize enableNotifications;
@synthesize fullName;
@synthesize detailedNotifications;
@synthesize signature;

+ (id)instantiate
{
	return 
		[[UINavigationController alloc] initWithRootViewController:
			[[SettingsViewController alloc] initWithNibName:@"SettingsViewController" bundle:nil]
		];
}

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];

	[self hackAddNavigationBarItems];
	[self hackToolbarColor];

	model::Settings *settings = masterSingleton->getCacheManager()->getSettings();
	fullName.text = toNSString(settings->get(ConstantsSettings::USERNAME, ""));
	signature.text = toNSString(settings->get(ConstantsSettings::SIGNATURE,""));
		
	[enableNotifications initToggle];
	[detailedNotifications initToggle];
	
	String notificationType = settings->get(ConstantsSettings::NOTIFICATION_TYPE, ConstantsPushNotifications::NOTIFICATION_TYPE_NONE);
	bool notifications = notificationType != ConstantsPushNotifications::NOTIFICATION_TYPE_NONE;
	bool details = notificationType == ConstantsPushNotifications::NOTIFICATION_TYPE_LONG;
	
	[enableNotifications setState:notifications];
	[detailedNotifications setState:details];
	
	detailedNotifications.enabled = notifications;
	detailedNotifications.alpha = notifications ? 1.0 : 0.4;	
    // Do any additional setup after loading the view from its nib.
}

- (void)viewDidUnload
{
	[self setEnableNotifications:nil];
	[self setFullName:nil];
	[self setDetailedNotifications:nil];
    [self setSignature:nil];
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
	return YES;
}

#pragma mark - 
#pragma mark editing

- (IBAction)onBackgroundTouched:(id)sender
{
	[self.view endEditing:YES];
}

#pragma mark -
#pragma mark events


- (IBAction)onSave:(id)sender 
{
	bool notifications = [enableNotifications getState];
	bool details = [detailedNotifications getState];

	model::Settings *settings = masterSingleton->getCacheManager()->getSettings();
	settings->set(ConstantsSettings::USERNAME, fullName.text.UTF8String);
	settings->set(ConstantsSettings::SIGNATURE, signature.text.UTF8String);

	settings->set(ConstantsSettings::NOTIFICATION_TYPE, 
		notifications ? 
			(details ? 
				ConstantsPushNotifications::NOTIFICATION_TYPE_LONG:
				ConstantsPushNotifications::NOTIFICATION_TYPE_SHORT) :
				
			ConstantsPushNotifications::NOTIFICATION_TYPE_NONE
	);
		
	settings->markDirty();
	
	__onEnableDeviceNotifications__(notifications);
	[self dismissModalViewControllerAnimated:TRUE];
}

- (IBAction)onCancel:(id)sender 
{
	[self dismissModalViewControllerAnimated:TRUE];
}

- (IBAction)onToggle:(id)sender 
{
	UIToggleButton *button = sender;
	bool state = [button toggle];
	
	if (button == enableNotifications)
	{
		[detailedNotifications setState:false];
		
		detailedNotifications.enabled = state;
		detailedNotifications.alpha = state ? 1.0 : 0.4;	
	}
}

#pragma mark -
#pragma mark hacks

- (void)hackToolbarColor
{
	self.navigationController.navigationBar.tintColor = [UIColor blackColor];
}

- (void)hackAddNavigationBarItems
{
	UIBarButtonItem *close = 
		[[UIBarButtonItem alloc] 
			initWithTitle:@"Cancel" 
			style:UIBarButtonItemStylePlain 
			target:self 
			action:@selector(onCancel:)
		];
		

	
	UIBarButtonItem *save = 
		[[UIBarButtonItem alloc] 
			initWithTitle:@"Save" 
			style:UIBarButtonItemStylePlain 
			target:self 
			action:@selector(onSave:)
		];

	self.navigationItem.leftBarButtonItem = close;
	self.navigationItem.rightBarButtonItem = save;
}

@end

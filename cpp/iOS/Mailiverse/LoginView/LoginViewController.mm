/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */


#import "LoginViewController.h"
#import "AppDelegate.h"
#import "FolderviewController.h"
#import "RevealFoldersViewController.h"
#import "JASidePanelController.h"

#include "mailiverse/mail/manager/Master.h"
#include "mailiverse/client/Authenticator.h"
#include "mailiverse/utilities/Log.h"
#include "mailiverse/core/constants/ConstantsClient.h"
#include "MasterSingleton.h"
#include "MMPool.h"

#include "math.h"

using namespace mailiverse::mail::manager;
using namespace mailiverse::core::store;
using namespace mailiverse::core::constants;
using namespace mailiverse::client;
using namespace mailiverse;


//-----------------------------------------------------------

@interface EnvironmentHolder : NSObject {

@public
	EnvironmentPtr environment;
}
@end

@implementation EnvironmentHolder

- (id)init:(Environment *)_environment
{
	if (self = [super init])
	{
		self->environment = _environment;
	}
	
	return self;
}

@end

//-----------------------------------------------------------

class AuthenticatorDelegate : public Authenticator::Delegate
{
	__weakptr LoginViewController *controller;
	
public:
	AuthenticatorDelegate (LoginViewController *_controller) :
		controller(_controller)
	{ }

	virtual void onStep(const std::string &what)
	{
		POOL_START
		[controller performSelectorOnMainThread:@selector(onStep:) withObject:[NSString stringWithUTF8String:what.c_str()] waitUntilDone:FALSE];
		POOL_STOP
	}

	virtual void onSuccess(EnvironmentPtr _environment) throws_ (Exception)
	{
		POOL_START
		EnvironmentHolder *holder = [[EnvironmentHolder alloc] init:(Environment *)_environment];
		[controller performSelectorOnMainThread:@selector(onSuccess:) withObject:holder waitUntilDone:FALSE];
		POOL_STOP
	}

	virtual void onFailure(const Exception &e) throws_ (Exception)
	{
		POOL_START
		[controller performSelectorOnMainThread:@selector(onFailure:) withObject:[NSString stringWithUTF8String:e.what().c_str()] waitUntilDone:FALSE];
		POOL_STOP
	}
} ;

//-----------------------------------------------------------


@interface LoginViewController () {
	AuthenticatorPtr auth;
	
	UIGestureRecognizer *hackDismissKeyboard;
}
@end

@implementation LoginViewController

@synthesize folderViewController;
@synthesize revealFoldersViewController;
@synthesize passwordField;
@synthesize nameField;
@synthesize loginButton;
@synthesize statusLabel;
@synthesize backgroundImage;
@synthesize backgroundImageTop;

+ (id)instantiate
{
	if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) 
	{
		return [[LoginViewController alloc] initWithNibName:@"LoginViewController_iPhone" bundle:nil];
	} 
	else 
	{
		return [[LoginViewController alloc] initWithNibName:@"LoginViewController_iPad" bundle:nil];
	}
}

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
	LogDebug(ui::LoginViewController, "init");

    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
		self.title = @"";
        // Custom initialization
    }
    return self;
}

- (void)dealloc
{
	LogDebug(ui::LoginViewController, "dealloc");
}

- (void)rotateImage:(UIImageView *)image rate:(float)rate offset:(float)offset
{
	CABasicAnimation* animation = [CABasicAnimation animationWithKeyPath:@"transform.rotation.z"];
	animation.fromValue = [NSNumber numberWithFloat:offset];
	animation.toValue = [NSNumber numberWithFloat: (2*M_PI + offset)];
	animation.duration = 120.0f / rate;             // this might be too fast
	animation.repeatCount = HUGE_VALF;     // HUGE_VALF is defined in math.h so import it
	[image.layer addAnimation:animation forKey:@"rotation"];
}

- (void)viewDidLoad
{
    [self.navigationController setNavigationBarHidden:YES animated:FALSE];
    [super viewDidLoad];	
}

- (IBAction)onBackgroundTouched:(id)sender;
{
	[self.view endEditing:YES];
}

- (void) viewWillAppear:(BOOL)animated
{
    [super viewWillAppear:animated];

	nameField.text = @"";
	passwordField.text = @"";
	
#ifdef _DEBUG
	nameField.text = @"YOUR";
	passwordField.text = @"TEST";
#endif	

	statusLabel.text = nil;

	[self rotateImage:backgroundImage rate:1.0f offset:0.0];
	[self rotateImage:backgroundImageTop rate:0.5f offset:0.5f];
}

- (void) viewWillDisappear:(BOOL)animated
{
    [super viewWillDisappear:animated];
}

- (void)viewDidUnload
{
    [self setPasswordField:nil];
    [self setNameField:nil];
    [self setLoginButton:nil];
    [self setStatusLabel:nil];
    [self setBackgroundImage:nil];
	[self setBackgroundImageTop:nil];
    [super viewDidUnload];
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

//---------------------------------------------

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
//    return (interfaceOrientation == UIInterfaceOrientationPortrait);
	return YES;
}

//-------------------------------------------

- (void)onStep:(NSString *)status
{
	statusLabel.text = status;
}

- (void)onSuccess:(EnvironmentHolder *)holder
{
	masterSingleton = Master::create(nameField.text.UTF8String + String("@mailiverse.com"), holder->environment);
	
	AppDelegate *appDelegate = (AppDelegate *)[[UIApplication sharedApplication] delegate];
	[appDelegate onLogin];

	masterSingleton->getInitializer()->start();
	loginButton.enabled = TRUE;
	auth = NULL;
}

- (void)onFailure:(NSString *)reason
{
	statusLabel.text = @"Login failed";
	loginButton.enabled = TRUE;
	auth = NULL;
}

- (IBAction)onLoginButton:(id)sender 
{
	[self.passwordField resignFirstResponder];
	[self.nameField resignFirstResponder];
	
	[self performSelectorInBackground:@selector(login) withObject:nil];
	loginButton.enabled = FALSE;
}

- (IBAction)onExit:(id)sender 
{
	exit(0);
}

- (void)login
{
	POOL_START {
	
		std::string name = nameField.text.UTF8String;
		std::string password = passwordField.text.UTF8String;
	//	passwordField.text = @"";
		
		auth = new Authenticator();
		auth->setDelegate(new AuthenticatorDelegate(self));
		auth->authenticate(name + ConstantsClient::AT_HOST, password);
	
	} POOL_STOP
}

@end

/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#import "ComposeViewController.h"
#include "mailiverse/Lib.h"
#include "MMUtilities.h"
#include "MasterSingleton.h"
#include <algorithm>

#import "ComposeHeaderLineCell.h"

using namespace mailiverse::utilities;
using namespace mailiverse::mail::model;
using namespace mailiverse;

const int MODE_NORMAL = 0;
const int MODE_ADDRESS = 1;
const int SUBJECT_SECTION = 3;
const int BODY_SECTION = 4;

@interface ComposeViewController () 
{
	ConversationPtr conversation;
	MailPtr mail;

	int mode;
	Vector<Vector<String>> sections;
	Vector<String> sectionLabels;
	
	NSIndexPath *editPath;
	UITextField *editText;
}

@end

@implementation ComposeViewController
@synthesize loadedTableCell;
@synthesize body;
@synthesize bodyCell;
@synthesize tableFooter;
@synthesize autoCompleteTableView;
@synthesize autoCompleteTableCell;

+ (id)instantiate
{
	return [[ComposeViewController alloc] initWithNibName:@"ComposeViewController" bundle:nil];
}

- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
	LogDebug(ui::MailComposeViewController, "init");

    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) 
	{
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
	[self hackToolbarColor];

	const char* labels[] = { "To:", "Cc:", "Bcc:", "Subject:", "Body:" };
	for (auto &i : labels)
	{
		self->sectionLabels.add(i);

		Vector<String> text;
		text.push_back("");
		self->sections.push_back(text);
	}
	self->mode = MODE_NORMAL;
	
	self.autoCompleteTableView.autoCompleteDelegate = self;
	[self hackAddNavigationBarItems];
	self.tableView.tableFooterView = tableFooter;
}

- (void)viewDidUnload
{
	[self setLoadedTableCell:nil];
	[self setBody:nil];
	[self setBodyCell:nil];
	[self setAutoCompleteTableCell:nil];
	[self setAutoCompleteTableView:nil];
	[self setTableFooter:nil];
    [super viewDidUnload];
	
	self.autoCompleteTableView.autoCompleteDelegate = nil;
    // Release any retained subviews of the main view.
    // e.g. self.myOutlet = nil;
}

- (BOOL)shouldAutorotateToInterfaceOrientation:(UIInterfaceOrientation)interfaceOrientation
{
	if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) 
		return (interfaceOrientation == UIInterfaceOrientationPortrait);
		
	return YES;
}

#pragma mark - Table view data source

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
	bool isAutoComplete = mode == MODE_ADDRESS && (indexPath.section == editPath.section) && (indexPath.row == editPath.row+1);
	bool isBodyRow = mode == MODE_NORMAL && indexPath.section == BODY_SECTION;
	
	if (isAutoComplete)
	{
		return 
			([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone) ?
				165 :
				500;
	}
	else
	if (isBodyRow)
	{
		float rowHeight = 
			std::min(
				std::max(body.contentSize.height, 100.0f),
				2000.0f
			);
			
		body.scrollEnabled = rowHeight < body.contentSize.height;
			
		return rowHeight;
	}
	
	return 43;
}

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView
{
	if (mode == MODE_NORMAL)
		return sections.size();
		
	// mode == MODE_ADDRESS
	return editPath.section+1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section
{
	if (mode == MODE_NORMAL)
		return sections[section].size();
		
	// mode == MODE_ADDRESS
	if (section < editPath.section)
		return sections[section].size();
		
	return editPath.row+2;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath
{
	LogDebug (ui::ComposeViewController, "tableView:cellForRowAtIndexPath " << indexPath.section << " " << indexPath.row);

	bool isAutoComplete = mode == MODE_ADDRESS && (indexPath.section == editPath.section) && (indexPath.row == editPath.row+1);
	bool isBodyRow = mode == MODE_NORMAL && indexPath.section == BODY_SECTION;
	
	if (isBodyRow)
	{
		return bodyCell;
	}
	else
	if (isAutoComplete)
	{
		return autoCompleteTableCell;
	}
	else
	{
		LogDebug (ui::ComposeViewController, "cellForRowAtIndexPath " << indexPath.section << " " << indexPath.row << sections[indexPath.section][indexPath.row]);
	
		ComposeHeaderLineCell *cell = 
			(ComposeHeaderLineCell *)loadTableViewCell(
				self, tableView,
				@"ComposeHeaderLineCell", 
				^(){ id result = loadedTableCell; loadedTableCell = nil; return result; }
			);
			
		[cell 
			onData:(sections[indexPath.section][indexPath.row])
			withLabel:sectionLabels[indexPath.section]
			isFirst:indexPath.row==0
			isLast:(indexPath.row==sections[indexPath.section].size()-1)
			isSubject:(indexPath.section==SUBJECT_SECTION)
		];
		
		cell.tag = indexPath.section;
		
		return cell;
	}
}

#pragma mark - AutoCompleteEditing



- (BOOL)textField:(UITextField *)textField shouldChangeCharactersInRange:(NSRange)range replacementString:(NSString *)string
{
	if (textField.tag == SUBJECT_SECTION)
		return YES;

	if ([[textField.text substringWithRange:range] compare:@">"]==0)
	{
		textField.text = @"";
		
		if (mode == MODE_ADDRESS)
			[self performSelector:@selector(hackMoveEverythingBackDownAndStopAutoComplete:) withObject:textField afterDelay:0.1];
		else
			[self removeRowIfEmptyAndReload:textField];

		return NO;
	}

	return YES;
}

- (void)textFieldDidChange:(UITextField *)textField
{
	NSIndexPath *path = getIndexPathForView(self.tableView, textField);
	LogDebug (ui::MailComposeViewController, "textFieldDidChange " << path.section << " " << path.row << " " << textField.text.UTF8String);

	sections[path.section][path.row] = textField.text.UTF8String;

	if (path.section == SUBJECT_SECTION)
		return;

	ComposeHeaderLineCell *cell = (ComposeHeaderLineCell *)[self.tableView cellForRowAtIndexPath:editPath];
	[cell onIdentityChange];

	if (mode == MODE_ADDRESS)
		[self.autoCompleteTableView onKeyChange:textField.text];

	if (mode == MODE_NORMAL && !sections[path.section][path.row].empty())
		[self performSelector:@selector(hackMoveEverythingUpAndStartTheAutoComplete:) withObject:textField afterDelay:0.1];
}

- (void)hackMoveEverythingUpAndStartTheAutoComplete:(UITextField *)textField
{
	editPath = getIndexPathForView(self.tableView, textField);

	LogDebug (ui::MailComposeViewController, "hackMoveEverythingUpAndStartTheAutoComplete " << editPath.section << " " << editPath.row << textField.text.UTF8String);

	[autoCompleteTableView onKeyChange:textField.text];

	mode = MODE_ADDRESS;
	[self.tableView reloadData];
	
	// we set the editText after the reload, so it is current
	ComposeHeaderLineCell *cell = (ComposeHeaderLineCell *)[self.tableView cellForRowAtIndexPath:editPath];
	[cell onAutoComplete];
	
	editText = cell.text;
	[editText becomeFirstResponder];

	[self.tableView scrollToRowAtIndexPath:editPath atScrollPosition:UITableViewScrollPositionTop animated:YES];
	self.tableView.scrollEnabled = FALSE;
}

- (void)hackMoveEverythingBackDownAndStopAutoComplete:(UITextField *)textField
{
	LogDebug (ui::MailComposeViewController, "hackMoveEverythingBackDownAndStopAutoComplete " << textField.text.UTF8String);
	[self removeRowIfEmpty:textField];

	editPath = nil;
	editText = nil;
	mode = MODE_NORMAL;

	[self.tableView reloadData];

	NSIndexPath *top = [NSIndexPath indexPathForRow:0 inSection:0];
	self.tableView.scrollEnabled = TRUE;
	[self.tableView scrollToRowAtIndexPath:top atScrollPosition:UITableViewScrollPositionTop animated:NO];
}

- (void)removeRowIfEmptyAndReload:(UITextField *)textField
{
	[self removeRowIfEmpty:textField];	
	[self.tableView reloadData];
}

- (bool)removeRowIfEmpty:(UITextField *)textField
{
	NSIndexPath *path = getIndexPathForView(self.tableView, textField);
	LogDebug (ui::MailComposeViewController, "removeRowIfEmpty " << path.section << " " << path.row << textField.text.UTF8String);
	
	if (([textField.text length] == 0) && (sections[path.section].size() > 1))
	{
		sections[path.section].erase(sections[path.section].begin()+path.row);
		return true;
	}
	
	return false;
}

- (NSArray *)valuesFor:(NSString *)_key
{
	std::string key = _key.UTF8String;
	NSMutableArray *array = [[NSMutableArray alloc] init];

	Vector<Identity *> addresses = masterSingleton->getAddressBook()->findPossibilities(key, 100);

	for (auto i : addresses)
	{
		NSString *name = [NSString stringWithFormat:@"%s", i->getName().c_str()];
		NSString *email = [NSString stringWithFormat:@"%s", i->getEmail().c_str()];
		[array addObject:[NSMutableArray arrayWithObjects:name, email, nil]];
	}

	return array;
}

- (void) onSelected:(NSArray *)text
{
	editText.text = [NSString stringWithFormat:@"%@ <%@>", [text objectAtIndex:0], [text objectAtIndex:1]];
	[self textFieldDidChange:editText];
	[self performSelector:@selector(hackMoveEverythingBackDownAndStopAutoComplete:) withObject:editText afterDelay:0.1];
}

- (BOOL)textFieldShouldReturn:(UITextField *)textField
{
	if (mode == MODE_ADDRESS)
		[self performSelector:@selector(hackMoveEverythingBackDownAndStopAutoComplete:) withObject:textField afterDelay:0.1];
	else
		[self removeRowIfEmptyAndReload:textField];
	
	return TRUE;
}
#pragma mark - Editing

- (IBAction)onAdd:(id)sender
{
	UIButton *button = sender;
	NSIndexPath *path = getIndexPathForView(self.tableView, button);
	
	LogDebug (ui::MailComposeViewController, "onAdd " << path.section << " " << path.row);
	
	if (!sections[path.section].back().empty())
		sections[path.section].push_back("");
	
	[self.tableView reloadData];
}

- (void)textViewDidChange:(UITextView *)textView
{
	if (textView == body)
		[self bodyDidChange];
}

- (void)bodyDidChange
{
	[UIView setAnimationsEnabled:NO];
	[self.tableView beginUpdates];	
	[self.tableView endUpdates];
	[UIView setAnimationsEnabled:YES];	
}

#pragma mark - Data

- (Vector<String>)calculateSectionItems:(const IdentityList &)list
{
	Vector<String> items;
	for (auto i : list)
		items.push_back(i->getFull());

	return items;
}

- (void) onData:(Mail *)_mail withConversation:(Conversation *)_conversation
{
	mail = _mail;
	conversation = _conversation;

	if (mail)
	{
		Recipients *recipients = mail->getHeader()->getRecipients();
		sections[0] = [self calculateSectionItems:recipients->getTo()];
		sections[1] = [self calculateSectionItems:recipients->getCc()];
		sections[2] = [self calculateSectionItems:recipients->getBcc()];

		for (auto &i : sections)
			if (i.empty())
				i.push_back("");

		Vector<String> subject;
		if (mail->getHeader()->getSubject())
				subject.push_back(*mail->getHeader()->getSubject());

		sections[3] = subject;

		if (mail->getBody()->getText())
			body.text = [NSString stringWithUTF8String:mail->getBody()->getText()->c_str()];
	}
	else
	{
		BodyPtr synthBody = masterSingleton->getActions()->calculateSignaturedBody("");
		body.text = toNSString(*synthBody->getText());
	}
}

#pragma mark - 
#pragma mark actions

- (void)dismiss
{
	if ([[UIDevice currentDevice] userInterfaceIdiom] == UIUserInterfaceIdiomPhone)
	{
		[self.navigationController popViewControllerAnimated:TRUE];
	}
	else
	{
		[self dismissModalViewControllerAnimated:TRUE];
	}
}

- (IBAction)onClose:(id)sender
{
	UIActionSheet *actionSheet =
	[[UIActionSheet alloc] initWithTitle:@"" delegate:self cancelButtonTitle:@"Cancel" destructiveButtonTitle:@"Discard" otherButtonTitles:@"Save", @"Revert", nil];

	actionSheet.actionSheetStyle = UIActionSheetStyleDefault;
	[actionSheet showInView:self.view];
}

- (void)actionSheet:(UIActionSheet *)actionSheet clickedButtonAtIndex:(NSInteger)buttonIndex
{
	const int
		DISCARD = 0,
		SAVE = 1,
		REVERT = 2;

	if (buttonIndex == DISCARD)
		[self onDiscard];
	else
	if (buttonIndex == SAVE)
		[self onSave];
	else
	if (buttonIndex == REVERT)
		[self dismiss];
}

- (IBAction)onSend:(id)sender
{
	[self transferFromUI];
	masterSingleton->getActions()->sendMail(conversation, mail);
	[self dismiss];
}

- (void)onDiscard
{
	if (mail)
		masterSingleton->getActions()->deleteMail(conversation, mail);

	[self dismiss];
}

- (void)onSave
{
	LogDebug(ui::MailComposeViewController, "onSave");

	[self transferFromUI];
	masterSingleton->getActions()->saveMail(conversation, mail);
	[self dismiss];
}

- (void)transferFromUI
{
	if (mail == NULL)
	{
		Pair<ConversationPtr, MailPtr> pair = masterSingleton->getActions()->newMail();
		conversation = pair.first;
		mail = pair.second;
	}

	Recipients *r = mail->getHeader()->getRecipients();

	r->setTo(r->toIdentityList(masterSingleton->getCacheManager(), sections[0]));
	r->setCc(r->toIdentityList(masterSingleton->getCacheManager(), sections[1]));
	r->setBcc(r->toIdentityList(masterSingleton->getCacheManager(), sections[2]));

	mail->getHeader()->setSubject(new String(sections[3][0]));
	mail->getBody()->setText(new String(body.text.UTF8String));
	mail->getHeader()->setBrief(mail->getBody()->calculateBrief());
}


#pragma mark -
#pragma mark Hacks

- (void)hackToolbarColor
{
	self.navigationController.navigationBar.tintColor = [UIColor blackColor];
}

- (void)hackAddNavigationBarItems
{
	UIBarButtonItem *close = 
		[[UIBarButtonItem alloc] 
			initWithTitle:@"Close" 
			style:UIBarButtonItemStylePlain 
			target:self 
			action:@selector(onClose:)
		];
		

	UIBarButtonItem *send =
		[[UIBarButtonItem alloc] 
			initWithTitle:@"Send" 
			style:UIBarButtonItemStylePlain 
			target:self 
			action:@selector(onSend:)
		];

	self.navigationItem.leftBarButtonItem = close;
	self.navigationItem.rightBarButtonItem = send;
}

@end

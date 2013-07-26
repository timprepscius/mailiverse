/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */


#include "FolderViewControllerEventListener.h"

#include "mailiverse/utilities/Functions.h"
#include "mailiverse/mail/manager/Lib.h"
#include "MMUtilities.h"
#include "MMPool.h"
#include "MasterSingleton.h"

using namespace mailiverse::mail::model;
using namespace mailiverse::mail;
using namespace mailiverse;

FolderViewControllerEventListener::FolderViewControllerEventListener (FolderViewController *_controller) :
	controller(_controller)
{ 
	masterSingleton->getEventPropagator()->add (
		manager::Events::Initialized,
		this,
		utilities::newbindC_GV(
			this, &FolderViewControllerEventListener::onInitialized
		)
	);

	masterSingleton->getEventPropagator()->add (
		manager::Events::LoadFolder,
		this,
		utilities::newbindC_G<Folder *>(
			this, &FolderViewControllerEventListener::onLoadFolder
		)
	);

	masterSingleton->getEventPropagator()->add (
		manager::Events::FolderListing,
		this,
		utilities::newbindC_G<Conversation *>(
			this, &FolderViewControllerEventListener::onFolderListing
		)
	);
	
	masterSingleton->getEventPropagator()->add (
		manager::Events::ChangedConversation,
		this,
		utilities::newbindC_G<Conversation *>(
			this, &FolderViewControllerEventListener::onChangedConversation
		)
	);
	
	masterSingleton->getEventPropagator()->add (
		manager::Events::NewConversation,
		this,
		utilities::newbindC_G<Conversation *>(
			this, &FolderViewControllerEventListener::onNewConversation
		)
	);

	masterSingleton->getEventPropagator()->add (
		manager::Events::CheckEnd,
		this,
		utilities::newbindC_GV(
			this, &FolderViewControllerEventListener::onCheckMailFinished
		)
	);
	
	masterSingleton->getEventPropagator()->add (
		manager::Events::CheckFailure,
		this,
		utilities::newbindC_GV(
			this, &FolderViewControllerEventListener::onCheckMailFailure
		)
	);

	masterSingleton->getEventPropagator()->add (
		manager::Events::CheckStep,
		this,
		utilities::newbindC_G<String *>(
			this, &FolderViewControllerEventListener::onCheckMailStep
		)
	);
}

FolderViewControllerEventListener::~FolderViewControllerEventListener ()
{
	controller = nil;

	masterSingleton->getEventPropagator()->remove(
		this
	);
}

void FolderViewControllerEventListener::onInitialized()
{
	__strong FolderViewController *strong = controller;
	if (strong)
	{
		POOL_START
		[strong performSelectorOnMainThread:@selector(onInitialized) withObject:nil waitUntilDone:FALSE];
		POOL_STOP
	}
}

void FolderViewControllerEventListener::onLoadFolder(Folder *f)
{
	__strong FolderViewController *strong = controller;
	if (strong)
	{
		POOL_START
		[strong performSelectorOnMainThread:@selector(onLoadFolder:) withObject:[MMPtr instantiate:new FolderPtr(f)] waitUntilDone:FALSE];
		POOL_STOP
	}
}

void FolderViewControllerEventListener::onFolderListing(Conversation *c)
{
	__strong FolderViewController *strong = controller;
	if (strong)
	{
		POOL_START
		[strong performSelectorOnMainThread:@selector(onFolderListing:) withObject:[MMPtr instantiate:new ConversationPtr(c)] waitUntilDone:FALSE];
		POOL_STOP
	}
}

void FolderViewControllerEventListener::onChangedConversation(Conversation *c)
{
	__strong FolderViewController *strong = controller;
	if (strong)
	{
		POOL_START
		[strong performSelectorOnMainThread:@selector(onChangedConversation:) withObject:[MMPtr instantiate:new ConversationPtr(c)] waitUntilDone:FALSE];
		POOL_STOP
	}
}

void FolderViewControllerEventListener::onNewConversation(Conversation *c)
{
	__strong FolderViewController *strong = controller;
	if (strong)
	{
		POOL_START
		[strong performSelectorOnMainThread:@selector(onNewConversation:) withObject:[MMPtr instantiate:new ConversationPtr(c)] waitUntilDone:FALSE];
		POOL_STOP
	}
}

void FolderViewControllerEventListener::onCheckMailStep (String *data)
{
	std::vector<std::string> split = utilities::split(*data, ":");
	NSString *status = [NSString stringWithFormat:@"%s/%s of %s",(split[0].c_str()),(split[1].c_str()),(split[2].c_str())];
	
	__strong FolderViewController *strong = controller;
	if (strong)
	{
		POOL_START
		[strong performSelectorOnMainThread:@selector(onCheckMailStep:) withObject:status waitUntilDone:FALSE];
		POOL_STOP
	}
	
}

void FolderViewControllerEventListener::onCheckMailFinished ()
{
	__strong FolderViewController *strong = controller;
	if (strong)
	{
		POOL_START
		[strong performSelectorOnMainThread:@selector(onCheckMailFinished) withObject:nil waitUntilDone:FALSE];
		POOL_STOP
	}
}

void FolderViewControllerEventListener::onCheckMailFailure ()
{
	__strong FolderViewController *strong = controller;
	if (strong)
	{
		POOL_START
		[strong performSelectorOnMainThread:@selector(onCheckMailFailure) withObject:nil waitUntilDone:FALSE];
		POOL_STOP
	}
}


/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */


#include "RevealFoldersViewControllerEventListener.h"

#include "mailiverse/utilities/Functions.h"
#include "mailiverse/mail/manager/Lib.h"
#include "MMUtilities.h"
#include "MasterSingleton.h"
#include "MMPool.h"

using namespace mailiverse::mail::model;
using namespace mailiverse::mail;
using namespace mailiverse;

RevealFoldersViewControllerEventListener::RevealFoldersViewControllerEventListener (RevealFoldersViewController *_controller) :
	controller(_controller)
{ 
	networkActivity = [[UINetworkActivityWrapper alloc] init];
	
	masterSingleton->getEventPropagator()->add (
		manager::Events::Initialized,
		this,
		utilities::newbindC_GV(
			this, &RevealFoldersViewControllerEventListener::onInitialized
		)
	);

	masterSingleton->getEventPropagator()->add (
		manager::Events::LoadFolder,
		this,
		utilities::newbindC_G<Folder *>(
			this, &RevealFoldersViewControllerEventListener::onLoadFolder
		)
	);

	masterSingleton->getEventPropagator()->add (
		manager::Events::SettingsLoaded,
		this,
		utilities::newbindC_G<Settings *>(
			this, &RevealFoldersViewControllerEventListener::onSettingsChanged
		)
	);

	masterSingleton->getEventPropagator()->add (
		manager::Events::SettingsChanged,
		this,
		utilities::newbindC_G<Settings *>(
			this, &RevealFoldersViewControllerEventListener::onSettingsChanged
		)
	);

	masterSingleton->getEventPropagator()->add (
		manager::Events::NewConversation,
		this,
		utilities::newbindC_G<Conversation *>(
			this, &RevealFoldersViewControllerEventListener::onNewConversation
		)
	);

	masterSingleton->getEventPropagator()->add (
		manager::Events::NewFolder,
		this,
		utilities::newbindC_G<Folder *>(
			this, &RevealFoldersViewControllerEventListener::onNewFolder
		)
	);

	masterSingleton->getEventPropagator()->add (
		manager::Events::CheckEnd,
		this,
		utilities::newbindC_GV(
			this, &RevealFoldersViewControllerEventListener::onCheckMailFinished
		)
	);
	
	//---------------------------

	masterSingleton->getEventPropagator()->add (
		manager::Events::UploadBegin,
		this,
		utilities::newbindC_GV(
			this, &RevealFoldersViewControllerEventListener::onNetworkBegin
		)
	);
	masterSingleton->getEventPropagator()->add (
		manager::Events::UploadEnd,
		this,
		utilities::newbindC_GV(
			this, &RevealFoldersViewControllerEventListener::onNetworkEnd
		)
	);
	masterSingleton->getEventPropagator()->add (
		manager::Events::DownloadBegin,
		this,
		utilities::newbindC_GV(
			this, &RevealFoldersViewControllerEventListener::onNetworkBegin
		)
	);
	masterSingleton->getEventPropagator()->add (
		manager::Events::DownloadEnd,
		this,
		utilities::newbindC_GV(
			this, &RevealFoldersViewControllerEventListener::onNetworkEnd
		)
	);
}

RevealFoldersViewControllerEventListener::~RevealFoldersViewControllerEventListener ()
{
	masterSingleton->getEventPropagator()->remove(
		this
	);
}

void RevealFoldersViewControllerEventListener::onInitialized()
{
	__strong RevealFoldersViewController *strong = controller;
	if (strong)
	{
		POOL_START
		[strong performSelectorOnMainThread:@selector(onInitialized) withObject:nil waitUntilDone:FALSE];
		POOL_STOP
	}
}

void RevealFoldersViewControllerEventListener::onSettingsChanged(Settings *s)
{
	__strong RevealFoldersViewController *strong = controller;
	if (strong)
	{
		POOL_START
		[strong performSelectorOnMainThread:@selector(onSettingsChanged:) withObject:[MMPtr instantiate:new SettingsPtr(s)] waitUntilDone:FALSE];
		POOL_STOP
	}
}

void RevealFoldersViewControllerEventListener::onLoadFolder(Folder *f)
{
	__strong RevealFoldersViewController *strong = controller;
	if (strong)
	{
		POOL_START
		[strong performSelectorOnMainThread:@selector(onLoadFolder:) withObject:[MMPtr instantiate:new FolderPtr(f)] waitUntilDone:FALSE];
		POOL_STOP
	}
}

void RevealFoldersViewControllerEventListener::onNewFolder(Folder *f)
{
	__strong RevealFoldersViewController *strong = controller;
	if (strong)
	{
		POOL_START
		[strong performSelectorOnMainThread:@selector(onNewFolder:) withObject:[MMPtr instantiate:new FolderPtr(f)] waitUntilDone:FALSE];
		POOL_STOP
	}
}

void RevealFoldersViewControllerEventListener::onNewConversation(Conversation *c)
{
	__strong RevealFoldersViewController *strong = controller;
	if (strong)
	{
		POOL_START
		[strong performSelectorOnMainThread:@selector(onNewConversation:) withObject:[MMPtr instantiate:new ConversationPtr(c)] waitUntilDone:FALSE];
		POOL_STOP
	}
}

void RevealFoldersViewControllerEventListener::onCheckMailFinished ()
{
	__strong RevealFoldersViewController *strong = controller;
	if (strong)
	{
		POOL_START
		[strong performSelectorOnMainThread:@selector(onCheckMailFinished) withObject:nil waitUntilDone:FALSE];
		POOL_STOP
	}
}

void RevealFoldersViewControllerEventListener::onNetworkBegin ()
{
	[networkActivity start];
}

void RevealFoldersViewControllerEventListener::onNetworkEnd ()
{
	[networkActivity stop];
}

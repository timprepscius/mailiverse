/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */



#ifndef __FolderViewControllerEventListener_h__
#define __FolderViewControllerEventListener_h__

#import "RevealFoldersViewController.h"
#import "UINetworkActivityWrapper.h"

#include "mailiverse/mail/model/Lib.h"
#include "MMPool.h"

class RevealFoldersViewControllerEventListener
{
	__weakptr RevealFoldersViewController *controller;
	
public:
	RevealFoldersViewControllerEventListener (RevealFoldersViewController *_controller);
	virtual ~RevealFoldersViewControllerEventListener ();

	void onInitialized();
	void onSettingsChanged(mailiverse::mail::model::Settings *s);
	void onNewFolder(mailiverse::mail::model::Folder *f);
	void onLoadFolder(mailiverse::mail::model::Folder *f);
	void onFolderListing(mailiverse::mail::model::Conversation *c);
	void onNewConversation(mailiverse::mail::model::Conversation *c);
	void onCheckMailFinished ();

	UINetworkActivityWrapper *networkActivity;
	void onNetworkBegin();
	void onNetworkEnd();
} ;

DECLARE_SMARTPTR(RevealFoldersViewControllerEventListener);


#endif 
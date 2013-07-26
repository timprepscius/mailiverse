/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */


#ifndef __FolderViewControllerEventListener_h__
#define __FolderViewControllerEventListener_h__

#include "mailiverse/mail/model/Lib.h"
#import "FolderViewController.h"
#include "mailiverse/Types.h"
#include "MMPool.h"

class FolderViewControllerEventListener
{
	__weakptr FolderViewController *controller;
	
public:
	FolderViewControllerEventListener (FolderViewController *_controller);
	virtual ~FolderViewControllerEventListener ();

	void onInitialized();
	void onLoadFolder(mailiverse::mail::model::Folder *f);
	void onFolderListing(mailiverse::mail::model::Conversation *c);
	void onChangedConversation(mailiverse::mail::model::Conversation *c);
	void onNewConversation(mailiverse::mail::model::Conversation *c);
	
	void onCheckMailStep (mailiverse::String *data);
	void onCheckMailFinished ();
	void onCheckMailFailure ();
} ;

DECLARE_SMARTPTR(FolderViewControllerEventListener);


#endif 
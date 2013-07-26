/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */


#ifndef PirateMailViewer_ConversationViewControllerEventListener_h
#define PirateMailViewer_ConversationViewControllerEventListener_h

#include "mailiverse/mail/model/Lib.h"
#import "FolderViewController.h"
#include "MMPool.h"

class ConversationViewControllerEventListener
{
	__weakptr ConversationViewController *controller;
	
public:
	ConversationViewControllerEventListener (ConversationViewController *_controller);
	
	virtual ~ConversationViewControllerEventListener ();
	virtual void onLoadConversation(mailiverse::mail::model::Conversation *c);
	virtual void onChangedConversation(mailiverse::mail::model::Conversation *c);
	virtual void onLoadMail(mailiverse::mail::model::Mail *m);
} ;

DECLARE_SMARTPTR(ConversationViewControllerEventListener);


#endif

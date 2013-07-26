/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "ConversationViewControllerEventListener.h"
#include "mailiverse/utilities/Functions.h"
#include "mailiverse/mail/manager/Lib.h"
#include "MMUtilities.h"
#include "MMPool.h"
#include "MasterSingleton.h"

using namespace mailiverse::mail::model;
using namespace mailiverse::mail;
using namespace mailiverse;

ConversationViewControllerEventListener::ConversationViewControllerEventListener (ConversationViewController *_controller) :
	controller(_controller)
{ 
	masterSingleton->getEventPropagator()->add (
		manager::Events::LoadConversation,
		this,
		utilities::newbindC_G<Conversation *>(
			this, &ConversationViewControllerEventListener::onLoadConversation
		)
	);

	masterSingleton->getEventPropagator()->add (
		manager::Events::ChangedConversation,
		this,
		utilities::newbindC_G<Conversation *>(
			this, &ConversationViewControllerEventListener::onChangedConversation
		)
	);

	masterSingleton->getEventPropagator()->add (
		manager::Events::LoadMail,
		this,
		utilities::newbindC_G<Mail *>(
			this, &ConversationViewControllerEventListener::onLoadMail
		)
	);
}
	
ConversationViewControllerEventListener::~ConversationViewControllerEventListener ()
{
	controller = nil;

	masterSingleton->getEventPropagator()->remove(
		this
	);
}
	
void ConversationViewControllerEventListener::onLoadConversation(Conversation *c)
{
	__strong ConversationViewController *strong = controller;
	if (strong)
	{
		POOL_START
		[controller performSelectorOnMainThread:@selector(onLoadConversation:) withObject:[MMPtr instantiate:new ConversationPtr(c)] waitUntilDone:FALSE];
		POOL_STOP
	}
}

void ConversationViewControllerEventListener::onChangedConversation(Conversation *c)
{
	__strong ConversationViewController *strong = controller;
	if (strong)
	{
		POOL_START
		[controller performSelectorOnMainThread:@selector(onChangedConversation:) withObject:[MMPtr instantiate:new ConversationPtr(c)] waitUntilDone:FALSE];
		POOL_STOP
	}
}

void ConversationViewControllerEventListener::onLoadMail(Mail *m)
{
	__strong ConversationViewController *strong = controller;
	if (strong)
	{
		POOL_START
		[strong performSelectorOnMainThread:@selector(onLoadMail:) withObject:[MMPtr instantiate:new MailPtr(m)] waitUntilDone:FALSE];
		POOL_STOP
	}
}


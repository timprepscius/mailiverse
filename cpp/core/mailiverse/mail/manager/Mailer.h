/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_manager_Mailer_h__
#define __mailiverse_mail_manager_Mailer_h__

#include "Servent.h"
#include "mailiverse/utilities/SmartPtr.h"
#include "../model/Conversation.h"
#include "mailiverse/core/util/HttpDelegate.h"
#include "mailiverse/core/util/Random.h"
#include "mailiverse/utilities/Monitor.h"

namespace mailiverse {
namespace mail {
namespace manager {

class Mailer : public Servent
{
public:
	struct SendObject {
		std::string password;
		model::ConversationPtr conversation;
		model::MailPtr mail;
	} ;
	
	core::util::Random random;
	typedef utilities::Monitor<List<SendObject>> Queue;
	Queue queue;
	
protected:
	core::util::HttpDelegatePtr httpDelegate;

public:
	Mailer (core::util::HttpDelegate *httpDelegate)
	{
		this->httpDelegate = httpDelegate;
	}

	void queueMail (const String &password, model::Conversation *conversation, model::Mail *mail);
	void sendQueue ();
	void doSend(const String &password, model::Conversation *conversation, model::Mail *mail);

	void onSendSucceeded (model::Conversation *conversation, model::Mail *mail);
	void onSendFailed (model::Conversation *conversation, model::Mail *mail, Exception &e);
};

DECLARE_SMARTPTR(Mailer);

} /* namespace manager */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* __mailiverse_mail_manager_Mailer_h__ */

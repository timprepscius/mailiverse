/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_manager_Actions_h__
#define __mailiverse_mail_manager_Actions_h__

#include "Servent.h"
#include "mailiverse/utilities/SmartPtr.h"
#include "../model/Conversation.h"
#include "../model/FolderFilter.h"
#include "mailiverse/Types.h"
#include "mailiverse/core/util/Callback.h"

namespace mailiverse {
namespace mail {
namespace manager {

class Actions : public Servent
{
public:
	Actions();
	virtual ~Actions();

	model::Body *calculateSignaturedBody (const String &body);

	Pair<model::ConversationPtr,model::MailPtr> newMail ();
	void saveMail (model::Conversation *conversation, model::Mail *mail);
	void deleteMail (model::Conversation *conversation, model::Mail *mail);
	void deleteConversation (model::Conversation *conversation);

	model::MailPtr replyToAll (model::Conversation *conversation, model::Mail *mail);
	model::MailPtr replyTo (model::Conversation *conversation, model::Mail *mail);
	model::MailPtr forward (model::Conversation *conversation, model::Mail *mail);

	void sendMail (model::Conversation *conversation, model::Mail *mail);
	void reindexConversation (model::Conversation *conversation);

	model::MailPtr reply (model::Recipients *recipients, model::Conversation *conversation, model::Mail *mail, model::Body *body);
	
	void markAndReindexConversations (const Set<cache::ID> &ids, const String &state, bool value);
	void markConversations (const Set<cache::ID> &ids, const String &state, bool value);
	
	void addToUserFolders (const Set<cache::ID> &conversations, const Set<model::FolderFilterPtr> &folders);
	void removeFromUserFolder (const Set<cache::ID> &conversations, model::FolderFilter *folder);
	
	void markSpam (const Set<cache::ID> &conversations, bool value);
	
	virtual void onInitialized () {}

//--------------------------------------------------------
protected:
	// this stuff should be reorganized out

	typedef void (Actions::*Action)();

	void invoke (Action action, core::util::Callback<> callback)
	{
		try
		{
			(this->*action)();
			callback.invoke();
		}
		catch (Exception &e)
		{
			callback.invoke(e);
		}
		
	}
	
public:
	void doCheckMail();
	void doUpdate();
	void doPartialUpdate();
	void doFlush();
	void doProcessSendQueue();
	void doEnableNotifications(const String &deviceId);

public:
	virtual void checkMail () { checkMail(core::util::Callback<>()); }
	virtual void checkMail (core::util::Callback<> callback) { invoke(&Actions::doCheckMail, callback); }

	virtual void update () { update(core::util::Callback<>()); }
	virtual void update (core::util::Callback<> callback) { invoke(&Actions::doUpdate, callback); }

	virtual void partialUpdate () { partialUpdate(core::util::Callback<>()); }
	virtual void partialUpdate (core::util::Callback<> callback) { invoke(&Actions::doPartialUpdate, callback); }
	
	virtual void flush () { flush(core::util::Callback<>()); }
	virtual void flush (core::util::Callback<> callback) { invoke(&Actions::doFlush, callback); }
	
	virtual void processSendQueue () { processSendQueue(core::util::Callback<>()); }
	virtual void processSendQueue (core::util::Callback<> callback) { invoke(&Actions::doPartialUpdate, callback); }
	
	virtual void enableNotifications (const String &deviceId=String()) { enableNotifications(deviceId, core::util::Callback<>()); }
	virtual void enableNotifications (const String &deviceId, core::util::Callback<> callback) {}
};

DECLARE_SMARTPTR(Actions);

} /* namespace manager */
} /* namespace model::Mail */
} /* namespace model::Mailiverse */
#endif /* ACTIONS_H_ */

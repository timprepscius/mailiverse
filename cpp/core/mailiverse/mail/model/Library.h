/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef LIBRARY_H_
#define LIBRARY_H_

#include "Record.h"
#include "../cache/Type.h"

namespace mailiverse {
namespace mail {
namespace model {

class Identity;
DECLARE_SMARTPTR(Identity);

class AddressBook;
DECLARE_SMARTPTR(AddressBook);

class Mail;
DECLARE_SMARTPTR(Mail);

class Conversation;
DECLARE_SMARTPTR(Conversation);

class Folder;
DECLARE_SMARTPTR(Folder);

class Settings;
DECLARE_SMARTPTR(Settings);

class FolderDefinition;
class Header;
class Body;
class Attachments;

class Library
{
public:
	Library() {}
	virtual ~Library() {}

	virtual IdentityPtr getIdentity() = 0;
	virtual AddressBookPtr getAddressBook () = 0;

	virtual MailPtr getMail(const Record &key) = 0;
	virtual MailPtr newMail(Header *header, Body *body, Attachments *attachments) = 0;
	
	virtual ConversationPtr getConversation(const Record &key) = 0;
	virtual ConversationPtr newConversation() = 0;
	virtual void reindexConversation(Conversation *) = 0;
	
	virtual FolderPtr getFolder(const cache::Type &type, const Record &key) = 0;
	virtual FolderPtr newFolder(const cache::Type &type, FolderDefinition *definition) = 0;

	virtual void onLoaded (Mail *) = 0;
	virtual void onLoaded (Conversation *) = 0;
	virtual void onLoaded (Folder *) = 0;
	virtual void onLoaded (Settings *) = 0;
	
	virtual void onDirty (Settings *) = 0;
};

} /* namespace model */
} /* namespace mail */
} /* namespace mailiverse */
#endif /* LIBRARY_H_ */

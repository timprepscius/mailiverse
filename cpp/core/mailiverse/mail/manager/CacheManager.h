/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_manager_CacheManager_h__
#define __mailiverse_mail_manager_CacheManager_h__

#include "mailiverse/utilities/SmartPtr.h"
#include "Servent.h"
#include "mailiverse/core/connector/StoreConnector.h"
#include "../cache/Lib.h"
#include "../model/Lib.h"
#include "mailiverse/utilities/Monitor.h"

namespace mailiverse {
namespace mail {
namespace manager {

class CacheManager : public Servent, public model::Library, public cache::StoreLibrary::Delegate
{
public:
	static const int MAX_CACHE_SIZE = 85 * 1000; // 25K

protected:
	core::crypt::CryptorSeed cryptorSeed;
	core::connector::StoreConnectorPtr connector;
	cache::StoreLibraryPtr storeLibrary;
	cache::CachePtr masterCache;
	model::SettingsPtr settings;
	
	model::ModelFactoryPtr modelFactory;

public:
	CacheManager(const core::crypt::CryptorSeed &cryptorSeed, core::connector::StoreConnector *connector);
	virtual ~CacheManager();

	model::Settings *getSettings();

	void initialize ();
	void onFirstRun ();

	bool shouldFlush ();
	void flush ();
	void update ();
	void partialUpdate ();
	void garbageCollect ();

	void onInitializeSucceeded();
	void onInitializeFailed(const Exception &e);

	// storeLibrary delegate
	
	virtual void onNewStore (cache::Store *);

	// model::Library

	virtual model::IdentityPtr getIdentity() override;
	virtual model::AddressBookPtr getAddressBook () override;

	virtual model::MailPtr getMail(const model::Record &key) override;
	virtual model::ConversationPtr getConversation(const model::Record &key) override;
	virtual model::FolderPtr getFolder(const cache::Type &type, const model::Record &key) override;

	virtual model::MailPtr newMail(model::Header *header, model::Body *body, model::Attachments *attachments) override;
	virtual model::ConversationPtr newConversation() override;
	virtual void reindexConversation(model::Conversation *) override;
	
	model::FolderPtr newFolder(const cache::ID &id, const cache::Type &type, model::FolderDefinition *);
	virtual model::FolderPtr newFolder(const cache::Type &type, model::FolderDefinition *) override;
	
	void newCache (const cache::ID &iid, const cache::ID &cid);

	void deleteFolder(model::Folder *folder);
	void deleteConversation(model::Conversation *conversation);
	void loadConversation(model::Conversation* conversation);
	void deleteMail(model::Mail *);

	virtual void onLoaded (model::Mail *) override;
	virtual void onLoaded (model::Conversation *) override;
	virtual void onLoaded (model::Folder *) override;
	virtual void onLoaded (model::Settings *) override;
	
	virtual void onDirty (model::Settings *) override;
};

DECLARE_SMARTPTR(CacheManager);

} /* namespace manager */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* __mailiverse_mail_manager_CacheManager_h__ */

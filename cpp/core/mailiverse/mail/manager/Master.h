/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_manager_Master_h__
#define __mailiverse_mail_manager_Master_h__

#include "ArrivalsMonitor.h"
#include "ArrivalsProcessor.h"
#include "Initializer.h"
#include "Indexer.h"
#include "EventDispatcher.h"
#include "mailiverse/core/store/Environment.h"
#include "mailiverse/core/crypt/Cryptor.h"
#include "Store.h"
#include "Actions.h"
#include "Mailer.h"
#include "CacheManager.h"
#include "../model/Lib.h"

namespace mailiverse {
namespace mail {
namespace manager {

class Master
{
protected:
	InitializerPtr initializer;
	StorePtr store;
	model::IdentityPtr identity;
	core::store::EnvironmentPtr environment;
	model::AddressBookPtr addressBook;
	IndexerPtr indexer;
	ArrivalsProcessorPtr arrivalsProcessor;
	ArrivalsMonitorPtr arrivalsMonitor;
	EventDispatcherPtr eventDispatcher;
	ActionsPtr actions;
	MailerPtr mailer;
	core::crypt::CryptorPtr cryptor;
	CacheManagerPtr cacheManager;

public:
	Master (
		Store *store,
		model::Identity *identity,
		core::store::Environment *environment,
		Indexer *indexer,
		model::AddressBook *addressBook,
		ArrivalsProcessor *arrivalsProcessor,
		ArrivalsMonitor *arrivalsMonitor,
		EventDispatcher *eventDispatcher,
		Actions *actions,
		Mailer *mailer,
		core::crypt::Cryptor *cryptor,
		CacheManager *cacheManager
	)
	{
		this->identity = identity;
		this->environment = environment;
		this->addressBook = addressBook;

		this->store = store;
		store->setMaster(this);

		this->initializer = new Initializer();
		initializer->setMaster(this);

		this->indexer = indexer;
		indexer->setMaster(this);

		this->arrivalsProcessor = arrivalsProcessor;
		arrivalsProcessor->setMaster(this);

		this->arrivalsMonitor = arrivalsMonitor;
		arrivalsMonitor->setMaster(this);

		this->eventDispatcher = eventDispatcher;

		this->actions = actions;
		actions->setMaster(this);

		this->mailer = mailer;
		mailer->setMaster(this);

		this->cryptor = cryptor;

		this->cacheManager = cacheManager;
		cacheManager->setMaster(this);
	}
	
	virtual ~Master ()
	{
		actions = NULL;
		initializer = NULL;
		arrivalsMonitor = NULL;
		arrivalsProcessor = NULL;
		mailer = NULL;
		cacheManager = NULL;
	}
	
	Initializer *getInitializer ()
	{
		return initializer;
	}

	Store *getStore ()
	{
		return store;
	}

	model::Identity *getIdentity ()
	{
		return identity;
	}

	core::store::Environment *getEnvironment ()
	{
		return environment;
	}

	model::AddressBook *getAddressBook ()
	{
		return addressBook;
	}

	Indexer *getIndexer()
	{
		return indexer;
	}

	CacheManager *getCacheManager()
	{
		return cacheManager;
	}

	ArrivalsProcessor *getArrivalsProcessor()
	{
		return arrivalsProcessor;
	}

	ArrivalsMonitor *getArrivalsMonitor()
	{
		return arrivalsMonitor;
	}

	EventDispatcher *getEventPropagator ()
	{
		return eventDispatcher;
	}

	Actions *getActions ()
	{
		return actions;
	}

	Mailer *getMailer ()
	{
		return mailer;
	}

	core::crypt::Cryptor *getCryptor ()
	{
		return cryptor;
	}
	
	static Master *create(const String &identityString, core::store::Environment *);
};

DECLARE_SMARTPTR(Master);

} /* namespace manager */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* __mailiverse_mail_manager_Master_h__ */

/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "CacheManager.h"
#include "mailiverse/core/connector/Lib.h"
#include "../model/Lib.h"
#include "../serializers/Lib.h"
#include "MasterCacheSerializer.h"
#include "../cache/Lib.h"
#include "Events.h"
#include "Master.h"
#include "Constants.h"
#include "mailiverse/utilities/Time.h"
#include "mailiverse/External.h"
#include "mailiverse/core/constants/ConstantsSettings.h"


using namespace mailiverse::mail::manager;
using namespace mailiverse::mail::cache;
using namespace mailiverse::core::connector;
using namespace mailiverse::core::constants;
using namespace mailiverse::mail::serializers;
using namespace mailiverse::mail::model;
using namespace mailiverse::mail;
using namespace mailiverse;

CacheManager::CacheManager(const core::crypt::CryptorSeed &_cryptorSeed, StoreConnector *_connector) :
	cryptorSeed(_cryptorSeed),
	connector(_connector)
{
	modelFactory = new ModelFactory(this);
}

CacheManager::~CacheManager()
{
	masterCache->markShutdown();
	masterCache = NULL;
	settings = NULL;
}

Settings *CacheManager::getSettings ()
{
	return settings;
}

//--------------------------------------

void CacheManager::initialize ()
{
	storeLibrary = new StoreLibrary(
		cryptorSeed,
		connector, 
		new StoreMemoryFactory(MAX_CACHE_SIZE), 
		new StoreMemorySerializer(),
		Constants::CACHE_PREFIX
	);
	
	cache::StorePtr managerStore = storeLibrary->instantiate(Constants::INDEX_PREFIX, ID::NONE, false);

	masterCache = new Cache(
		NULL,
		new MasterCacheSerializer(new StoreMemorySerializer()),
		managerStore,
		true
	);

	settings = new Settings();
	settings->setLibrary(this);
	masterCache->link(
		Constants::SETTINGS_KEY,
		settings
	);

	masterCache->link(
		Constants::MAIL_KEY,
		new IndexedItems (
			storeLibrary, Constants::MAIL_PREFIX,
			false,
			new ModelFactory (this),
			new ModelSerializer(),
			false
		)
	);

	masterCache->link(
		Constants::CONVERSATION_KEY,
		new IndexedItems (
			storeLibrary, Constants::CONVERSATION_PREFIX,
			false,
			new ModelFactory (this),
			new ModelSerializer(),
			false
		)
	);

	masterCache->link(
		Constants::FOLDER_KEY,
		new IndexedItems (
			storeLibrary, Constants::FOLDER_PREFIX,
			false,
			new ModelFactory (this),
			new ModelSerializer(),
			true
		)
	);

	// why is this down here?  because I want to make sure that there is an initial listing
	// which happens before any partials... if I put it higher then the instantiate method
	// will in turn call the delegate, which will start an update already, but I want 
	// my special update to run first
	storeLibrary->setDelegate(this);

	getMaster()->getActions()->update(
		core::util::Callback<>(
			utilities::newbindC(this, &CacheManager::onInitializeSucceeded),
			utilities::newbindC_1<Exception>(this, &CacheManager::onInitializeFailed)
		)
	);
	
	getMaster()->getEventPropagator()->add(
		Events::InitiateLoadConversation, this, 
		EventPropagator::Callback (
			utilities::newbindC_G<Conversation *>(
				this,
				&CacheManager::loadConversation 
			)
		)
	);
}

void CacheManager::onInitializeSucceeded()
{
	getMaster()->getEventPropagator()->signal(Events::Initialize_IndexedCacheLoadComplete, NULL);
}

void CacheManager::onInitializeFailed(const Exception &e)
{
	if (e.what() == "empty")
		getMaster()->getEventPropagator()->signal(Events::Initialize_FirstRun, NULL);
	else
		getMaster()->getEventPropagator()->signal(Events::Initialize_IndexedCacheLoadFailed, NULL);
}

void CacheManager::onFirstRun ()
{
	masterCache->markCreate();
	masterCache->get(0,Constants::SETTINGS_KEY)->markCreate();
	masterCache->get(0,Constants::MAIL_KEY)->markCreate();
	masterCache->get(0,Constants::CONVERSATION_KEY)->markCreate();
	masterCache->get(0,Constants::FOLDER_KEY)->markCreate();
}

IdentityPtr CacheManager::getIdentity()
{
	return getMaster()->getIdentity();
}

AddressBookPtr CacheManager::getAddressBook ()
{
	return getMaster()->getAddressBook();
}

MailPtr CacheManager::getMail(const Record &key)
{
	return
		masterCache->getItem<IndexedItems>(0,Constants::MAIL_KEY)->
			getItem<Mail>(model::Types::Mail, key.first);
}

ConversationPtr CacheManager::getConversation(const Record &key)
{
	ConversationPtr conversation = 
		masterCache->getItem<IndexedItems>(0,Constants::CONVERSATION_KEY)->
			getItemNoAcquire<Conversation>(Types::Conversation, key.first);

	getMaster()->getEventPropagator()->signal(
		Events::InitiateLoadConversation, 
		utilities::newArg<WeakConversationPtr>((Conversation *)conversation)
	);

	return conversation;
}

void CacheManager::loadConversation(Conversation *_conversation)
{
	ConversationPtr conversation = _conversation;
	if (conversation)
	{
		if (!conversation->isLoaded())
		{
			masterCache->getItem<IndexedItems>(0,Constants::CONVERSATION_KEY)->
				getItem<Conversation>(Types::Conversation, conversation->getID());
		}
	}
}

void CacheManager::deleteMail(Mail *mail)
{
	mail->markDeleted();
	getMaster()->getEventPropagator()->signal(
		Events::DeleteMail, utilities::newArg<MailPtr>(mail)
	);
}

void CacheManager::deleteConversation(Conversation *conversation)
{
	conversation->markDeleted();
	getMaster()->getEventPropagator()->signal(
		Events::DeleteConversation, utilities::newArg<ConversationPtr>(conversation)
	);
}

bool CacheManager::shouldFlush ()
{
	if (!masterCache || !storeLibrary)
		return false;
		
	if (getMaster()->getArrivalsMonitor()->isChecking())
		return false;

	if (storeLibrary->hasDirtyChildren())
		return true;

	if (masterCache->hasDirtyChildren())
		return true;
		
	return false;
}

void CacheManager::flush ()
{
	if (!masterCache || !storeLibrary)
		return;

	// we don't flush during mail check, cause we would flush a lot, disrupt network
	if (!getMaster()->getArrivalsMonitor()->isChecking())
	{
		// if everything that was previously flushed is now written
		if (!storeLibrary->hasDirtyChildren())
			if (masterCache->hasDirtyChildren())
				masterCache->flush();
		
		if (storeLibrary->hasDirtyChildren())
			storeLibrary->flush();
	}
}

void CacheManager::update ()
{
	storeLibrary->update(false);
	masterCache->update();
}

void CacheManager::partialUpdate ()
{
	storeLibrary->partialUpdate(true);
	masterCache->update();
}

void CacheManager::onNewStore (cache::Store *)
{
	getMaster()->getActions()->partialUpdate();
}

FolderPtr CacheManager::getFolder(const Type &type, const Record &key)
{
	return
		masterCache->getItem<IndexedItems>(0,Constants::FOLDER_KEY)->
			getItem<Folder>(type, key.first);
}

ConversationPtr CacheManager::newConversation ()
{
	ConversationPtr conversation = (Conversation*)modelFactory->instantiate(Types::Conversation);
	masterCache->getItem<IndexedItems>(Types::Conversation,Constants::CONVERSATION_KEY)->put(conversation);
	
	getMaster()->getEventPropagator()->signal(
		Events::NewConversation,
		utilities::newArg<ConversationPtr>(conversation)
	);
	
	return conversation;
}

MailPtr CacheManager::newMail(Header *header, Body *body, Attachments *attachments)
{
	MailPtr mail = (Mail*)modelFactory->instantiate(Types::Mail);
	mail->setHeader(header);
	mail->setBody(body);
	mail->setAttachments(attachments);
	
	masterCache->getItem<IndexedItems>(0,Constants::MAIL_KEY)->put(mail);

	if (header->getUIDL()==NULL)
		header->setUIDL(new String(mail->getID().toFileSystemSafe()));
		
	getMaster()->getEventPropagator()->signal(
		Events::NewMail,
		utilities::newArg<MailPtr>(mail)
	);
	
	return mail;
}

void CacheManager::newCache (const cache::ID &iid, const cache::ID &cid)
{
	masterCache->getItem<IndexedItems>(0,iid)->createCache(cid);
}

FolderPtr CacheManager::newFolder(const cache::ID &id, const cache::Type &type, FolderDefinition *definition)
{
	FolderPtr folder = (Folder *)modelFactory->instantiate(type);
	folder->setFolderDefinition(definition);
	
	masterCache->getItem<IndexedItems>(0,Constants::FOLDER_KEY)->link(id, folder);
	folder->markCreate();
	
	getMaster()->getEventPropagator()->signal(
		Events::NewFolder,
		utilities::newArg<FolderPtr>(folder)
	);
	
	return folder;
}

FolderPtr CacheManager::newFolder(const cache::Type &type, FolderDefinition *definition)
{
	FolderPtr folder = (Folder *)modelFactory->instantiate(type);
	folder->setFolderDefinition(definition);
	
	masterCache->getItem<IndexedItems>(0,Constants::FOLDER_KEY)->put(folder);
	
	getMaster()->getEventPropagator()->signal(
		Events::NewFolder,
		utilities::newArg<FolderPtr>(folder)
	);
	
	return folder;
}

void CacheManager::onLoaded(Conversation *conversation)
{
	getMaster()->getEventPropagator()->signal(
		Events::LoadConversation, utilities::newArg<ConversationPtr>(conversation)
	);
}

void CacheManager::reindexConversation(Conversation *conversation)
{
	getMaster()->getActions()->reindexConversation(conversation);
}

void CacheManager::onLoaded(Folder *folder)
{
	getMaster()->getEventPropagator()->signal(
		Events::LoadFolder, utilities::newArg<FolderPtr>(folder)
	);
}

void CacheManager::onLoaded(Mail *mail)
{
	getMaster()->getEventPropagator()->signal(
		Events::LoadMail, utilities::newArg<MailPtr>(mail)
	);
}

void CacheManager::onLoaded(Settings *settings)
{
	getMaster()->getIdentity()->setName(settings->get(ConstantsSettings::USERNAME));
	getMaster()->getEventPropagator()->signal(
		Events::SettingsLoaded, utilities::newArg<SettingsPtr>(settings)
	);

}

void CacheManager::onDirty(Settings *settings)
{
	getMaster()->getIdentity()->setName(settings->get(ConstantsSettings::USERNAME));

	getMaster()->getEventPropagator()->signal(
		Events::SettingsChanged, utilities::newArg<SettingsPtr>(settings)
	);
}

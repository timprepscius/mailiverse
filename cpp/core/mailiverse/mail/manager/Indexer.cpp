/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "Indexer.h"
#include "Events.h"
#include "Master.h"
#include "Constants.h"
#include "../model/FolderFilterSimple.h"

using namespace mailiverse::mail::manager;
using namespace mailiverse::mail::model;
using namespace mailiverse::mail::cache;
using namespace mailiverse::mail;
using namespace mailiverse;

const ID 
	Indexer::KnownFolderIds::RootCache = ID::fromLong(1),
	Indexer::KnownFolderIds::SystemFoldersId = ID::combine(RootCache, ID::fromLong(1)),
	Indexer::KnownFolderIds::RepositoryId = ID::combine(RootCache, ID::fromLong(2)),
	Indexer::KnownFolderIds::AllId = ID::combine(RootCache, ID::fromLong(3)),
	Indexer::KnownFolderIds::InboxId = ID::combine(RootCache, ID::fromLong(4)),
	Indexer::KnownFolderIds::SentId = ID::combine(RootCache, ID::fromLong(5)),
	Indexer::KnownFolderIds::DraftsId = ID::combine(RootCache, ID::fromLong(6)),
	Indexer::KnownFolderIds::SpamId = ID::combine(RootCache, ID::fromLong(7)),
	Indexer::KnownFolderIds::TrashId = ID::combine(RootCache, ID::fromLong(8)),
	Indexer::KnownFolderIds::UserFoldersId = ID::combine(RootCache, ID::fromLong(100));

Indexer::Indexer()
{
}

Indexer::~Indexer()
{
}

void Indexer::initialize ()
{
	CacheManager *cacheManager = getMaster()->getCacheManager();

	FolderPtr _systemFolders = cacheManager->getFolder(Types::FolderMaster, Record(KnownFolderIds::SystemFoldersId,Date(0)));
	systemFolders = dynamic_cast<FolderMaster *>((Folder *)_systemFolders);

	FolderPtr _repository = cacheManager->getFolder(Types::FolderRepository, Record(KnownFolderIds::RepositoryId,Date(0)));
	FolderPtr _spam = cacheManager->getFolder(Types::FolderFilter, Record(KnownFolderIds::SpamId,Date(0)));
	FolderPtr _userFolders = cacheManager->getFolder(Types::FolderFilterSet, Record(KnownFolderIds::UserFoldersId,Date(0)));
	repository = dynamic_cast<FolderRepository *>((Folder *)_repository);
	spam = dynamic_cast<FolderFilter *>((Folder *)_spam);
	userFolders = dynamic_cast<FolderFilterSet *>((Folder *)_userFolders);
}

void Indexer::onFirstRun ()
{
	CacheManager *cacheManager = getMaster()->getCacheManager();
	
	cacheManager->newCache(Constants::FOLDER_KEY,KnownFolderIds::RootCache);

	FolderPtr _systemFolders = cacheManager->newFolder (
		KnownFolderIds::SystemFoldersId,
		Types::FolderMaster,
		new FolderDefinition(Constants::FOLDER_SYSTEM_ROOT)
	);
	
	FolderPtr _userFolders = cacheManager->newFolder (
		KnownFolderIds::UserFoldersId,
		Types::FolderFilterSet,
		new FolderDefinition(Constants::FOLDER_USER_ROOT)
	);

	FolderMaster *systemFolders = dynamic_cast<FolderMaster *>((Folder *)_systemFolders);
	FolderFilterSet *userFolders = dynamic_cast<FolderFilterSet *>((Folder *)_userFolders);

	FolderPtr fs;
	fs = cacheManager->newFolder(
		KnownFolderIds::RepositoryId,
		Types::FolderRepository, 
		new FolderDefinition(Constants::FOLDER_REPOSITORY)
	);
	fs->markCreate();
	systemFolders->addFolderBack(fs);

	fs = cacheManager->newFolder (
		KnownFolderIds::AllId,
		Types::FolderFilter,
		(new FolderDefinition(Constants::FOLDER_ALL))
			->setState(
				NULL,
				TransportState::fromString(
					TransportState::TRASH + TransportState::DELIMITER +
					TransportState::SPAM
				)
			)
	);
	systemFolders->addFolderBack(fs);

	fs = cacheManager->newFolder (
		KnownFolderIds::InboxId,
		Types::FolderFilter,
		(new FolderDefinition(Constants::FOLDER_INBOX))
			->setState(
				TransportState::fromString(TransportState::RECEIVED),
				TransportState::fromString(
					TransportState::TRASH + TransportState::DELIMITER +
					TransportState::SPAM
				)
			)
	);
	systemFolders->addFolderBack(fs);

	fs = cacheManager->newFolder (
		KnownFolderIds::SentId,
		Types::FolderFilter,
		(new FolderDefinition(Constants::FOLDER_SENT))
			->setState(
				TransportState::fromString(TransportState::SENT),
				TransportState::fromString(
					TransportState::TRASH + TransportState::DELIMITER +
					TransportState::SPAM
				)
			)
	);
	systemFolders->addFolderBack(fs);

	fs = cacheManager->newFolder (
		KnownFolderIds::DraftsId,
		Types::FolderFilter,
		(new FolderDefinition(Constants::FOLDER_DRAFTS))
			->setState(
				TransportState::fromString(TransportState::DRAFT + TransportState::DELIMITER + TransportState::SENDING),
				TransportState::fromString(
					TransportState::TRASH
				)
			)
	);
	systemFolders->addFolderBack(fs);

	fs = cacheManager->newFolder (
		KnownFolderIds::SpamId,
		Types::FolderFilter,
		(new FolderDefinition(Constants::FOLDER_SPAM))
			->setState(
				TransportState::fromString(TransportState::SPAM),
				TransportState::fromString(TransportState::TRASH)
			)
			->setBayesianDictionary(new Dictionary())
			->setAutoBayesian(false)
	);
	fs->markCreate();
	systemFolders->addFolderBack(fs);

	fs = cacheManager->newFolder (
		KnownFolderIds::TrashId,
		Types::FolderFilter,
		(new FolderDefinition(Constants::FOLDER_TRASH))
			->setState(
				TransportState::fromString(TransportState::TRASH),
				NULL
			)
	);
	systemFolders->addFolderBack(fs);
	
	// everything is created, let's incorporate into ourself
	initialize();
}


ConversationPtr Indexer::addMail (Mail *mail)
{
	Header *header = mail->getHeader();
	bool isNewConversation;
	
	ConversationPtr conversation = repository->getMatchingConversation(header);
	if (conversation)
	{
		conversation->addItem(mail);
		isNewConversation = false;
	}
	else
	{
		conversation = master->getCacheManager()->newConversation();
		conversation->addItem(mail);
		isNewConversation = true;
	}

	// before we do anything we do the spam detection
	conversation->getHeader()->getTransportState()->mark(
		TransportState::SPAM, spam->getFolderDefinition()->bayesianMatches(conversation)
	);
	
	if (isNewConversation)
		addConversation(conversation);
	else
		conversationChanged (conversation);
	

	// if the mail is new, no external key yet
	if (mail->getHeader()->getExternalKey())
	{
		systemFolders->addExternalKey(*mail->getHeader()->getExternalKey(), *mail->getHeader()->getDate());
	}

	if (mail->getHeader()->getUIDL())
	{
		systemFolders->addUIDL(*mail->getHeader()->getUIDL(), *mail->getHeader()->getDate());
	}

	return conversation;
}

void Indexer::addFailure (Mail *mail)
{
	addMail (mail);
}

void Indexer::addFailure (const String &externalKey, const Date &date)
{
	systemFolders->addExternalKey(externalKey, date);
}

void Indexer::addFailure (Direction::Enum direction, const String &path, const Date &date, const Exception &e)
{
	try
	{
		HeaderPtr header = new Header();
		header->setSubject(new String("Mail failed parsing.  Look at original for file."));
		header->setExternalKey(new String(path));
		header->setDate(new Date(date));
		if (direction == Direction::IN)
			header->setTransportState(TransportState::fromString(TransportState::RECEIVED));
		else
			header->setTransportState(TransportState::fromString(TransportState::SENT));

		BodyPtr body = new Body();
		body->setText(new String("Failed to load: " + e.what()));

		MailPtr mail = new Mail(header, body, NULL);
		addFailure(mail);
	}
	catch (Exception &em)
	{
		addFailure(path, date);
	}
}

void Indexer::addDuplicate (const String &externalKey, const Date &date)
{
	systemFolders->addExternalKey(externalKey, date);
}

bool Indexer::containsExternalKey (const String &externalKey)
{
	return systemFolders->containsExternalKey(externalKey);
}

bool Indexer::containsUIDL(const String &uidl)
{
	return systemFolders->containsUIDL(uidl);
}

void Indexer::addConversation (Conversation *conversation)
{
	for (auto &e : systemFolders->getFolders())
		e->conversationAdded(conversation);
	for (auto &e : userFolders->getFolders())
		e->conversationAdded(conversation);
}

void Indexer::removeConversation (Conversation *conversation)
{
	for (auto &e : systemFolders->getFolders())
		e->conversationDeleted(conversation);
	for (auto &e : userFolders->getFolders())
		e->conversationDeleted(conversation);
}

void Indexer::conversationChanged (Conversation *conversation)
{
	if (conversation)
	{
		for (auto &e : systemFolders->getFolders())
			e->conversationChanged(conversation);
		for (auto &e : userFolders->getFolders())
			e->conversationChanged(conversation);
	}

	master->getEventPropagator()->signalOnce(Events::ChangedConversation, utilities::newArg<ConversationPtr>(conversation));
}

void Indexer::replyMail (Conversation *conversation, Mail *mail)
{
	systemFolders->addUIDL(*mail->getHeader()->getUIDL(), *mail->getHeader()->getDate());

	conversationChanged(conversation);
}

ConversationPtr Indexer::newMail (Mail *mail)
{
	return addMail(mail);
}

FolderPtr Indexer::getSystemFolder (const String &folderName)
{
	for (auto &e : systemFolders->getFolders())
		if (e->getFolderDefinition()->getName() == folderName)
			return e;

	return NULL;
}

List<FolderPtr> Indexer::getSystemFolders ()
{
	return systemFolders->getFolders();
}

List<FolderPtr> Indexer::getUserFolders ()
{
	return userFolders->getFolders();
}

Folder *Indexer::getRepository ()
{
	return repository;
}

Folder *Indexer::getInbox ()
{
	return master->getCacheManager()->getFolder(Types::FolderFilter, Record(KnownFolderIds::InboxId,Date(0)));
}

FolderFilter *Indexer::getSpam ()
{
	return spam;
}


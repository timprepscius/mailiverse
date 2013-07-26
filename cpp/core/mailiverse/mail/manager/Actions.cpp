/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "Actions.h"
#include "Master.h"
#include "mailiverse/core/constants/ConstantsSettings.h"
#include "mailiverse/core/constants/ConstantsEnvironmentKeys.h"
#include "mailiverse/core/constants/ConstantsPushNotifications.h"
#include "mailiverse/core/crypt/Hash.h"
#include "mailiverse/mail/model/Manipulations.h"

using namespace mailiverse::core::crypt;
using namespace mailiverse::core::constants;
using namespace mailiverse::core;
using namespace mailiverse::mail::model;
using namespace mailiverse::mail::manager;
using namespace mailiverse::utilities;
using namespace mailiverse::core::util;
using namespace mailiverse;

Actions::Actions()
{
}

Actions::~Actions()
{
}

Body *Actions::calculateSignaturedBody (const String &body)
{
	String signature = getMaster()->getCacheManager()->getSettings()->get(ConstantsSettings::SIGNATURE);
	return new Body(new String("\r\n\r\n" + signature + "\r\n" + body), NULL);
}

Pair<ConversationPtr,MailPtr> Actions::newMail ()
{
	MailPtr mail = getMaster()->getCacheManager()->newMail (
		new Header (
			NULL, // external key
			NULL, // original key
			NULL,
			getMaster()->getIdentity(),
			new Recipients(),
			NULL,
			new Date(Date::now()),
			TransportState::fromString(TransportState::DRAFT),
			NULL
		),
		calculateSignaturedBody(""),
		new Attachments ()
	);

	ConversationPtr conversation = getMaster()->getIndexer()->newMail(mail);
	return Pair<ConversationPtr,MailPtr>(conversation,mail);
}

void Actions::saveMail (Conversation *conversation, Mail *mail)
{
	mail->getHeader()->setDate(new Date(Date::now()));
	mail->markDirty();
	conversation->itemChanged(mail);
	conversation->markDirty();
	
	getMaster()->getIndexer()->conversationChanged(conversation);
}

void Actions::deleteMail (Conversation *conversation, Mail *mail)
{
	conversation->removeItem(mail);
	getMaster()->getCacheManager()->deleteMail(mail);

	if (conversation->getNumItems()==0)
		deleteConversation(conversation);
	else
		getMaster()->getIndexer()->conversationChanged(conversation);
}

void Actions::deleteConversation (Conversation *conversation)
{
	for (auto &mail : conversation->getItems())
	{
		conversation->removeItem((model::Mail*)mail);
		getMaster()->getCacheManager()->deleteMail((model::Mail*)mail);
	}

	getMaster()->getIndexer()->removeConversation(conversation);
	getMaster()->getCacheManager()->deleteConversation(conversation);
}

MailPtr Actions::replyToAll (Conversation *conversation, Mail *mail)
{
	return reply (
		new Recipients (
			mail->getHeader()->filterMe(
				mail->getHeader()->calculateReplyAll(getMaster()->getCacheManager()),
				getMaster()->getIdentity()
			),
			IdentityList(),
			IdentityList(),
			IdentityList()
		),
		conversation, mail, calculateSignaturedBody(mail->getBody()->calculateReply())
	);
}

MailPtr Actions::replyTo (Conversation *conversation, Mail *mail)
{
	return reply (
		new Recipients (
			mail->getHeader()->calculateReplyTo(getMaster()->getCacheManager()),
			IdentityList(), IdentityList(), IdentityList()
		),
		conversation, mail, calculateSignaturedBody(mail->getBody()->calculateReply())
	);
}

MailPtr Actions::forward (Conversation *conversation, Mail *mail)
{
	return reply (
		new Recipients (
			IdentityList(),
			IdentityList(),
			IdentityList(),
			IdentityList()
		),
		conversation, mail, new Body(mail->getBody()->getText(), mail->getBody()->getHTML())
	);
}

void Actions::sendMail (Conversation *conversation, Mail *mail)
{
	Mailer *sendMail = getMaster()->getMailer();

	sendMail->queueMail(
		getMaster()->getEnvironment()->get(core::constants::ConstantsEnvironmentKeys::SMTP_PASSWORD),
		conversation,
		mail
	);
	
	processSendQueue();	
}

void Actions::reindexConversation (Conversation *conversation)
{
	getMaster()->getIndexer()->conversationChanged(conversation);
}

MailPtr Actions::reply (Recipients *recipients, Conversation *conversation, Mail *mail, Body *body)
{
	MailPtr reply = getMaster()->getCacheManager()->newMail (
		new Header (
			NULL, // external key
			NULL, // original key
			NULL,
			getMaster()->getIdentity(),
			recipients,
			mail->getHeader()->getSubject(),
			new Date(Date::now()),
			TransportState::fromString(TransportState::DRAFT),
			NULL
		),
		body ? body : new Body(),
		new Attachments ()
	);

	getMaster()->getIndexer()->replyMail(conversation, reply);
	conversation->addItem(reply);

	return reply;
}

void Actions::markConversations (const Set<cache::ID> &ids, const String &state, bool value)
{
	for (auto &i : ids)
	{
		ConversationPtr conversation = getMaster()->getCacheManager()->getConversation(Record(i,Date::None));
		conversation->apply(Manipulations::markState(conversation, state, value));
	}
}

void Actions::markSpam (const Set<cache::ID> &ids, bool value)
{
	markAndReindexConversations(ids, TransportState::SPAM, value);
}

void Actions::markAndReindexConversations (const Set<cache::ID> &ids, const String &state, bool value)
{
	for (auto &i : ids)
	{
		ConversationPtr conversation = getMaster()->getCacheManager()->getConversation(Record(i,Date::None));
		conversation->apply(Manipulations::markStateAndReindex(conversation, state, value));
	}
}

void Actions::addToUserFolders (const Set<cache::ID> &conversations, const Set<FolderFilterPtr> &folders)
{
	for (auto &i : conversations)
	{
		ConversationPtr conversation = getMaster()->getCacheManager()->getConversation(Record(i,Date::None));
		conversation->apply(Manipulations::addToUserFolders(conversation, folders));
	}
}

void Actions::removeFromUserFolder (const Set<cache::ID> &conversations, FolderFilter *folder)
{
	for (auto &i : conversations)
	{
		ConversationPtr conversation = getMaster()->getCacheManager()->getConversation(Record(i,Date::None));
		conversation->apply(Manipulations::removeFromUserFolder(conversation, folder));
	}
}


//-----------------------------------------------

void Actions::doCheckMail ()
{
	getMaster()->getArrivalsMonitor()->check();
}

void Actions::doPartialUpdate ()
{
	getMaster()->getCacheManager()->partialUpdate();
}

void Actions::doUpdate ()
{
	getMaster()->getCacheManager()->update();
}

void Actions::doFlush ()
{
	getMaster()->getCacheManager()->flush();
}

void Actions::doProcessSendQueue ()
{
	getMaster()->getMailer()->sendQueue();
}

void Actions::doEnableNotifications (const String &deviceId)
{
	String email = getMaster()->getIdentity()->getCanonicalEmail();
	SettingsPtr settings = getMaster()->getCacheManager()->getSettings();
	if (!settings)
		return;
		
	String notificationType = settings->get(ConstantsSettings::NOTIFICATION_TYPE, ConstantsPushNotifications::NOTIFICATION_TYPE_SHORT);

	HashSha256 hash;
	String emailHash = Base64::encode(hash.generate(email));

	json::Object o;
	o[ConstantsPushNotifications::USER] = json::String(emailHash);
	o[ConstantsPushNotifications::NOTIFICATION_TYPE] = json::String(notificationType);
	if (!deviceId.empty())
	{
		o[ConstantsPushNotifications::DEVICE_TYPE] = json::String(ConstantsPushNotifications::DEVICE_TYPE_APPLE);
		o[ConstantsPushNotifications::DEVICE_ID] = json::String(deviceId);
	}
	Block block = toBlock(toString(o));
	
	core::crypt::CryptorPtr cryptor = 
		new core::crypt::CryptorRSAAES(
			core::crypt::CryptorRSAFactory::fromResources(
				InternalResource::get("mailiverse::mail::manager::Notifications", "truststore.jks"),
				core::Block()
			)
		);
	
	HttpDelegate http;
	http.execute(
		HttpDelegate::PUT, 
		ConstantsClient::WEB_SERVER_TOMCAT + "Notifications",
		Map<String,String>(), 
		cryptor->encrypt(block)
	);

}

/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "Mailer.h"
#include "Master.h"
#include "Events.h"
#include "mailiverse/core/constants/ConstantsClient.h"
#include "mailiverse/utilities/Log.h"

using namespace mailiverse::mail::manager;
using namespace mailiverse::mail::model;
using namespace mailiverse::mail;
using namespace mailiverse::core::constants;
using namespace mailiverse::core::crypt;
using namespace mailiverse::core::util;
using namespace mailiverse::utilities;
using namespace mailiverse;

void Mailer::queueMail (const String &password, model::Conversation *conversation, model::Mail *mail)
{
	try
	{
		mail->getHeader()->unmarkState(model::TransportState::DRAFT);
		mail->getHeader()->markState(model::TransportState::SENDING);
		mail->getHeader()->getRecipients()->registerRecipients(getMaster()->getAddressBook());
		mail->markDirty();
		
		conversation->itemChanged(mail);
		getMaster()->getIndexer()->conversationChanged(conversation);

		SendObject s;
		s.password = password;
		s.conversation = conversation;
		s.mail = mail;
		
		Queue::Writer writer(queue);
		writer->push_back(s);
		queue.signal();
	}
	catch (Exception &e)
	{
		onSendFailed(conversation, mail, e);
	}
}

void Mailer::sendQueue ()
{
	Queue::Writer writer(queue);
	while (!writer->isEmpty())
	{
		SendObject s = writer->front();
		writer->pop_front();
		
		doSend(s.password, s.conversation, s.mail);
	}
}

void Mailer::doSend(const String &password, model::Conversation *conversation, model::Mail *mail)
{
	try
	{
		Map<String,String> sendMap;

		IdentityPtr identity = getMaster()->getIdentity();

		sendMap.put("user", identity->getEmail());
		sendMap.put("password", password);

		sendMap.put("from", identity->getFull());
		sendMap.put("to", utilities::joinToString(mail->getHeader()->getRecipients()->getTo(), ","));
		sendMap.put("cc", utilities::joinToString(mail->getHeader()->getRecipients()->getCc(), ","));
		sendMap.put("bcc", utilities::joinToString(mail->getHeader()->getRecipients()->getBcc(), ","));
		sendMap.put("replyTo", utilities::joinToString(mail->getHeader()->getRecipients()->getReplyTo(), ","));

		if (mail->getHeader()->getSubject())
			sendMap.put("subject", *mail->getHeader()->getSubject());
		if (mail->getBody()->hasText())
			sendMap.put("text", *mail->getBody()->getText());
		if (mail->getBody()->hasHTML())
			sendMap.put("html", *mail->getBody()->getHTML());

		sendMap.put("messageId", *mail->getHeader()->getUIDL());

		core::crypt::CryptorPtr cryptor = 
			new core::crypt::CryptorRSAAES(
				core::crypt::CryptorRSAFactory::fromResources(
					InternalResource::get("mailiverse::mail::manager::Mailer", "truststore.jks"),
					core::Block()
				)
			);
		
		json::Object o;
		for (auto &i : sendMap)
			o[i.first] = json::String(i.second);
			
		std::string serialized = "java.util.HashMap!" + toString(o);
		
		LogDebug(mailiverse::mail::manager::Mailer, "send map " << serialized);
		
		core::Block content = core::crypt::Base64::encodeBytes(cryptor->encrypt(toBlock(serialized)));
	
		HttpDelegate::HeadersContent result = httpDelegate->execute(
			HttpDelegate::PUT, ConstantsClient::WEB_SERVER_TOMCAT + "Send?random="+ toString(random.nextLong()), Map<String,String>(), content
		);
		
		LogDebug(mailiverse::mail::manager::Mailer, "send result " << toString(result.second));
		
		if (utilities::trim(toString(result.second))!="Ok")
			throw Exception("Mail failed with error");
		
		onSendSucceeded(conversation, mail);
	}
	catch (Exception &e)
	{
		LogDebug(mailiverse::mail::manager::Mailer, "exception " << e.what());
		onSendFailed(conversation, mail, e);
	}
}

void Mailer::onSendSucceeded (model::Conversation *conversation, model::Mail *mail)
{
	mail->getHeader()->unmarkState(model::TransportState::SENDING);
	mail->getHeader()->markState(model::TransportState::SENT);
	mail->markDirty();

	conversation->itemChanged(mail);
	master->getIndexer()->conversationChanged(conversation);
	master->getEventPropagator()->signal(Events::SendSucceeded, utilities::newArg<model::MailPtr>(mail));
}

void Mailer::onSendFailed (model::Conversation *conversation, model::Mail *mail, Exception &e)
{
	mail->getHeader()->unmarkState(model::TransportState::SENDING);
	mail->getHeader()->markState(model::TransportState::DRAFT);
	mail->markDirty();

	conversation->itemChanged(mail);
	master->getIndexer()->conversationChanged(conversation);
	master->getEventPropagator()->signal(Events::SendFailed, utilities::newArg<model::MailPtr>(mail));
}


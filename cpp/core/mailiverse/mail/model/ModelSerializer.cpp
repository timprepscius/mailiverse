/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "ModelSerializer.h"
#include "Mail.h"
#include "Conversation.h"
#include "Folder.h"
#include "../serializers/ZipSerializer.h"
#include "../serializers/JsonSerializerStandard.h"

using namespace mailiverse::mail::model;
using namespace mailiverse::mail;

ModelSerializer::ModelSerializer () 
{
	mailSerializer = new serializers::ZipSerializer(new serializers::JsonSerializerStandard<Mail>());
	conversationSerializer = new serializers::ZipSerializer(new serializers::JsonSerializerStandard<Conversation>());
	folderSerializer = new serializers::ZipSerializer(new serializers::JsonSerializerStandard<Folder>());
}

ModelSerializer::~ModelSerializer () 
{
	delete mailSerializer;
	delete conversationSerializer;
	delete folderSerializer;
}

cache::Value ModelSerializer::serialize(cache::Item *item)
{
	if (dynamic_cast<Mail *>(item))
		return mailSerializer->serialize(item);
	else
	if (dynamic_cast<Conversation *>(item))
		return conversationSerializer->serialize(item);
	else
	if (dynamic_cast<Folder *>(item))
		return folderSerializer->serialize(item);
		
	assert(false);
	return cache::Value();
}

void ModelSerializer::deserialize(cache::Item *item, const cache::Value &value)
{
	if (dynamic_cast<Mail * >(item))
		mailSerializer->deserialize(item, value);
	else
	if (dynamic_cast<Conversation *>(item))
		conversationSerializer->deserialize(item, value);
	else
	if (dynamic_cast<Folder *>(item))
		folderSerializer->deserialize(item, value);
	else
		assert(false);
}

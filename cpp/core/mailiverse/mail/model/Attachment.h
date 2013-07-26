/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_model_Attachment_h__
#define __mailiverse_mail_model_Attachment_h__

#include "mailiverse/utilities/SmartPtr.h"
#include "mailiverse/core/crypt/Base64.h"
#include "mailiverse/core/crypt/Hash.h"


namespace mailiverse {
namespace mail {
namespace model {

class Attachment
{
protected:
	StringPtr id;
	StringPtr disposition;
	StringPtr mimeType;

	core::BlockPtr data;
	bool loaded;

public:
	Attachment (String *id, String *disposition, String *mimeType)
	{
		this->disposition = disposition;
		this->id = id;
		this->mimeType = mimeType;
		loaded = false;
	}

	static String *getAttachmentId (String *disposition, String *id)
	{
		bool hasDisposition = disposition;
		bool hasId = id;
		bool hasFileName = false;

		if (hasDisposition)
		{
			String oneLineDisposition = utilities::join(utilities::splitLines(*disposition)," ");
			hasFileName = utilities::icontains(oneLineDisposition, "filename=");
		}

		if (hasDisposition || hasId)
		{
			if (hasId)
				return new String(*id);

			if (hasFileName)
				return new String(calculateId(*disposition));
		}

		return NULL;
	}

	static String calculateId (const String &disposition)
	{
		core::crypt::HashSha256 hash;
		return core::crypt::Base64::encode(hash.generate(disposition));
	}

	String getDataBase64 ()
	{
		return core::crypt::Base64::encode(*data);
	}

	String *getId ()
	{
		return id;
	}

	core::Block *getData ()
	{
		return data;
	}

	void setData (core::Block *data)
	{
		this->data = data;

		loaded = true;
	}

	void clearData ()
	{
		this->data = 0;
		loaded = false;
	}

	String *getDisposition ()
	{
		return disposition;
	}

	String *getMimeType ()
	{
		return mimeType;
	}

	bool isLoaded ()
	{
		return loaded;
	}
};

DECLARE_SMARTPTR(Attachment);

} /* namespace model */
} /* namespace mail */
} /* namespace mailiverse */
#endif /* ATTACHMENT_H_ */

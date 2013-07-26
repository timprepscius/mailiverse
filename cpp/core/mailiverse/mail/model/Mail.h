/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_model_Mail_h__
#define __mailiverse_mail_model_Mail_h__

#include "mailiverse/utilities/SmartPtr.h"
#include "Model.h"
#include "Header.h"
#include "Body.h"
#include "Attachments.h"

namespace mailiverse {
namespace mail {
namespace model {

class Mail : public Model
{
	DECLARE_ITEM(Mail);

protected:
	HeaderPtr header;
	BodyPtr body;
	AttachmentsPtr attachments;

public:
	Mail () {}

	Mail(Header *_header, Body *_body, Attachments *_attachments) :
		header(_header),
		body(_body),
		attachments(_attachments)
	{
		LogDebug(mailiverse::mail::model::Mail, "construct");
	}

	virtual ~Mail() 
	{
		LogDebug(mailiverse::mail::model::Mail, "destruct");
	}
	
	void onLoaded () override
	{
		getLibrary()->onLoaded(this);
	}

	Header *getHeader ()
	{
		return header;
	}

	void setHeader (Header *_header)
	{
		header = _header;
	}

	Body *getBody ()
	{
		return body;
	}

	void setBody (Body *_body)
	{
		body = _body;
	}

	Attachments *getAttachments ()
	{
		return attachments;
	}

	void setAttachments (Attachments *_attachments)
	{
		attachments = _attachments;
	}
};

DECLARE_SMARTPTR(Mail);

} /* namespace model */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* MAIL_H_ */

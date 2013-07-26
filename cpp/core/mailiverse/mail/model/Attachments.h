/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_model_Attachments_h__
#define __mailiverse_mail_model_Attachments_h__

#include "mailiverse/utilities/SmartPtr.h"
#include "Attachment.h"

namespace mailiverse {
namespace mail {
namespace model {

class Attachments
{
protected:
	List<AttachmentPtr> attachments;
	bool loaded;

public:
	Attachments() : loaded(false) {}
	virtual ~Attachments() {}

	void addAttachment (Attachment *attachment)
	{
		attachments.add(attachment);
	}

	void removeAttachmentId (Attachment *attachment)
	{
		attachments.remove(attachment);
	}

	List<AttachmentPtr> &getList ()
	{
		return attachments;
	}

	Attachment *getAttachment (const String &id)
	{
		for (auto &a : attachments)
		{
			if (*a->getId()==id)
				return a;
		}

		return 0;
	}

	void setLoaded (bool loaded)
	{
		this->loaded = loaded;
	}

	bool isLoaded ()
	{
		return loaded;
	}
};

DECLARE_SMARTPTR(Attachments);

} // namespace
} /* namespace utilities */
} /* namespace mailiverse */

#endif /* ATTACHMENTS_H_ */

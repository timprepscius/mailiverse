/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef RECIPIENTS_H_
#define RECIPIENTS_H_

#include "Identity.h"
#include "UnregisteredIdentity.h"
#include "AddressBook.h"
#include "Library.h"
#include "mailiverse/core/constants/ConstantsMailJson.h"

namespace mailiverse {
namespace mail {
namespace model {

class Recipients
{
public:
	IdentityList to, cc, bcc, replyTo;

	IdentityList toIdentityList (Library *library, String is)
	{
		return toIdentityList(library, utilities::split(is, ","));
	}
	
	IdentityList toIdentityList (Library *library, const std::vector<std::string> &parts)
	{
		AddressBook *addressBook = library->getAddressBook();
		IdentityList identities;

		for (auto &part : parts)
		{
			std::string trimmed = utilities::trim(part);
			if (trimmed.empty())
				continue;

			IdentityPtr identity = addressBook->getIdentity(new UnregisteredIdentity(trimmed));
			if (!identities.contains(identity))
				identities.add(identity);
		}

		return identities;
	}

	Recipients ()
	{
	}

	Recipients (
		const IdentityList &_to,
		const IdentityList &_cc,
		const IdentityList &_bcc,
		const IdentityList &_replyTo
	) :
		to(_to),
		cc(_cc),
		bcc(_bcc),
		replyTo(_replyTo)
	{}

	void add (const IdentityList &from, IdentityList &to)
	{
		for (auto &i : from)
			if (!to.contains(i))
				to.add(i);
	}

	void add (const Recipients *r)
	{
		add(r->to, to);
		add(r->cc, cc);
		add(r->bcc, bcc);
		add(r->replyTo, replyTo);
	}

	const IdentityList &getTo()
	{
		return to;
	}

	void setTo (const IdentityList &to)
	{
		this->to = to;
	}

	const IdentityList &getCc()
	{
		return cc;
	}

	void setCc (const IdentityList &cc)
	{
		this->cc = cc;
	}

	const IdentityList &getBcc()
	{
		return bcc;
	}

	void setBcc (const IdentityList &bcc)
	{
		this->bcc = bcc;
	}

	const IdentityList &getReplyTo()
	{
		return replyTo;
	}

	void setReplyTo (const IdentityList &replyTo)
	{
		this->replyTo = replyTo;
	}
	
	IdentityList &get (const String &bucket)
	{
		if (bucket == core::constants::ConstantsMailJson::To)
			return to;
		if (bucket == core::constants::ConstantsMailJson::Cc)
			return cc;
		if (bucket == core::constants::ConstantsMailJson::Bcc)
			return bcc;
		if (bucket == core::constants::ConstantsMailJson::ReplyTo)
			return replyTo;
			
		assert(false);
		return to;
	}

	IdentityList getAll ()
	{
		IdentityList all;
		Set<Identity *> found;
		
		all.insert(all.end(), to.begin(), to.end());
		all.insert(all.end(), cc.begin(), cc.end());
		all.insert(all.end(), bcc.begin(), bcc.end());
		all.insert(all.end(), replyTo.begin(), replyTo.end());

		IdentityList once;
		for (auto &i : all)
		{
			if (!found.contains(i))
			{
				once.push_back(i);
				found.add(i);
			}
		}
		
		return once;
	}

	void registerRecipients (AddressBook *addressBook, IdentityList &to)
	{
		IdentityList save = to;
		to.clear();

		for (auto &i : save)
		{
			to.add(addressBook->getIdentity((Identity *)i));
		}
	}

	void registerRecipients (AddressBook *addressBook)
	{
		registerRecipients(addressBook, to);
		registerRecipients(addressBook, cc);
		registerRecipients(addressBook, bcc);
		registerRecipients(addressBook, replyTo);
	}

	bool contains (Identity *identity)
	{
		return
			to.contains(identity) ||
			cc.contains(identity) ||
			cc.contains(identity) ||
			replyTo.contains(identity);
	}

	std::string toShortList ()
	{
		List<String> shorts;
		for (auto &i : getAll())
			shorts.add(i->getShortName());

		return utilities::join(shorts, ", ");
	}
};

DECLARE_SMARTPTR(Recipients);

} /* namespace model */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* RECIPIENTS_H_ */

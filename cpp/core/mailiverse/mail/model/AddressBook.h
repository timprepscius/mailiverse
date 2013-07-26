/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_model_AddressBook_h__
#define __mailiverse_mail_model_AddressBook_h__

#include "Identity.h"
#include "UnregisteredIdentity.h"
#include "mailiverse/utilities/Algorithm.h"
#include "mailiverse/utilities/Monitor.h"
#include "mailiverse/Types.h"

namespace mailiverse {
namespace mail {
namespace model {

class AddressBook
{
protected:
	utilities::EmptyMonitor monitor;
	
	IdentitySet people;
	Map<String, Identity *> indexedByEmail;

protected:
	void clearIndices (Identity *identity)
	{
		indexedByEmail.remove(identity->getEmail());
	}

	void index (Identity *identity)
	{
		if (!identity->getEmail().empty())
			indexedByEmail.put(identity->getEmail(),identity);
	}

public:
	AddressBook() {}
	virtual ~AddressBook() {}

	bool hasIdentity (UnregisteredIdentity *identity)
	{
		utilities::EmptyMonitor::Reader lock(monitor);
		return indexedByEmail.containsKey(identity->getEmail());
	}

	Identity *getIdentity (Identity *identity)
	{
		if (dynamic_cast<UnregisteredIdentity *>(identity))
			return registerIdentity (dynamic_cast<UnregisteredIdentity *>(identity));

		return identity;
	}

	Identity *registerIdentity (UnregisteredIdentityPtr identity)
	{
		utilities::EmptyMonitor::Writer lock(monitor);

		IdentityPtr stored = indexedByEmail.getv(identity->getEmail());

		if (stored)
		{
			clearIndices(stored);
			stored->copyFrom (*identity);
		}
		else
		{
			stored = new Identity();
			stored->copyFrom(*identity);
			people.add(stored);
		}

		index(stored);

		return stored;
	}

	void removeIdentity (Identity *identity)
	{
		utilities::EmptyMonitor::Writer lock(monitor);

		IdentityPtr stored = indexedByEmail.getv(identity->getEmail());

		if (stored)
		{
			clearIndices (stored);
			people.erase(stored);
		}
	}

	Vector<Identity *> getAddressList () const
	{
		utilities::EmptyMonitor::Reader lock(monitor);

		Vector<Identity *> result;
		result.addAll(people);
		
		return result;
	}

	Vector<Identity *> findPossibilities (const String &key, int max) const
	{
		utilities::EmptyMonitor::Reader lock(monitor);

		Vector<Identity *> results;

		for (auto &i : people)
		{
			Identity *person = i;
			if (
				utilities::icontains(person->getName(), key) || 
				utilities::icontains(person->getEmail(), key)
			)
			{
				results.push_back(person);
				if (results.size() >= max)
					break;
			}
		}
		
		return results;
	}	
	
public:
	IdentityList parseAddressString (const std::string &s)
	{
		IdentityList list;
		auto split = utilities::split(s, ",");
		for (auto &address : split)
		{
			address = utilities::trim(address);
			if (!address.empty())
				list.add(getIdentity (new UnregisteredIdentity(address)));
		}

		return list;
	}

	IdentityList parseUnfinishedAddressString (const std::string &s)
	{
		IdentityList list;
		auto split = utilities::split(s, ",");
		for (auto &address : split)
		{
			address = utilities::trim(address);
			if (!address.empty())
			{
				UnregisteredIdentityPtr uri = new UnregisteredIdentity (address);
				if (hasIdentity(uri))
					list.add(getIdentity(uri));
				else
					list.add((Identity*)uri);
			}
		}

		return list;
	}


};

DECLARE_SMARTPTR(AddressBook);

} /* namespace model */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* __mailiverse_mail_model_AddressBook_h__ */

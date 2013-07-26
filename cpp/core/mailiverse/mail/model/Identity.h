/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_Identity_h__
#define __mailiverse_mail_Identity_h__

#include <string>
#include "mailiverse/utilities/SmartPtr.h"
#include "mailiverse/Types.h"
#include "mailiverse/utilities/Strings.h"

namespace mailiverse {
namespace mail {
namespace model {

class Identity {

protected:
	static utilities::EmptyMonitor monitor;
	
	String name;
	String email;
	bool isPrimary;
	
public:
	Identity () : isPrimary(false) {}
	virtual ~Identity () {}

	Identity (const String &_name, const String &_email, bool _isPrimary=false) :
		name(_name),
		email(_email),
		isPrimary(_isPrimary)
	{
		
	}

	Identity (const String &full)
	{
		std::pair<String, String> parsed = parseFull(full);
		name = parsed.first;
		email = parsed.second;
	}
	
	static std::pair<String, String> parseFull (const String &full)
	{
		String name;
		String email;

		int indexOfLessThan = full.rfind('<');
		int indexOfGreaterThan = full.find('>', indexOfLessThan);
		if (indexOfLessThan != -1 && indexOfGreaterThan != -1)
		{
			name = full.substr(0, indexOfLessThan);
			name = utilities::trim(name);

			String t = full.substr(indexOfLessThan+1);
			email = t.substr(0, indexOfGreaterThan - indexOfLessThan - 1);
		}
		else
		{
			email = utilities::trim(full);
		}

		return std::pair<String, String>(name, email);
	}
	
	bool hasValidEmail ()
	{
		utilities::EmptyMonitor::Reader lock(monitor);
		
		int oneAt = email.find("@");
		if (oneAt < 1)
			return false;

		int twoAt = email.find("@", oneAt+1);
		if (twoAt != -1)
			return false;
			
		if (email.find_first_of(" \t\n\r,'\"")!=-1)
			return false;

		return true;
	}

	String getFull () const
	{
		utilities::EmptyMonitor::Reader lock(monitor);

		if (!name.empty())
			return name + " " + getEnclosedEmail();

		return getEnclosedEmail();
	}

	String toString() const
	{
		return getFull();
	}

	String getName () const
	{
		utilities::EmptyMonitor::Reader lock(monitor);

		return name;
	}

	void setName (const String &_name)
	{
		utilities::EmptyMonitor::Writer lock(monitor);
		name = _name;
	}

	String getEmail () const
	{
		utilities::EmptyMonitor::Reader lock(monitor);
		return email;
	}

	String getEnclosedEmail () const
	{
		return "<" + getEmail() + ">";
	}
	
	String getCanonicalEmail () const
	{
		utilities::EmptyMonitor::Reader lock(monitor);
		
		return utilities::toLowerCase(email);
	}

	String getShortName () const
	{
		utilities::EmptyMonitor::Reader lock(monitor);

		if (isPrimary)
			return "me";

		int space = name.find(" ");
		int comma = name.find(",");

		if (!name.empty())
			if (space!=-1)
				if (comma!=-1)
					return utilities::trimQuotes(name.substr(space+1));
				else
					return utilities::trimQuotes(name.substr(0, space));
			else
				return name;

		int at = email.find("@");
		if (at != -1)
			return email.substr(0, at);

		return email;
	}

	String getLongName () const
	{
		utilities::EmptyMonitor::Reader lock(monitor);
		
		return name;
	}

	void copyFrom (const Identity &identity)
	{
		utilities::EmptyMonitor::Writer lock(monitor);
	
		if (!isPrimary)
		{
			if (name.empty())
				name = identity.name;
			if (email.empty())
				email = identity.email;
		}
	}
	
	void setPrimary (bool primary)
	{
		this->isPrimary = primary;
	}
} ;

DECLARE_SMARTPTR(Identity);
typedef List<IdentityPtr> IdentityList;
typedef Set<IdentityPtr> IdentitySet;

} // namespace
} // namespace
} // namespace

#endif

/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_model_FolderMaster_h__
#define __mailiverse_mail_model_FolderMaster_h__

#include "FolderSet.h"
#include "mailiverse/core/crypt/Hash.h"
#include "mailiverse/core/crypt/Base64.h"
#include "mailiverse/Types.h"

namespace mailiverse {
namespace mail {
namespace model {

class FolderMaster : public FolderSet
{
	DECLARE_ITEM(FolderMaster);

protected:
	core::crypt::HashSha256 hasher;
	Map<String,Date> externalKeys;
	Map<String,Date> uidls;

public:
	FolderMaster() :
		FolderSet(Types::FolderFilter)
	{}

	String hash (const String &key)
	{
		return core::crypt::Base64::encode(hasher.generate(key));
	}

	void addExternalKey (const String &id, const Date &date)
	{
		externalKeys.put(hash(id), date);
		markDirty();
	}

	bool containsExternalKey (const String &id)
	{
		return externalKeys.containsKey(hash(id));
	}

	void addUIDL (const String &uidl, const Date &date)
	{
		uidls.put(hash(uidl), date);
		markDirty();
	}

	bool containsUIDL (const String &uidl)
	{
		return uidls.containsKey(hash(uidl));
	}

	const Map<String,Date> &getUIDLHashes()
	{
		return uidls;
	}

	void setUIDLHashes (const List<Pair<String,Date> > &l)
	{
		uidls.clear();
		uidls.insert(l.begin(), l.end());
	}

	const Map<String,Date> &getExternalKeyHashes ()
	{
		return externalKeys;
	}

	void setExternalKeyHashes (const List<Pair<String,Date> > &l)
	{
		externalKeys.clear();
		externalKeys.insert(l.begin(), l.end());
	}
};

DECLARE_SMARTPTR(FolderMaster);

} /* namespace model */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* __mailiverse_mail_model_FolderMaster_h__ */

/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_model_Settngs_h__
#define __mailiverse_mail_model_Settngs_h__

#include "mailiverse/utilities/SmartPtr.h"
#include "Model.h"

namespace mailiverse {
namespace mail {
namespace model {

class Settings : public Model
{
	DECLARE_ITEM(Settings);

public:
	typedef Model Super;
	typedef Map<String,String> KV;
	
protected:
	KV settings;
	
public:	
	Settings() 
	{
	}
	
	virtual ~Settings ()
	{
	}
	
	void onLoaded () override
	{
		Super::onLoaded();
		getLibrary()->onLoaded(this);
	}
	
	void onDirty () override
	{
		Super::onDirty();
		getLibrary()->onDirty(this);
	}

	String get (const String &key)
	{
		return settings.getv(key);
	}
	
	String get(const String &key, const String &defaultz)
	{
		if (settings.containsKey(key))
			return get(key);
		
		return defaultz;
	}
	
	void set(const String &key, const String &value)
	{
		String existing = get(key);
		if (existing != value)
		{
			settings.put(key, value);
			markDirty();
		}
	}

	KV &getKV() 
	{
		return settings;
	}
	
	void setKV(const KV &kv)
	{
		settings = kv;
	}
};

DECLARE_SMARTPTR(Settings);

} // namespace
} // namespace
} // namespace

#endif 

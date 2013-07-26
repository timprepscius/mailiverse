/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_mail_cache_Item_h__
#define __mailiverse_mail_cache_Item_h__

#include "mailiverse/utilities/SmartPtr.h"
#include "Info.h"
#include "ItemSerializer.h"
#include "mailiverse/utilities/Log.h"

namespace mailiverse {
namespace mail {
namespace cache {

class ItemCollection;
DECLARE_SMARTPTR(ItemCollection);

#define DECLARE_ITEM(x) \
	public: \
		static std::string className() { return #x; } \
		virtual std::string getClassName() const { return x::className(); } \
	private: \

class Item : public Info
{
	DECLARE_ITEM(Item);
	ItemCollectionPtr owner;
	
public:
	typedef Info Super;
	
protected:
	typedef Vector<core::util::Callback<>> OnLoadCallbacks;
	OnLoadCallbacks onLoadCallbacks;
	
public:
	Item() :
		owner(NULL)
	{
		LogDebug(mailiverse::mail::cache::Item, this << " constructor");
	}

	virtual ~Item ();
	
	virtual void setOwner (ItemCollection *owner)
	{
		this->owner = owner;
	}
	
	ItemCollection *getOwner ()
	{
		return owner;
	}

	virtual void flush () { }
	virtual void update () { }
	
	virtual void onDirty () override;
	
	void apply (core::util::Callback<> callback)
	{
		if (isLoaded())
			callback.invoke();
		else
			onLoadCallbacks.push_back(callback);
	}

	void onLoaded () override
	{
		Super::onLoaded();
		
		OnLoadCallbacks _onLoadCallbacks = onLoadCallbacks;
		onLoadCallbacks.clear();
		
		for (auto &i : _onLoadCallbacks)
			apply(i);
	}
	
	void onShutdown () override
	{
		Super::onShutdown ();
		onLoadCallbacks.clear();
		setOwner(NULL);
	}
};

typedef utilities::SmartPtr<Item> ItemPtr;
typedef utilities::WeakPtr<Item> ItemWeakPtr;


} /* namespace cache */
} /* namespace mail */
} /* namespace mailiverse */

#endif /* __mailiverse_mail_cache_Item_h__ */

/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#include "Item.h"
#include "ItemCollection.h"

using namespace mailiverse::mail::cache;

Item::~Item()
{
	LogDebug(mailiverse::mail::cache::Item, this << " destructor " << getID().str());
}

void Item::onDirty ()
{
	if (owner)
		owner->onDirty(this);
}


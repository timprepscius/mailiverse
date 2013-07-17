/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.cache;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import core.callback.Callback;
import core.callback.CallbackDefault;

public abstract class ItemCollection extends Item implements ItemOwner
{
	@Override
	public boolean hasDirtyChildren ()
	{
		return areAnyChildrenDirty();
	}
	
	public void onDirty (Item item)
	{
		onDirty();
	}
	
	public abstract Map<?, ? extends Item> getItemMap ();
	
	public final boolean areAnyChildrenDirty ()
	{
		for (Entry<?, ? extends Item> i : getItemMap().entrySet())
		{
			Item item = i.getValue();
			if (item.isDirty() || item.hasDirtyChildren())
				return true;
		}
		
		return false;
	}
	
	public void onFlush ()
	{
	}
	
	public Callback onFlush_ ()
	{
		return new CallbackDefault() {
			public void onSuccess(Object... arguments) throws Exception {
				onFlush();
				next(arguments);
			}
		};
	}
	
	public void itemAdded (Item item)
	{
		item.setOwner(this);
		
		if (item.isDirty() || item.hasDirtyChildren())
			onDirty(item);
	}
	
	public void itemRemoved (Item item)
	{
		item.setOwner(null);
	}
	
	public String toString ()
	{
		return super.toString() + (areAnyChildrenDirty() ? " ChildrenDirty" : "");
	}
}

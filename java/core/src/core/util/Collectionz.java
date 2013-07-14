/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Collectionz
{
	public static <T> List<T> toMutableList(T[] a)
	{
		ArrayList<T> l = new ArrayList<T>();
		for (T t : a)
			l.add(t);
		
		return l;
	}
	
	public static <T> Collection<T> filterNull (Collection<T> items)
	{
		List<T> l = new ArrayList<T>();

		for (T i : items)
			if (i != null)
				l.add(i);
		
		return l;
	}
	
	public static <T> Collection<T> filterNull (T... items)
	{
		List<T> l = new ArrayList<T>();

		for (T i : items)
			if (i != null)
				l.add(i);
		
		return l;
	}
	
	static public void removeByFirst (Collection<? extends Pair> items, Object remove)
	{
		Object found = null;
		for (Pair x : items)
		{
			if (x.first.equals(remove))
			{
				found = x;
				break;
			}
		}
		
		if (found != null)
			items.remove(found);
	}

	static public void removeBySecond (Collection<? extends Pair> items, Object remove)
	{
		Object found = null;
		for (Pair x : items)
		{
			if (x.second.equals(remove))
			{
				found = x;
				break;
			}
		}
		
		if (found != null)
			items.remove(found);
	}

	static public boolean containsByFirst (Collection<? extends Pair> items, Object remove)
	{
		for (Pair x : items)
		{
			if (x.first.equals(remove))
				return true;
		}
		
		return false;
	}

	static public boolean containsBySecond (Collection<? extends Pair> items, Object remove)
	{
		for (Pair x : items)
		{
			if (x.second.equals(remove))
				return true;
		}
		
		return false;
	}
}

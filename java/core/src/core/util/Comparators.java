/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.util;

import java.util.Comparator;

public class Comparators
{
	public static class SortBySecond<S> implements Comparator<Pair<?,S>>
	{
		Comparator<S> c;
		
		public SortBySecond(Comparator<S> c)
		{
			this.c = c;
		}
		
		@Override
		public int compare(Pair<?,S> p1, Pair<?,S> p2)
		{
			return c.compare(p1.second, p2.second);
		}
	}

	public static class SortByFirst<S> implements Comparator<Pair<S,?>>
	{
		Comparator<S> c;
		
		public SortByFirst(Comparator<S> c)
		{
			this.c = c;
		}
		
		@Override
		public int compare(Pair<S,?> p1, Pair<S,?> p2)
		{
			return c.compare(p1.first, p2.first);
		}
	}
	
	public static class SortByFirstReverse<S> implements Comparator<Pair<S,?>>
	{
		Comparator<S> c;
		
		public SortByFirstReverse(Comparator<S> c)
		{
			this.c = c;
		}
		
		@Override
		public int compare(Pair<S,?> p1, Pair<S,?> p2)
		{
			return c.compare(p2.first, p1.first);
		}
	}

	public static class SortBySecondNatural<S extends Comparable<S>> implements Comparator<Pair<?,S>>
	{

		@Override
		public int compare(Pair<?, S> o1, Pair<?, S> o2)
		{
			return o1.second.compareTo(o2.second);
		}
	}

	public static class SortByFirstNatural<S extends Comparable<S>> implements Comparator<Pair<S,?>>
	{
		@Override
		public int compare(Pair<S,?> o1, Pair<S,?> o2)
		{
			return o1.first.compareTo(o2.first);
		}
	}

	public static class SortBySecondNaturalOpposite<S extends Comparable<S>> implements Comparator<Pair<?,S>>
	{
		@Override
		public int compare(Pair<?, S> o1, Pair<?, S> o2)
		{
			return o2.second.compareTo(o1.second);
		}
	}

	public static class SortByFirstNaturalOpposite<S extends Comparable<S>> implements Comparator<Pair<S,?>>
	{
		@Override
		public int compare(Pair<S,?> o1, Pair<S,?> o2)
		{
			return o2.first.compareTo(o1.first);
		}
	}
}

/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.util;

import java.io.Serializable;

public class Pair <F, S> implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public F first;
	public S second;
	
	public Pair(F first, S second)
	{
		this.first = first;
		this.second = second;
	}
	
	public Pair ()
	{
		first = null;
		second = null;
	}
	
	public boolean equals (Object other)
	{
		if (this == other)
			return true;
		
		if (other instanceof Pair)
		{
			Pair<F,S> pair = (Pair<F,S>)other;
			return this.first.equals(pair.first) && this.second.equals(pair.second);	
		}
		
		return false;
	}
	
	public static <X,Y> Pair<X,Y> create(X x, Y y)
	{
		return new Pair<X,Y>(x,y);
	}
}

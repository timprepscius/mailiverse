/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.util;

import java.io.Serializable;

public class Triple <F, S, T> implements Serializable
{
	private static final long serialVersionUID = 1L;
	
	public F first;
	public S second;
	public T third;
	
	public Triple (F first, S second, T third)
	{
		this.first = first;
		this.second = second;
		this.third = third;
	}
	
	public Triple ()
	{
		first = null;
		second = null;
		third = null;
	}
}

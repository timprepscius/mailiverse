/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.util;

import core.util.CallSingle;

abstract class CallSingleWithVariables<T,V> implements CallSingle<T,V>
{
	protected Object[] v;
	
	public CallSingleWithVariables(Object... v)
	{
		this.v = v; 
	}
	
	@SuppressWarnings("unchecked")
	public <T> T V(int i)
	{
		return (T)v[i];
	}
}

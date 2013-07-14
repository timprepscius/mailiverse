/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.util;

public abstract class RunnableWithVariables implements Runnable
{
	protected Object[] v;
	
	public RunnableWithVariables(Object... v)
	{
		this.v = v; 
	}
	
	@SuppressWarnings("unchecked")
	public <T> T V(int i)
	{
		return (T)v[i];
	}
}

/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.callback;


public abstract class CallbackWithVariables extends Callback
{
	protected Object[] v;
	
	public CallbackWithVariables()
	{
	}
	
	public CallbackWithVariables(Object...v)
	{
		this.v = v;
	}
	

	@SuppressWarnings("unchecked")
	public <T> T V(int i)
	{
		return (T)v[i];
	}
}

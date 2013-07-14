/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.util;

import java.util.Date;

import core.callback.Callback;
import core.callback.CallbackDefault;

public class LogOut
{
	String prefix;
	String cached = "";
	DateFormat dateFormat = new DateFormat("HH:mm:ss.SSS");
	
	public LogOut (Class<?> clazz)
	{
		prefix = clazz.getName()+":";
	}
	
	public LogOut(String prefix) 
	{
		this.prefix = prefix;
	}

	public final String build (Object...arguments)
	{
		StringBuilder builder = new StringBuilder();
		builder.append(prefix);

		for (int j=0;j<arguments.length; ++j)
		{
			builder.append(" ");
			builder.append(arguments[j]);
		}
	
		return builder.toString();
	}
	
	public final void println (Object...arguments)
	{
		LogPlatform.println(build(arguments));
	}
	
	public final void print (String s)
	{
		cached += s;
	}
	
	public final void flush ()
	{
		LogPlatform.println(cached);
		cached = "";
	}

	public final void debug (Object...arguments)
	{
		println(arguments);
	}
	
	public final void error (Object...arguments)
	{
		println(arguments);
	}

	public final void debugPart (Object...arguments)
	{
		print(Strings.concat(arguments, " "));
	}
	
	public final void debugFlush ()
	{
		flush();
	}
	
	public final String format (String format, Object...args)
	{
		String s = "";
		s += format;
		for (Object o : args)
		{
			s += o;
			s += " ";
		}
		
		return s;
	}
	
	public void exception (Exception e)
	{
		LogPlatform.printException(e);
	}
	
	public final Callback debug_(Object...args)
	{
		return new CallbackDefault(new Object[] { args }) {
			
			@Override
			public void onSuccess(Object... arguments) throws Exception {
				Object[] args = V(0);
				LogOut.this.debug(args);
				next(arguments);
			}
		};
	}

	public void trace(Object...args) {
	}
}

package core.util;

import core.callback.Callback;
import core.callback.CallbackDefault;

public class LogNull
{
	LogOut out;
	
	public LogNull (Class<?> clazz)
	{
		out = new LogOut(clazz);
	}
	
	public LogNull(String string) {
		out = new LogOut(string);
	}

	public final void debug (Object...arguments)
	{
		out.debug(arguments);
	}
	
	public final void debugPart (Object...arguments)
	{
		out.debugPart(arguments);
	}
	
	public final void debugFlush ()
	{
		out.debugFlush();
	}
	
	public final void error (Object...arguments)
	{
		out.println(arguments);
	}
	
	public final String format (String format, Object...args)
	{
		return out.format(format,  args);
	}
	
	public final Callback debug_(Object...args)
	{
		return new CallbackDefault(args) {
			
			@Override
			public void onSuccess(Object... arguments) throws Exception {
				debug(v);
				debug(arguments);
				next(arguments);
			}
		};
	}
	
	public final void trace (Object...args)
	{
		
	}
	
	public void exception (Exception e)
	{
		out.exception(e);
	}
}

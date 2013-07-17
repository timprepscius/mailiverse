package com.jordanzimmerman;     

import java.io.IOException;

/**
 * Exception thrown when authentication fails
 * <p>
 * Released into the public domain
 *
 * @author Jordan Zimmerman - jordan@jordanzimmerman.com
 * @see SRPFactory Full Documentation
 * @version 1.1
 */
public class SRPAuthenticationFailedException extends IOException
{
	public SRPAuthenticationFailedException()
	{
		super();
	}

	public SRPAuthenticationFailedException(String message)
	{
		super(message);
	}
}

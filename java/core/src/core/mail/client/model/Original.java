/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.model;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.List;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;

import core.util.LogNull;

@Export()
public class Original implements Exportable
{
	static LogNull log = new LogNull(Original.class);

	String path;
	boolean loaded;
	
	Exception exception;
	byte[] data;
	
	public Original (String path)
	{
		this.path = path;
	}
	
	public String getPath ()
	{
		return path;
	}
	
	public void setData (byte[] data)
	{
		this.data = data;
		this.loaded = true;
	}
	
	public boolean hasData ()
	{
		return data!=null;
	}
	
	public String getDataAsString () throws UnsupportedEncodingException
	{
		return new String(data);
	}
	
	public boolean isLoaded ()
	{
		return loaded;
	}
	
	public boolean hasException()
	{
		return exception!=null;
	}

	public void setException(Exception exception)
	{
		this.exception = exception;
		this.loaded = true;
	}
	
	public Exception getException()
	{
		return exception;
	}
	
	public boolean equals (Original rhs)
	{
		return super.equals(rhs);
	}
}

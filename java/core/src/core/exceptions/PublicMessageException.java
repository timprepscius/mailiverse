/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.exceptions;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;

@Export
public class PublicMessageException extends RuntimeException implements Exportable
{
	public String message;
	
	public PublicMessageException (String message)
	{
		this.message = message;
	}
	
	public PublicMessageException (String message, Exception e)
	{
		super(e);
		this.message = message;
	}
	
	@Override
	public String getMessage ()
	{
		return message;
	}
}

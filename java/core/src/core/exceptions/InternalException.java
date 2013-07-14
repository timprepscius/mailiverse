/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.exceptions;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;

@Export
public class InternalException extends PublicMessageException implements Exportable
{
	public InternalException (Exception e)
	{
		super("An internal error has occurred with " + e.getClass().getName(), e);
	}
	
	public InternalException (String with, Exception e)
	{
		super("An internal error has occurred with " + with, e);
	}
}

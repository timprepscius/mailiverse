/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.model;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;

@Export()
public class UnregisteredIdentity extends Identity
{
	private static final long serialVersionUID = 1L;

	public UnregisteredIdentity (String name, String email)
	{
		super(name, email, false);
	}

	public UnregisteredIdentity (String full)
	{
		super(full);
	}
	
}

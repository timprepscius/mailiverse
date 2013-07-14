/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.crypt;

import java.io.Serializable;


public class PasswordValidator implements Serializable
{
	private static final long serialVersionUID = 1L;

	public String user;
	public String password;
	byte[] payload;
	
	public PasswordValidator (String user, String password, byte[] payload)
	{
		this.user = user;
		this.password = password;
		this.payload = payload;
	}
	
	public PasswordValidator (String user, String password)
	{
		this(user, password, null);
	}
}

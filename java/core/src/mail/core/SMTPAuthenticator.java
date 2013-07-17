/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.core;

import javax.mail.PasswordAuthentication;

public class SMTPAuthenticator extends javax.mail.Authenticator
{
	String email;
	String password;
	
	public SMTPAuthenticator (String email, String password)
	{
		this.email = email;
		this.password = password;
	}
	
    public PasswordAuthentication getPasswordAuthentication()
    {
        return new PasswordAuthentication(email, password);
    }
}

/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.model;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;


import java.io.Serializable;

import core.util.LogNull;
import core.util.LogOut;
import core.util.Pair;
import core.util.Strings;

@Export()
public class Identity implements Serializable, Exportable
{
	private static final long serialVersionUID = 1L;
	static LogNull log = new LogNull(Identity.class);
	
	String name;
	String email;
	boolean isPrimary = false;
	String publicKey;
	
	protected Identity ()
	{
	}
	
	protected Identity (String name, String email, boolean isPrimary)
	{
		this.name = name;
		this.email = email;
		this.isPrimary = isPrimary;
	}
	
	/**
	 * Rewrite with regular expressions
	 * @param full
	 */
	public Identity (String full)
	{
		Pair<String, String> parsed = parseFull(full);
		this.name = parsed.first;
		this.email = parsed.second;
	}
	
	public void setPrimary (boolean primary)
	{
		this.isPrimary = primary;
	}
	
	public static Pair<String,String> parseFull (String full)
	{
		String name = null;
		String email = null;
		
		int indexOfLessThan = full.lastIndexOf('<');
		int indexOfGreaterThan = full.indexOf('>', indexOfLessThan);
		if (indexOfLessThan != -1 && indexOfGreaterThan != -1)
		{
			name = full.substring(0, indexOfLessThan);
			name = name.trim();
			if (name.isEmpty())
				name = null;

			String t = full.substring(indexOfLessThan+1);
			email = t.substring(0, indexOfGreaterThan - indexOfLessThan - 1);
		}
		else
		{
			email = full;
		}
		
		return new Pair<String, String>(name, email);
	}
	
	public String getFull()
	{
		if (name != null && !name.isEmpty())
			return name + " " + getEnclosedEmail();
		
		return getEnclosedEmail();
	}
	
	public String toString()
	{
		return getFull();
	}
	
	public String debug ()
	{
		return super.toString() + getFull();
	}

	public String getName() 
	{
		return name;
	}

	public void setName(String name) 
	{
		this.name = name;
	}
	
	public String getEmail() 
	{
		return email;
	}
	
	public String getEnclosedEmail ()
	{
		if (email != null)
			return "<" + email + ">";
		
		return null;
	}

	public void setEmail(String email) 
	{
		this.email = email;
	}
	
	public String getShortName ()
	{
		if (isPrimary)
			return "me";
		
		if (name != null)
			if (name.contains(" "))
				if (name.contains(","))
					return Strings.trimQuotes(name.substring(name.indexOf(' ')+1));
				else
					return Strings.trimQuotes(name.substring(0, name.indexOf(' ')));
			else
				return name;
		
		return email;
	}
	
	public String getLongName ()
	{
		return name;
	}
	
	/**
	 * This should check which information is better, 
	 * possibly based on amount of information in the information?
	 * @param identity
	 */
	public void copyFrom (Identity identity)
	{
		if (!this.isPrimary)
		{
			log.debug("copying from ", identity.isPrimary, identity.name, identity.email, this.isPrimary, this.name, this.email);

			if (
				this.name == null || 
				( (identity.name != null) && (this.name.length() < identity.name.length()) )
				)
			{
				this.name = identity.name;
			}
	
			if (this.email == null)
				this.email = identity.email;
		}
	}
	
	public void setPublicKey (String publicKey)
	{
		this.publicKey = publicKey;
	}

	public boolean hasPublicKey() 
	{
		return publicKey != null;
	}

	public String getPublicKey() 
	{
		return publicKey;
	}
}

package mail.client.model;

import mail.client.CacheManager;

public class PublicKey extends Model 
{
	String email;
	String publicKey;
	
	public PublicKey(CacheManager manager) 
	{
		super(manager);
	}
	
	public void setEmail (String email)
	{
		this.email = email;
	}
	
	public String getEmail (String email)
	{
		return email;
	}
	
	public void setPublicKey (String publicKey)
	{
		this.publicKey = publicKey;
	}
	
	public String getPublicKey ()
	{
		return publicKey;
	}

}

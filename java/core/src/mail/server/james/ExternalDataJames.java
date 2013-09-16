package mail.server.james;

import org.apache.james.cli.probe.impl.JmxServerProbe;

import mail.server.db.ExternalData;

public class ExternalDataJames implements ExternalData  
{
	JmxServerProbe jamesConnection;

	public ExternalDataJames () throws Exception
	{
		jamesConnection = new JmxServerProbe("localhost");	
	}
	
	@Override
	public void addUser(String name, String password) throws Exception 
	{
		jamesConnection.addUser(name, password);
	}

	@Override
	public void removeUser(String name) throws Exception 
	{
		jamesConnection.removeUser(name);
	}

	@Override
	public void setUserPassword(String name, String password) throws Exception 
	{
		jamesConnection.setPassword(name, password);
	}

}

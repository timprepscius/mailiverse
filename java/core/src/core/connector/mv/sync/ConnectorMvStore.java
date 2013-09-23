package core.connector.mv.sync;

import java.util.List;

import core.callback.CallbackSync;
import core.connector.ConnectorException;
import core.connector.FileInfo;
import core.connector.mv.ClientInfoMvStore;
import core.connector.sync.StoreConnector;
import core.util.HttpDelegateFactory;

public class ConnectorMvStore implements StoreConnector 
{
	private ClientInfoMvStore clientInfo;
	private core.connector.mv.async.ConnectorMvStore connector;

	public ConnectorMvStore (ClientInfoMvStore clientInfo) throws Exception
	{
		this.clientInfo = clientInfo;
		this.connector = new core.connector.mv.async.ConnectorMvStore(clientInfo, HttpDelegateFactory.create());
	}
	
	@Override
	public void open() throws ConnectorException 
	{
	}

	@Override
	public void close() throws ConnectorException 
	{
	}

	@Override
	public List<FileInfo> listDirectory(String path) throws ConnectorException 
	{
		try
		{
			CallbackSync sync = new CallbackSync(connector.list_(path)).invoke();
			return sync.<List<FileInfo>>export();
		}
		catch (Exception e)
		{
			throw new ConnectorException(e);
		}
	}

	@Override
	public void createDirectory(String path) throws ConnectorException 
	{
		try
		{
			CallbackSync sync = new CallbackSync(connector.createDirectory_(path)).invoke();
			sync.checkException();
		}
		catch (Exception e)
		{
			throw new ConnectorException(e);
		}
	}

	@Override
	public byte[] get(String path) throws ConnectorException 
	{
		try
		{
			CallbackSync sync = new CallbackSync(connector.get_(path)).invoke();
			return sync.<byte[]>export();
		}
		catch (Exception e)
		{
			throw new ConnectorException(e);
		}
	}

	@Override
	public byte[] get(String path, long size) throws ConnectorException 
	{
		try
		{
			CallbackSync sync = new CallbackSync(connector.get_(path)).invoke();
			return sync.<byte[]>export();
		}
		catch (Exception e)
		{
			throw new ConnectorException(e);
		}
	}

	@Override
	public void put(String path, byte[] contents) throws ConnectorException 
	{
		try
		{
			CallbackSync sync = new CallbackSync(connector.put_(path, contents)).invoke();
			sync.checkException();
		}
		catch (Exception e)
		{
			throw new ConnectorException(e);
		}
	}

	@Override
	public void move(String from, String to) throws ConnectorException 
	{
		try
		{
			CallbackSync sync = new CallbackSync(connector.move_(from, to)).invoke();
			sync.checkException();
		}
		catch (Exception e)
		{
			throw new ConnectorException(e);
		}
	}

	@Override
	public void delete(String path) throws ConnectorException 
	{
		try
		{
			CallbackSync sync = new CallbackSync(connector.delete_(path)).invoke();
			sync.checkException();
		}
		catch (Exception e)
		{
			throw new ConnectorException(e);
		}
	}

	@Override
	public boolean ensureDirectories(String... folders) 
	{
		CallbackSync sync = new CallbackSync(connector.ensureDirectories_(folders)).invoke();
		return sync.<Boolean>exportNoException();
	}
}

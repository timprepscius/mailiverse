/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.connector.sync;

import java.util.List;

import core.connector.ConnectorException;
import core.connector.FileInfo;
import core.crypt.Cryptor;
import core.util.Streams;
import core.util.Zip;


public class EncryptedStoreConnector implements StoreConnector
{
	StoreConnector store;
	Cryptor cryptor;
	
	public EncryptedStoreConnector (Cryptor cryptor, StoreConnector store)
	{
		this.store = store;
		this.cryptor = cryptor;
	}
	
	@Override
	public void open() throws ConnectorException
	{
		store.open();
	}

	@Override
	public void close() throws ConnectorException
	{
		store.close();
	}

	@Override
	public List<FileInfo> listDirectory(String path) throws ConnectorException
	{
		return store.listDirectory(path);
	}

	@Override
	public void createDirectory(String path) throws ConnectorException
	{
		store.createDirectory(path);
	}

	@Override
	public byte[] get(String path) throws ConnectorException
	{
		try
		{
			return Zip.inflate(cryptor.decrypt(store.get(path)));
		}
		catch (Exception e)
		{
			throw new ConnectorException(e);
		}
	}

	@Override
	public byte[] get(String path, long size) throws ConnectorException
	{
		return get(path);
	}

	@Override
	public void put(String path, byte[] contents) throws ConnectorException
	{
		try
		{
			store.put(path, cryptor.encrypt(Zip.deflate(contents)));
		}
		catch (Exception e)
		{
			throw new ConnectorException(e);
		}
	}

	@Override
	public void move(String from, String to) throws ConnectorException
	{
		store.move(from, to);
	}

	@Override
	public void delete(String path) throws ConnectorException
	{
		store.delete(path);
	}

	@Override
	public boolean ensureDirectories(String... folders)
	{
		return store.ensureDirectories(folders);
	}

}

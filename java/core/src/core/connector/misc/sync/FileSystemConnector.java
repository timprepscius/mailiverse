/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.connector.misc.sync;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import core.connector.ConnectorException;
import core.connector.FileInfo;
import core.connector.sync.StoreConnector;
import core.util.FileSystem;
import core.util.Streams;


public class FileSystemConnector implements StoreConnector
{
	String prefix;
	
	public FileSystemConnector (String root)
	{
		this.prefix = root + "/";
	}
		
	@Override
	public void open() throws ConnectorException
	{
		File directory = new File(prefix);
		if (!directory.exists())
			throw new ConnectorException("Store directory does not exist");
	}

	@Override
	public void close()
	{
	}

	@Override
	public List<FileInfo> listDirectory(String path) throws ConnectorException
	{
		return FileSystem.allFilesFor(new File(prefix + path));
	}

	@Override
	public void createDirectory(String path) throws ConnectorException
	{
		File dir = new File (prefix + path);
		if (!dir.mkdir())
		{
			throw new ConnectorException("Unable to create directory");
		}
	}

	@Override
	public byte[] get(String path) throws ConnectorException
	{
		try
		{
			return Streams.readFullyBytes(new FileInputStream(prefix + path));
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
			if (path.contains("/"))
				ensureDirectories(path.substring(0, path.lastIndexOf("/")));
			
			FileOutputStream fos = new FileOutputStream(prefix + path);
			fos.write(contents);
			fos.close();
		}
		catch (IOException e)
		{
			throw new ConnectorException(e);
		}
	}

	@Override
	public void move(String from, String to) throws ConnectorException
	{
		File fromFile = new File(prefix + from);
		File toFile = new File (prefix, to);
		
		if (!fromFile.renameTo(toFile))
			throw new ConnectorException("move file failed");
	}

	@Override
	public void delete(String path) throws ConnectorException
	{
		File file = new File(prefix + path);
		if (!file.delete())
			throw new ConnectorException("delete file failed");
	}

	public boolean ensureDirectories (String ... folders)
	{
		for (String folder : folders)
		{
			File path = new File (prefix + folder);
			path.mkdirs();
		}
			
		return true;
	}
}

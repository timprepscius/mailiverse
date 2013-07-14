/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.connector.dropbox.sync;

import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import core.connector.dropbox.ClientInfoDropbox;

import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.DropboxAPI.Entry;
import com.dropbox.client2.session.AccessTokenPair;
import com.dropbox.client2.session.AppKeyPair;
import com.dropbox.client2.session.Session;
import com.dropbox.client2.session.WebAuthSession;

import core.connector.ConnectorException;
import core.connector.FileInfo;
import core.connector.sync.StoreConnector;
import core.util.Streams;


public class DropboxConnector implements StoreConnector 
{
	ClientInfoDropbox clientInfo;
	DropboxAPI<?> db;
	
	public DropboxConnector (ClientInfoDropbox clientInfo)
	{
		this.clientInfo = clientInfo;
	}

	public DropboxAPI<?> createConnection (ClientInfoDropbox info)
	{
		AppKeyPair appKeyPair = new AppKeyPair(info.getAppKey(),info.getAppSecret());
		AccessTokenPair userTokenKeyPair = new AccessTokenPair(info.getTokenKey(), info.getTokenSecret());
		
		WebAuthSession sourceSession = 
			new WebAuthSession(appKeyPair, Session.AccessType.APP_FOLDER, userTokenKeyPair);

		DropboxAPI<?> sourceClient = new DropboxAPI<WebAuthSession>(sourceSession);
		
		return sourceClient;
	}

	
	public void open () throws ConnectorException
	{
		try
		{
			db = createConnection(clientInfo);
		}
		catch (Exception e)
		{
			throw new ConnectorException(e);
		}
	}
	
	public void close () 
	{
		db = null;
	}
	
	protected String getGlobalPath (String path)
	{
		if (clientInfo.getUserPrefix() != null)
			return "/" + clientInfo.getUserPrefix() + "/" + path;
			
		return "/" + path;
	}
	
	protected String getUserPath (String path)
	{
		if (clientInfo.getUserPrefix() != null)
			return path.substring(2 + clientInfo.getUserPrefix().length());
		
		return path.substring(1);
	}
	
	@Override
	public List<FileInfo> listDirectory(String path) throws ConnectorException 
	{
		try
		{
			if (!path.endsWith("/"))
				path = path + "/";
			
			Entry directory = db.metadata(getGlobalPath(path), 10000, null, true, null);
			List<FileInfo> listing = new ArrayList<FileInfo>(directory.contents.size());
			
			SimpleDateFormat dateTimeFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
			for (Entry file : directory.contents)
			{
				if (file.isDeleted)
					continue;
				
				String fullPath = getUserPath(file.path);
				String relativePath = fullPath.substring(path.length());
				
				listing.add(
					new FileInfo(
						fullPath,
						relativePath,
						file.bytes, dateTimeFormat.parse(file.modified),
						file.rev
					)
				);
			}
			
			Collections.sort(listing, new FileInfo.SortByDateAscending());
			
			return listing;
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
			db.createFolder(getGlobalPath(path));
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
			Entry meta = db.metadata(getGlobalPath(path), 1, null, true, null);
			if (meta.isDeleted)
				throw new ConnectorException("File deleted");
			
			return Streams.readFullyBytes(db.getFileStream(getGlobalPath(path), null));
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
			db.putFileOverwrite(getGlobalPath(path), new ByteArrayInputStream(contents), contents.length, null);			
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
			db.move(getGlobalPath(from), getGlobalPath(to));
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
			db.delete(getGlobalPath(path));
		}
		catch (Exception e)
		{
			throw new ConnectorException(e);
		}
	}

	public boolean ensureDirectories (String ... folders)
	{
		for (String folder : folders)
		{
			String[] parts = folder.split("/");

			String path = "";
			for (String part : parts)
			{
				if (!path.isEmpty())
					path += "/";
				
				path += part;
				String fullPath = getGlobalPath(path);
				try
				{
					db.createFolder(fullPath);
				}
				catch (Exception e) 
				{ 
					System.out.format("Folder[%s] already exists.\n", fullPath);
				}
			}
		}
			
		return true;
	}
}

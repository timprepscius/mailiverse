/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.connector.dropbox.async;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import core.callback.Callback;
import core.callback.CallbackDefault;
import core.callback.CallbackWithVariables;
import core.connector.FileInfo;
import core.connector.async.AsyncStoreConnectorHelper;
import core.connector.dropbox.ClientInfoDropbox;
import core.util.DateFormat;
import core.util.FastRandom;
import core.util.HttpDelegate;
import core.util.LogNull;

public class ConnectorDropbox extends AsyncStoreConnectorHelper
{
	static LogNull log = new LogNull(ConnectorDropbox.class);
	
	ClientInfoDropbox info;
	HttpDelegate httpDelegate;
	FastRandom fastRandom;
	
	public ConnectorDropbox(ClientInfoDropbox clientInfo, HttpDelegate httpDelegate)
	{
		this.info = clientInfo;
		this.httpDelegate = httpDelegate;
		fastRandom = new FastRandom();
	}
	
	protected String getGlobalPath (String path)
	{
		if (info.getUserPrefix() != null)
			return info.getUserPrefix() + "/" + path;
			
		return path;
	}

	public void listDirectoryFinished (String containingPath, Callback callback, Object... arguments)
	{
		log.debug("listDirectoryFinished");
		try
		{
			if (arguments[0] instanceof Exception)
				throw (Exception)arguments[0];
			
			String result = (String)arguments[0];
			List<FileInfo> fileInfos = new ArrayList<FileInfo>();
			
			JSONObject o = new JSONObject(result);
			JSONArray contents = (JSONArray) o.get("contents");
	
			DateFormat dateTimeFormat = new DateFormat("EEE, d MMM yyyy HH:mm:ss Z");

			for (int i=0; i<contents.length(); ++i)
			{
				JSONObject f = (JSONObject) contents.get(i);
				String dropboxPath = (String) f.get("path");
				
				int realPath = dropboxPath.indexOf(containingPath);
				if (realPath == -1)
					continue;
				
				String path = dropboxPath.substring(realPath);
				String relativePath = path.substring(containingPath.length());
				
				long size = ((Integer)f.get("bytes")).longValue();
				String time = (String) f.get("modified");
				String revision = (String) f.getString("rev");
				
				Date date = dateTimeFormat.parse(time);
				FileInfo fi = new FileInfo(path, relativePath, size, date, revision);
				
				fileInfos.add(fi);
			}
			
			Collections.sort(fileInfos, new FileInfo.SortByDateAscending());
			for (FileInfo i : fileInfos)
				log.debug ("path: ", i.path, " date:", i.date);

			callback.invoke(fileInfos);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
			callback.invoke(e);
		}
	}
	
	public void searchDirectoryFinished (String containingPath, Callback callback, Object... arguments)
	{
		log.debug("listDirectoryFinished");
		try
		{
			if (arguments[0] instanceof Exception)
				throw (Exception)arguments[0];
			
			String result = (String)arguments[0];
			List<FileInfo> fileInfos = new ArrayList<FileInfo>();
			
			JSONArray contents = new JSONArray(result);
			DateFormat dateTimeFormat = new DateFormat("EEE, d MMM yyyy HH:mm:ss Z");

			for (int i=0; i<contents.length(); ++i)
			{
				JSONObject f = (JSONObject) contents.get(i);
				String dropboxPath = (String) f.get("path");
				
				int realPath = dropboxPath.indexOf(containingPath);
				if (realPath == -1)
					continue;
				
				String path = dropboxPath.substring(realPath);
				String relativePath = path.substring(containingPath.length());
				
				long size = ((Integer)f.get("bytes")).longValue();
				String time = (String) f.get("modified");
				String revision = (String) f.getString("rev");
				
				Date date = dateTimeFormat.parse(time);
				FileInfo fi = new FileInfo(path, relativePath, size, date, revision);
				
				fileInfos.add(fi);
			}
			
			Collections.sort(fileInfos, new FileInfo.SortByDateAscending());
			for (FileInfo i : fileInfos)
				log.debug ("path: ", i.path, " date:", i.date);

			callback.invoke(fileInfos);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
			callback.invoke(e);
		}
	}	
	public void list (String path, Callback callback)
	{
		if (path.endsWith("/"))
			doList(path, callback);
		else
			doSearch(path, callback);
	}
	
	public void doSearch(String path, Callback callback)
	{
		log.debug("searchDirectory",path);
		try
		{
			String API_METADATA_URL="https://api.dropbox.com/1/search/sandbox";
	
			String globalPath = getGlobalPath(path);
			String directory = globalPath.substring(0, globalPath.lastIndexOf('/'));
			String file = globalPath.substring(globalPath.lastIndexOf('/')+1);
			
			String url = 
				API_METADATA_URL +
		        "/" + directory +
				"?query=" + file +
		        "&oauth_consumer_key=" + info.getAppKey() +
		        "&oauth_token=" + info.getTokenKey() + 
		        "&oauth_signature_method=PLAINTEXT" + 
		        "&oauth_signature=" + info.getAppSecret() + "%26" + info.getTokenSecret() + 
		        "&oauth_timestamp=" + new Date().getTime() + 
		        "&oauth_nonce=" + fastRandom.nextInt();  
			
			httpDelegate.execute (HttpDelegate.GET, url, null, false, false, null,
				new CallbackWithVariables(callback, path) {
					@Override
					public void invoke(Object... arguments)
					{
						Callback callback = V(0);
						String path = V(1);
						searchDirectoryFinished(path, callback, arguments);
					}
				}
			);
		}
		catch (Exception e)
		{
			callback.invoke(e);
		}
	}
	
	public void doList(String path, Callback callback) 
	{
		log.debug("listDirectory",path);
		try
		{
			String API_METADATA_URL="https://api.dropbox.com/1/metadata/sandbox";
	
			String url = 
				API_METADATA_URL +
		        "/" + getGlobalPath(path) + "?" +
		        "oauth_consumer_key=" + info.getAppKey() + "&" +
		        "oauth_token=" + info.getTokenKey() + "&" +
		        "oauth_signature_method=PLAINTEXT" + "&" +
		        "oauth_signature=" + info.getAppSecret() + "%26" + info.getTokenSecret() + "&" + 
		        "oauth_timestamp=" + new Date().getTime() + "&" +
		        "oauth_nonce=" + fastRandom.nextInt();  
			
			httpDelegate.execute (HttpDelegate.GET, url, null, false, false, null,
				new CallbackWithVariables(callback, path) {
					@Override
					public void invoke(Object... arguments)
					{
						Callback callback = V(0);
						String path = V(1);
						listDirectoryFinished(path, callback, arguments);
					}
				}
			);
		}
		catch (Exception e)
		{
			callback.invoke(e);
		}
	}

	public void createDirectory(String path, Callback callback) 
	{
		log.debug("createDirectory",path);
		try
		{
			String API_METADATA_URL="https://api.dropbox.com/1/fileops/create_folder";
	
			String url = 
				API_METADATA_URL +
				"?root=sandbox" + 
		        "&path="+ getGlobalPath(path) + 
		        "&locale=en" + 
		        "&oauth_consumer_key=" + info.getAppKey() +
		        "&oauth_token=" + info.getTokenKey() +
		        "&oauth_signature_method=PLAINTEXT" +
		        "&oauth_signature=" + info.getAppSecret() + "%26" + info.getTokenSecret() + 
		        "&oauth_timestamp=" + new Date().getTime() +
		        "&oauth_nonce=" + fastRandom.nextInt();  
			
			httpDelegate.execute(HttpDelegate.GET, url, null, false, false, null, callback);
		}
		catch (Exception e)
		{
			callback.invoke(e);
		}
		
	}

	public void get(String path, Callback callback) 
	{
		log.debug("get",path);
		try
		{
			String API_METADATA_URL="https://api-content.dropbox.com/1/files/sandbox";
	
			String url = 
				API_METADATA_URL +
		        "/" + getGlobalPath(path) + "?" +
		        "oauth_consumer_key=" + info.getAppKey() + "&" +
		        "oauth_token=" + info.getTokenKey() + "&" +
		        "oauth_signature_method=PLAINTEXT" + "&" +
		        "oauth_signature=" + info.getAppSecret() + "%26" + info.getTokenSecret() + "&" + 
		        "oauth_timestamp=" + new Date().getTime() + "&" +
		        "oauth_nonce=" + fastRandom.nextInt();  
			
			log.debug(url);
			
			httpDelegate.execute(HttpDelegate.GET, url, null, false, true, null, grabVersionGet_().setReturn(callback));
		}
		catch (Exception e)
		{
			callback.invoke(e);
		}
	}

	public void put(String path, byte[] contents, Callback callback) 
	{
		log.debug("put",path);
		try
		{
			String API_METADATA_URL="https://api-content.dropbox.com/1/files_put/sandbox";
	
			String url = 
				API_METADATA_URL +
		        "/" + getGlobalPath(path) + "?" +
		        "oauth_consumer_key=" + info.getAppKey() + "&" +
		        "oauth_token=" + info.getTokenKey() + "&" +
		        "oauth_signature_method=PLAINTEXT" + "&" +
		        "oauth_signature=" + info.getAppSecret() + "%26" + info.getTokenSecret() + "&" + 
		        "oauth_timestamp=" + new Date().getTime() + "&" +
		        "oauth_nonce=" + fastRandom.nextInt();  
			
			log.debug(url);
			
			httpDelegate.execute(HttpDelegate.PUT, url, null, true, false, contents, grabVersionPut_().setReturn(callback));
		}
		catch (Exception e)
		{
			callback.invoke(e);
		}
	}

	public void delete(String path, Callback callback) 
	{
		log.debug("delete",path);
		try
		{
			String API_METADATA_URL="https://api.dropbox.com/1/fileops/delete";
	
			String url = 
				API_METADATA_URL +
				"?root=sandbox" +
				"&path=" + getGlobalPath(path) +
		        "&oauth_consumer_key=" + info.getAppKey() +
		        "&oauth_token=" + info.getTokenKey() +
		        "&oauth_signature_method=PLAINTEXT" + 
		        "&oauth_signature=" + info.getAppSecret() + "%26" + info.getTokenSecret() + 
		        "&oauth_timestamp=" + new Date().getTime() + 
		        "&oauth_nonce=" + fastRandom.nextInt();  
			
			log.debug(url);
			
			httpDelegate.execute(HttpDelegate.POST, url, null, false, false, null, callback);
		}
		catch (Exception e)
		{
			callback.invoke(e);
		}
	}
	
	public Callback grabVersionPut_()
	{
		return new CallbackDefault() {
			public void onSuccess(Object... arguments) throws Exception {
				String response = (String) arguments[0];
				JSONObject json = new JSONObject(response);
				next(json.getString("rev"));
			}
		};
	}
	
	public Callback grabVersionGet_()
	{
		return new CallbackDefault() {
			public void onSuccess(Object... arguments) throws Exception {
				String[][] headers = (String[][])arguments[1];
				for (String[] pair : headers)
				{
					if (pair[0].equals("x-dropbox-metadata"))
					{
						JSONObject json = new JSONObject(pair[1]);
						next(arguments[0], json.getString("rev"));
						
						return;
					}
				}
				
				throw new Exception("No x-dropbox-metadata response header");
			}
		};
	}
}

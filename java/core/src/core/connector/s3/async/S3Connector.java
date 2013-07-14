/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.connector.s3.async;

import java.util.ArrayList;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import core.util.Base64;
import core.callback.Callback;
import core.callback.CallbackDefault;
import core.callback.CallbackWithVariables;
import core.connector.FileInfo;
import core.connector.async.AsyncStoreConnectorHelper;
import core.connector.s3.ClientInfoS3;
import core.crypt.HashSha256;
import core.crypt.HmacSha1;
import core.util.DateFormat;
import core.util.FastRandom;
import core.util.HttpDelegate;
import core.util.LogNull;
import core.util.Strings;
import core.util.XML;

public class S3Connector extends AsyncStoreConnectorHelper 
{
	static LogNull log = new LogNull(S3Connector.class);
	final int LOCK_INTERVAL = 10 * 1000;	
	
	ClientInfoS3 info;
	HttpDelegate httpDelegate;
	
	HmacSha1 mac;
	static FastRandom fastRandom = new FastRandom();
	

	protected String createUrlPrefix ()
	{
		return "https://" + info.getBucketEndpoint() + "/" + info.getBucketName() + "/";
	}
	
	protected String createRandomPostfix ()
	{
		return "random=" + fastRandom.nextLong();
	}
	
	// This method converts AWSSecretKey into crypto instance.
	protected void setKey(String AWSSecretKey) throws Exception
	{
		mac = new HmacSha1(Strings.toBytes(AWSSecretKey));
	}

	// This method creates S3 signature for a given String.
	protected String sign(String data) throws Exception
	{
	  // Signed String must be BASE64 encoded.
	  byte[] signBytes = mac.mac(Strings.toBytes(data));
	  String signature = Base64.encode(signBytes);
	  return signature;
	}	
	
	protected String format(String format, Date date)
	{
		DateFormat df = new DateFormat(format);
		String dateString = df.format(date, 0) + " GMT";
		return dateString;
	}
	
	protected String[][] makeHeaders (String keyId, String method, String contentMD5, String contentType, int contentLength, Date date, String resource) throws Exception
	{
		String fmt = "EEE, dd MMM yyyy HH:mm:ss";
		String dateString = format(fmt, date);
		
		// Generate signature
		StringBuffer buf = new StringBuffer();
		buf.append(method).append("\n");
		buf.append(contentMD5).append("\n");
		buf.append(contentType).append("\n");
		buf.append("\n"); // empty real date header
		buf.append("x-amz-date:");
		buf.append(dateString).append("\n");
		buf.append(resource);
		
		log.debug("Signing:{" + buf.toString() + "}");
		String signature = sign(buf.toString());
		
		String[][] headers;
		if (method.equals("PUT"))
		{
			headers = new String[][] {
				{"X-Amz-Date" , dateString },
				{"Content-Type", contentType },
				{"Content-Length", ""+contentLength },
				{"Authorization", "AWS " + keyId + ":" + signature }
			};
		}
		else
		{
			headers = new String[][] {
				{"X-Amz-Date" , dateString },
				{"Authorization", "AWS " + keyId + ":" + signature }
			};
		}
		
		return headers;
	}
	
	public S3Connector(ClientInfoS3 clientInfo, HttpDelegate httpDelegate) throws Exception
	{
		this.info = clientInfo;
		this.httpDelegate = httpDelegate;
		
		setKey (info.getSecretKey());
	}
	
	long toVersionFromString (String s) throws Exception
	{
		HashSha256 hash = new HashSha256();
		byte[] result = hash.hash(Strings.toBytes(s));
		
		long l = 
			((long)result[0]) |
			((long)result[1] << 8) |
			((long)result[2] << 16) |
			((long)result[3] << 24);
		        
		return l;
	}	

	public void listDirectoryFinished (List<FileInfo> files, Callback callback, String path, Object... arguments)
	{
		log.debug("listDirectoryFinished");
		try
		{
			if (arguments[0] instanceof Exception)
				throw (Exception)arguments[0];
			
			String result = (String)arguments[0];
			log.trace(result);
			
			DateFormat dateTimeFormat = new DateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z' Z");

			Object doc = XML.parse(result);
			Object[] nodes = XML.getElementsByTagName(doc, "Contents");
            for (Object currentNode : nodes)
            {
            	if ( XML.getNodeType(currentNode) == XML.ELEMENT_NODE ) 
            	{
            		Object keyNode = XML.getElementsByTagName(currentNode, "Key")[0];
            		Object etagNode = XML.getElementsByTagName(currentNode, "ETag")[0];
            		Object sizeNode = XML.getElementsByTagName(currentNode, "Size")[0];
            		Object lastModifiedNode = XML.getElementsByTagName(currentNode, "LastModified")[0];

            		log.trace(XML.textOf(keyNode), XML.textOf(sizeNode), XML.textOf(etagNode), XML.textOf(lastModifiedNode));
            		
            		String fullPath = XML.textOf(keyNode);
            		String relativePath = fullPath.substring(path.length());
            		
            		FileInfo fi = new FileInfo(
	                	fullPath,
	                	relativePath,
	                	Long.parseLong(XML.textOf(sizeNode)),
	                	dateTimeFormat.parse(XML.textOf(lastModifiedNode) + " GMT"),
	                	XML.textOf(etagNode)
	                );
            		
            		files.add(fi);
            	}
            }
            
            if (XML.textOf(XML.getElementsByTagName(doc, "IsTruncated")[0]).equals("true"))
    		{
            	log.debug("results were truncated, requesting more...");
            	listIterative(files, path, callback);
    		}
            else
            {
            	log.debug("results were complete, invoking callback");
            	
    			Collections.sort(files, new FileInfo.SortByDateAscending());
    			for (FileInfo i : files)
    				log.trace ("path: ", i.path, " date:", i.date);

    			callback.invoke(files);
    		}
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			
			callback.invoke(e);
		}
	}
		
	@Override
	public void list(String path, Callback callback) 
	{
		listIterative(new ArrayList<FileInfo>(), path, callback);
	}
	
	public void listIterative(List<FileInfo> files, String path, Callback callback) 
	{
		log.debug("listDirectory",path);
		try
		{
			String url = 
				createUrlPrefix() +
				"?prefix=" + path + "&max-keys=1000" + 
				(!files.isEmpty() ? ("&marker=" + files.get(files.size()-1).path) : "") +
				"&" + createRandomPostfix();
			
			log.debug(url);

			String[][] headers = makeHeaders (
				info.getAccessId(), "GET", "", "", 0, new Date(), "/" + info.getBucketName() + "/"
			);
			
			httpDelegate.execute (HttpDelegate.GET, url, headers, false, false, null,
				new CallbackWithVariables(files, callback, path) {
					@Override
					public void invoke(Object... arguments)
					{
						List<FileInfo> files = V(0);
						Callback callback = V(1);
						String path = V(2);
						listDirectoryFinished(files, callback, path, arguments);
					}
				}
			);
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			
			callback.invoke(e);
		}
	}
	
	@Override
	public void createDirectory(String path, Callback callback) 
	{
		log.debug("createDirectory",path);
		callback.invoke(path);
	}

	@Override
	public void get(String path, Callback callback) 
	{
		log.debug("get",path);
		try
		{
			String url = 
				createUrlPrefix() + 
				path + 
				"?" + createRandomPostfix();
			
			log.debug(url);
			
			String[][] headers = makeHeaders (
				info.getAccessId(), "GET", "", "", 0, new Date(), "/" + info.getBucketName() + "/" + path
			);
			
			httpDelegate.execute(HttpDelegate.GET, url, headers, false, true, null, grabVersion_(true).setReturn(callback));
		}
		catch (Throwable e)
		{
			callback.invoke(e);
		}
	}
	
	public Callback grabVersion_(boolean includeResponseData)
	{
		return new CallbackDefault(includeResponseData) {
			public void onSuccess(Object... arguments) throws Exception {
				
				boolean includeResponseData = V(0);
				String[][] headers = (String[][])arguments[1];
				
				for (String[] pair : headers)
				{
					if (pair[0].equals("ETag"))
					{
						if (includeResponseData)
							next(arguments[0], pair[1]);
						else
							next(pair[1]);
						
						return;
					}
				}
				
				throw new Exception("No ETag Response header");
			}
		};
	}
	
	@Override
	public void put(String path, byte[] contents, Callback callback) 
	{
		log.debug("put",path);
		try
		{
			String url = 
				createUrlPrefix() + 
				path + 
				"?" + createRandomPostfix();
			
			log.debug(url);
			
			String[][] headers = makeHeaders (
					info.getAccessId(), "PUT", "", "application/octet-stream", contents.length, new Date(), "/" + info.getBucketName() + "/" + path
				);

			httpDelegate.execute(HttpDelegate.PUT, url, headers, true, false, contents, grabVersion_(false).setReturn(callback));
		}
		catch (Throwable e)
		{
			callback.invoke(e);
		}
	}
	
	@Override
	public void delete(String path, Callback callback) 
	{
		log.debug("delete",path);
		
		try
		{
			String url = 
				createUrlPrefix() + 
				path + 
				"?" + createRandomPostfix();
				
			log.debug(url);
			
			String[][] headers = makeHeaders (
					info.getAccessId(), "DELETE", "", "", 0, new Date(), "/" + info.getBucketName() + "/" + path
				);

			httpDelegate.execute(HttpDelegate.DELETE, url, headers, true, false, null, callback);
		}
		catch (Throwable e)
		{
			callback.invoke(e);
		}
	}
}

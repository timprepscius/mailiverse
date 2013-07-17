/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.model;

import java.util.Date;

import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;

import core.util.Base64;

import core.crypt.HashSha256;
import core.util.LogNull;
import core.util.Strings;

@Export()
public class Attachment implements Exportable
{
	static LogNull log = new LogNull(Attachment.class);

	String id;
	String disposition;
	String mimeType;
	byte[] data = null;
	
	boolean loaded = false;
	
	public Attachment (String id, String disposition, String mimeType)
	{
		log.debug("Attachment", id,disposition,mimeType);
		
		this.disposition = disposition;
		this.id = id;
		this.mimeType = mimeType;
	}

	static public String getAttachmentId (String disposition, String id) throws Exception
	{
		boolean hasDisposition = disposition != null;
		boolean hasId = id!=null;
		boolean hasFileName = false;
		if (hasDisposition)
		{
			String oneLineDisposition = Strings.concat(Strings.splitLines(disposition)," ");
			hasFileName = oneLineDisposition.toLowerCase().matches(".*filename=.*");
			log.debug(oneLineDisposition, "hasFileName", hasFileName);
		}
		
		if (hasDisposition || hasId)
		{
			if (hasId)
				return id;
			
			if (hasFileName)
				return calculateId(disposition);
		}
		
		return null;
	}
	
	static protected String calculateId (String disposition) throws Exception
	{
		HashSha256 hash = new HashSha256();
		return Base64.encode(hash.hash(Strings.toBytes(disposition)));
	}
	
	public String getDataBase64 ()
	{
		try
		{
			log.debug("getDataBase64 a ", data.length, " ", new Date());
	
			return Base64.encode(data);
		}
		finally
		{
			log.debug("getDataBase64 b", new Date());
		}
	}
	
	public String getId ()
	{
		return id;
	}

	public byte[] getData ()
	{
		return data;
	}
	
	public void setData (byte[] data)
	{
		this.data = data;
		loaded = true;
	}
	
	public void clearData ()
	{
		this.data = null;
		loaded = false;
	}
	
	public String getDisposition ()
	{
		return disposition;
	}
	
	public String getMimeType ()
	{
		return mimeType;
	}
	
	public boolean isLoaded ()
	{
		return loaded;
	}
}

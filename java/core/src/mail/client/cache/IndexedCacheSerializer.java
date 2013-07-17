/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import core.util.LogNull;
import core.util.LogOut;
import core.util.Streams;

public class IndexedCacheSerializer extends ItemSerializerSync
{
	protected final static byte VERSION = 1;
	
	static LogNull log = new LogNull(IndexedCacheSerializer.class);
	public byte[] serialize(Item item) throws Exception 
	{
		log.debug("serialize", item);
		IndexedCache indexedCache = (IndexedCache)item;
		
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bos.write(VERSION);
		Streams.writeBoundedArray(bos, indexedCache.workCacheID.serialize());
		
		return bos.toByteArray();
	}

	public void deserialize(Item item, byte[] bytes) throws Exception 
	{
		log.debug("deserialize", item);
		IndexedCache indexedCache = (IndexedCache)item;
		
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		int version = bis.read();
		indexedCache.workCacheID.deserialize(Streams.readBoundedArray(bis));
	}
	
};
/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.cache;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import mail.client.cache.Store.Data;

import core.util.LogNull;
import core.util.LogOut;
import core.util.Pair;
import core.util.Streams;

public class StoreSerializer extends ItemSerializerSync
{
	protected final static byte VERSION = 1;
	
	static LogNull log = new LogNull(StoreSerializer.class);
	
	@Override
	public byte[] serialize(Item item) throws IOException 
	{
		log.debug(this, "serialize", item);
		
		Store store = (Store)item;
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		bos.write(VERSION);
		
		Streams.writeInt(bos, store.map.size());
		for (Entry<ID, Data> value : store.map.entrySet())
		{
			Streams.writeBoundedArray(bos, value.getKey().serialize());
			Streams.writeBoundedArray(bos, value.getValue().bytes); 
			Streams.writeBoundedArray(bos, value.getValue().getLocalVersion().toBytes());
		}
		
		return bos.toByteArray();
	}

	@Override
	public void deserialize(Item item, byte[] bytes) throws IOException 
	{
		log.debug(this, "deserialize", item);

		Store store = (Store)item;
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		int version = bis.read();
		
		// NEVER clear, because, we may overwrite 
		// store.map.clear();
		// deletes happen through writing a zero byte array, not by removing
		
		int size = Streams.readInt(bis);
		for (int i=0; i<size; ++i)
		{
			byte[] id = Streams.readBoundedArray(bis);
			byte[] value = Streams.readBoundedArray(bis);
			byte[] itemVersion = Streams.readBoundedArray(bis);
			
			store.update(ID.deserialize(id), new Version(itemVersion), value);
		}
	}

}

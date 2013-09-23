package store.server;

import java.util.List;

import store.server.db.DbStore;
import core.connector.FileInfo;
import core.util.Pair;

public class StoreServer 
{
	DbStore store;

	public StoreServer () throws Exception
	{
		store = new DbStore();
		store.ensureTables();
	}
	
	public Pair<String,String> newKeyPair (String userName) throws Exception
	{
		Pair<String, String> v = StoreUtils.createKeyIdAndSecretKey();
		DbStore store = new DbStore();
		int userId = store.getUserId(userName);
		store.addUserKeyPair(userId, v.first, v.second);
		
		return v;
	}
	
	public void newUser (String userName) throws Exception
	{
		store.addUser(userName);
	}
	
	public Pair<String,String> newUserWithKeyPair (String userName) throws Exception
	{
		newUser (userName);
		return newKeyPair(userName);
	}
	
	public String putKeyValue (String keyId, String key, byte[] value) throws Exception
	{
		return store.putKeyValue(store.getUserIdAndSecretKey(keyId).first, key, value);
	}

	public Pair<String, byte[]> getKeyValue (String keyId, String key) throws Exception
	{
		return store.getKeyValue(store.getUserIdAndSecretKey(keyId).first, key);
	}

	public void removeKeyValue (String keyId, String key) throws Exception
	{
		store.removeKeyValue (store.getUserIdAndSecretKey(keyId).first, key);
	}
	
	public void removeUser (String name) throws Exception
	{
		store.removeUser (name);
	}
	
	public List<FileInfo> listKeys (String keyId, String key) throws Exception
	{
		return store.listKeys(store.getUserIdAndSecretKey(keyId).first, key);
	}
}

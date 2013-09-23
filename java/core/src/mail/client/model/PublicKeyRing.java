package mail.client.model;

import java.util.List;

import core.util.Pair;
import mail.client.CacheManager;
import mail.client.cache.ID;

public class PublicKeyRing extends Model 
{
	List<Pair<ID, Identity>> ring;

	public PublicKeyRing(CacheManager manager) 
	{
		super(manager);
	}

	public void addPublicKeyFromCache(ID id, Identity identity) 
	{
		ring.add(Pair.create(id,  identity));
	}

	public void addPublicKey(ID id, Identity identity) 
	{
		ring.add(Pair.create(id,  identity));
		markDirty();
	}

	public List<Pair<ID,Identity>> getPublicKeys() 
	{
		return ring;
	}

}

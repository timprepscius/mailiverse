/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.cache;

public class StoreFactory implements ItemFactory
{
	int requestedMaxSize;
	
	public StoreFactory (int requestedMaxSize)
	{
		this.requestedMaxSize = requestedMaxSize;
	}
	
	public Item instantiate(Type type) {
		return new Store(requestedMaxSize);
	}
}

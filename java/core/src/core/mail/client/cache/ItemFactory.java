/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.cache;

public interface ItemFactory 
{
	public Item instantiate (Type type);
}

/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client.cache;

public class Operation {
	enum Type 
	{
		GET,
		PUT,
		REMOVE
	}

	public Type type;
	public ID id;
	public Item t;
	
	public Operation(Type type, ID id, Item t)
	{
		this.type = type;
		this.id = id;
		this.t = t;
	}
}

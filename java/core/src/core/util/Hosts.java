package core.util;

public class Hosts 
{
	public static String getHostFor (String key)
	{
		try
		{
			return ExternalResource.getTrimmedString("hosts/" + key);
		}
		catch (Exception e)
		{
			throw new RuntimeException("Internal error. #9204 " + key);
		}
	}

}

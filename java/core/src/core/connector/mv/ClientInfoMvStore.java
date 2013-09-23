package core.connector.mv;

import core.constants.ConstantsClient;
import core.constants.ConstantsMvStore;
import core.util.Environment;

public class ClientInfoMvStore 
{
	private String accessId;
	private String secretKey;

	public ClientInfoMvStore (Environment e)
	{
		accessId = e.checkGet(ConstantsMvStore.AccessKeyId);
		secretKey = e.checkGet(ConstantsMvStore.SecretKey);
	}
	
	public String getBucketEndpoint ()
	{
		return ConstantsClient.SERVER_TOMCAT;
	}
	
	public String getAccessId()
	{
		return accessId;
	}
	
	public String getSecretKey()
	{
		return secretKey;
	}

}

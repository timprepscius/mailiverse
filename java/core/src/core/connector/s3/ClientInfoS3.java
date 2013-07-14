/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.connector.s3;

import core.constants.ConstantsS3;
import core.util.Environment;


public class ClientInfoS3 
{
	private String bucketName;
	private String bucketRegion;
	private String accessId;
	private String secretKey;

	public ClientInfoS3 (Environment e)
	{
		bucketName = e.get(ConstantsS3.AWSBucketName);
		bucketRegion = e.get(ConstantsS3.AWSBucketRegion);
		accessId = e.checkGet(ConstantsS3.AWSAccessKeyId);
		secretKey = e.checkGet(ConstantsS3.AWSSecretKey);
	}
	
	public String getBucketEndpoint ()
	{
		String bucketEndPoint = null;
		
		if (bucketRegion == null || bucketRegion.equals(""))
			bucketEndPoint = "s3.amazonaws.com";
		else
			bucketEndPoint = "s3-" + bucketRegion + ".amazonaws.com";

		return bucketEndPoint;
	}
	
	public String getBucketName()
	{
		return bucketName;
	}
	
	public String getBucketRegion ()
	{
		return bucketRegion;
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

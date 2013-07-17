/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package app.tools;

import java.util.Map;

import core.util.Passwords;

import mail.server.storage.AWSStorageDelete;

public class ExpungeS3User
{
	public static void main(String[] _args) throws Exception
	{
		Map<String, String> args = Arguments.map(_args, 0);
		
		String accessKey = args.get("accesskey");
		String secretKey = args.get("secretkey");
		String bucketName = args.get("bucketname");
		
		if (bucketName == null)
			throw new IllegalArgumentException();
		
		AWSStorageDelete deleter = new AWSStorageDelete();
		
		if (accessKey == null && secretKey == null)
			deleter.delete(bucketName);
		else
			deleter.delete(bucketName, accessKey, secretKey);
	}
}

/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.server.storage;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;


import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.AccessKeyMetadata;
import com.amazonaws.services.identitymanagement.model.DeleteAccessKeyRequest;
import com.amazonaws.services.identitymanagement.model.DeleteGroupRequest;
import com.amazonaws.services.identitymanagement.model.DeleteUserPolicyRequest;
import com.amazonaws.services.identitymanagement.model.DeleteUserRequest;
import com.amazonaws.services.identitymanagement.model.ListAccessKeysRequest;
import com.amazonaws.services.identitymanagement.model.ListAccessKeysResult;
import com.amazonaws.services.identitymanagement.model.RemoveUserFromGroupRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteBucketRequest;
import com.amazonaws.services.s3.model.DeleteObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import core.connector.s3.sync.SimpleAWSCredentials;
import core.util.LogOut;
import core.util.Passwords;

public class AWSStorageDelete extends AWSStorageCommon
{
	static LogOut log = new LogOut(AWSStorageDelete.class);

	public AWSStorageDelete () throws IOException
	{
	}
	
	protected void deleteUser(AmazonIdentityManagement im, String group, String user, String policy, boolean force) throws Exception
	{
		try
		{
			log.debug("deleting user policy", user, policy);
			im.deleteUserPolicy(new DeleteUserPolicyRequest().withUserName(user).withPolicyName(policy));
		}
		catch (Exception e)
		{
			if (!force)
				throw e;
			
			log.exception(e);
		}
		
		try
		{
			log.debug("deleting access keys", user);
			ListAccessKeysResult accessKeys = im.listAccessKeys(new ListAccessKeysRequest().withUserName(user));
			for (AccessKeyMetadata i : accessKeys.getAccessKeyMetadata())
			{
				log.debug("deleting access key", user, i.getAccessKeyId());
				im.deleteAccessKey(new DeleteAccessKeyRequest().withUserName(user).withAccessKeyId(i.getAccessKeyId()));
			}		
		}
		catch (Exception e)
		{
			if (!force)
				throw e;
			
			log.exception(e);
		}
		
		try
		{
			log.debug("removing user from group", group, user);
			im.removeUserFromGroup(new RemoveUserFromGroupRequest().withGroupName(group).withUserName(user));
		}
		catch (Exception e)
		{
			if (!force)
				throw e;
			
			log.exception(e);
		}

		try
		{
			log.debug("deleting user", user);
			im.deleteUser(new DeleteUserRequest().withUserName(user));
		}
		catch (Exception e)
		{
			if (!force)
				throw e;
			
			log.exception(e);
		}
	}
	
	protected void deleteBucketContents(AmazonS3 s3, String bucketName) throws Exception
	{
		while (true)
		{
			List<String> keys = new ArrayList<String>();
			
			log.debug("creating batch delete");
			
			ObjectListing listing = s3.listObjects(bucketName);
			for (S3ObjectSummary i : listing.getObjectSummaries())
			{
				log.debug("key", i.getKey());
				keys.add(i.getKey());
			}
			
			if (keys.isEmpty())
				break;

			DeleteObjectsRequest req = new DeleteObjectsRequest(bucketName).withKeys(keys.toArray(new String[0]));
			log.debug("deleting");
			s3.deleteObjects(req);
		}
	}
	
	public void delete(String bucketName) throws Exception
	{
		String awsAccessKeyId = Passwords.getPasswordFor("BucketCreate-AWS-AccessKey");
		String awsSecretKey = Passwords.getPasswordFor("BucketCreate-AWS-SecretKey");
		
		delete (bucketName, awsAccessKeyId, awsSecretKey);
	}
	
	public void delete(String bucketName, String awsAccessKeyId, String awsSecretKey) throws Exception
	{
		log.debug("will delete", bucketName);
		SimpleAWSCredentials credentials = new SimpleAWSCredentials(awsAccessKeyId, awsSecretKey);
	    AmazonS3 s3 = new AmazonS3Client(credentials);
		AmazonIdentityManagement im = new AmazonIdentityManagementClient(credentials);
	
		log.debug("deriving names");
		deriveNames(bucketName);

		deleteUser(im, groupName, readWriteIdentity, policyReadWriteName, false);
		deleteUser(im, groupName, writeIdentity, policyWriteName, false);

		log.debug("deleting group", groupName);
		im.deleteGroup(new DeleteGroupRequest().withGroupName(groupName));
		
		deleteBucketContents(s3, bucketName);
		
		log.debug("deleting bucket");
		s3.deleteBucket(new DeleteBucketRequest(bucketName));
	}
}

/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.server.storage;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;


import com.amazonaws.services.identitymanagement.AmazonIdentityManagement;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.AccessKey;
import com.amazonaws.services.identitymanagement.model.AddUserToGroupRequest;
import com.amazonaws.services.identitymanagement.model.CreateAccessKeyRequest;
import com.amazonaws.services.identitymanagement.model.CreateGroupRequest;
import com.amazonaws.services.identitymanagement.model.CreateUserRequest;
import com.amazonaws.services.identitymanagement.model.PutUserPolicyRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.BucketCrossOriginConfiguration;
import com.amazonaws.services.s3.model.BucketWebsiteConfiguration;
import com.amazonaws.services.s3.model.CORSRule;
import com.amazonaws.services.s3.model.Region;

import core.connector.s3.sync.SimpleAWSCredentials;
import core.util.LogOut;
import core.util.Maps;
import core.util.Pair;
import core.util.Passwords;
import core.util.Strings;
import core.util.Base64;

public class AWSStorageCreation extends AWSStorageCommon
{
	SecureRandom random = new SecureRandom();
	String bucketNameRandomId;
	String awsAccessKeyId, awsSecretKey;
	
	LogOut log = new LogOut(AWSStorageCreation.class);

	public AWSStorageCreation () throws IOException
	{
		awsAccessKeyId = Passwords.getPasswordFor("BucketCreate-AWS-AccessKey");
		awsSecretKey = Passwords.getPasswordFor("BucketCreate-AWS-SecretKey");
	}
	
	protected String generateBucketName (String email)
	{
		String name = email.substring(0, email.indexOf('@'));
		String domain = email.substring(email.indexOf('@')+1);
		
		log.debug("generateBucketName:",email,name,domain);
		
		String[] domainParts = domain.split("\\.");
		ArrayList<String> parts = new ArrayList<String>();

		// reverse, for com.mailiverse
		for (int i=domainParts.length-1; i>=0; i--)
			parts.add(domainParts[i]);
		parts.add(name);
		
		parts.add("" + new BigInteger("" + Math.abs(random.nextLong())).toString(16));
		
		return Strings.concat(parts, ".").toLowerCase();
	}
	
	public Map<String,String> create (String email, String region) throws Exception
	{
		log.debug ("I will now figure out what region to put things in", region);
		Region awsRegion = Region.valueOf(region);
		String awsRegionString = awsRegion.toString();
		if (awsRegionString == null)
			awsRegionString = "";
		
		String awsRegionStringEndPoint = 
			awsRegionString.isEmpty() ? "s3.amazonaws.com" : ("s3-" + awsRegionString + ".amazonaws.com");
		
		log.debug ("I will now log in to S3 and the IdentityManagement to check these credentials.");
		
		SimpleAWSCredentials credentials = new SimpleAWSCredentials(awsAccessKeyId, awsSecretKey);
	    AmazonS3 s3 = new AmazonS3Client(credentials);
		AmazonIdentityManagement im = new AmazonIdentityManagementClient(credentials);

	    log.debug ("Successfully logged into S3");
		
		log.debug ("I will now derive names for items");
		deriveNames(generateBucketName(email));

		log.debug (
			"I will now try to:\n" +
			"  1. Create the S3 Bucket with name ",bucketName,"\n" +
			"  2. Create two IAM Identities for permissions -\n" +
			"       ",writeIdentity," to be sent to the mail server to be able to write to the mailbox.\n" +
			"       ",writeIdentity," to be stored in your configuration to enable the mail client to read and write mail.\n\n"
		);

					
		s3.setEndpoint(awsRegionStringEndPoint);
		s3.createBucket(bucketName, awsRegion);

		log.debug("Setting website configuration");
		
		BucketWebsiteConfiguration bwc = new BucketWebsiteConfiguration("index.html");
		s3.setBucketWebsiteConfiguration(bucketName, bwc);
		log.debug("Done");
		
		log.debug("Enabling CORS");
		CORSRule rule1 = new CORSRule()
	    .withId("CORSRule1")
	    .withAllowedMethods(Arrays.asList(new CORSRule.AllowedMethods[] { 
	            CORSRule.AllowedMethods.GET, CORSRule.AllowedMethods.PUT, CORSRule.AllowedMethods.DELETE}))
	    .withAllowedOrigins(Arrays.asList(new String[] {"*"}))
		.withMaxAgeSeconds(3000)
		.withAllowedHeaders(Arrays.asList(new String[] {"*"}))
		.withExposedHeaders(Arrays.asList(new String[] {"ETag"}));
	            
		BucketCrossOriginConfiguration cors = new BucketCrossOriginConfiguration();
		cors.setRules(Arrays.asList(new CORSRule[] {rule1}));

		s3.setBucketCrossOriginConfiguration(bucketName, cors);
		log.debug("Done");
		
		log.format("Creating group %s ... ", groupName);
		im.createGroup(new CreateGroupRequest().withGroupName(groupName));
		log.debug("Done");
		
		log.format("Creating user %s ... ", writeIdentity);
		im.createUser(new CreateUserRequest().withUserName(writeIdentity));
		log.debug("Done");
		
		log.format("Adding user %s to group %s ... ", writeIdentity, groupName);
		im.addUserToGroup(new AddUserToGroupRequest().withGroupName(groupName).withUserName(writeIdentity));
		log.debug("Done");

		log.format("Creating user %s ... ", readWriteIdentity);
		im.createUser(new CreateUserRequest().withUserName(readWriteIdentity));
		log.debug("Done");
		
		log.format("Adding user %s to group %s ... ", readWriteIdentity, groupName);		
		im.addUserToGroup(new AddUserToGroupRequest().withGroupName(groupName).withUserName(readWriteIdentity));
		log.debug("Done");

		log.format("Creating permissions for %s to write to bucket %s ... \n", writeIdentity, bucketName);

		String writePolicyRaw = 
		"{								\n"+
		"  #Statement#: [				\n"+
		"    {							\n"+
		"      #Sid#: #SID#,			\n"+
		"      #Action#: [				\n"+
		"        #s3:PutObject#,		\n"+
		"        #s3:PutObjectAcl#		\n"+
		"      ],						\n"+
		"      #Effect#: #Allow#,		\n"+
		"      #Resource#: [			\n"+
		"        #arn:aws:s3:::BUCKET/*#\n"+
		"      ]						\n"+
		"    }							\n"+
		"  ]							\n"+
		"}\n";
				
		String writePolicy = writePolicyRaw.replaceAll("#", "\"").replace("SID", policyWriteName).replace("BUCKET", bucketName);
//		q.println ("Policy definition: " + writePolicy);
		im.putUserPolicy(new PutUserPolicyRequest().withUserName(writeIdentity).withPolicyDocument(writePolicy).withPolicyName(policyWriteName));
		log.debug("Done");
		
		log.format("Creating permissions for %s to read/write to bucket %s ... \n", writeIdentity, bucketName);

		String readWritePolicyRaw = 
			"{								\n"+
			"  #Statement#: [				\n"+
			"  {							\n"+
			"      #Sid#: #SID#,			\n"+
			"      #Action#: [				\n"+
			"        #s3:PutObject#,		\n"+
			"        #s3:PutObjectAcl#,		\n"+
			"        #s3:DeleteObject#,		\n"+
			"        #s3:Get*#,				\n"+
			"        #s3:List*#				\n"+
			"      ],						\n"+
			"      #Effect#: #Allow#,		\n"+
			"      #Resource#: [			\n"+
			"        #arn:aws:s3:::BUCKET/*#,\n"+
			"        #arn:aws:s3:::BUCKET#	\n"+
			"      ]						\n"+
			"    }							\n"+
			"  ]							\n"+
			"}\n";	
		
		String readWritePolicy = readWritePolicyRaw.replaceAll("#", "\"").replace("SID", policyReadWriteName).replace("BUCKET", bucketName);
//		q.println ("Policy definition: " + readPolicy);
		im.putUserPolicy(new PutUserPolicyRequest().withUserName(readWriteIdentity).withPolicyDocument(readWritePolicy).withPolicyName(policyReadWriteName));
		log.debug("Done");
		
		log.format("Requesting access key for %s", writeIdentity);
		writeAccessKey = im.createAccessKey(new CreateAccessKeyRequest().withUserName(writeIdentity)).getAccessKey();
		log.format("Received [%s] [%s] Done.\n", writeAccessKey.getAccessKeyId(), writeAccessKey.getSecretAccessKey());
		
		log.format("Requesting access key for %s", readWriteIdentity);
		readWriteAccessKey = im.createAccessKey(new CreateAccessKeyRequest().withUserName(readWriteIdentity)).getAccessKey();
		log.format("Received [%s] [%s] Done.\n", readWriteAccessKey.getAccessKeyId(), readWriteAccessKey.getSecretAccessKey());
		
		log.debug();
		log.debug("I have finished the creating the S3 items.\n");
		
		return Maps.toMap(
			"bucketName", bucketName, 
			"bucketRegion", awsRegionString,
			"writeAccessKey", writeAccessKey.getAccessKeyId(),
			"writeSecretKey", writeAccessKey.getSecretAccessKey(),
			"readWriteAccessKey", readWriteAccessKey.getAccessKeyId(),
			"readWriteSecretKey", readWriteAccessKey.getSecretAccessKey()
		);
	}
}

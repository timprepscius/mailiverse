/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.server.storage;

import com.amazonaws.services.identitymanagement.model.AccessKey;

public class AWSStorageCommon
{
	String writeIdentity, readWriteIdentity;
	String bucketName, groupName, policyWriteName, policyReadWriteName;
	AccessKey writeAccessKey, readWriteAccessKey;
	
	public void deriveNames (String bucketName) throws Exception
	{
		this.bucketName = bucketName;
		groupName = bucketName + ".Group";
		writeIdentity = bucketName + ".Write";
		readWriteIdentity = bucketName + ".ReadWrite";
		policyWriteName = bucketName + ".Write";
		policyReadWriteName = bucketName + ".ReadWrite";
		policyWriteName = policyWriteName.replace(".", "");
		policyReadWriteName = policyReadWriteName.replace(".", "");
	}

}

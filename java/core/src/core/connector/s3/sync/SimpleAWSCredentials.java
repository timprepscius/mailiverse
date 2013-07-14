/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.connector.s3.sync;


import com.amazonaws.auth.AWSCredentials;

import core.constants.ConstantsS3;
import core.util.Environment;

public class SimpleAWSCredentials implements AWSCredentials
{
    private String a, s;
    
    public SimpleAWSCredentials(String a, String s)
    {
            this.a = a;
            this.s = s;
    }

    public SimpleAWSCredentials(Environment e)
    {
    	this(e.checkGet(ConstantsS3.AWSAccessKeyId), e.checkGet(ConstantsS3.AWSSecretKey));
    }
    
    @Override
    public String getAWSAccessKeyId()
    {
            return a;
    }

    @Override
    public String getAWSSecretKey()
    {
            return s;
    }
}

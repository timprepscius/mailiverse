/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.srp.server;

import java.math.BigInteger;

import core.util.Pair;
import core.util.Triple;


public interface SRPServerUserSessionDb 
{
	public void rateLimitFailure (String userName) throws Exception;
	public void markFailure (String userName) throws Exception;
	
	public Triple<String, BigInteger, BigInteger> getUserVVS (String userName) throws Exception;
	
	public void testCreate (String version, String userName) throws Exception;
	public void createUser (String version, String userName, BigInteger v, BigInteger s, byte[] extra) throws Exception;
}

/**
 * Author: Timothy Prepscius
 * License: BSD + keep my name in the code!
 */

#ifndef __mailiverse_core_srp_SRPFactory_h__
#define __mailiverse_core_srp_SRPFactory_h__

#include "../Types.h"
#include "SRPClientSession.h"
#include "SRPConstants.h"
#include "SRPServerSession.h"
#include "SRPVerifier.h"
#include "SRPUtils.h"

namespace mailiverse {
namespace core {
namespace srp {

/**
 * An implementation of SRP-6a - Secure Remote Password Protocol. See <a href="http://srp.stanford.edu">http://srp.stanford.edu</a>
 * and <a href="http://srp.stanford.edu/ndss.html">http://srp.stanford.edu/ndss.html</a>. The improvements described in
 * <a href="http://srp.stanford.edu/srp6.ps">SRP-6: Improvements and Refinements to the Secure Remote Password Protocol</a> have been incorporated.
 * <p>
 *
 * SRP attempts to eliminate many of the security problems involved in a client/server user authentication.
 * I don't understand the math, but the ideas are farily simple. On the server, store a mathematically generated number that is
 * based on a user chosen password and a randomly generated "salt". Both the client and server maintain a predetermined prime
 * number "N" and a "primitive root" based on N called "g". The nature of all these numbers allows an authentication without
 * the server needing to save the password. The client asks for the salt that was created, then a series of calculations
 * are performed with the client and server exchanging the calculated values. At the end of this, both the client and server
 * can safely know that authentication has occurred.
 * <p>
 *
 * From the SRP website, SRP assures:
 * <ol>
 * <li>No useful information about the password P or its associated private key x is revealed during a successful run. Specifically, we wish to prevent an attacker from being able to guess and verify passwords based on exchanged messages.</li>
 * <li>No useful information about the session key K is revealed to an eavesdropper during a successful run. Since K is a cryptographically strong key instead of a limited-entropy password, we are not concerned about guessing attacks on K, as long as K cannot be computed directly by an intruder.</li>
 * <li>Even if an intruder has the ability to alter or create his own messages and make them appear to originate from Carol or Steve, the protocol should prevent the intruder from gaining access to the host or learning any information about passwords or session keys. At worst, an intruder should only be able to cause authentication to fail between the two parties (often termed a denial-of-service attack).</li>
 * <li>If the host's password file is captured and the intruder learns the value of v, it should still not allow the intruder to impersonate the user without an expensive dictionary search.   </li>
 * <li>If the session key of any past session is compromised, it should not help the intruder guess at or otherwise deduce the user's password.</li>
 * <li>If the user's password itself is compromised, it should not allow the intruder to determine the session key K for past sessions and decrypt them. Even present sessions should at least be protected from passive eavesdropping.</li>
 * </ol>
 * <p>
 * 
 * <i>Using this library:</i><br>
 * For general use, you should only need to directly use these three classes: {@link SRPFactory}, {@link SRPInputStream}
 * and {@link SRPOutputStream}. Besides these three, you will use two POJOs: {@link SRPConstants} and {@link SRPVerifier}
 * <p>
 * For all interactions, you obtain an {@link SRPFactory} via one of the static getInstance() methods. The no-args version uses
 * default values for the prime number and primitive root. The other version allows you to specify values for these. 
 * <p>
 * The first activity is to generate a "verifier" for a password. Given a password P, this is accomplished via the {@link SRPFactory#makeVerifier(byte[])}
 * method. E.g.
<code><pre>
        SRPFactory.getInstance().makeVerifier(P);
</pre></code>
 * This value should be stored away referenced via a username.
 * <p>
 *
 * The second activity is a client/server session. On the server, allocate a {@link SRPServerSessionRunner} loaded with
 * a session from {@link SRPFactory#newServerSession(SRPVerifier)}. On the client, allocate a {@link SRPClientSessionRunner}
 * loaded with a session from {@link SRPFactory#newClientSession(byte[])}. Once you have a Session Runner, you can pass it to an
 * {@link SRPInputStream} and an {@link SRPOutputStream}. For each of these streams, call both {@link SRPInputStream#authenticate(SRPRunner, SRPOutputStream)}
 * and {@link SRPOutputStream#authenticate(SRPRunner, SRPInputStream)}. Once authenticated, use them as you would any I/O stream. All I/O
 * on these streams are encrypted using <a href="http://en.wikipedia.org/wiki/Advanced_Encryption_Standard">AES</a> with the SRP session key as the encryption key.
 * <p>
 *
 * <i>Stream Protocol</i><br>
 * The SRPInputStream/SRPOutStream authenticate using, essentially, the protocol as specified here: <a href="http://srp.stanford.edu/design.html">http://srp.stanford.edu/design.html</a>.
 * All values are sent as {@link java.math.BigInteger#toString(int)} with a radix of 16. The only difference is that the method to combine values is
 * unique to this library (see {@link SRPUtils#combine(java.math.BigInteger, java.math.BigInteger)}). If at any point authentication
 * fails, the stream is closed.
 * <p>
 * Once authentication is complete, the streams use the following protocol to send data:<br>
<code><pre>
	[data size][newline]
	[data]
</pre></code>
 * <br>
 * "data size" is the number of bytes in the data block. The size is specified as radix 16 BigInteger. The data block is
 * encrypted via AES using K as the key. K is an MD5 hash of S. A new data block is sent each time flush() is called on the
 * output stream.
 * <p>
 *
 * IMPORTANT: This library relies on <a href="http://java.sun.com/products/jce/">JCE</a>
 * <p>
 *
 * Released into the public domain
 *
 * @author Jordan Zimmerman - jordan@jordanzimmerman.com
 * @version 1.4 a) Updated to use the SRP-6a spec. b) Updated Javadoc. 2/27/07
 * @version 1.3 Updated to use the SRP-6 spec 2/21/07
 * @version 1.2
 */
class SRPFactory
{
protected:
	static SRPFactory *instance;

public:
	/**
	 * Return a factory that uses default constants
	 *
	 * @return the factory
	 */
	static SRPFactory *getInstance()
	{
		if (!instance)
			instance = new SRPFactory(getDefaultConstants());

		return instance;
	}

	/**
	 * Create a new "verifier" (v in the SRP docs). A random salt value is created.
	 *
	 * @param password bytes of the password. Client will need this same password later on for a client session.
	 *
	 * @return the verifier
	 */
	SRPVerifier makeVerifier(Block password)
	{
		return SRPUtils::makeVerifier(fConstants, password);
	}

	/**
	 * Start a new client session.
	 *
	 * @param password The same password that was passed to {@link #makeVerifier(byte[])}
	 *
	 * @return the client session. Normally, this is passed directly to a new {@link SRPClientSessionRunner}
	 */
	SRPClientSessionPtr newClientSession(const Key &password)
	{
		return new SRPClientSession(fConstants, password);
	}
	
	SRPClientSessionPtr newClientSession()
	{
		return new SRPClientSession(fConstants);
	}

	/**
	 * Start a new server session.
	 *
	 * @param verifier The same verifier that was returned by {@link #makeVerifier(byte[])}
	 *
	 * @return the server session. Normally, this is passed directly to a new {@link SRPServerSessionRunner}
	 */
	SRPServerSessionPtr newServerSession(SRPVerifier verifier)
	{
		return new SRPServerSession(fConstants, verifier);
	}

	SRPFactory(SRPConstants constants) :
		fConstants(constants)
	{
	}

private:

	static SRPConstants getDefaultConstants();
	SRPConstants fConstants;
} ;

} // namespace
} // namespace
} // namespace

#endif

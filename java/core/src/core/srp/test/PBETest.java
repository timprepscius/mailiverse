/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.srp.test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Random;

import core.exceptions.CryptoException;
import core.crypt.KeyPairFromPassword;
import core.crypt.KeyPairFromPasswordCryptor;
import core.crypt.PBE;
import core.util.Streams;


public class PBETest
{
	public static void main (String[] args) throws CryptoException, IOException
	{
		Random random = new Random();
		
		String password = "TESTPASSWORD";

		String s = "I am a string which is not to short not to long.   I really should be much much larger. " +
				"but what can I do.  I wonder, in reality this string would be a very long random number. ";
		
		for (int i=0; i<50; ++i)
			s += BigInteger.valueOf(Math.abs(random.nextLong())).toString(32);

		KeyPairFromPassword keyPair = new KeyPairFromPassword(password);
		KeyPairFromPasswordCryptor cryptor = new KeyPairFromPasswordCryptor(keyPair);
		byte[] verifier = keyPair.getVerifier();
		
		System.out.println(new BigInteger(verifier));
		
		byte[] e = cryptor.encrypt(s.getBytes());
		
		FileOutputStream fe = new FileOutputStream("PBETest.enc");
		fe.write(e);
		fe.flush();
		
		FileOutputStream fp = new FileOutputStream("PBETest.plain");
		fp.write(e);
		fp.flush();

		byte[] in = Streams.readFullyBytes(new FileInputStream("PBETest.enc"));
		String match = new String(cryptor.decrypt(in));
		
		if (match.equals(s))
			System.out.println("decryption succeeded");
	}
}

/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package app.tools;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Map.Entry;

import key.server.sql.KeyUserDb;

import core.crypt.KeyPairFromPassword;
import core.crypt.KeyPairFromPasswordCryptor;
import core.exceptions.CryptoException;
import core.util.Environment;
import core.util.JSONSerializer;
import core.util.Zip;

public class ShowUserKeyBlock
{
	public static void main (String[] _args) throws CryptoException, IOException, SQLException, ClassNotFoundException
	{
		Class.forName("com.mysql.jdbc.Driver");

		Map<String,String> args = Arguments.map(_args,0);
		String user = args.get("user");
		String password = args.get("password");
		if (user == null || password == null)
			throw new IllegalArgumentException();
		
		KeyPairFromPassword keyPair = new KeyPairFromPassword(password);
		keyPair.generate();
		
		KeyPairFromPasswordCryptor cryptor = new KeyPairFromPasswordCryptor(keyPair);
		
		KeyUserDb db = new KeyUserDb();
		byte[] encryptedBlock = db.getBlock(user);
		byte[] block = Zip.inflate(cryptor.decrypt(encryptedBlock));
		
		Environment e = JSONSerializer.deserialize(block);
		for (Entry<String, String> i : e.entrySet())
		{
			System.out.println(i.getKey() + ":" + i.getValue());
		}
	}

}

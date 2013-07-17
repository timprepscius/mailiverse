/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.server.db;

import java.io.IOException;
import java.sql.SQLException;

import core.exceptions.CryptoException;
import core.crypt.PBE;
import core.server.srp.db.UserDb;
import core.server.srp.db.sql.Catalog;
import core.util.Environment;
import core.util.JSONSerializer;
import core.util.Passwords;
import core.util.Zip;


public class MailUserDb extends UserDb
{
	PBE cryptor;
	
	public MailUserDb() throws CryptoException, IOException
	{
		super(new Catalog());
		
		String password = Passwords.getPasswordFor("mail-pbe");
		cryptor = new PBE(password, PBE.DEFAULT_SALT_0, PBE.DEFAULT_ITERATIONS, 256);
	}
	
	public byte[] getBlock (String userName) throws IOException, SQLException, CryptoException
	{
		return Zip.inflate(cryptor.decrypt(super.getMailBlock(userName)));
	}
	
	public byte[] setBlock(String userName, byte[] block) throws IOException, SQLException, CryptoException
	{
		return super.setMailBlock(userName, cryptor.encrypt(Zip.deflate(block)));
	}	

	public Environment getUserEnvironment (String user) throws IOException, SQLException, CryptoException, ClassNotFoundException
	{
		byte[] block = getBlock(user);		
		return JSONSerializer.deserialize(block);
	}
	
	public void putUserEnvironment (String user, Environment e) throws IOException, SQLException, CryptoException
	{
		setBlock(user, JSONSerializer.serialize(e));
	}
	
	public Environment getDeletedUserEnvironment (String userName) throws Exception
	{
		byte[] block = Zip.inflate(cryptor.decrypt(super.getDeletedMailBlock(userName)));
		return JSONSerializer.deserialize(block);
	}
}

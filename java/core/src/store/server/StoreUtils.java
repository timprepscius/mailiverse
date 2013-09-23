package store.server;

import java.util.Map;

import store.server.db.DbStore;

import core.crypt.HmacSha1;
import core.util.Base64;
import core.util.LogOut;
import core.util.Pair;
import core.util.SecureRandom;
import core.util.Strings;

public class StoreUtils 
{
	static LogOut log = new LogOut(StoreUtils.class);
	
	// This method creates S3 signature for a given String.
	static protected String sign(HmacSha1 mac, String data) throws Exception
	{
	  // Signed String must be BASE64 encoded.
	  byte[] signBytes = mac.mac(Strings.toBytes(data));
	  String signature = Base64.encode(signBytes);
	  return signature;
	}	
	
	static public Pair<String, String> createKeyIdAndSecretKey ()
	{
		byte[] keyId = new byte[32];
		byte[] secretKey = new byte[32];
		SecureRandom random = new SecureRandom();
		random.nextBytes(keyId);
		random.nextBytes(secretKey);
		
		return Pair.create(Base64.encode(keyId), Base64.encode(secretKey));
	}

	static public void verifySignature (String data, String keyId, String signature) throws Exception
	{
		DbStore dbStore = new DbStore();
		Pair<Integer, String> userIdAndSecretKey = dbStore.getUserIdAndSecretKey(keyId);

		log.debug ("verifySignature", data, userIdAndSecretKey.second);
		
		HmacSha1 userMac = new HmacSha1(Base64.decode(userIdAndSecretKey.second));
		String correctSignature = sign(userMac, data);

		if (!correctSignature.equals(signature))
			throw new Exception ("Signature mismatch");
	}
	
	/**
	 * returns the keyId of the user
	 * 
	 * @param action
	 * @param headers
	 * @param resource
	 * @param content
	 * @return
	 * @throws Exception
	 */
	static public String verifyUser (String action, Map<String,String> headers, String resource, byte[] content) throws Exception
	{
		String method = action;
		String dateString = headers.get(ConstantsMvServer.HEADER_DATE);
		String contentType = headers.get(ConstantsMvServer.HEADER_CONTENT_TYPE);
		String contentLength = headers.get(ConstantsMvServer.HEADER_CONTENT_LENGTH);
		
		if (contentLength != null)
			if (Integer.parseInt(contentLength) != content.length)
				throw new Exception ("Length mismatch");
		
		StringBuffer buf = new StringBuffer();
		buf.append(method).append("\n");
		buf.append("").append("\n");
		buf.append(contentType != null ? contentType : "").append("\n");
		buf.append("\n"); // empty real date header
		buf.append(dateString).append("\n");
		buf.append(resource);

		String[] authorizationParts = headers.get(ConstantsMvServer.HEADER_AUTHORIZATION).split(":");
		String keyId = authorizationParts[0].split(" ")[1];
		String signature = authorizationParts[1];
		
		verifySignature (buf.toString(), keyId, signature);
		
		return keyId;
	}
	
/*
	public byte[] getFile (String userPath) throws Exception
	{
		FileInputStream f = new FileInputStream(ConstantsMvServer.PATH + userPath);
		byte[] result = Streams.readFullyBytes(f);
		f.close();
		
		return result;
	}
	
	public void putFile (String userPath, byte[] bytes) throws Exception
	{
		FileOutputStream f = new FileOutputStream (ConstantsMvServer.PATH + userPath);
		f.write(bytes);
		f.close();
	}
	
	private List<File> listFiles(File dir, List<File> files)
	{
	    if (!dir.isDirectory())
	    {
	        files.add(dir);
	        return files;
	    }

	    for (File file : dir.listFiles())
	    	listFiles(file, files);
	    
	    return files;
	}
	
	public List<FileInfo> listKeys (String userPath)
	{
		List<File> files = listFiles (new File (ConstantsMvServer.PATH + userPath), new ArrayList<File>());
		
		List<FileInfo> fileInfos = new ArrayList<FileInfo>();
		for (File file : files)
		{
			FileInfo info = new FileInfo(
				file.getPath(), 
				file.getPath(), 
				file.length(), 
				new Date(file.lastModified()), 
				"" + file.lastModified()
			);
			
			fileInfos.add(info);
		}
		
		return fileInfos;
	}
*/
}

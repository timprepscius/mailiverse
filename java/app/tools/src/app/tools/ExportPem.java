/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package app.tools;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.security.KeyStore;
import java.security.PublicKey;
import java.util.Map;

import core.util.Base64;

public class ExportPem {

	public static void main (String[] args) throws Exception
	{
		Map<String,String> a = Arguments.map(args, 1);
		if (!Arguments.containsAll(a, new String[] { "file"}))
			throw new IllegalArgumentException();
		
		String fileNameIn = a.get("file");
		FileInputStream in = new FileInputStream(fileNameIn);
		
		KeyStore tks = KeyStore.getInstance("JKS");
        tks.load(in, "password".toCharArray());
        PublicKey publicKey = tks.getCertificate(tks.aliases().nextElement()).getPublicKey();
		
        byte[] bytes = publicKey.getEncoded();
        
        String fileNameOut = fileNameIn + ".pem.b64";
        FileWriter out = new FileWriter (fileNameOut);
        out.write(Base64.encode(bytes));
        out.close();
	}
	
}

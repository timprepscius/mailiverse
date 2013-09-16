package core.crypt;

import java.io.IOException;
import java.io.InputStream;

import app.service.JSInvoker;
import core.callback.Callback;
import core.callback.CallbackDefault;
import core.util.Base64;
import core.util.Pair;

public class CryptorPGPFactory 
{
	protected native String jsGenerate(int bits, String userId, String password) /*-{
		var keyPair = $wnd.pgp_genKeyPair(bits, userId, password);
		var joined = keyPair.publicKey + "," + keyPair.privateKey;
		return joined;
	}-*/;

	protected native void jsGenerate(int bits, String userId, String password, Object callback) /*-{
		$wnd.mAsync.pgp_genKeyPair(
			{ invoke: function(keyPair) { callback.invoke(keyPair.publicKey, keyPair.privateKey); } },
			bits,
			userId, password
		);
	}-*/;

	public Pair<byte[], byte[]> generate (int bits, String userId, String password)
	{
		String joined = jsGenerate(bits, userId, password);
		String[] split = joined.split(",");
		
		return new Pair<byte[], byte[]>(Base64.decode(split[0]), Base64.decode(split[1]));
	}
	
	public void generate (int bits, String userId, String password, Callback callback)
	{
		Callback synth = new CallbackDefault() {
			public void onSuccess(Object... arguments) throws Exception 
			{
				String split[] = { (String)arguments[0], (String)arguments[1] };
				callback.invoke(new Pair<byte[], byte[]>(Base64.decode(split[0]), Base64.decode(split[1])));
			}
		}.setReturn(callback);
		
		jsGenerate(bits, userId, password, JSInvoker.wrap(synth));
	}

	public static CryptorPGP fromResources(InputStream publicKey, InputStream privateKey) throws IOException {
		return new CryptorPGPJS(publicKey, privateKey);
	}

	public static CryptorRSA fromString(String publicKey, Object object) {
		// TODO Auto-generated method stub
		return null;
	}
	
}

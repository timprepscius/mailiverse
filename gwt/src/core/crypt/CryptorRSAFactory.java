package core.crypt;

import java.io.IOException;
import java.io.InputStream;

import app.service.JSInvoker;
import core.callback.Callback;
import core.callback.CallbackDefault;
import core.util.Base64;
import core.util.Pair;

public class CryptorRSAFactory 
{
	protected native String jsGenerate(int bits) /*-{
		var keyPair = $wnd.rsa_genKeyPair(bits);
		var joined = keyPair.publicKey + "," + keyPair.privateKey;
		return joined;
	}-*/;

	protected native void jsGenerate(int bits, Object callback) /*-{
		$wnd.mAsync.rsa_genKeyPair(
			{ invoke: function(keyPair) { callback.invoke(keyPair.publicKey, keyPair.privateKey); } },
			bits
		);
	}-*/;

	public Pair<byte[], byte[]> generate (int bits)
	{
		String joined = jsGenerate(bits);
		String[] split = joined.split(",");
		
		return new Pair<byte[], byte[]>(Base64.decode(split[0]), Base64.decode(split[1]));
	}
	
	public void generate (int bits, Callback callback)
	{
		Callback synth = new CallbackDefault() {
			public void onSuccess(Object... arguments) throws Exception 
			{
				String split[] = { (String)arguments[0], (String)arguments[1] };
				callback.invoke(new Pair<byte[], byte[]>(Base64.decode(split[0]), Base64.decode(split[1])));
			}
		}.setReturn(callback);
		
		jsGenerate(bits, JSInvoker.wrap(synth));
	}

	public static CryptorRSA fromResources(InputStream publicKey, InputStream privateKey) throws IOException {
		return new CryptorRSAJS(publicKey, privateKey);
	}

	public static CryptorRSA fromString(String publicKey, Object object) {
		// TODO Auto-generated method stub
		return null;
	}
	
}

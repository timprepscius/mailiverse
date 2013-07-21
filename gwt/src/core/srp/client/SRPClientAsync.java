package core.srp.client;

import app.service.JSInvoker;

import com.google.gwt.core.client.JavaScriptObject;
import core.util.Base64;
import core.callback.Callback;
import core.callback.CallbackDefault;

public class SRPClientAsync
{
	JavaScriptObject key;
	
	native JavaScriptObject jsInitialize (String password64) /*-{
		return $wnd.mSupport.srp_client_initialize(password64);
	}-*/;
	
	native void jsSetSalt (JavaScriptObject key, String salt64, JavaScriptObject callback) /*-{
		$wnd.mAsync.srp_client_setSalt(callback, key, salt64);
	}-*/;
	
	native void jsSetServerPublicKey (JavaScriptObject key, String server64, JavaScriptObject callback) /*-{
		$wnd.mAsync.srp_client_setServerPublicKey(callback, key, server64);
	}-*/;

	native void jsValidateServerEvidence (JavaScriptObject key, String evidence64, JavaScriptObject callback) /*-{
		$wnd.mAsync.srp_client_validateServerEvidence(callback, key, evidence64);
	}-*/;

	native String jsGetSessionKey (JavaScriptObject key) /*-{
		return $wnd.mSupport.srp_client_getSessionKey(key);
	}-*/;

	native String jsGetPublicKey (JavaScriptObject key) /*-{
		return $wnd.mSupport.srp_client_getPublicKey(key);
	}-*/;

	native String jsGetEvidenceValue (JavaScriptObject key) /*-{
		return $wnd.mSupport.srp_client_getEvidenceValue(key);
	}-*/;
	
	public SRPClientAsync (byte[] password)
	{
		key = jsInitialize(Base64.encode(password));
	}
	
	public Callback setSalt_(byte[] bs)
	{
		return new CallbackDefault(Base64.encode(bs))
		{
			public void onSuccess(Object... arguments) throws Exception {
				jsSetSalt(key, (String)V(0), JSInvoker.wrap(callback));
			}
		};
	}
	
	public Callback setServerPublicKey_(byte[] publicKey)
	{
		return new CallbackDefault(Base64.encode(publicKey))
		{
			public void onSuccess(Object... arguments) throws Exception {
				jsSetServerPublicKey(key, (String)V(0), JSInvoker.wrap(callback));
			}
		};
	}

	public Callback validateServerEvidenceValue_M2_(byte[] evidence)
	{
		return new CallbackDefault(Base64.encode(evidence))
		{
			public void onSuccess(Object... arguments) throws Exception {
				jsValidateServerEvidence(key, (String)V(0), JSInvoker.wrap(callback));
			}
		};
	}
	
	public byte[] getSessionKey()
	{
		return Base64.decode(jsGetSessionKey(key));
	}
	
	public byte[] getPublicKey()
	{
		return Base64.decode(jsGetPublicKey(key));
	}
	
	public byte[] getEvidenceValue()
	{
		return Base64.decode(jsGetEvidenceValue(key));
	}
}

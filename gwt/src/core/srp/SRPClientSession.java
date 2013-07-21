package core.srp;

import java.math.BigInteger;

import org.json.JSONException;
import org.json.JSONObject;
import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;

import com.jordanzimmerman.SRPAuthenticationFailedException;
import com.jordanzimmerman.SRPFactory;

import core.util.Base64;

@Export()
public class SRPClientSession extends com.jordanzimmerman.SRPClientSession implements Exportable
{
	protected SRPClientSession(JSONObject json) throws JSONException 
	{
		fConstants = SRPFactory.DEFAULT_CONSTANTS;
		
		if (json.has("p"))
			fPassword = Base64.decode(json.getString("p"));
		if (json.has("S"))
			fCommonValue_S = new BigInteger(Base64.decode(json.getString("S")));
		if (json.has("M1"))
			fEvidenceValue_M1 = new BigInteger(Base64.decode(json.getString("M1")));
		if (json.has("x"))
			fPrivateKey_x = new BigInteger(Base64.decode(json.getString("x")));
		if (json.has("A"))
			fPublicKey_A = new BigInteger(Base64.decode(json.getString("A")));
		if (json.has("a"))
			fRandom_a = new BigInteger(Base64.decode(json.getString("a")));
		if (json.has("K"))
			fSessionKey_K = Base64.decode(json.getString("K"));
	}
	
	static public SRPClientSession fromJSON(String json) throws JSONException
	{
		return new SRPClientSession(new JSONObject(json));
	}
	
	public String toJSON () throws JSONException
	{
		JSONObject json = new JSONObject();
		
		if (fPassword != null)
			json.put("p", Base64.encode(fPassword));
		if (fCommonValue_S != null)
			json.put("S", Base64.encode(fCommonValue_S.toByteArray()));
		if (fEvidenceValue_M1 != null)
			json.put("M1", Base64.encode(fEvidenceValue_M1.toByteArray()));
		if (fPrivateKey_x != null)
			json.put("x", Base64.encode(fPrivateKey_x.toByteArray()));
		if (fPublicKey_A != null)
			json.put("A", Base64.encode(fPublicKey_A.toByteArray()));
		if (fRandom_a != null)
			json.put("a", Base64.encode(fRandom_a.toByteArray()));
		if (fSessionKey_K != null)
			json.put("K", Base64.encode(fSessionKey_K));
		
		return json.toString();
	}
	
	public void setPassword(String password64)
	{
		fPassword = Base64.decode(password64);
	}
	
	public void setSalt(String salt64)
	{
		setSalt_s(Base64.decode(salt64));
	}

	public void setServerPublicKey(String publicKey64) throws Exception
	{
		setServerPublicKey_B(Base64.decode(publicKey64));
	}

	public void validateServerEvidence(String evidence64) throws Exception
	{
		validateServerEvidenceValue_M2(Base64.decode(evidence64));
	}
}

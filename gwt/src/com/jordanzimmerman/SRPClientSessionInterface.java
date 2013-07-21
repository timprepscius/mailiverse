package com.jordanzimmerman;

public interface SRPClientSessionInterface {

	public void setSalt_s(byte[] bs);
	public void setServerPublicKey_B(byte[] publicKey) throws SRPAuthenticationFailedException;
	public byte[] getSessionKey_K();
	public byte[] getPublicKey_A();
	public byte[] getEvidenceValue_M1();
	public void validateServerEvidenceValue_M2(byte[] evidence) throws SRPAuthenticationFailedException;

}

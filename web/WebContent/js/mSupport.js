mSupport = {

rsa_getPrivateKey: function(pem64)
{
	var pem = pem64;
	var key = new RSAKey();
	key.readPrivateKeyFromPEMString(pem);
	return key;
},

rsa_getPublicKey: function(pem64)
{
	var pem = pem64;
	var key = new X509();
	key.readCertPEM(pem);
	return key.subjectPublicKeyRSA;
},

rsa_tests: function()
{
	console.log("testing 128");
	mSupport.rsa_testKeyPair(rsa_genKeyPair(128));

	console.log("testing 256");
	mSupport.rsa_testKeyPair(rsa_genKeyPair(256));

	console.log("testing 512");
	mSupport.rsa_testKeyPair(rsa_genKeyPair(512));

	console.log("testing 2048");
	mSupport.rsa_testKeyPair(rsa_genKeyPair(2048));
	
	console.log("testing 1024");
	mSupport.rsa_testKeyPair(rsa_genKeyPair(1024));

	console.log("testing finished");
},

rsa_genKeyPair: function(bits)
{
	var key = new RSAKey();
	key.generate(bits, "10001"); // hex 65537
	
	var privHex = key.genPKCS1();
	var pubHex = key.genX509();
	
	var priv = hex2b64(privHex);
	var pub = hex2b64(pubHex);
	
	var result = { publicKey: pub, privateKey: priv } ;
	return result;
},

rsa_encrypt: function(key, bytes64)
{
	var bytes = b64tohex(bytes64);
	var result = key.encrypt(bytes);
	var result64 = hex2b64(result);
	
	return result64;
},

rsa_encrypt_serialized_key: function(keyS, bytes64)
{
	var key = new RSAKey();
	key.deserialize(keyS);
	
	return mSupport.rsa_encrypt(key, bytes64);
},

rsa_decrypt: function(key, bytes64)
{
	var bytes = b64tohex(bytes64);
	var result = key.decrypt(bytes);
	var result64 = hex2b64(result);
	return result64;
},

rsa_decrypt_serialized_key: function(keyS, bytes64)
{
	var key = new RSAKey();
	key.deserialize(keyS);
	
	return mSupport.rsa_decrypt(key, bytes64);
},

sha256_hash: function(bytes64)
{
	var bytes = sjcl.codec.base64.toBits(bytes64);
	var out = sjcl.hash.sha256.hash(bytes);
	var out64 = sjcl.codec.base64.fromBits(out);  
	return out64;
},

sha1_hash: function(bytes64)
{
	var bytes = CryptoJS.enc.Base64.parse(bytes64);
	var out = CryptoJS.SHA1(bytes);
	var out64 = out.toString(CryptoJS.enc.Base64);
	return out64;
},

sha1_hmac: function(key64, bytes64)
{
	var bytes = CryptoJS.enc.Base64.parse(bytes64);
	var key = CryptoJS.enc.Base64.parse(key64);
	var out = CryptoJS.HmacSHA1(bytes, key);
	var out64 = out.toString(CryptoJS.enc.Base64);
	return out64;
},

zip_inflate: function(data64)
{
	var data = Base64.decode(data64);
	var result = do_inflate(data);
	return Base64.encode(result);
},

zip_deflate: function(data64)
{
	var data = Base64.decode(data64);
	var result = do_deflate(data);
	var result64 = Base64.encode(result);
	
	if (mSupport.zip_inflate(result64) != data64)
	{
		var msg = "zip deflation failed, the world is fucked, again..";
		alert(msg);
		log(msg);
		throw new Error(msg);
	}
	
	return result64;
},

aes_encrypt: function(key64, iv64, bytes64)
{
	return mSupport.aes_encrypt_CryptoJS(key64, iv64, bytes64);
},

aes_encrypt_CryptoJS: function(key64, iv64, bytes64)
{
	var key = CryptoJS.enc.Base64.parse(key64);
	var iv = CryptoJS.enc.Base64.parse(iv64);
	var bytes = CryptoJS.enc.Base64.parse(bytes64);
	
	var t = CryptoJS.AES.encrypt(bytes, key, { mode: CryptoJS.mode.CBC, padding: CryptoJS.pad.Pkcs7, iv:iv });
	var result = t.ciphertext.toString(CryptoJS.enc.Base64);
	
	return result;
},

aes_encrypt_SJCL: function(key64, iv64, bytes64)
{
	var bytes = sjcl.codec.base64.toBits(bytes64);
	var key = sjcl.codec.base64.toBits(key64);
	var iv = sjcl.codec.base64.toBits(iv64);

	var j = sjcl.encrypt(key, bytes,{ key:key, iv:iv, ks:256 });
	var result = JSON.parse(j).ct;

	return result;
},

aes_decrypt: function(key64, iv64, bytes64)
{
	return mSupport.aes_decrypt_CryptoJS(key64, iv64, bytes64);
},

aes_decrypt_CryptoJS: function(key64, iv64, bytes64)
{
	var key = CryptoJS.enc.Base64.parse(key64);
	var iv = CryptoJS.enc.Base64.parse(iv64);
	var bytes = CryptoJS.enc.Base64.parse(bytes64);

	var cjs = CryptoJS.algo.AES.createDecryptor(key, { mode: CryptoJS.mode.CBC, padding: CryptoJS.pad.Pkcs7, iv:iv });
	var t = cjs.finalize(bytes);
	var result = t.toString(CryptoJS.enc.Base64);

	return result;
},

aes_decrypt_SJCL: function(key64, iv64, bytes64)
{
	var bytes = sjcl.codec.base64.toBits(bytes64);
	var key = sjcl.codec.base64.toBits(key64);
	var iv = sjcl.codec.base64.toBits(iv64);

	var j = sjcl.encrypt(key, bytes,{ key:key, iv:iv, ks:256 });
	var result = JSON.parse(j).ct;

	return result;
},

pbe_genKey: function(password, salt64, iterationCount, keyLength)
{
	return mSupport.pbe_genKey_SJCL(password, salt64, iterationCount, keyLength);
},

pbe_genKey_CryptoJS: function(password, salt64, iterationCount, keyLength)
{
	var key256Bits = CryptoJS.PBKDF2(password, salt64, { keySize: keyLength/32, iterations: iterationCount});
},

pbe_genKey_SJCL: function(password, salt64, iterationCount, keyLength)
{
	var salt = sjcl.codec.base64.toBits(salt64);
	var p = { salt: salt, iter:iterationCount };
	p = sjcl.misc.cachedPbkdf2(password, p);
	return sjcl.codec.base64.fromBits(p.key);
},

srp_client_initialize: function(password64)
{
	return new SRPClient({}).setPassword(password64).serialize();
},

srp_client_setSalt: function(state, salt64)
{
	return new SRPClient(state).setSalt(salt64).serialize();
},

srp_client_setServerPublicKey: function(state, publicKey64)
{
	return new SRPClient(state).setServerPublicKey(publicKey64).serialize();
},

srp_client_validateServerEvidence: function(state, evidence64)
{
	return new SRPClient(state).validateServerEvidence(evidence64).serialize();
},

srp_client_getSessionKey: function(state)
{
	return new SRPClient(state).getSessionKey();
},

srp_client_getPublicKey: function(state)
{
	return new SRPClient(state).getPublicKey();
},

srp_client_getEvidenceValue: function(state)
{
	return new SRPClient(state).getEvidenceValue();
},

ping: function()
{
	return 'ping';
}

};


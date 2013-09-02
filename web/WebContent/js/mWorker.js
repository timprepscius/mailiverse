
importScripts(
	'Base64.js',
	'Utf.js',
	"crypt/sjcl.js",

	"crypt/tomwu/base64.js",
	"crypt/tomwu/jsbn.js",
	"crypt/tomwu/jsbn2.js",
	"crypt/tomwu/rsa.js",
	"crypt/tomwu/rsa2.js",
	"crypt/tomwu/prng4.js",
	"crypt/tomwu/rng.js",
	
	"crypt/jsrsasign-1.3/rsapem-1.1.js",
	"crypt/jsrsasign-1.3/asn1hex-1.1.js",
	"crypt/jsrsasign-1.3/x509-1.1.js",    
	"crypt/asn1writer.js",
	
	"crypt/CryptoJS/rollups/aes.js",
	"crypt/CryptoJS/rollups/hmac-sha1.js",
	"crypt/CryptoJS/components/enc-base64-min.js",
	
	"zip/support.js",
	"zip/inflate.js",
	"zip/deflate.js",
	
	'mSupport.js'
);

self.onmessage = function(e) 
{
	if (!e.data)
		return ;
	
	var data = e.data;

	try
	{
		var result = mSupport[data.cmd].apply(null, data.args);
		self.postMessage({callback:data.callback, result:result, original:data});
	}
	catch (exception)
	{
		self.postMessage({callback:data.callback, exception:exception.toString(), original:data});
	}
};


/**
 * Uses the new array typed in javascript to binary base64 encode/decode
 * at the moment just decodes a binary base64 encoded
 * into either an ArrayBuffer (decodeArrayBuffer)
 * or into an Uint8Array (decode)
 * 
 * References:
 * https://developer.mozilla.org/en/JavaScript_typed_arrays/ArrayBuffer
 * https://developer.mozilla.org/en/JavaScript_typed_arrays/Uint8Array
 */

var Base64 = {
	_key : "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=",
	_encode : null,
	_decode : null,

	initialize: function()
	{
		if (this._decode != null)
			return;
		
		this._decode = new Uint8Array(0xFF);
		for (var i=0; i<this._key.length; ++i)
			this._decode[this._key.charCodeAt(i)] = i;
		
		this._encode = new Uint8Array(this._key.length);
		for (var i=0; i<this._key.length; ++i)
			this._encode[i] = this._key.charCodeAt(i) & 0xFF;
	},
	
	decode: function(input) 
	{
		var bytes = new Uint8Array(input.length);
		
		var i;
		for (i=0; i<input.length; ++i)
			bytes[i] = input.charCodeAt(i) & 0xFF;
		
		return Base64.decodeBytes(bytes);
	},
	
	decodeBytes: function(input)
	{
		this.initialize();
		
		//get last chars to see if are valid
		var lkey1 = this._decode[input[input.length-1]];		 
		var lkey2 = this._decode[input[input.length-2]];		 
	
		var bytes = (input.length/4) * 3;
		if (lkey1 == 64) bytes--; //padding chars, so skip
		if (lkey2 == 64) bytes--; //padding chars, so skip
		
		var out = [];
		var chr1, chr2, chr3;
		var enc1, enc2, enc3, enc4;
		var i = 0;
		var j = 0;
		
		while (j < input.length) 
		{	
			enc1 = this._decode[input[j++]];
			enc2 = this._decode[input[j++]];
			enc3 = this._decode[input[j++]];
			enc4 = this._decode[input[j++]];
	
			chr1 = (enc1 << 2) | (enc2 >> 4);
			chr2 = ((enc2 & 15) << 4) | (enc3 >> 2);
			chr3 = ((enc3 & 3) << 6) | enc4;
	
			out[i++] = chr1 & 0xFF;			
			if (enc3 != 64) out[i++] = chr2 & 0xFF;
			if (enc4 != 64) out[i++] = chr3 & 0xFF;
		}
	
		return out;	
	},

	encodeBytes : function(input)
	{
		this.initialize();

		var length = input.length;
		var bytes = ((length+2)/3)*4;
	    var output = [];
	    
	    var i = 0;
	    var chr1, chr2, chr3, enc1, enc2, enc3, enc4;
	    
	    var j=0;
	    while (i < length) 
	    {
	    	var count = length - i;
		    chr1 = input[i++] & 0xFF;
	        chr2 = (i<length) ? (input[i++]&0xFF) : 0;
	        chr3 = (i<length) ? (input[i++]&0xFF) : 0;

	        enc1 = chr1 >> 2;
	        enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
	        enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
	        enc4 = chr3 & 63;

	        if (count == 1) 
	            enc3 = enc4 = 64;
	        else 
	        if (count == 2) 
	            enc4 = 64;

	        output[j++] = this._encode[enc1];
	        output[j++] = this._encode[enc2];
	        output[j++] = this._encode[enc3];
	        output[j++] = this._encode[enc4];
	    }
	    
	    return output;
	},
	
	encode: function(input)
	{
		var bytes = this.encodeBytes(input);
		var s = "";
		
		for (var i=0; i<bytes.length; ++i)
			s += String.fromCharCode(bytes[i]);
		
		return s;
	}
} ;

/** end **/
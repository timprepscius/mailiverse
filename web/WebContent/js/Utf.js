Utf = {
	
	toBytes : function (string) {
//		log("toBytes", string);
		var bytes = [];
		
		for (var n = 0; n < string.length; n++) {
 
			var c = string.charCodeAt(n)&0xFFFF;
 
			if (c < 128) {
				bytes.push(c);
			}
			else if((c > 127) && (c < 2048)) {
				bytes.push((c >> 6) | 192);
				bytes.push((c & 63) | 128);
			}
			else {
				bytes.push((c >> 12) | 224);
				bytes.push(((c >> 6) & 63) | 128);
				bytes.push((c & 63) | 128);
			}
		}
 
		return bytes;
	},
	
	toBinString : function(bytes) {
		var s="";
		for (var i=0; i<bytes.length; ++i)
			s+=String.fromCharCode(bytes[i]);
		
		return s;
	},
 
	fromBinString : function(s) {
		var bytes = [];
		
		for (var i=0; i<s.length; ++i)
			bytes.push(s.charCodeAt(i));
		
		return bytes;
	},

	toString : function (bytes) {
		var string = "";
		var i = 0;
		var c = c1 = c2 = 0;
 
		while ( i < bytes.length ) {
 
			c = bytes[i] & 0xFF;
 
			if (c < 128) {
				string += String.fromCharCode(c);
				i++;
			}
			else if((c > 191) && (c < 224)) {
				c2 = bytes[i+1];
				string += String.fromCharCode(((c & 31) << 6) | (c2 & 63));
				i += 2;
			}
			else {
				c2 = bytes[i+1];
				c3 = bytes[i+2];
				string += String.fromCharCode(((c & 15) << 12) | ((c2 & 63) << 6) | (c3 & 63));
				i += 3;
			}
 
		}
 
//		log("toString",string);
		return string;
	}	
	
};

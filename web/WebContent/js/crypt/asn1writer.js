
function _asn1gen_pad(s,n,z)
{
	if (z == undefined)
		z = "0";
	if (n == undefined)
		n = s.length + s.length%2;
	
	var r = "";
	var i;
	for (i=s.length; i<n; ++i)
		r+=z;
	
	return r+ s;
}

function _asn1gen_genHeader(type, length, addition)
{
	if (addition == undefined)
		addition = 0;
	
	var bytesNeeded = 
		(length < 0x0F ? 0 :
		(length < 0xFF ? 1 : 
		(length < 0xFFFF ? 2 : 
		(length < 0xFFFFFFFF ? 4 : 8))));
	
	var header;
	if (bytesNeeded == 0)
	{
		header = type + "0" + length.toString(16) 
	}
	else
	{
		header = 
			type + 
			"8" + bytesNeeded.toString(16) + 
			_asn1gen_pad(length.toString(16),bytesNeeded*2);
	}
	
	return header;
}

function _asn1gen_lengthOfChildren(children)
{
	var length = 0;
	var i;
	for (i=0; i<children.length; ++i)
		length += children[i].length / 2;
	
	return length;
}

function _asn1gen_genSequence(children)
{
	return _asn1gen_genHeader("30", _asn1gen_lengthOfChildren(children)) + children.join("");
}

function _asn1gen_genOctetString(children)
{
	return _asn1gen_genHeader("04", _asn1gen_lengthOfChildren(children)) + children.join("");
}

function _asn1gen_genBitString(children)
{
	return _asn1gen_genHeader("03", _asn1gen_lengthOfChildren(children)+1) + "00" + children.join("");
}

function _asn1gen_genInteger(hexValue)
{
	var i = parseInt("0x"+ hexValue[0]);
	if (i >= 8)
		hexValue = "00" + hexValue;

	return _asn1gen_genHeader("02", (hexValue.length/2)) + hexValue;
}

function _asn1gen_genX509(hexN, hexE)
{
	var result = 
		_asn1gen_genSequence([
			"300D06092A864886F70D0101010500", // header
			_asn1gen_genBitString([
			    _asn1gen_genSequence([
			       _asn1gen_genInteger(_asn1gen_pad(hexN)),              
			       _asn1gen_genInteger(_asn1gen_pad(hexE)),              
			    ])
			])
		]);
	
	return result;
}		
	
function asn1gen_genX509_key()
{
	var result = 	
		_asn1gen_genX509(
			this.n.toString(16),
			this.e.toString(16)
		);
	
	return result;
}


function asn1gen_genPKCS1(n1, e1, d1, p1, q1, dp1, dq1, co1)
{
	var result = 
		_asn1gen_genSequence([
			"020100300D06092A864886F70D0101010500", // header
			_asn1gen_genOctetString([
			    _asn1gen_genSequence([
   			       _asn1gen_genInteger(_asn1gen_pad("00")),              
			       _asn1gen_genInteger(_asn1gen_pad(n1)),              
			       _asn1gen_genInteger(_asn1gen_pad(e1)),              
			       _asn1gen_genInteger(_asn1gen_pad(d1)),              
			       _asn1gen_genInteger(_asn1gen_pad(p1)),              
			       _asn1gen_genInteger(_asn1gen_pad(q1)),              
			       _asn1gen_genInteger(_asn1gen_pad(dp1)),              
			       _asn1gen_genInteger(_asn1gen_pad(dq1)),              
			       _asn1gen_genInteger(_asn1gen_pad(co1)),              
			    ])
			])
		]);
	
	return result;
}

function asn1gen_genPKCS1_key()
{
	var result = 
		asn1gen_genPKCS1(
			this.n.toString(16),
			this.e.toString(16),
			this.d.toString(16),
			this.p.toString(16),
			this.q.toString(16),
			this.dmp1.toString(16),
			this.dmq1.toString(16),
			this.coeff.toString(16)
		);

	return result;
}

rsa_serialize_members = [ 'n', 'e', 'd', 'p', 'q', 'dmp1', 'dmq1', 'coeff' ];

function rsa_serialize()
{
	var s = {};
	
	var i;
	for (i=0; i<rsa_serialize_members.length; ++i)
	{
		var key = rsa_serialize_members[i];
		if (this[key] != undefined)
			s[key] = this[key].toString(16);
	}
	
	return s;
}

function rsa_deserialize (s)
{
	var i;
	for (i=0; i<rsa_serialize_members.length; ++i)
	{
		var key = rsa_serialize_members[i];
		if (s[key] != undefined)
			this[key] = new BigInteger(s[key],16);
	}
}

RSAKey.prototype.genPKCS1 = asn1gen_genPKCS1_key;
RSAKey.prototype.genX509 = asn1gen_genX509_key;

RSAKey.prototype.serialize = rsa_serialize;
RSAKey.prototype.deserialize = rsa_deserialize;


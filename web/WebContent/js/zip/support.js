
// because the inflate.js & deflate.js write to the zip variable blah
zip = {};

function join_uint8arrays (arrays)
{
	var l = 0, i, j;
	for (i=0; i<arrays.length; ++i)
		l += arrays[i] ? arrays[i].length : 0;
		
	var result = new Uint8Array(l);
	
	l = 0;
	for (i=0; i<arrays.length; ++i)
	{
		var array = arrays[i];
		if (array)
		{
			for (j=0; j<array.length; j++)
				result[l++] = array[j];
		}
	}
	
	return result;
}

function do_inflate(data)
{
	data = join_uint8arrays([data]);
	var inflater = new zip.Inflater();
	var dataWithoutHeaderOrCrc = data.subarray(2, data.length-4);
	
	var results = [ inflater.append(dataWithoutHeaderOrCrc) ];
	results.push(inflater.flush());
	
	return join_uint8arrays(results);
}

function do_deflate(data)
{
	data = join_uint8arrays([data]);
	var deflater = new zip.Deflater();
	var header = new Uint8Array(2);
	header[0] = 0x78;
	header[1] = 0x9c;
	
	var crc = new Uint8Array(4);
	crc[0] = crc[1] = crc[2] = crc[3] = 0;
	
	var results = [];
	results.push(header);
	results.push(deflater.append(data));
	results.push(deflater.flush());
	results.push(crc);
	
	return join_uint8arrays(results);
}


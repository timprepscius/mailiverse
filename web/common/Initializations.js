function __keydownRandomCollector__()
{
	var d = new Date();
	sjcl.random.addEntropy(d.valueOf(), 2, "keydown");
}

function startRandom()
{
	sjcl.random.startCollectors();
	
	if (window.addEventListener) 
	{
		 window.addEventListener("keydown", __keydownRandomCollector__, false);
	}
	else
	if (document.attachEvent)
	{
		document.attachEvent("keydown", __keydownRandomCollector__);
	}
		
	if (window.crypto && (window.crypto.getRandomValues))
	{
		var array;
		if (window.crypto.getRandomValues)
		{
			array = new Uint32Array(1024);
			window.crypto.getRandomValues(array);
		}
		
		for (var i = 0; i < array.length; i++) {
			sjcl.random.addEntropy(array[i], 32, "crypto.random");
		}
	}
	else
	{
		var now = new Date();
		
		// this is sooo lame, but firefox is not generating enough information for the random
		// number generator to seed properly by the time we log in or sign up
		// probably safer this way anyways
		$.ajax({ url: Constants.TOMCAT_SERVER + "Random?nocache=" + now.valueOf() })
			.done(function ( data ) {
				var bytes = Base64.decode(data);
				for (var i=0; i<bytes.length/4; ++i)
				{
					var o = i*4;
					var n = (bytes[o+0]) |
							(bytes[o+1] << 8) |
							(bytes[o+2] << 16) |
							(bytes[o+3] << 24);
					
					sjcl.random.addEntropy(n, 32, "web.random");
				}
			});

	}
}

$(function() { startRandom(); });

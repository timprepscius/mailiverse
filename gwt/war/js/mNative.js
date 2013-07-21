mNative = {

	queue : [],
	
	readRequest: function()
	{
		var result;
		if (mNative.queue.length > 0)
		{
			var m = mNative.queue.shift();
			result = "+" + JSON.stringify(m);
		}
		else
		{
			result = "-";		
		}
		
		return result;
	},
	
	writeResponse: function(response)
	{
		if (mNative.onResponse)
		{
			var res = response;
			onNextTick(function() {
				mNative.onResponse(JSON.parse(response));
			});
		}
	},
	
	sendRequest: function(m)
	{
		mNative.queue.push(m);
		keyedTimer('mNative.sendRequest',function() { location.href = 'native://event=onRequest' }, 10);
	},
	
};



mDispatch = {
		
	log: function()
	{
//		console.log.apply(console, arguments);
	},
	
	mode: 'sync',
	
	onCalculateEventEnd : null,
	onCalculateEventBegin : null,
	asyncCallbacks: {},
	asyncCallbackId: 0,
	numAsyncCalculations:0,

	handleResponse: function(data)
	{
		mDispatch.numAsyncCalculations--;
		if (window.onCalculateEventEnd)
			window.onCalculateEventEnd(mDispatch.numAsyncCalculations);
		
		var callback = mDispatch.asyncCallbacks[data.callback];
		delete mDispatch.asyncCallbacks[data.callback];

		mDispatch.log("onmessage ",data.callback,data.original.cmd);
		
		if (callback)
			if (data.exception)
				callback.invoke(new Error(data.exception));
			else
				callback.invoke(data.result);
	},
	
	onWorkerResponse: function(message)
	{
		var data = message.data;
		if (data.original.cmd == 'ping')
		{
			if (mDispatch.mode != 'native')
			{
				mDispatch.log("ping received switching to worker mode");
				mDispatch.mode = 'worker';
			}
		}
		else
		{
			mDispatch.handleResponse(data);
		}
	},

	dispatch: function(cmd, a, force)
	{
		mDispatch.numAsyncCalculations++;
		if (window.onCalculateEventBegin)
			window.onCalculateEventBegin(mDispatch.numAsyncCalculations);

		var callback = a[0];
		var args = Array.prototype.slice.call(a,1);
		force = (force == undefined) ? false : force;

		var mode = mDispatch.mode;
		
		if (mode == 'worker' && startsWith(cmd,'srp'))
			mode = 'async';
		
		if (mode == 'sync')
		{
			try
			{
				mDispatch.log("sync",cmd);
				var result = mSupport[cmd].apply(null, args);
				callback.invoke(result);
			}
			catch (exception)
			{
				if (cmd != 'ping')
					if (callback)
						callback.invoke(exception);
			}
			
			mDispatch.numAsyncCalculations--;
			if (window.onCalculateEventEnd)
				window.onCalculateEventEnd(mDispatch.numAsyncCalculations);
		}
		else
		if (mode == 'async')
		{
			var callbackId = mDispatch.asyncCallbackId++;
			mDispatch.asyncCallbacks[callbackId] = callback;

			setTimeout(
				function(){
					var request = {cmd:cmd, args:args, callback:callbackId};
					var response = {original:request, callback:callbackId};
					
					try
					{
						response.result = mSupport[cmd].apply(null,args);
					}
					catch (exception)
					{
						response.exception = exception;
					}
					
					mDispatch.handleResponse(response);
				},
				0
			);
		}
		else
		if (mode == 'worker')
		{
			var callbackId = mDispatch.asyncCallbackId++;
			mDispatch.asyncCallbacks[callbackId] = callback;
		
			mDispatch.log("worker ",callbackId, cmd);
			mDispatch.worker.postMessage({cmd:cmd, args:args, callback:callbackId});
		}
		else
		if (mode == 'native')
		{
			var callbackId = mDispatch.asyncCallbackId++;
			mDispatch.asyncCallbacks[callbackId] = callback;
		
			mDispatch.log("native",callbackId, cmd);
			mNative.sendRequest({cmd:cmd, args:args, callback:callbackId})			
		}
	},
	
	startWorker: function()
	{
		if (mDispatch.mode != 'native')
		{
			mDispatch.log("starting worker");
			
			mDispatch.mode = 'worker';
			mDispatch.worker = new Worker('js/mWorker.js');
			mDispatch.worker.onmessage = mDispatch.onWorkerResponse;
			mDispatch.worker.postMessage({cmd:'ping', args:[], callback:-1});
		}
	},
	
	startNative: function()
	{
		if (mDispatch.mode == 'worker')
		{
			mDispatch.worker.postMessage({cmd:'shutdown'});
		}
		
		mDispatch.mode = 'native';
		mNative.onResponse = mDispatch.handleResponse;
		mNative.sendRequest({cmd:'cout',args:["starting native"], callback:-1});
	},
};

// $(document).ready(setTimeout(function() { mDispatch.startWorker(); }, 250));

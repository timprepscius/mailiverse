
var mDelegateOverride = 
{
	socketConstruct: function(delegate, _url)
	{
		var url = 'ws://white:9876/';
		log('socketConstruct', url);
	        var jsWebSocket = new WebSocket(url);
        
		jsWebSocket.javaDelegate = delegate;
		
		jsWebSocket.onopen = function() { 
			var self = this;
			onSoon(function() {
				log("socket.onOpen", "->",self.javaDelegate);

				if (self.javaDelegate) 
					self.javaDelegate.onEvent("onOpen"); 
			});
		};
		
		jsWebSocket.onclose = function() { 
			var self = this;
			onSoon(function() {
				log("socket.onClose", "->",self.javaDelegate);
				
				if (self.javaDelegate)  
					self.javaDelegate.onEvent("onClose"); 
			});
		};
		
		jsWebSocket.onerror = function() { 
			var self = this;
			onSoon(function() {
				log("socket.onError", "->",self.javaDelegate);
				
				if (self.javaDelegate) 
					self.javaDelegate.onEvent("onError");  
			});
		};
		
		jsWebSocket.onmessage = function(socketResponse) {
			var self = this;
			var response = socketResponse;
			onSoon(function() {
				log("socket.onMessage", response.data, "->",self.javaDelegate);
				
				var data = '' + response.data;
				if (self.javaDelegate)
					self.javaDelegate.onMessage(data);
			});
		};
            
		return jsWebSocket;
	},
	
	socketSend: function(socket, message)
	{
		log("socketSend:", socket, message);
		socket.send(message);
	},
	
	socketClose: function(socket)
	{
		log("socketClose:", socket);
		socket.javaDelegate = null;
	},
		
} ;

for (x in mDelegateOverride)
{
	delegate[x] = mDelegateOverride[x];
	console.log("overriding " + x);
}

function log()
{
	var out = [ logFormatDate() + ":" ];
	for (var i=0; i<arguments.length; ++i)
		out.push("" + arguments[i]);

	if (mDispatch.mode == 'native')
	{
		mNative.sendRequest({cmd:'cout',args:[out.join(" ")], callback:-1});		
	}
	else
	{
		console.log(out.join(" "));
	}
}

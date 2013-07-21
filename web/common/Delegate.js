var mDelegateCommon = 
{
	socketConstruct: function(delegate, _url)
	{
		var url = '' + _url;
		
		log('socketConstruct', url);
        var jsWebSocket = new WebSocket(url);
        
		jsWebSocket.javaDelegate = delegate;
		
		jsWebSocket.onopen = function() { 
			log("socket.onOpen", "->",this.javaDelegate);

			if (this.javaDelegate) 
				this.javaDelegate.onEvent("onOpen"); 
		};
		
		jsWebSocket.onclose = function() { 
			log("socket.onClose", "->",this.javaDelegate);
			
			if (this.javaDelegate)  
				this.javaDelegate.onEvent("onClose"); 
		};
		
		jsWebSocket.onerror = function() { 
			log("socket.onError", "->",this.javaDelegate);
			
			if (this.javaDelegate) 
				this.javaDelegate.onEvent("onError");  
		};
		
		jsWebSocket.onmessage = function(socketResponse) {
			log("socket.onMessage", socketResponse.data, "->",this.javaDelegate);
			
			var data = '' + socketResponse.data;
			if (this.javaDelegate)
				this.javaDelegate.onMessage(data);
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


	//--------------------------------------------------------------------------
	executeURL: function(action, url, headers, binaryInput, binaryOutput, contents, callback)
	{
		// http://www.html5rocks.com/en/tutorials/file/xhr2/
		// http://miskun.com/javascript/binary-file-loading-in-javascript/
		
		log("delegate.executeURL ", action, url, headers, binaryOutput, binaryOutput, contents ? contents.length : 0);
		
		var disableWrite = false;
		if (disableWrite && (action == "PUT" || contents!=null))
		{
			callback.invoke(null);
			return;
		}
		
	    var req = new XMLHttpRequest();
	    req.open(action, url, true);  
	    req.timeout = 5000;
	
	    if (headers != null)
    	{
	    	for (var i=0; i<headers.length; ++i)
    		{
	    		var s = headers[i];
	    		req.setRequestHeader(s[0], s[1]);
    		}
    	}
	    
	    if (binaryInput)
	    {
		    if (contents)
		    	contents = Utf.toBinString(Base64.decode(contents));
	    }
	    if (binaryOutput)
	    {
	    	if (req.overrideMimeType)
	    		req.overrideMimeType('text/plain; charset=x-user-defined');
	    	else
	    		req.responseType = "arraybuffer";
	    }
	    
	    req.onreadystatechange = function() {
	
	    	// if it is finished
	    	if (this.readyState == 4)
			{
	    		log("finishing url ", url, " ", this.status);

	    		var getResponseHeadersHack = function(xhr) {
	    			var headers = ['ETag','Content-Type', 'x-dropbox-metadata'];
	    			var result = [];
	    			for (var i=0; i<headers.length; ++i)
	    			{
	    				var headerName = headers[i];
	    				if (xhr.getResponseHeader(headerName))
	    					result.push([headerName, xhr.getResponseHeader(headerName)]);
	    			}
	    			
	    			return result;
	    		};
	    		
	    		// and it succeeded
		    	if (200 <= this.status && this.status < 300)
		    	{
		    		if (binaryOutput)
		    		{
		    			var b64 = null;
		    			
		    			if (req.overrideMimeType)
		    			{
			    			var data = Utf.fromBinString(req.responseText);
			    			b64 = Base64.encode(data);
		    			}
		    			else
	    				{
			    			var data = new Uint8Array(req.response);
			    			b64 = Base64.encode(data);
	    				}
		    			
		    			log("url binary data length " + (b64.length * 3 / 4));
			    		callback.invoke(b64, getResponseHeadersHack(this));
		    		}
		    		else
		    		{
		    			log("url data length " + req.responseText.length);
		    			callback.invoke(req.responseText, getResponseHeadersHack(this));
		    		}
		    	}
		    	else
		    	{
					log("url NULL data");
		    		callback.invoke(null, getResponseHeadersHack(this));
		    	}
		    	
		    	this.onreadystatechange = null;
			}
	    };
		
	    req.send(contents);
	}
		
} ;

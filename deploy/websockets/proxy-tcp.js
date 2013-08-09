var WebSocket = require('ws');
var WebSocketServer = WebSocket.Server;
var http = require('http');
var net = require('net');

var server = 
  http.createServer(function (req, res) {
    res.writeHead(200, {'Content-Type': 'text/plain'});
      res.end('Not implemented');
    });
    
server.listen(8081, "0.0.0.0", null);
closeServer = function() { self._server.close(); };

var incrementingId = 0;

function createWebSocket(path, ws) {
	console.log('connection');

	ws.xStringVal = (new Date()) + ":" + incrementingId++;
	ws.xString = function() { return ws.xStringVal; };
	ws.proxy = net.createConnection(path);
	ws.proxy.buffer = "";
	ws.proxy.connected = false;
	ws.proxy.outQueue = [];
	ws.proxy.inQueue = [];

	ws.proxy.sendQueue = function() {
		if (!this.connected) {
			while (this.outQueue.length > 0)
			{
				var message = ws.proxy.outQueue.shift();
				console.log(ws.xString() + ' C -> ' + message);  
				ws.proxy.write(message.trim() + "\r\n");
			}
		}
	};

	ws.proxy.receiveQueue = function() {
		while (this.inQueue.length > 0)
		{
			var message = ws.proxy.inQueue.shift();
			console.log(ws.xString() + ' <- S ' + message);  
			ws.send(message.trim() + "\r\n");
		}
	};

	ws.proxy.queueAndReceive = function() {
		var retPos = -1;
		while ((retPos=this.buffer.indexOf("\n"))!=-1)
		{
			var next = this.buffer.substring(0, retPos);
			this.buffer = this.buffer.substring(retPos+1);
			this.inQueue.push(next.trim());
		}

		this.receiveQueue();
	};

	ws.proxy.on('data', function(message) { 
		console.log(ws.xString() + ' <- Q ' + message); 
		this.buffer += message; 
		ws.proxy.queueAndReceive(); 
	});
	ws.proxy.on('close', function() { console.log(ws.xString() + ' close S'); ws.close() });
	ws.proxy.on('open', function() { ws.proxy.connected = true; ws.proxy.sendQueue(); });

	ws.on('message', function(message) { 
		console.log(ws.xString() + ' Q ->', message);
		ws.proxy.outQueue.push(message);
		ws.proxy.sendQueue();
	});

	ws.on('close', function() { console.log(ws.xString() + ' C close'); ws.proxy.end(); });
}


wss = new WebSocketServer({server:server, path:"/Mailiverse/KeyServer"});
wss.on('connection', function(ws) { createWebSocket(7000, ws); });

wss = new WebSocketServer({server:server, path:"/Mailiverse/MailServer"});
wss.on('connection', function(ws) { createWebSocket(7001, ws); }); 


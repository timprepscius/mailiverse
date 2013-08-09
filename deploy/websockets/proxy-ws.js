var WebSocket = require('ws');
var WebSocketServer = WebSocket.Server;
var http = require('http');

var server = 
  http.createServer(function (req, res) {
    res.writeHead(200, {'Content-Type': 'text/plain'});
      res.end('Not implemented');
    });
    
server.listen(8081, "0.0.0.0", null);
closeServer = function() { self._server.close(); };

function createWebSocket(path, ws) {
	console.log('connection');

	ws.proxy = new WebSocket(path);
	ws.proxy.queue = [];

	ws.proxy.on('message', function(message) { console.log('<- P ' + message); ws.send(message); });
	ws.proxy.on('close', function() { console.log('close P'); ws.close() });
	ws.proxy.on('open', function() {
		while (ws.proxy.queue.length > 0)
		{
			var message = ws.proxy.queue.shift();
			console.log('C -> ' + message);  
			ws.proxy.send(message);
		}
		ws.proxy.queue = null;
	});

	ws.on('message', function(message) { 
		if (ws.proxy.queue != null)
		{
			console.log('C q ' + message);
			ws.proxy.queue.push(message);
		}
		else 
		{
			console.log('C -> ' + message);  
			ws.proxy.send(message); 
		}
	});

	ws.on('close', function() { console.log('C close'); ws.proxy.close(); });
}


wss = new WebSocketServer({server:server, path:"Mailiverse/KeyServer"});
wss.on('connection', function(ws) { createWebSocket('ws://red:8080/Mailiverse/KeyServer', ws); });

wss = new WebSocketServer({server:server, path:"Mailiverse/MailServer"});
wss.on('connection', function(ws) { createWebSocket('ws://red:8080/Mailiverse/MailServer', ws); }); 


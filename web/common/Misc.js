
function startsWith(l,r)
{
	return l.lastIndexOf(r, 0) === 0;
}
function _bind(o)
{
	return app.service.JSInvoker.wrap(o);
}

function htmlForTextWithEmbeddedNewlines(text) {
    var htmls = [];
    var lines = text.split(/\n/);
    // The temporary <div/> is to perform HTML entity encoding reliably.
    //
    // document.createElement() is *much* faster than jQuery('<div/>')
    // http://stackoverflow.com/questions/268490/
    //
    // You don't need jQuery but then you need to struggle with browser
    // differences in innerText/textContent yourself
    var tmpDiv = jQuery(document.createElement('div'));
    for (var i = 0 ; i < lines.length ; i++) {
        htmls.push(tmpDiv.text(lines[i]).html());
    }
    return htmls.join("<br>");
}

function htmlBalance(value) {
  return $('<div/>').html(value).html();
}

function htmlEncode(value){
  return $('<div/>').text(value).html();
}

function htmlDecode(value){
  return $('<div/>').html(value).text();
}

function stripNotNiceHTML(html) 
{
	// remove the <html> tag
	html = html.replace(/[\s\S]*<\s*html[\s>]+[\s\S]*?>/gi, "");

	// try to remove the head block
	html = html.replace(/[\s\S]*<\s*\/head[\s>]+[\s\S]*?>/gi, "");
	
	// remove scripting if possible
	html = html.replace(/<\s*script[\s>]+[\s\S]*?<\s\/script\[\s\S]*?>/gi,"");
	
	// remove before the body
	html = html.replace(/[\s\S]*<\s*body>/gi,"<div>");
	html = html.replace(/[\s\S]*<\s*body\s/gi,"<div ");
	
	// remove after the body
	html = html.replace(/<\s*\/body[\s>]+[\s\S]*/gi,"</div>");
	
	// remove the html tag ender and everyhting aftewards
	html = html.replace(/<\s*\/html[\s>]+[\s\S]*/gi,"");

	return html;
}

function trimQuotes(s)
{
	if (s == null)
		return null;
	
	return s.replace(/^\"*|\"*$/g, "");	
}

function NOHREF(x)
{
	var result = "" + x;
	result = result.replace(/__HREF/g, 'href="javascript:void(0)"');
	return result;
}

function log() {}

function logFormatDate ()
{
        var d = new Date();
        return "" + d.getHours() + ":" + d.getMinutes() + ":" + d.getSeconds() + "." + d.getMilliseconds();
}

function log_()
{
    var out = [ logFormatDate() + ":" ];
    for (var i=0; i<arguments.length; ++i)
            out.push("" + arguments[i]);

	console.log(out.join(" "));
}

function logException(e)
{
	console.log(e);
}

var clearingTimers = {};

function keyedTimer(key, f, interval)
{
	if (clearingTimers[key] != undefined)
		window.clearTimeout(clearingTimers[key]);
		
	clearingTimers[key] = window.setTimeout(f, interval);
}

function onNextTick (f)
{
	window.setTimeout(f, 1);
}

function onSoon(f)
{
	window.setTimeout(f,50);
}

function isAlphaNumeric (t)
{
	return (/^[a-z0-9]+$/i).test(t);
}


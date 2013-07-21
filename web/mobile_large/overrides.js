
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

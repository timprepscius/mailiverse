/***** mInitializer *****/

mInitializer = {
	onDocumentComplete: function()
	{
		log("on document complete initializing");
		mEditor.initialize();
		mThreads.initialize();
		mReader.initialize();
		mCompose.initialize(); 
		mMain.initialize();
		log("initialized");
	},
	
	onDocumentPartial: function()
	{
		log("onDocumentPartial event dispatch");
		mLogin.initialize();
	},
	
	onServiceBootstrap: function()
	{
		setInterval( function(){ MService.dispatchEvents(); }, 250);		
	}
} ;

function onMailiverseBootstrapGWT(service)
{
	MService = service;
	mInitializer.onServiceBootstrap();
}

function onMailiverseServiceLoaded ()
{
	MService.setDelegate(delegate);
	mLogin.enableButton();
}

$(function() { mInitializer.onDocumentComplete();});

MService = null;

function __mailiverseIsFullyLoaded__()
{
	return MService != null && $('#_mMain') != null;
}

function __indicateNative__()
{
	$('#_SignInUp').css('background-color', '#C1EFC1');
}

function __ios_start__()
{
	if (__mailiverseIsFullyLoaded__())
	{
		mDispatch.startNative();
		__indicateNative__();
		return 'Yes';
	}

	return 'No';
}

function __log_out__()
{
	location.reload();
}

/***** mInitializer *****/

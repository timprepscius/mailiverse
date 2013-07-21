/***** mInitializer *****/

mInitializer = {
	onDocumentComplete: function()
	{
		mSignUp.initialize();
		log("initialized");
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
	MService.setDelegate(mDelegate);
	$('#_mLoading').hide();
	mSignUp.onServiceLoaded();
}

$(function() { mInitializer.onDocumentComplete();});


/***** mInitializer *****/

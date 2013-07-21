/***** mInitializer *****/

mInitializer = {
	onDocumentComplete: function()
	{
		log("on document complete initializing");
		mRefill.initialize();
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
	mRefill.onServiceLoaded();
}

$(function() { mInitializer.onDocumentComplete();});


/***** mInitializer *****/

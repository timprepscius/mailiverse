/***** mOriginal.js *****/

mOriginal = {
		
	current: null,
	
	open : function(path)
	{
		$('#_mOriginal_header').text('');
		$('#_mOriginal_content').text('');
		$('#_mOriginal').show();

		mOriginal.current = mMain.client.getOriginal(path);
		$('#_mOriginal_header').text(mOriginal.current.getPath());
	},
		
	close : function()
	{
		mOriginal.current = null;
		$('#_mOriginal').hide();
	},
	
	onOriginalLoaded: function(orig)
	{
		if (mOriginal.current != null && mOriginal.current.equals(orig))
		{
			var data = null;
			if (orig.hasException())
				data = '' + orig.getException().toString();
			else
				data = '' + orig.getDataAsString();
			
			$('#_mOriginal_content').text(data);
		}
	}
};

/***** mOriginal.js *****/

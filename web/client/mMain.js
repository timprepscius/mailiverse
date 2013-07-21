/***** mMain.js *****/

 $(document).ready(function() {
	 $('input.deletable').wrap('<span class="deleteicon" />').after($('<span/>').click(function() {
 $(this).prev('input').val('').change();
  }));
});

//------------------------------------------------------------------------------------------//

mMain = 
{
	client: null,
	isUploading: false,
	isDownloading: false,
	isCacheDirty: false,
	isCaching: false,
	isCheckingMail: false,
	checkMailIsReady: false,
	
	CHECK_MAIL_INTERVAL: 30,
	ATTEMPT_FLUSH_INTERVAL: 1,
	GARBAGE_COLLECT_INTERVAL: 5,
	checkMailCountdown: 5,
	
	signOut: function()
	{
		window.location.reload();
	},
	
	authenticate: function(name, password)
	{
		log("authenticating with ", name, " password ", password);
		MService.authenticate(name, password);	
	},
		
	checkMail: function()
	{
		log("checkMail: ", mMain.client);
		mMain.checkMailIsReady = true;
		mMain.attemptCheckMail();
	},
	
	flushStore: function()
	{
		mMain.client.flushStore();
	},
	
	onSettingsFinished: function (shouldSave)
	{
		if (shouldSave)
			mMain.client.getSettings().set("name", $('#_mMain_settings_name').val());

		$("#_mMain_settings").hide();
	},
	
	showSettings: function()
	{
		$('#_mMain_settings_name').val(mMain.client.getSettings().get("name", ""));
		$("#_mMain_settings").show();
	},
	
	compose: function()
	{
		mCompose.open();
		mReader.close();
		mThreads.close();
		mOriginal.close();
	},
	
	composeMail: function(conversation, mail)
	{
		mReader.close();
		mThreads.close();
		mOriginal.close();
		mCompose.openMail(conversation, mail);
	},
	
	showOriginal: function(mail)
	{
		mCompose.close();
		mReader.close();
		mThreads.close();
		mOriginal.open(mail);
	},
	
	
	//--------------------------------------------------
	
	onSend: function()
	{
		mFolders.render();
		mThreads.onDirty();
	},
	
	onSendSucceeded: function()
	{
		mMain.checkMail();
		mFolders.render();
		mThreads.onDirty();
	},
	
	onSendFailed: function()
	{
		mFolders.render();
		mThreads.onDirty();
	},
	
	//--------------------------------------------------

	onNetworkError: function(reason)
	{
		$('#_mHeader_network_error').text(reason);
	},
	
	updateStatusButton: function(v, b)
	{
		if (v)
		{
			$(b+"_on").show();
			$(b+"_off").hide();
		}
		else
		{
			$(b+"_on").hide();
			$(b+"_off").show();
		}
	},

	onCheckBegin: function()
	{
		mMain.isCheckingMail = true;
		$('#_mHeader_network_error').text('');
		mMain.updateStatusButton(mMain.isCheckingMail, '#_mHeader_check_image');
		$('#_mHeader_checkMail_button').prop('disabled', true);
	},
	
	onCheckStep: function(numbers)
	{
		var split = numbers.split(":");
		$('#_mMain_checkMail_message').text(split[0] + " / " + split[1] + " of "+ split[2]);
		
	},
	
	onCheckEnd: function()
	{
		mMain.isCheckingMail = false;
		mMain.updateStatusButton(mMain.isCheckingMail, '#_mHeader_check_image');
		$('#_mHeader_checkMail_button').prop('disabled', false);
		$('#_mMain_checkMail_message').text('');
	},
	
	onUploadBegin: function()
	{
		mMain.isUploading = true;
		mMain.updateStatusButton(mMain.isUploading, '#_mHeader_uploading');
	},
	
	onUploadEnd: function()
	{
		mMain.isUploading = false;
		mMain.updateStatusButton(mMain.isUploading, '#_mHeader_uploading');
	},
	
	onDownloadBegin: function()
	{
		mMain.isDownloading = true;
		mMain.updateStatusButton(mMain.isDownloading, '#_mHeader_downloading');
	},
	
	onDownloadEnd: function()
	{
		mMain.isDownloading = false;
		mMain.updateStatusButton(mMain.isDownloading, '#_mHeader_downloading');
	},

	//----------------------------------------
	
	onCalculateBegin: function()
	{
		mMain.updateStatusButton(true, '#_mHeader_calculating');
	},
	
	onCalculateEnd: function()
	{
		mMain.updateStatusButton(false, '#_mHeader_calculating');
	},
	
	onCacheDirty: function()
	{
		mMain.isCacheDirty = true;
		mMain.updateStatusButton(mMain.isCacheDirty, '#_mHeader_cacheDirty');
	},

	onCacheClean: function()
	{
		mMain.isCacheDirty = false;
		mMain.updateStatusButton(mMain.isCacheDirty, '#_mHeader_cacheDirty');
	},
	
	onCacheBegin: function()
	{
		mMain.isCaching = true;
		mMain.updateStatusButton(mMain.isCaching, '#_mHeader_caching');
	},
	
	onCacheSuccess: function()
	{
		$('#_mHeader_network_error').text('');
	},
	
	onCacheEnd: function()
	{
		mMain.isCaching = false;
		mMain.updateStatusButton(mMain.isCaching, '#_mHeader_caching');
	},
	
	//-----------------------------------------
	
	attemptFlush: function()
	{
		mMain.flushStore();
	},
	
	attemptCheckMail: function()
	{
		if (!mMain.isCheckingMail)
		{
			mMain.client.checkMail();
			mMain.checkMailCountdown = mMain.CHECK_MAIL_INTERVAL;
		}
	},
	
	attemptCheckMailCountdown: function()
	{
		if (mMain.checkMailIsReady)
			mMain.checkMailCountdown--;
		
		var countDown = mMain.checkMailCountdown;
		if (mMain.checkMailCountdown<=0)
		{
			countDown = '...';
			mMain.attemptCheckMail();
		}
		
		$('#_mHeader_checkMail_button').html('Check Mail (' + countDown + ')');
	},
	
	//-------------------------------------------------------
	
	garbageCollect: function()
	{
		MService.garbageCollect();
	},
	
	onDaysLeft: function(r)
	{
		if (r.hasException())
		{
			$('#_mHeader_service_time_value').text(r.getException());

			$('#_mHeader_service_time').removeClass("error warning");
			$('#_mHeader_service_time').addClass("error");
		}
		else
		{
			$('#_mHeader_service_time_value').text(r.getObject());
			
			var daysLeft = parseInt("" + r.getObject());
			$('#_mHeader_service_time').removeClass("error warning");
			
			if (daysLeft <= 0)
				$('#_mHeader_service_time').addClass("error");
			if (daysLeft < 30)
				$('#_mHeader_service_time').addClass("warning");
		}
	},
	
	refreshDaysLeft : function()
	{
		$('#_mHeader_service_time_value').text('?');
		
		mMain.client.getDaysLeft({
			invoke : function(r){ mMain.onDaysLeft(r); }
		});
	},
	
	onCheckMailIsReady: function()
	{
		mMain.checkMailIsReady = true;
	},
	
	onInitialized: function()
	{
		mFolders.openWithName("system", "All");
		setInterval( function(){ mMain.attemptFlush(); }, mMain.ATTEMPT_FLUSH_INTERVAL * 1000);
		setInterval( function(){ mMain.attemptCheckMailCountdown(); }, 1 * 1000);
		setInterval( function(){ mMain.garbageCollect(); }, mMain.GARBAGE_COLLECT_INTERVAL * 1000);
		
		// onNextTick( function() { mMain.refreshDaysLeft(); });
	},

	initialize: function() 
	{
		mReader.close();
		mCompose.close();
		
		// looks better when it is open on startup
		// mThreads.close();
		
		mOriginal.close();		
	}
} ;

/***** mMain.js *****/

function onCalculateEventEnd(num)
{
	if (num == 0)
		mMain.onCalculateEnd();
}

function onCalculateEventBegin(num)
{
	if (num == 1)
		mMain.onCalculateBegin();
}

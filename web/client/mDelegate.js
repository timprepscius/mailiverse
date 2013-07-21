/***** mDelegate.js *****/

var delegate = 
{
	onLogin: function(p) 
	{
		if (p.hasException())
		{
			log('onLogin failed');
			mLogin.loginFailed();
		}
		else
		{
			log('onLogin succeeded');
			mMain.client = p.getObject();
			mLogin.loginSucceeded();
		}
	},
	
	onLoginStep: function(s)
	{
		mLogin.loginStep(s);
	},
		
	signal: function(_e,p) 
	{
		e = '' + _e;
		log('signal ',e,p);
		
		if (e == 'onAuthenticationStep')
			this.onLoginStep(p);
		else
		if (e == 'onLogin')
			this.onLogin(p);
		else
		if (e == 'onInitialized')
			mMain.onInitialized();
		else
		if (e == 'onFirstRunInitialization')
			mMain.onCheckMailIsReady();
		else
			

		if (e == 'onCacheDirty')
			mMain.onCacheDirty();
		else
		if (e == 'onCacheClean')
			mMain.onCacheClean();
		else
		if (e == 'onCacheBegin')
			mMain.onCacheBegin();
		else
		if (e == 'onCacheEnd')
			mMain.onCacheEnd();
		else
		if (e == 'onCacheFailure')
			mMain.onNetworkError(p);
		else
		if (e == 'onCacheSuccess')
			mMain.onCacheSuccess();
		else
			
			
		if (e == 'onCheckBegin')
			mMain.onCheckBegin();
		else
		if (e == 'onCheckStep')
			mMain.onCheckStep(p);
		else
		if (e == 'onCheckEnd')
			mMain.onCheckEnd();
		else
		if (e == 'onCheckFailure')
			mMain.onNetworkError("Failed to check mail.");
		else
			
			
		if (e == 'onUploadBegin')
			mMain.onUploadBegin();
		else
		if (e == 'onUploadEnd')
			mMain.onUploadEnd();
		else
		if (e == 'onDownloadBegin')
			mMain.onDownloadBegin();
		else
		if (e == 'onDownloadEnd')
			mMain.onDownloadEnd();
		else
			
		if (e == 'onSendSucceeded')
			mMain.onSendSucceeded();
		else
		if (e == 'onSendFailed')
			mMain.onSendFailed();
			
		else
		if (e=='renderFolders')
			mFolders.render();
		else
		if (e=='renderThreads')
			mThreads.onDirty();
		else
		if (e=='onNewFolder' || e == 'onLoadFolder')
		{
			keyedTimer('renderFolders', function() {delegate.signal('renderFolders');}, 100);

			if (e=='onLoadFolder')
				mThreads.onDirtyFolder(p.getName());
		}
		else
		if (e=='onLoadFolderPart')
		{
			mThreads.onDirtyFolder(p.getName());
			mMain.onCheckMailIsReady();
		}
		else
		if (e=='onNewConversation' || e == 'onNewMail' || e == 'onChangedConversation')
		{
			keyedTimer('renderThreads', function() {delegate.signal('renderThreads');}, 100);
			keyedTimer('renderFolders', function() {delegate.signal('renderFolders');}, 100);
		}
		else
		if (e=='onLoadConversation')
		{
			keyedTimer('renderThreads', function() {delegate.signal('renderThreads');}, 100);
		}
		else
		if (e == 'onLoadMail')
		{
			mReader.partialRender(p);
		}
		else
		if (e == 'onDeleteMail')
		{
			mReader.refresh();
			keyedTimer('renderThreads', function() {delegate.signal('renderThreads');}, 100);
			keyedTimer('renderFolders', function() {delegate.signal('renderFolders');}, 100);
		}
		else
		if (e == 'onOriginalLoaded')
		{
			mOriginal.onOriginalLoaded(p);
		}
		else
		if (e == 'onAttachmentsLoaded')
		{
			mReader.onAttachmentsLoaded(p);
		}
	}
};

for (x in mDelegateCommon)
{
	delegate[x] = mDelegateCommon[x];
	console.log(x);
}

mDelegate = delegate;

/***** mDelegate.js *****/

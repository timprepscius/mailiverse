/********************************/

mFolders = {
		
	selectedFolderSet: null,
	selectedFolderId: null,
		
	open: function(folderSet, folderId)
	{
		log("mFolders.openSystemFolder begin ", folderSet, folderId);

		mFolders.selectedFolderSet = folderSet;
		mFolders.selectedFolderId = folderId;
		
		mFolders.render();
		mCompose.close();
		mReader.close();
		mOriginal.close();
		mThreads.show(folderSet, folderId);
		
		log("mMain.openSystemFolder end ", folderSet, folderId);
	},
	
	openWithName: function(folderSet, folderName)
	{
		mFolders.open(folderSet, mFolders.getFolderIdWithName(folderSet, folderName));
	},
		
	getFolder: function(folderSet, folderId)
	{
		if (folderSet == "system")
			return mMain.client.getSystemFolder(folderId);
		
		return mMain.client.getUserFolder(folderId);
	},
	
	renderFolderSet: function(folderSet, foldersIds, exclude)
	{
		var all = [];
		for (var i=0; i<foldersIds.length; ++i)
		{
			var folderId = foldersIds[i];
			var folder = mFolders.getFolder(folderSet, folderId);
			var folderName = folder.getName();
			
			if (folderName == exclude)
				continue;

			var selected = '';
			if (folderSet == mFolders.selectedFolderSet && folderId == mFolders.selectedFolderId)
				selected = 'class="selected"';
			var count = folder.getNumConversations();
			var row = '<tr><td #SELECTED#><a __HREF onClick="mFolders.open(\'#SET#\',\'#ID#\');">#NAME#(#COUNT#)</a></td></tr>';
			row = row
					.replace(/#SELECTED#/g, selected)
					.replace(/#SET#/g, folderSet)
					.replace(/#ID#/g, folderId)
					.replace(/#NAME#/g, folderName)
					.replace(/#COUNT#/g, ""+count);
			
			all.push(row);
		}
		return all;
	},
	
	renderUserFoldersToMoreOptionsTags: function()
	{
		var foldersIds = mMain.client.getUserFolders();
		
		var all = [];
		for (var i=0; i<foldersIds.length; ++i)
		{
			var folderId= foldersIds[i];
			var folder = mMain.client.getUserFolder(folderId);
			var folderName = folder.getName();
			
			all.push(
				"<option value='add:#ID#'>#NAME#</option>"
					.replace(/#ID#/g, folderId)
					.replace(/#NAME#/g, folderName)
			);
		}
		
		$('#_mThreads_content_header_moreActions_folders').html(all.join(''));
		$('#_mReader_content_header_moreActions_folders').html(all.join(''));
	},

	render: function()
	{
		log("mFolders.render begin");
		var folderIds = mMain.client.getSystemFolders();
		var all = mFolders.renderFolderSet("system", folderIds, "Repository");
		$('#_mMain_systemFolders').html(NOHREF(all.join('')));
		
		var folderIds = mMain.client.getUserFolders();
		var all = mFolders.renderFolderSet("user", folderIds, null);
		$('#_mMain_userFolders').html(NOHREF(all.join('')));
		
		mFolders.renderUserFoldersToMoreOptionsTags();
		
		var hasSelectedUserFolder = mFolders.selectedFolderSet == "user";
		$('#_mMain_userFoldersAdd_deleteUserFolder').prop('disabled', !hasSelectedUserFolder);	
		$('#_mMain_userFoldersAdd_renameUserFolder').prop('disabled', !hasSelectedUserFolder);	

		log("mFolders.render end");
	},
	
	newUserFolder: function()
	{
		mMain.client.newUserFolder();
	},
	
	deleteUserFolder: function ()
	{
		mMain.client.deleteUserFolder(mFolders.selectedFolderId);
		mFolders.openWithName("system", "All");
	},
	
	onUserFolderRename: function(name)
	{
		mFolders.getFolder(mFolders.selectedFolderSet, mFolders.selectedFolderId).setName(name);
		mFolders.render();
	},
	
	onRenameFinished: function(shouldSave)
	{
		if (shouldSave)
			mFolders.onUserFolderRename($('#_mMain_userFolders_renameForm_name').val());
		
		$("#_mMain_userFolders_renameForm").hide();
	},
	
	renameUserFolder: function ()
	{
		$('#_mMain_userFolders_renameForm_name').val(
			mFolders.getFolder(mFolders.selectedFolderSet, mFolders.selectedFolderId).getName()
		);
		
		$("#_mMain_userFolders_renameForm").show();
	},
	
	getFolderIdWithName: function(folderSet, folderName)
	{
		var folderIds = mMain.client.getSystemFolders();
		for (var i=0; i<folderIds.length; ++i)
		{
			var folder = mFolders.getFolder(folderSet, folderIds[i]);
			if (folder.getName() == folderName)
				return folderIds[i];
		}
		
		return null;
	},
};

/*********************************/
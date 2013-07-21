/***** mThreads.js *****/

mThreads = {
		
	visible: false,
	dirty: false,

	jsConversations: null,
	javaConversations: null,
	maxConversations: 0,
	
	currentFolderSet:null,
	currentFolderId:null,
	currentFolderName:null,
	
	currentSearch:null,
	currentPosition:0,
	threadsPerPage: 25,

	showSpecials: function(currentFolderSet, currentFolderName)
	{
		var isTrash = currentFolderName == 'Trash' && currentFolderSet == 'system';
		if (isTrash)
		{
			$('#_mThreads_content_header_delete').hide();
			$('#_mThreads_content_header_destroy').show();
			$('#_mThreads_content_header_undelete').show();
		}
		else
		{
			$('#_mThreads_content_header_delete').show();
			$('#_mThreads_content_header_undelete').hide();
			$('#_mThreads_content_header_destroy').hide();
		}

		$('#_mThreads_content_header_select_all').prop('checked', false);
	},
		
	show: function(folderSet, folderId)
	{
		log("mThreads.show begin");
		
		if (mThreads.currentFolderSet != folderSet || mThreads.currentFolderId != folderId)
		{
			mThreads.dirty = true;
			mThreads.currentFolderSet = folderSet;
			mThreads.currentFolderId = folderId;
			mThreads.currentPosition = 0;

			var folder = mFolders.getFolder(folderSet, folderId);
			mThreads.currentFolderName = folder.getName();
			mThreads.showSpecials(folderSet, mThreads.currentFolderName);
		}

		if (mThreads.dirty)
			mThreads.render();
		
		if (!mThreads.visible)
		{
			mThreads.visible = true;
			$('#_mThreads_content_header').show();
			$('#_mThreads_container').show();
		}
		log("mThreads.show end");
	},
	
	close: function()
	{
		log("mThreads.close begin");
		mThreads.visible = false;
		
		$('#_mThreads_content_header').hide();
		$('#_mThreads_container').hide();
		log("mThreads.close end");
	},
	
	onDirty: function()
	{
		mThreads.dirty = true;
		if (mThreads.visible)
			mThreads.render();
	},
	
	onSearch: function(s)
	{
		if (s == "")
			s = null;
		mThreads.currentSearch = s;
		mThreads.onDirty();
	},
	
	onDirtyFolder: function(folderName)
	{
		if (startsWith(folderName, mThreads.currentFolderId))
		{
			mThreads.dirty = true;
			if (mThreads.visible)
				mThreads.render();
		}
	},
	
	applyToMarked: function(f)
	{
		var marked = [];
		for (var i=0; i<mThreads.jsConversations.length; ++i)
		{
			if ($('#_mThreads_select_' + i).prop('checked'))
				marked.push(i);
		}
		
		for (var i=0; i<marked.length; ++i)
			f.call(this, mThreads.javaConversations[marked[i]]);
		
		mThreads.render();
		$('#_mThreads_content_header_moreActions').val("none");
	},
	
	//------------------------------------------------------------------------

	onDestroy:function()
	{
		mThreads.applyToMarked(function(c) {
			mMain.client.deleteConversation(c);
		});
	},
	
	onDelete: function()
	{
		mThreads.applyToMarked(function(c) {
			c.markState("TRASH");
			mMain.client.reindexConversation(c);
		});
	},
	
	onUndelete: function()
	{
		mThreads.applyToMarked(function(c) {
			c.unmarkState("TRASH");
			mMain.client.reindexConversation(c);
		});
	},
	
	markAsRead: function()
	{
		mThreads.applyToMarked(function(c) {
			c.markState("READ");
		});
	},
	
	markAsUnread: function()
	{
		mThreads.applyToMarked(function(c) {
			c.unmarkState("READ");
		});
	},
	
	markAsSpam: function()
	{
		mThreads.applyToMarked(function(c) {
			c.markState("SPAM");
			mMain.client.reindexConversation(c);
		});
	},

	markAsNotSpam: function()
	{
		mThreads.applyToMarked(function(c) {
			c.unmarkState("SPAM");
			mMain.client.reindexConversation(c);
		});
	},
	
	addToUserFolder: function(folderId)
	{
		mThreads.applyToMarked(function(c) {
			mMain.client.addToUserFolder(folderId, c);
		});
		
		mFolders.render();
	},
	
	removeFromThisFolder: function()
	{
		if (mThreads.currentFolderSet != "user")
			return;
		
		mThreads.applyToMarked(function(c) {
			mMain.client.removeFromUserFolder(mThreads.currentFolderId, c);
		});
		
		mFolders.render();
	},
	
	onMoreAction: function(action)
	{
		if (action == 'rd')
			mThreads.markAsRead();
		else
		if (action == 'urd')
			mThreads.markAsUnread();
		else
		if (action == 'sp')
			mThreads.markAsSpam();
		else
		if (action == 'nsp')
			mThreads.markAsNotSpam();
		else
		if (action == 'rem')
			mThreads.removeFromThisFolder();
		else
		if (startsWith(action, "add:"))
			mThreads.addToUserFolder(action.substr(4));
	},
	
	onSelectAll: function()
	{
		var checked = $('#_mThreads_content_header_select_all').prop('checked');
		
		for (var i=0; i<mThreads.jsConversations.length; ++i)
		{
			$('#_mThreads_select_' + i).prop('checked',checked);
		}
	},

	//------------------------------------------------------------------------

	getCurrentJavaFolder: function()
	{
		return mFolders.getFolder(mThreads.currentFolderSet, mThreads.currentFolderId);
	},
	
	render: function() 
	{
		log("mThreads.render begin");
		
		var javaFolder = mThreads.getCurrentJavaFolder();
		log("got javaFolder ", javaFolder);
		
		mThreads.javaConversations = 
			_bind(
				javaFolder.getConversations(
					mThreads.currentPosition, mThreads.threadsPerPage, mThreads.currentSearch
				)
			);
		mThreads.maxConversations = javaFolder.getNumConversations();
		
		log("mThreads.render after getConversations");

		var jsConversationsJSONOld = mThreads.jsConversationsJSON;
		mThreads.jsConversationsJSON = '' + mMain.client.getMinimalJSONForConversations(mThreads.javaConversations);
		if (jsConversationsJSONOld == mThreads.jsConversationsJSON)
		{
			log("conversationJSON is equal, returning, but going to render anyway");
//			return;
		}
		else
		{
//			log(jsConversationsJSONOld,mThreads.jsConversationsJSON);
		}

		mThreads.jsConversations = JSON.parse(mThreads.jsConversationsJSON);

		/*
		var numThreads = mThreads.javaConversations.length;
		for (var i=0; i<numThreads; ++i)
		{
			var javaConversation = mThreads.javaConversations[i];
			mThreads.jsConversations.push(JSON.parse('' + javaConversation.getJSONForThreads()));
		}
		*/
			
		mThreads.renderData(mThreads.jsConversations);
		mThreads.dirty = false;

		log("mThreads.render end");
	},
	
	renderData: function(j)
	{
		log("mThreads.renderData begin");
		log("num conversations ", j.length);
		
		var all = [];
		
		for (var i=0; i<j.length; ++i)
		{
			var d = j[i];
			var ab = '<a __HREF onClick="mThreads.openConversation('+i+');">';
			var ae = '</a>';

			var content = null;
			if (d.loaded)
			{
				var clazz = "unread";
				if (d.state.indexOf("READ")!=-1)
					clazz = "read";
				
				var draftMarker = "";
				if (d.state.indexOf("DRAFT")!=-1)
					draftMarker= '<span class="important_tag">Draft</span>';
				
				var sendMarker = "";
				if (d.state.indexOf("SENDING")!=-1)
					sendMarker= '<span class="important_tag">Sending</span>';

				var attachmentMarker = "";
				if (d.state.indexOf("ATTACHMENT")!=-1)
					attachmentMarker= '<img src="img/attachment.png" class="thread_icon"/>';

				var numItems = d.numItems > 1 ? (' (' + d.numItems + ')') : '';
				content = 
					'<td class="select"><input type="checkbox" class="selector" id="_mThreads_select_'+i+'"></td>' +
					'<td class="participants">' + d.participants + numItems + draftMarker + sendMarker + attachmentMarker + '</td>' +
					'<td class="content">' + ab + 
						'<span class="subject">' + htmlEncode(d.subject) + '</span>' + 
						'<span class="brief">' + htmlEncode(d.brief) + '</span>' + ae + '</td>' +
					'<td class="date">' + d.date + '</td>';
			}
			else
			{
				content = 
					'<td class="loading" colspan="4">Loading...</td>';
			}
			
			var row = '<tr id="mThread_'+i+'" class="' + clazz + '">' + content + '</tr>';			
			all.push(row);
		}
		
		$('#_mThreads').html(NOHREF(all.join('')));
		
		$('#_mMain_content_header_range_numbers').html(NOHREF(
			mThreads.currentPosition + "-" + 
			(mThreads.currentPosition + mThreads.jsConversations.length) + " of " +
			((mThreads.currentSearch == null) ? mThreads.maxConversations : "?")
		));
		
		log("mThreads.renderData end");
	},
	
	markUiRead: function(i, read)
	{
		mThreads.jsConversations[i].read = read;
		$('#mThread_' + i).attr("class", read ? "read" : "unread");
	},
	
	openConversation: function(i)
	{
		log("mThreads.openConversation");
		
		mThreads.close();
		mThreads.markUiRead(i, true);
		mReader.open(mThreads.javaConversations[i]);
	
		var set = mThreads.currentFolderSet;
		var id = mThreads.currentFolderId;
		var name = mThreads.currentFolderName;
		$('#mReader_content_header_navigation').html(
			NOHREF('<a __HREF onClick="mFolders.open("#SET#", "#ID#");">Back to #NAME#</a>')
				.replace("#SET#", set)
				.replace("#ID#", id)
				.replace("#NAME#", name)
		);

		log("mThreads.openConversation end");
	},

	clipPosition: function()
	{
		var maxPosition = 
			mThreads.maxConversations - 
				(mThreads.maxConversations % mThreads.threadsPerPage);
		
		if (mThreads.currentPosition > maxPosition)
			mThreads.currentPosition = maxPosition;
		
		if (mThreads.currentPosition < 0)
			mThreads.currentPosition = 0;
	},
	
	onMove: function(d)
	{
		mThreads.currentPosition += d * mThreads.threadsPerPage;
		mThreads.clipPosition();
		mThreads.dirty = true;
		mThreads.render();
	},
	
	onNewest: function()
	{
		mThreads.currentPosition = 0;
		mThreads.dirty = true;
		mThreads.render();
	},
	
	onOldest: function()
	{
		mThreads.currentPosition = 0xFFFFFF;
		mThreads.clipPosition();
		mThreads.dirty = true;
		mThreads.render();
	},

	initialize: function()
	{
	}
} ;

/***** mThreads.js *****/

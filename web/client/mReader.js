/***** mReader.js *****/

mReader = 
{
	conversation: null,
	mails:null,
	numMails:0,
	mailsAlreadyRendered:null,
	
	open: function(conversation)
	{
		log("mReader.open");
		
		mQuickEditor.begin();
		mReader.conversation = conversation;
		mReader.render();
		
		var isTrash = conversation.getHeader().hasState("TRASH");
		if (isTrash)
		{
			$('#_mReader_content_header_delete').hide();
			$('#_mReader_content_header_undelete').show();
			$('#_mReader_content_header_destroy').show();
		}
		else
		{
			$('#_mReader_content_header_delete').show();
			$('#_mReader_content_header_undelete').hide();
			$('#_mReader_content_header_destroy').hide();
		}
		
		$('#_mReader').show();
		$('#_mReader_content_header').show();
		
		log("mReader.open end");
	},
	
	close: function()
	{
		log("mReader.close");
		
		$('#_mReader').hide();
		$('#_mReader_content_header').hide();
		
		mQuickEditor.end();
		mReader.numMail = 0;
		mReader.mails = null;
		mReader.conversation = null;
		
		log("mReader.close end");
	},
	
	refresh: function()
	{
		var conversation = mReader.conversation;
		if (conversation)
		{
			mReader.close();
			mReader.open(conversation);
		}
	},

	createIdentitiesDiv: function(tag,identities) 
	{
		log("createIdentitiesDiv");
		var all = [];
		for (var i=0; i<identities.length; ++i)
		{
			var identity = identities[i];
			var name = identity.getLongName();
			var email = identity.getEmail();

			var template = 
				'<span class="email">' + 
				(name != null ? '<span class="name">__NAME</span>' : "") +
				(email != null ? '<span class="address">&lt;__EMAIL&gt;</span>' : "") +
				'</span>';
		
			template = template.replace(/__NAME/g, name);
			template = template.replace(/__EMAIL/g, email);
			all.push(template);
		}
		
		var prefix = '<div class="__TAG email_div">__TAG:';
		prefix = prefix.replace(/__TAG/g, tag);

		var postfix = '</div>';
				
		log("createIdentitiesDiv finishing");
		return prefix + all.join(', ') + postfix;
	},
	
	createHeader: function(header)
	{
		log("createHeader");
		
		var all = [];
		
		var author = header.getAuthor();
		var authors = author != null ? [author] : [];
		
		all.push(
			'<div class="top">' + mReader.createIdentitiesDiv("From", authors) +'</div>' +
			'<div class="date">' + header.getDate().toString() + '</div>'
		);
		
		var recipients = header.getRecipients();
		
		if (recipients != null)
		{
			var to = _bind(recipients.getTo());
			var cc = _bind(recipients.getCc());
			var bcc = _bind(recipients.getBcc());
			var replyTo = _bind(recipients.getReplyTo());
			
			if (to.length > 0)
				all.push(mReader.createIdentitiesDiv("To", to));
			if (cc.length > 0)
				all.push(mReader.createIdentitiesDiv("Cc", cc));
			if (bcc.length > 0)
				all.push(mReader.createIdentitiesDiv("Bcc", bcc));
			if (replyTo.length > 0)
				all.push(mReader.createIdentitiesDiv("ReplyTo", replyTo));
		}
		
		log("createHeader finished");
		
		return '<div class="header">' + all.join('') + '</div>';
	},
	
	createBody: function(id, mail)
	{
		log("createBody");
		var body = mail.getBody();

		if (body.hasHTML())
		{
			// need to fix this better
			var html = '' + body.getStrippedHTML();
			log("createBody after getStripped");

			// strip out the images with regex
			html = html.replace(/(<[\s\S]*?img[\s\S]*?)src=([\s\S]*?>)/gi,"$1src='https://localhost/unloadable' nosrc=$2");
			html = htmlBalance(html);
			
			/*
			$(div).find("img").each(function(index) {
			   $(this).attr('nosrc', $(this).attr('src'));
			   $(this).attr('src', 'unloadable');
			});
			*/
			
//			return '<iframe class="body" srcdoc="' + html.replace(/"/g,'\\"') + '"></iframe>';
			return '<div id="_mReader_body_'+ id + '" class="body">' + html + '</div>';
		}
		return '<div class="body"><pre>' + htmlEncode(body.calculateTextWithoutReply()) + '</pre></div>';
	},
	
	createAttachments: function(id, mail)
	{
		log("createAttachments");
		var attachments = mail.getAttachments();
		if (attachments == null)
			return '';
		
		var html = [];
		var attaches = _bind(attachments.getList());
		for (var i=0; i<attaches.length; ++i)
		{
			var attachment = attaches[i];

			var mimeType = attachment.getMimeType();
			var contentTypeParts = mimeType != null ? mimeType.match(/(.*?)\/.*/) : null;
			if (contentTypeParts == null)
				continue;

			var contentType = contentTypeParts[1];
			
			var disposition = attachment.getDisposition();
			var dispositionParts = disposition != null ? disposition.match(/.*filename=(.*)/) : null;
			if (dispositionParts == null)
				continue;
			
			var fileName = trimQuotes(dispositionParts[1]);
			
			var attachmentId = attachment.getId();

			html.push (
				'<div><a __HREF onClick="mReader.saveAttachment(__ID, \'' + attachmentId + '\')">' +
				'<img src="img/content-type/' + contentType + '.png"/>' + fileName + 
				'</a></div>'
			);
		}
		
		log("createAttachments finishing");
		
		if (html.length == 0)
			return '';
		
		var isLoaded = "";
		if (attachments.isLoaded())
			isLoaded = "loaded";
		
		var result = '<div id="_mReaderAttachments___ID" class="attachments '+isLoaded+'"><div class="disable_overlay"/><img class="progress" src="img/downloading.png"/>' + html.join('') + '</div>';
		result = result.replace(/__ID/g, id);
		
		log("createAttachments finished");
		return NOHREF(result);
	},
	
	createReplyBanner: function(i)
	{
		var result = 
			'<div class="replyBanner">' +
			'<a __HREF onClick="mReader.replyTo(__ID);">Reply</a> | ' +
			'<a __HREF onClick="mReader.replyToAll(__ID);">Reply All</a> | ' +
			'<a __HREF onClick="mReader.forward(__ID);">Forward</a> | ' +
//			'<a __HREF onClick="mReader.trash(__ID);">Delete</a> | ' +
			'<a __HREF onClick="mReader.showOriginal(__ID);">Show original</a> | ' +
			'<a __HREF onClick="mReader.enableImages(__ID);">Enable images</a> | ' +
			'<a __HREF onClick="mReader.requestEnableAttachments(__ID);">Enable embedded media &amp attachments</a>' +
			'</div>';
		
		result = result.replace(/__ID/g, i);
		return NOHREF(result);
	},
		
	renderMail: function(mail, i)
	{
		log("renderMail");
		var header = mail.getHeader();
		var content = null;
		
		if (header.hasState("DRAFT"))
		{
			content = mQuickEditor.createForMail(mReader.conversation, mail);
		}
		else
		{
			content = 
				mReader.createHeader(header) +
				mReader.createReplyBanner(i) + 
				mReader.createBody(i, mail) + 
				mReader.createAttachments(i, mail);
		}

		log("renderMail finished");
		return content;
	},
	
	renderQuickReply: function(lastMail)
	{
		log("renderQuickReply");
		
		var result = "";
		if (!lastMail.getHeader().hasState("DRAFT"))
		{			
			result = mQuickEditor.createForConversation(mReader.conversation, lastMail);
			result = '<div class="class="mQuickReply">' + result + '</div>';
		}
		
		log("renderQuickReply end");
		
		return result;
	},
	
	partialRender: function(mail)
	{
		log("mReader.partialRender");
		for (var i=0; i<mReader.numMails; ++i)
		{
			if (mReader.mails[i].equals(mail))
			{
				if (!mReader.mailsAlreadyRendered[i])
				{
					$("#_mReaderMail_"+i).html(mReader.renderMail(mail,i));
					mReader.mailsAlreadyRendered[i] = true;
					
					if (i==mReader.numMails-1)
					{
						$("#_mReaderQuickReply").html(mReader.renderQuickReply(mail));
						mQuickEditor.enable();
					}
				}
				
				break;
			}
		}
		log("mReader.partialRender end");
	},
	
	render: function()
	{
		log("mReader.render");
		mReader.conversation.markState("READ");
		mReader.mails = _bind(mReader.conversation.getItems());
		mReader.numMails = mReader.mails.length;
		mReader.mailsAlreadyRendered = [];
		
		// create the rows
		var mailRows = [];
		var lastMail = null;
		
		for (var i=0; i<mReader.numMails; ++i)
		{
			log("mail ", i);
			
			var mail = mReader.mails[i];
			var content = null;

			if (mail.isLoaded())
			{
				content = mReader.renderMail(mail, i);
				mReader.mailsAlreadyRendered.push(true);
			}
			else
			{
				content = "Loading...";
				mReader.mailsAlreadyRendered.push(false);
			}
			
			mailRows.push('<div class="mail" id="_mReaderMail_'+i+'">' + content + '</div>');
			lastMail = mail;
		}
			
		// create the footer
		var reply = "";
		if (lastMail!=null && lastMail.isLoaded())
			reply = mReader.renderQuickReply(lastMail);
		
		replyFooter = '<div id="_mReaderQuickReply">' + reply + '</div>';		
		
		// put it all together
		var spacer = '<div class="spacer"></div>';
		$('#_mReader_subject').html(mReader.conversation.getHeader().getSubject());
		$('#_mReader_items').html(mailRows.join(spacer) + replyFooter);

		// enable any editors we added
		mQuickEditor.enable();
		log("mReader.render end");
	},
	
	//------------------------------------------------
	
	showOriginal: function(id)
	{
		mMain.showOriginal(mReader.mails[id]);
	},
	
	onAttachmentsLoaded: function(mail)
	{
		if (mReader.mails == null)
			return;
		
		log("onAttachmentsLoaded");

		var found = -1;
		for (var j=0; j<mReader.mails.length; ++j)
		{
			var o = mReader.mails[j];
			if (o != null && o.equals(mail))
			{
				found = j;
				break;
			}
		}		
		
		if (found!=-1)
		{
			var mail = mReader.mails[found];
			mReader.enableAttachments(found);
		}
	},
	
	enableImages:function(mailId)
	{
		$('#_mReader_body_' + mailId).find("img").each(function(index) {
		   $(this).attr('src', $(this).attr('nosrc'));
		});
	},
	
	enableAttachments: function(mailId)
	{
		var mail = mReader.mails[mailId];
		var attachments = _bind(mail.getAttachments().getList());
		for (var i=0; i<attachments.length; ++i)
		{
			var a = attachments[i];
			
			var id = a.getId();
			log("searching for ",id);
			
			if (id.charAt(0)=='<')
				id = id.substr(1);
			if (id.charAt(id.length-1)=='>')
				id = id.substr(0,id.length-1);
			
			var images = $('#_mReaderMail_' + mailId + ' img');
			for (var j=0; j<images.length; ++j)
			{
				var image = images[j];
				if (image && image.src && (image.src.toLowerCase()==('cid:' + id.toLowerCase())))
				{
					var data = a.getDataBase64();
					var mimeType = a.getMimeType();
					var semiColon = mimeType.indexOf(';');
					if (semiColon != -1)
						mimeType = mimeType.substr(0,semiColon);
				
					log(id, " -> ", mimeType, " : ", mimeType);
					image.src = "data:"+ mimeType + ";base64," + data;
				}
			}
		}
		
		$('#_mReaderAttachments_'+ mailId).removeClass("loading");
		$('#_mReaderAttachments_'+ mailId).addClass("loaded");
	},
	
	requestEnableAttachments: function(id)
	{
		if (false && mReader.mails[id].getAttachments().isLoaded())
		{
			mReader.enableAttachments(id);
		}
		else
		{
			mMain.client.loadAttachments(mReader.mails[id]);
			$('#_mReaderAttachments_'+ id).removeClass("loaded");
			$('#_mReaderAttachments_'+ id).addClass("loading");
		}
	},
	
	saveAttachment: function(i, id)
	{
		log("saveAttachment", i, id);
		var mail = mReader.mails[i];
		var attachment = mail.getAttachments().getAttachment(id);
		if (attachment != null)
		{
			log("found attachment");
			
			if (!attachment.isLoaded())
			{
				alert('Attachment is not loaded, please load attachments.');
				return ;
			}
			
			var mimeType = attachment.getMimeType();
			var contentTypeParts = mimeType.match(/(.*?);.*/);
			var contentType = contentTypeParts[1];
			
			var disposition = attachment.getDisposition();
			var dispositionParts = disposition.match(/.*filename=(.*)/);
			var fileName = trimQuotes(dispositionParts[1]);
			
			log(mimeType, contentType, disposition, fileName);
			if (fileName != null)
			{
				log("about to save as", contentType, fileName);
				var blob = new Blob([Base64.decode(attachment.getDataBase64())], { 'type': contentType });
				saveAs(blob, fileName);		
				log("done");
			}
			else
			{
				log("couldn't parse disposition");
			}
		}
	},
	
	replyTo: function(id)
	{
		var mail = mReader.mails[id];
		var newMail = mMain.client.replyTo(mReader.conversation, mail);
		mMain.composeMail(mReader.conversation, newMail);
	},
	
	replyToAll: function(id)
	{
		var mail = mReader.mails[id];
		var newMail = mMain.client.replyToAll(mReader.conversation, mail);
		mMain.composeMail(mReader.conversation, newMail);
	},
	
	forward: function(id)
	{
		var mail = mReader.mails[id];
		var newMail = mMain.client.forward(mReader.conversation, mail);
		mMain.composeMail(mReader.conversation, newMail);
	},
	
	//------------------------------------------------------------------------------
	
	destroyConversation: function()
	{
		mMain.client.deleteConversation(mReader.conversation);
	},

	deleteConversation: function()
	{
		mReader.conversation.markState("TRASH");
		mMain.client.reindexConversation(mReader.conversation);
		mFolders.openWithName("system", "Trash");
	},
	
	undeleteConversation: function()
	{
		mReader.conversation.unmarkState("TRASH");
		mMain.client.reindexConversation(mReader.conversation);
		mFolders.openWithName("system", "All");
	},
	
	markAsSpam: function()
	{
		mReader.conversation.markState("SPAM");
		mMain.client.reindexConversation(conversation);
	},

	markAsNotSpam: function()
	{
		mReader.conversation.unmarkState("SPAM");
		mMain.client.reindexConversation(conversation);
	},
	
	onMoreAction: function(action)
	{
		if (action == 'sp')
			mReader.markAsSpam();
		else
		if (action == 'nsp')
			mReader.markAsNotSpam();
	},
	
	//------------------------------------------------------------------------

	initialize: function() 
	{
		
	}
};

/***** mReader.js *****/

/***** mQuickEditor.js *****/

mQuickEditor = 
{
	editors: {},
	nextId: 0,

	begin: function()
	{
		mQuickEditor.nextEditorId = 0;
	},
	
	end: function()
	{
		var editors = mQuickEditor.editors;
		for (var id in editors)
		{
			var editor = editors[id];
			if (editor.instance)
				mEditor.close(editor.instance);
		}
		
		mQuickEditor.editors = [];
	},
		
	createReplyHeaderForConversation: function(c, m)
	{
		return '<div class="header">' + mReader.createIdentitiesDiv("To", m.calculateReplyTo()) + '</div>';
	},
	
	createReplyHeaderForMail: function(mail)
	{
		return mReader.createHeader(mail.getHeader());
	},
	
	createHTML: function(id, replyHeader, displayIncludeQuotedTextOption)
	{
		var editorFooter = 
			'<div class="footer">' +
				'<span><button id="mQuickEditorSend___ID" onClick="mQuickEditor.send(\'__ID\');" class="btn">Send</button></span>' +
				'<span><button id="mQuickEditorSave___ID" onClick="mQuickEditor.save(\'__ID\');" class="btn">Save Draft</button></span>' +
				'<span><button id="mQuickEditorDiscard___ID" onClick="mQuickEditor.discard(\'__ID\');" class="btn">Discard</button></span>' +
				(displayIncludeQuotedTextOption ?
					'<span id="mQuickEditorIncludeQuote___ID"><input id="mQuickEditorIncludeQuoteCheckbox___ID" type="checkbox" checked="true"><span class="includeText">Include quoted text with reply</span></input></span>' :
					''
				) +
			'</div>';
		
		var editor = 
			'<div id="mQuickEditor___ID" class="mQuickEditor">' +
				'<div class="more-options"><button class="btn compose-btn" onClick="mQuickEditor.moreOptions(\'__ID\');">Full compose</button></div>' +
				'<div class="quick-label">Quick Compose</div>' +
				replyHeader +
				'<div class="body"><textarea id="__ID">' + '</textarea></div>' +
				editorFooter + 
			'</div>';
		
		editor = editor.replace(/__ID/g, id);
		
		return editor;
	},
	
	createForMail: function(conversation, mail)
	{
		var id = "QE_" + mQuickEditor.nextId++;
		log("createForMail: ", id);
		mQuickEditor.editors[id] = { isEnabled:false, isNew:false, conversation:conversation, mail:mail, instance:null };
		
		return mQuickEditor.createHTML(id, mQuickEditor.createReplyHeaderForMail(mail), false);
	},
	
	createForConversation: function(conversation, mail)
	{
		var id = "QE_" + mQuickEditor.nextId++;
		log("createForConversation: ", id);
		mQuickEditor.editors[id] = { isEnabled:false, isNew:true, conversation:conversation, mail:mail, instance:null };
		
		return mQuickEditor.createHTML(id, mQuickEditor.createReplyHeaderForConversation(conversation,mail), true);
	},
	
	discard: function(id)
	{
		log("mQuickEditor.discard ", id);
		var editor = mQuickEditor.editors[id];
		var numItems = editor.conversation.getNumItems();
		mMain.client.deleteMail(editor.conversation, editor.mail);
		log("mQuickEditor.discard end ", id);
		
		// was this the last mail?
		if (numItems == 1)
			mMain.openFolder("All");
	},
	
	enable: function()
	{
		log ("mQuickEditor.enable");
		
		var editors = mQuickEditor.editors;
		for (var id in editors)
		{
			var editor = editors[id];
			if (editor.instance)
				continue;
			
			log("mQuickEditor.enable no instance for ", id);
			
			var content = null;
			if (editor.isNew)
			{
				$('#mQuickEditorDiscard_' + id).prop("disabled", true);
			}
			else
			{
				log('enable !isNew');
				var body = editor.mail.getBody();
				if (body.hasHTML())
					content = body.getHTML();
				else
				{
					content = ""+ body.getText();
					content = content.replace(/\n/g, '<br />');
				}
			}
			log('enable: ', content);
			
			editor.instance = mEditor.open(id, content);
		}
		
		log ("mQuickEditor.enable end");
	},
	
	save: function(id)
	{
		var editors = mQuickEditor.editors;
		var editor = editors[id];
		
		var content = mEditor.getContent(editor.instance);
		console.log("save:\ntext:", content.text," \nhtml:",content.html);

		
		if (editor.isNew)
		{
			var includeQuoted = $('#mQuickEditorIncludeQuoteCheckbox_' + id).prop("checked");			
			if (includeQuoted)
			{
				var replyBody = mMain.client.calculateReplyBody(editor.mail);
				content.text += "\r\n" + replyBody.getText();
				content.html += replyBody.getHTML();
				
				mEditor.setContent(editor.instance, content.html);
			}

			editor.mail = 
				_bind(
					mMain.client.replyTo(
						editor.conversation, 
						editor.mail
					)
				);
			editor.isNew = false;
			$('#mQuickEditorIncludeQuote_' + id).hide();
		}
		
		console.log("setBody:\ntext:", content.text," \nhtml:",content.html);
		editor.mail.setBody(content.text, content.html);
		mMain.client.saveMail(editor.conversation, editor.mail);
		$('#mQuickEditorDiscard_' + id).removeAttr('disabled');
	},
	
	moreOptions: function(id)
	{
		var editors = mQuickEditor.editors;
		var editor = editors[id];

		mQuickEditor.save(id);
		mMain.composeMail(editor.conversation,editor.mail);
	},
	
	send: function(id)
	{
		log("sending: ", id);
		
		mQuickEditor.save(id);
		
		var editors = mQuickEditor.editors;
		var editor = editors[id];
		
		log("calling sendMail");
		mMain.client.sendMail(editor.conversation, editor.mail);
		mMain.onSend();
		
		mReader.refresh();
	}
};

/***** mQuickEditor.js *****/

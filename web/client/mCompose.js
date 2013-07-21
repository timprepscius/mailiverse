/***** mCompose.js *****/

mCompose = {
		
	availableTags : [],
	conversation: null,
	mail: null,
	editor: null,

	populateAvailableTags: function()
	{
		mCompose.availableTags = [];
		
		var addresses = mMain.client.getAddressList();
		var addressesLength = addresses.length;
		
		for (var i=0; i<addressesLength; ++i)
			mCompose.availableTags.push(addresses[i].toString());
	},
	
	open: function() 
	{
		mCompose.conversation = null;
		mCompose.mail = null;
		
		$('#_mCompose_content_header').show();
		$('#_mCompose').show();
		
		$('#_mCompose_to').val('');
		$('#_mCompose_cc').val('');
		$('#_mCompose_bcc').val('');
		$('#_mCompose_subject').val('');
		
		$('#_mComposeEditor_discard').attr('disabled', true);
		
		mCompose.editor = mEditor.open("_mCompose_body",mMain.client.getSignatureHTML());
		mCompose.populateAvailableTags();
	},

	close: function() 
	{
		$('#_mCompose_content_header').hide();
		$('#_mCompose').hide();
		
		mEditor.close("_mCompose_body");
	},
	
	addressString: function(addresses)
	{
		var java = _bind(addresses);
		var js = [];
		
		var javaLength = java.length;
		for (var i=0; i<javaLength; ++i)
			js.push('' + java[i]);
		
		return js.join(', ');
	},
	
	openMail: function(conversation, mail)
	{
		mCompose.conversation = conversation;
		mCompose.mail = mail;
		
		$('#_mCompose_content_header').show();
		$('#_mCompose').show();
		
		var header = mail.getHeader();
		var body = mail.getBody();
		var recipients = header.getRecipients();
		
		$('#_mCompose_to').val(mCompose.addressString(recipients.getTo()));
		$('#_mCompose_cc').val(mCompose.addressString(recipients.getCc()));
		$('#_mCompose_bcc').val(mCompose.addressString(recipients.getBcc()));
		$('#_mCompose_subject').val(header.getSubject());

		$('#_mComposeEditor_discard').removeAttr('disabled');

		mCompose.editor = mEditor.open("_mCompose_body", body.getHTML());
		mCompose.populateAvailableTags();
	},
	
	discard: function()
	{
		mMain.client.deleteMail(mCompose.conversation, mCompose.mail);
		mFolders.openWithName("system", "All");
	},
	
	save: function()
	{
		var content = mEditor.getContent(mCompose.editor);
		log("save:", content.text, " \n\n ", content.html);

		if (mCompose.mail == null)
		{
			var conversationMailPair = mMain.client.newMail();
			mCompose.conversation = conversationMailPair[0];
			mCompose.mail = _bind(conversationMailPair[1]);
		}
		
		var header = mCompose.mail.getHeader();
		var recipients = header.getRecipients();
		
		header.setSubject( $('#_mCompose_subject').val() );
		recipients.setTo( mMain.client.buildAddressList ( $('#_mCompose_to').val() ) );
		recipients.setCc( mMain.client.buildAddressList( $('#_mCompose_cc').val() ) );
		recipients.setBcc( mMain.client.buildAddressList ( $('#_mCompose_bcc').val() ) );
		mCompose.mail.setBody(content.text, content.html);
		mMain.client.saveMail(mCompose.conversation, mCompose.mail);
		$('#_mComposeEditorDiscard').removeAttr('disabled');
	},
	
	send: function()
	{
		log("mCompose.save");
		
		mCompose.save();
		mMain.client.sendMail(mCompose.conversation, mCompose.mail);
		mMain.onSend();

		mFolders.openWithName("system", "Sent");
	},
		
	initialize: function() 
	{
 		function split( val ) {
 			return val.split( /,\s*/ );
 		}
		function extractLast( term ) {
			return split( term ).pop();
		}
		
		var autoCompleteDefinition = {
			minLength: 1,
			source: function( request, response ) {
				// delegate back to autocomplete, but extract the last term
				response( $.ui.autocomplete.filter(mCompose.availableTags, extractLast( request.term ) ) );
			},
			focus: function() {
				// prevent value inserted on focus
				return false;
			},
			select: function( event, ui ) {
				var terms = split( this.value );
				// remove the current input
				terms.pop();
				// add the selected item
				terms.push( ui.item.value );
				// add placeholder to get the comma-and-space at the end
				terms.push( "" );
				this.value = terms.join( ", " );
				return false;
			}
		};

 		$( "#_mCompose_to" )
 			// don't navigate away from the field on tab when selecting an item
 			.bind( "keydown", function( event ) {
 				if ( event.keyCode === $.ui.keyCode.TAB &&
 						$( this ).data( "autocomplete" ).menu.active ) {
 					event.preventDefault();
 				}
 			})
 			.autocomplete(autoCompleteDefinition);		

 		$( "#_mCompose_cc" )
			// don't navigate away from the field on tab when selecting an item
			.bind( "keydown", function( event ) {
				if ( event.keyCode === $.ui.keyCode.TAB &&
						$( this ).data( "autocomplete" ).menu.active ) {
					event.preventDefault();
				}
			})
			.autocomplete(autoCompleteDefinition);		
	
 		$( "#_mCompose_bcc" )
			// don't navigate away from the field on tab when selecting an item
			.bind( "keydown", function( event ) {
				if ( event.keyCode === $.ui.keyCode.TAB &&
						$( this ).data( "autocomplete" ).menu.active ) {
					event.preventDefault();
				}
			})
			.autocomplete(autoCompleteDefinition);		
	}
} ;

/***** mCompose.js *****/

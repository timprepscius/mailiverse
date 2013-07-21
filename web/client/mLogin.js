/***** mLogin.js *****/

mLogin = {
		
	enableButton: function() {
		$('#_mLogin_button').removeAttr("disabled");
		$('#_mLogin_loading').html("Service loaded.");
		$('#_mLogin_loading').attr('class', 'mLogin_loading_ok');
	},

	disableForm: function() {
		$('#_mLogin_name').prop('disabled', true);
		$('#_mLogin_password').prop('disabled', true);
		$('#_mLogin_button').prop("disabled", true);
	},
	
	enableForm: function() {
		$('#_mLogin_name').removeAttr('disabled');
		$('#_mLogin_password').removeAttr('disabled');
		$('#_mLogin_button').removeAttr("disabled");
	},
	
	loginStart: function() {
		// why is this an array index?
		$('#_mLogin_status').html("Authenticating...");
		mLogin.disableForm();
		
		var name = $('#_mLogin_name').val();
		var password = $('#_mLogin_password').val();
		$('#_mLogin_password').val('');
		
		onSoon(function() { 
			mLogin.loginAuthenticate(name, password);
		});
	},
	
	loginAuthenticate: function(name, password) {
		mMain.authenticate(name + Constants.ATHOST, password);		
	},
	
	loginFailed: function() {
		$('#_mLogin_status').html("Authentication failed.");
		mLogin.enableForm();
	},
	
	loginSucceeded: function() {
		$('#_mLogin').hide();
		
		$('#_mLogin_backdrop').hide();
		$('#_mHeader').show();
		$('#_mMain').show();
		$('#_mMain_footer').show();
		$('body').css('background-color', 'white');
	},
	
	loginStep: function(step) {
		var html = $('#_mLogin_status').html();
		$('#_mLogin_status').html(html + step);
	},
	
	initialize: function() {
		$('#_mLogin_button').on('click', function(e) {mLogin.loginStart();});
		mLogin.enableForm();
		$('#_mLogin_name').val('');		
	}
};

/***** mLogin.js *****/


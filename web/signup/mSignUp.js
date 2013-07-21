/***** mSignup ******/

mSignUp = {

	executeText: "",
		
	service: null,
	validate: { name: false, password: false, password_check:false, storageAuthorized:true, captcha:false, tos:false },
	storage : "mailiverse",
	name : null,
	password : null,
		
	userToken: { key: null, secret : null },
	dropboxAuthorizationCode: null,
	captchaToken:null,
	
	initialize: function()
	{
	},
	
	onStorageChange: function()
	{
		var possible = [ 'mailiverse', 'dropbox' ];
		var value = $('input[name=storage]:checked').val();
		$('#storage_' + value).show();

		for (var i=0; i<possible.length; ++i)
		{
			if (possible[i] != value)
				$('#storage_' + possible[i]).hide();
		}
		
		if (value == 'mailiverse')
		{
			mSignUp.validate['storageAuthorized'] = true;
		}
		else
		{
			mSignUp.validate['storageAuthorized'] = false;
			mSignUp.manualTestDropboxAlreadyAuthorized();
		}
		mSignUp.storage = value;
		mSignUp.testReady();
		
	},
	
	onServiceLoaded: function()
	{
		log("onServiceLoaded");
		mSignUp.service = MService.getSignUp();
		mSignUp.authorizeDropbox_getToken();
	},
	
	onNameChange: function()
	{
		log("onNameChange");
		
		var name = $('#_mSignUp_name').val();
		if (mSignUp.name == name)
			return;
		
		mSignUp.name = name;
		mSignUp.validate['name'] = false;

		if (!isAlphaNumeric(name))
		{
			$('#_mSignUp_name_ctl').removeClass('success');		
			$('#_mSignUp_name_ctl').addClass('error');
			$('#_mSignUp_name_help').text("Not alpha numeric");
		}
		else
		{
			$('#_mSignUp_name_ctl').removeClass('error');
			$('#_mSignUp_name_ctl').removeClass('success');		
			$('#_mSignUp_name_help').text("Checking...");
			
			keyedTimer("onNameChange", function() { mSignUp.checkName(name); }, 500);
		}
		mSignUp.testReady();
	},
	
	onPasswordChange: function()
	{
		log("onPasswordChange");
		
		mSignUp.checkPassword();
	},
	
	checkPassword: function()
	{
		log("checkPassword");
		
		var password = $('#_mSignUp_password').val();
		mSignUp.password = password;
		
		var strength = getPasswordStrength(password)
		if (strength.score < 2)
		{
			$('#_mSignUp_password_ctl').removeClass("success");
			$('#_mSignUp_password_ctl').addClass("error");
			$('#_mSignUp_password_help').text(strength.text);
			mSignUp.validate['password'] = false;
		}
		else
		{
			$('#_mSignUp_password_ctl').removeClass("error");
			$('#_mSignUp_password_ctl').addClass("success");
			$('#_mSignUp_password_help').text(strength.text);
			mSignUp.validate['password'] = true;
		}
		
		var password_check = $('#_mSignUp_password_check').val();
		
		if (password != password_check)
		{
			$('#_mSignUp_password_check_ctl').removeClass("success");
			$('#_mSignUp_password_check_ctl').addClass("error");
			$('#_mSignUp_password_check_help').text("Password does not match");
			
			mSignUp.validate['password_check'] = false;
		}
		else
		{
			$('#_mSignUp_password_check_ctl').removeClass("error");
			$('#_mSignUp_password_check_ctl').addClass("success");
			$('#_mSignUp_password_check_help').text("Ok");
			mSignUp.validate['password_check'] = true;
		}
		
		mSignUp.testReady();
	},

	checkName: function(name)
	{
		log("checkName");
		mSignUp.service.test(name + Constants.ATHOST, { name: name, invoke: function(result) { mSignUp.onCheckComplete(this.name, result); }});
	},
	
	onCheckComplete: function(name, result)
	{
		if (name != mSignUp.name)
			return;
		
		if (result.hasException())
		{
			$('#_mSignUp_name_ctl').addClass('error');
			$('#_mSignUp_name_help').text(result.getException().getMessage());
			mSignUp.validate['name'] = false;
		}
		else
		{
			$('#_mSignUp_name_ctl').addClass('success');
			$('#_mSignUp_name_help').text("Ok");
			mSignUp.validate['name'] = true;
		}
		
		mSignUp.testReady();
	},
	
	submitCaptcha: function()
	{
		var challenge = Recaptcha.get_challenge();
		var response = Recaptcha.get_response();;
		
		var url = Constants.TOMCAT_SERVER + "CaptchaResponse";
		var params = "recaptcha_challenge_field=" + challenge + "&recaptcha_response_field=" + response;
		$.getJSON(url + "?" + params, function(json) { mSignUp.onCaptchaResponse(json.succeeded, json.token); })
			.error(function() { $('#_mSignUp_captcha_help').text("There was a system error, please try again."); });
	},
	
	onCaptchaResponse: function(succeeded, token)
	{
		log("onCaptchaResponse");
		
		mSignUp.validate['captcha'] = succeeded;

		if (succeeded)
		{
			var height = $('#_mSignUp_captcha').height();
			$('#_mSignUp_captcha').text("Captcha successfully completed.");
			$('#_mSignUp_captcha').addClass('success');
			$('#_mSignUp_captcha').height(height);

			mSignUp.captchaToken = token;
		}
		else
		{
			$('#_mSignUp_captcha_help').text("Failed, please try again.");
			Recaptcha.reload();
		}
		mSignUp.testReady();

	},
	
	authorizeDropbox_getToken: function()
	{
		mSignUp.service.requestAuthorizationToken({ 
			invoke: function(r) 
			{
				if (r.hasException())
				{
					$('#_mSignUp_dropbox_ctl').text("Failed to get authorization token.  Dropbox may be undergoing maintainence. Please try again later.");
					log(r.getException());
				}
				else
				{
					var p = JSON.parse(r.getObject());
					mSignUp.userToken = {
						key : p.key,
						secret: p.secret
					};
					
					mSignUp.authorizeDropbox_enableButton();
				}
			}
		});
	},
	
	authorizeDropbox_enableButton : function()
	{
		var url = "https://www.dropbox.com/1/oauth/authorize" + 
			"?oauth_token=" + mSignUp.userToken.key +
			"&oauth_callback=" + escape("https://" + Constants.WEB_HOST + "/DropboxAuthorized.html") + 
			"&locale=en";
		
		$('#_mSignUp_dropbox').prop("href", url);
		$('#_mSignUp_dropbox').removeAttr("disabled");
	},
	
	authorizeDropbox_disableButton : function()
	{
		$('#_mSignUp_dropbox').prop("href", "javascript:void");
		$('#_mSignUp_dropbox').attr("disabled", true);
	},

	manualTestDropboxAlreadyAuthorized : function()
	{
		var userAuthorizationCode = $('#_mSignUp_dropbox_authorization').val();
		mSignUp.validate["storageAuthorized"] = userAuthorizationCode == mSignUp.userToken.key;
		mSignUp.testReady();
	},
	
	onDropboxAuthorization: function()
	{
		var userAuthorizationCode = $('#_mSignUp_dropbox_authorization').val();
		if (userAuthorizationCode == mSignUp.userToken.key)
		{
			mSignUp.validate["storageAuthorized"] = true;
			$('#_mSignUp_dropbox_ctl').removeClass("error");
			$('#_mSignUp_dropbox_ctl').addClass("success");
			$('#_mSignUp_dropbox_authorization_help').text("Ok");
			mSignUp.authorizeDropbox_disableButton();
		}
		else
		{
			mSignUp.validate["storageAuthorized"] = false;
			$('#_mSignUp_dropbox_ctl').removeClass("success");
			$('#_mSignUp_dropbox_ctl').addClass("error");
			$('#_mSignUp_dropbox_authorization_help').text("Invalid");
			mSignUp.authorizeDropbox_enableButton();
		}
		
		mSignUp.testReady();
	},
	
	
	onTosCheck: function()
	{
		mSignUp.validate["tos"]= $('#_mSignUp_tosCheck').is(':checked');
		
		mSignUp.testReady();
	},
	
	testReady : function()
	{
		log("testing");
		
		for (var i in mSignUp.validate)
		{
			if (mSignUp.validate.hasOwnProperty(i))
			{
				if (mSignUp.validate[i] == false)
				{
					$('#_mSignUp_submit').attr("disabled", true);
					log("failed");
					return;
				}
			}
		}
		$('#_mSignUp_submit').removeAttr("disabled");
		log("succeeded");
	},
	
	signUp : function()
	{
		$('#_mSignUpExecute').show();
		
		var storageInfo = {};
		if (mSignUp.storage == "mailiverse")
			storageInfo = { region: $('#storage_mailiverse_region').val() };
		
		var signUpDelegate = { 
				progress: function(x) { 
					mSignUp.executeText += "\n" + x;
					$('#_mSignUpExecute_label').html(htmlForTextWithEmbeddedNewlines(mSignUp.executeText)); 
				}, 
				invoke: function(r) {
					mSignUp.onSignUp(r);
				}
			};

		signUpDelegate.progress("Calculating RSA Key (Chrome: 15 seconds, Firefox: 2 min, Safari: 4 min)...");
		
		mSignUp.service.signUp(
			mSignUp.storage,
			JSON.stringify(storageInfo),
			mSignUp.name + Constants.ATHOST,
			mSignUp.password,
			mSignUp.captchaToken,
			mSignUp.userToken.key,
			mSignUp.userToken.secret,
			signUpDelegate
		);
	},
	
	onSignUp: function(r)
	{
		if (r.hasException())
		{
			$('#_mSignUpExecute_failure').text("Failed: " + r.getException().getMessage());
			$('#_mSignUpExecute_failure').show();
		}
		else
		{
			$('#_mSignUpExecute_success').show();
			$('#_mSignUpExecute_navigate_away').removeAttr('disabled');
		}
	},
	
	exitPage: function()
	{
		$('#_mSignUp_name').val('');
		$('#_mSignUp_password').val('');
		$('#_mSignUp_password_check').val('');
		$('#_mSignUp_dropbox_authorization').val('');
		
		onNextTick(function() { window.location.href='index.html';});
	}
	
};

/***** mSignup ******/

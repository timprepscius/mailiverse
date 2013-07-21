/***** mDelete ******/

mDelete = {

	executeText: "",
	validate : {name: false, password: false},	
	
	name : null,
	password : null,
	
	initialize: function()
	{
	},
		
	onServiceLoaded: function()
	{
		log("onServiceLoaded");
		mDelete.service = MService.getDelete();
	},
	
	onNameChange: function()
	{
		log("onNameChange");
		
		var name = $('#_mDelete_name').val();
		if (mDelete.name == name)
			return;

		mDelete.name = name;
		mDelete.validate['name'] = 
			mDelete.name.length > 0;

		mDelete.testReady();
	},
	
	onPasswordChange: function()
	{
		log("onPasswordChange");
		
		mDelete.checkPassword();
	},
	
	checkPassword: function()
	{
		log("checkPassword");
		
		var password = $('#_mDelete_password').val();
		mDelete.password = password;
		
		mDelete.validate['password'] =
			(mDelete.password.length > 0);
		
		mDelete.testReady();
	},

	testReady : function()
	{
		log("testing");
		
		for (var i in mDelete.validate)
		{
			if (mDelete.validate[i] == false)
			{
				$('#_mDelete_submit').attr("disabled", true);
				log("failed");
				return;
			}
		}
		$('#_mDelete_submit').removeAttr("disabled");
		log("succeeded");
	},
	
	doDelete : function()
	{
		$('#_mDeleteExecute').show();
		
		var deleteDelegate = { 
				progress: function(x) { 
					mDelete.executeText += "\n" + x;
					$('#_mDeleteExecute_label').html(htmlForTextWithEmbeddedNewlines(mDelete.executeText)); 
				}, 
				invoke: function(r) {
					mDelete.onDelete(r);
				}
			};

		deleteDelegate.progress("Starting...");
		
		mDelete.service.doDelete(
			mDelete.name + Constants.ATHOST,
			mDelete.password,
			deleteDelegate
		);
	},
	
	onDelete: function(r)
	{
		if (r.hasException())
		{
			$('#_mDeleteExecute_failure').text("Failed: " + r.getException().getMessage());
			$('#_mDeleteExecute_failure').show();
		}
		else
		{
			$('#_mDeleteExecute_success').show();
			$('#_mDeleteExecute_navigate_away').removeAttr('disabled');
		}
	},	
};

/***** mDelete ******/

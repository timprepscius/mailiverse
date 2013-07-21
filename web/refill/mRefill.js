
mRefill = {

	service: null,
	validate: { name:false },
	payment : null,
	name : null,
	amount : null,
	executeText : "Starting...",
		
	initialize: function()
	{
		mRefill.onPaymentChange();
	},
	
	onServiceLoaded: function()
	{
		mRefill.service = MService.getRefill();
	},
	
	onPaymentChange: function()
	{
		var possible = [ 'stripe', 'bitcoin' ];
		var value = $('input[name=payment]:checked').val();
		$('#payment_' + value).show();

		for (var i=0; i<possible.length; ++i)
		{
			if (possible[i] != value)
				$('#payment_' + possible[i]).hide();
		}
		
		if (value != 'stripe')
		{
			delete mRefill.validate.stripe_card_number;
			delete mRefill.validate.stripe_card_cvc;
			delete mRefill.validate.stripe_card_expiry;
		}
		else
		if (value != 'bitcoin')
		{
			delete mRefill.validate.bitcoin_verified;
		}
		
		if (value == 'stripe')
		{
			mRefill.onStripeAmountChange();
			mRefill.onStripeCardNumberChange();
			mRefill.onStripeCardCVCChange();
			mRefill.onStripeCardExpiryChange();
		}
		else
		if (value == 'bitcoin')
		{
			mRefill.onBitPayAmountChange();
			mRefill.validate.bitcoin_verified = false;
		}
		
		mRefill.payment = value;		
		mRefill.testReady();
	},

	onBitPayAmountChange : function()
	{
		$('#bitpay_embedded_dialog').html('');
		mRefill.amount = $('input[name=amount_bitpay]:checked').val();
		
		$('#_mRefill_bitpay_request_invoice').removeAttr("disabled");
	},
	
	onBitPayRequestInvoice : function()
	{
		var url = 
			Constants.TOMCAT_SERVER + "BitPayCreateInvoice" + 
			"?email=" + mRefill.name + Constants.ATHOST +
			"&price=" + mRefill.amount;
		
		$.ajax(url, {
		    success: function(data) { mRefill.onBitPayRequestInvoiceSuccess(JSON.parse(data)); },
			failure: function(data) { mRefill.onBitPayRequestInvoiceFailure(JSON.parse(data)); }
		   }
		);
	},
	
	onBitPayRequestInvoiceSuccess : function(data)
	{
		$('#bitpay_embedded_dialog').html('<iframe class="bitpay-iframe" src="' + data.url + "&view=iframe" + '" frameBorder="0" />');
	},
	
	onBitPayRequestInvoiceFailure : function(data)
	{
		$('#bitpay_embedded_dialog').text('Request failed, please try again later.');
	},
	
	validateField : function(x,v,t) 
	{
		if (v)
		{
			$('#_mRefill_' + x + "_ctl").removeClass('error');
			$('#_mRefill_' + x + "_ctl").addClass('success');
			
			if (t)
				$('#_mRefill_' + x + "_help").text("Ok");
		}
		else
		{
			$('#_mRefill_' + x + "_ctl").removeClass('success');
			$('#_mRefill_' + x + "_ctl").addClass('error');
			
			if (t)
				$('#_mRefill_' + x + "_help").text(t);
		}
		
		mRefill.validate[x] = v;
	},
	
	onNameChange: function()
	{
		log("onNameChange");
		
		var name = $('#_mRefill_name').val();
		if (mRefill.name == name)
			return;
		
		mRefill.name = name;
		mRefill.validate['name'] = false;

		if (!isAlphaNumeric(name))
		{
			$('#_mRefill_name_ctl').removeClass('success');		
			$('#_mRefill_name_ctl').addClass('error');
			$('#_mRefill_name_help').text("Not alpha numeric");
		}
		else
		{
			$('#_mRefill_name_ctl').removeClass('error');
			$('#_mRefill_name_ctl').removeClass('success');		
			$('#_mRefill_name_help').text("Checking...");
			
			keyedTimer("onNameChange", function() { mRefill.checkName(name); }, 500);
		}
		mRefill.testReady();
	},
	
	checkName: function(name)
	{
		log("checkName");
		mRefill.service.test(name + Constants.ATHOST, { name: name, invoke: function(result) { mRefill.onCheckComplete(this.name, result); }});
	},
		
	onCheckComplete: function(name, result)
	{
		if (name != mRefill.name)
			return;
		
		if (result.hasException())
		{
			if (result.getException().getMessage().indexOf('exists')!=-1)
			{
				$('#_mRefill_name_ctl').addClass('success');
				$('#_mRefill_name_help').text("Ok");
				mRefill.validate['name'] = true;
			}
			else
			{
				$('#_mRefill_name_ctl').addClass('error');
				$('#_mRefill_name_help').text(result.getException().getMessage());
				mRefill.validate['name'] = false;
			}
		}
		else
		{
			$('#_mRefill_name_ctl').addClass('error');
			$('#_mRefill_name_help').text('User does not exist');
			mRefill.validate['name'] = false;
		}
		
		mRefill.testReady();
	},
	
	
	onStripeAmountChange: function()
	{
		mRefill.amount = $('input[name=amount_bitpay]:checked').val();
	},
	
	onStripeCardNumberChange : function()
	{
		mRefill.validateField('stripe_card_number',Stripe.validateCardNumber($('#stripe_card_number').val()));
		mRefill.testReady();
	},

	onStripeCardCVCChange : function ()
	{
		mRefill.validateField('stripe_card_cvc',Stripe.validateCVC($('#stripe_card_cvc').val()));
		mRefill.testReady();
	},
	
	onStripeCardExpiryChange : function ()
	{
		mRefill.validateField('stripe_card_expiry',
			Stripe.validateExpiry($('#stripe_card_expiry_month').val(), $('#stripe_card_expiry_year').val()));
		mRefill.testReady();
	},
	
	onStripeResponseHandler : function (data)
	{											
		alert(data);	
	},

	testReady : function()
	{
		log("testing");
		
		for (var i in mRefill.validate)
		{
			log(i);
			if (mRefill.validate[i] == false)
			{
				$('#_mRefill_submit').attr("disabled", true);
				log("failed");
				return;
			}
		}
		$('#_mRefill_submit').removeAttr("disabled");
		log("succeeded");
	},
	
	makePayment : function()
	{
		$('#_mRefillExecute').show();
		
		var paymentDetails = 
			mRefill.payment == 'stripe' ?
				{ 
					number : $('#stripe_card_number').val(), 
					cvc : $('#stripe_card_cvc').val(), 
					month : $('#stripe_card_expiry_month').val(),
					year : $('#stripe_card_expiry_year').val()
				} :
				{
					token : $('#bitcoin_token').val()
				};
			
		
		mRefill.service.makePayment(
			mRefill.name + Constants.ATHOST,
			mRefill.amount,
			mRefill.payment,
			paymentDetails,
			{ 
				progress: function(x) { 
					mRefill.executeText += "\n" + x;
					$('#_mRefillExecute_label').html(htmlForTextWithEmbeddedNewlines(mRefill.executeText)); 
				}, 
				invoke: function(r) {
					mRefill.onPaid(r);
				}
			}
		);
	},
	
	onPaid: function(r)
	{
		if (r.hasException())
		{
			mRefill.executeText += "\n\n" + "Failed: " + r.getException().getMessage(); 
			$('#_mRefillExecute_label').html(htmlForTextWithEmbeddedNewlines(mRefill.executeText)); 
		}
		else
		{
			$('#_mRefillExecute_success').show();
		}
	},
	
	exitPage: function()
	{
		$('#_mRefill_name').val('');
		$('#_mRefill_password').val('');
		$('#_mRefill_password_check').val('');
		$('#_mRefill_dropbox_authorization').val('');
		
		onNextTick(function() { window.location.href='index.html';});
	}
	
};

/***** mRefill ******/

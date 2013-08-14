/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package app.service;
import mail.auth.MailServerAuthenticatorNoThread;
import mail.client.EventPropagator;

import org.json.JSONObject;
import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;

import core.callback.Callback;
import core.callback.CallbackWithVariables;
import core.constants.ConstantsClient;
import core.io.IoChain;
import core.util.HttpDelegate;
import core.util.LogNull;
import core.util.LogOut;

@Export()
public class JSRefill implements Exportable
{
	static LogNull log = new LogNull(JSSignUp.class);
	
	Main main;
	
	JSRefill (Main main)
	{
		this.main = main;
	}
	
    static class RefillInfo {
    	public static enum Storage { Mailiverse, Dropbox };
    	
    	String email;
    	JSResult<Boolean> callback;
    	
    	String paymentMethod;
    	
    	String stripeCardNumber, stripeCardExpMonth, stripeCardExpYear, stripeCardCVC;
    	String stripeTransactionID;
    	
    	String bitpayToken;
    	String amount;
    	
    	RefillInfo (
    		String email, String amount,
			JSResult<Boolean> callback
		)
		{
    		this.email = email;
    		this.callback = callback;
    		this.amount = amount;
		}	
    	
    	void initializeStripe (
	    	String stripeCardNumber, String stripeCardCVC, String stripeCardExpMonth, String stripeCardExpYear
		)
    	{
    		this.paymentMethod = "stripe";
    		this.stripeCardNumber = stripeCardNumber;
    		this.stripeCardCVC = stripeCardCVC;
    		this.stripeCardExpMonth = stripeCardExpMonth;
    		this.stripeCardExpYear = stripeCardExpYear;
		}
    	
    	void initializeBitPay (String bitpayToken)
    	{
    		this.paymentMethod = "bitpay";
    		this.bitpayToken = bitpayToken;
    	}
    }
    	
    
    public void makePayment (
    	String name,
    	String amount,
    	String paymentMethod,
    	Object paymentDetails,
    	Object callback
    )
    {
    	
    	log.debug("refill", name, amount, paymentMethod);
    	
    	RefillInfo info = new RefillInfo(
    		name, amount,
    		new JSResult<Boolean>(callback)
    	);
    	
    	if (paymentMethod.equals("stripe"))
    		info.initializeStripe(
    			JSInvoker.getMember(paymentDetails, "number").toString(), 
    			JSInvoker.getMember(paymentDetails, "cvc").toString(),
    			JSInvoker.getMember(paymentDetails, "month").toString(),
    			JSInvoker.getMember(paymentDetails, "year").toString()
    		);
    	else
    	if (paymentMethod.equals("bitpay"))
    		info.initializeBitPay(
    			JSInvoker.getMember(paymentDetails, "token").toString()
    		);
    	
    	main.eventPropagator.signal(
    		EventPropagator.INVOKE, 
    		new Callback() {
    			public void invoke(Object... arguments)
    			{
    				refill_step_requestPaymentStripeStep1((RefillInfo)arguments[0]);
    			}
    		},
    		info
    	);
    }
    
	public void test (String user, Object callback) throws Exception
    {
    	log.debug("test");
		JSResult<Object> result = new JSResult<Object>(callback);

		String url = ConstantsClient.MAIL_SERVER_WEBSOCKET;

		 MailServerAuthenticatorNoThread.testCreate_(
			user,
			new JSStreamSessionWebSocket(url, main.delegate)
		).addCallback(result).invoke();
    }
    
    
    protected void refill_step_requestPayment(RefillInfo info)
    {
    	refill_step_requestPaymentStripeStep1(info);
    }
    
    protected void refill_step_requestPaymentStripeStep1(RefillInfo info)
    {
    	final String stripePublishableKey = "YOUR_STRIPE_TOKEN";
    	    	
    	log.debug("refill_step_requestPaymentStripeStep1");
		try
		{
			String url = 
					"https://api.stripe.com/v1/tokens?" + 
						"card[number]=" + info.stripeCardNumber +
						"&card[exp_month]=" + info.stripeCardExpMonth + 
						"&card[exp_year]=" + info.stripeCardExpYear +
						"&card[cvc]=" +  info.stripeCardCVC + 
						"&key=" + stripePublishableKey + 
						"&_method=POST";
					
			JSHttpDelegate http = new JSHttpDelegate(main.delegate);
			http.execute(
				HttpDelegate.GET, url, null, 
				false, false, null, 
				
				new CallbackWithVariables(info) {
				public void invoke(Object... arguments)
				{
			    	log.debug("signUp_step_requestPaymentStripeStep1 callback");
			    	RefillInfo info = V(0);
					try
					{
						if (arguments[0] instanceof Exception)
							throw (Exception)arguments[0];
						
						String response = (String)arguments[0];
						JSONObject o = new JSONObject(response);
						String stripeTransactionID = o.getString("id");
						info.stripeTransactionID = stripeTransactionID;
						
						JSInvoker.invoke(info.callback.getCallback(), "progress", new Object[] { "Requested CreditCard Stripe Token." });
						refill_step_requestPaymentStripeStep2(info);
					}
					catch (Exception e)
					{
						e.printStackTrace();
			    		info.callback.invoke(new Exception("Could not request Stripe token, no charge was made."));
					}
				}
			});
		}
		catch (Exception e)
		{
			e.printStackTrace();
    		info.callback.invoke(e);
		}
    }
    
    protected void refill_step_requestPaymentStripeStep2(RefillInfo info)
    {
    	log.debug("refil_step_requestPaymentStripeStep2");
		try
		{
			String url = 
					ConstantsClient.WEB_SERVER_TOMCAT + "StripePayment" + 
					"?email=" + info.email + 
					"&stripeTransactionID=" + info.stripeTransactionID +
					"&amount=" + info.amount;
					
			JSHttpDelegate http = new JSHttpDelegate(main.delegate);
			http.execute(HttpDelegate.GET, url, null, false, false, null, new CallbackWithVariables(info) {
				public void invoke(Object... arguments)
				{
			    	log.debug("signUp_step_requestPaymentStripeStep2 callback");
			    	RefillInfo info = V(0);
					try
					{
						if (arguments[0] instanceof Exception)
							throw (Exception)arguments[0];
						
						String response = (String)arguments[0];
						JSONObject o = new JSONObject(response);
						if (o.has("error"))
						{
							throw new Exception(o.getJSONObject("error").getString("message"));
						}
						else
						{
							JSInvoker.invoke(info.callback.getCallback(), "progress", new Object[] 
								{ "Purchase succeeded.\n\n" + "Please save the reciept: "+ o.getString("id")	}
							);
						}
						
						refill_finish(info);
					}
					catch (Exception e)
					{
						e.printStackTrace();
			    		info.callback.invoke(e);
					}
				}
			});
		}
		catch (Exception e)
		{
			e.printStackTrace();
    		info.callback.invoke(e);
		}
    	
    }
    
    protected void refill_finish(RefillInfo info)
    {
    	log.debug("signUp_finish");
    	info.callback.invoke(true);
    	log.debug("signUp_finish_finished");
    }
}

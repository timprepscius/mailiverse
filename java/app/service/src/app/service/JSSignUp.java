/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package app.service;


import java.security.NoSuchAlgorithmException;
import core.srp.client.SRPClientListener;
import java.util.Date;
import core.util.SecureRandom;
import key.auth.KeyServerAuthenticatorNoThread;
import mail.auth.MailServerAuthenticatorNoThread;
import mail.client.EventPropagator;
import core.constants.ConstantsDropbox;

import core.util.Base64;
import org.json.JSONObject;
import org.timepedia.exporter.client.Export;
import org.timepedia.exporter.client.Exportable;

import core.callback.Callback;
import core.callback.CallbackChain;
import core.callback.CallbackDefault;
import core.callback.CallbackWithVariables;
import core.constants.ConstantsMvStore;
import core.constants.ConstantsS3;
import core.constants.ConstantsEnvironmentKeys;
import core.constants.ConstantsClient;
import core.constants.ConstantsStorage;
import core.constants.ConstantsVersion;
import core.crypt.CryptorPGPFactory;
import core.crypt.CryptorRSAFactory;
import core.crypt.KeyPairFromPassword;
import core.io.IoChain;
import core.util.Environment;
import core.util.HttpDelegate;
import core.util.JSON_;
import core.util.JSON_.JSONException;
import core.util.LogNull;
import core.util.Pair;
import core.util.Strings;

@Export()
public class JSSignUp implements Exportable, SRPClientListener
{
	static LogNull log = new LogNull(JSSignUp.class);
	static SecureRandom random = new SecureRandom();
	
	Main main;
	
	JSSignUp (Main main)
	{
		this.main = main;
	}
	
	public void requestAuthorizationToken (Object callback)
	{
		JSResult<String> result = new JSResult<String>(callback);

		try
		{
			String url = 
					"https://api.dropbox.com/1/oauth/request_token" +
						"?oauth_consumer_key=" + ConstantsClient.DROPBOX_APPKEY +
						"&oauth_signature_method=PLAINTEXT" + 
						"&oauth_signature=" +  ConstantsClient.DROPBOX_APPSECRET + "%26" +
						"&oauth_nonce=\"" + (new Date()).getTime() + "\"";
					
			JSHttpDelegate http = new JSHttpDelegate(main.delegate);
			http.execute(HttpDelegate.GET, url, null, false, false, null, new CallbackWithVariables(result) {
				public void invoke(Object... arguments)
				{
					JSResult<String> result = (JSResult<String>)V(0);
					try
					{
						if (arguments[0] instanceof Exception)
							throw (Exception)arguments[0];
						
						String response = (String)arguments[0];
						String token=null, tokenSecret=null;
						String[] parts = response.split("&");
						for (String part : parts)
						{
							String[] keyValue = part.split("=");
							String key = keyValue[0];
							String value = keyValue[1];
							
							if (key.equalsIgnoreCase("oauth_token_secret"))
								tokenSecret = value;
							else
							if (key.equalsIgnoreCase("oauth_token"))
								token = value;
						}
						
						Object o = JSON_.newObject();
						JSON_.put(o, "key", JSON_.newString(token));
						JSON_.put(o, "secret", JSON_.newString(tokenSecret));
						
						result.invoke(o.toString());
					}
					catch (Exception e)
					{
						result.invoke(e);
					}
				}
			});
		}
		catch (Exception e)
		{
			result.invoke(e);
		}
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
    
    static class SignUpInfo {
    	public static enum Storage { S3, Mailiverse, Dropbox };
    	
    	String name, password, captchaToken;
    	Environment serverEnvironment, clientEnvironment, completeEnvironment;
    	JSResult<Boolean> callback;
    	
    	KeyPairFromPassword keyPair;
    	
    	Storage storage;
    	String storageRegion;
    	
    	String dropboxAppKey, dropboxAppSecret;
    	String dropboxAuthToken, dropboxAuthSecret;
    	String dropboxUserToken, dropboxUserSecret;
    	
    	
    	String awsBucketName, awsBucketRegion;
    	String awsWriteAccessKey, awsWriteSecretKey;
    	String awsReadWriteAccessKey, awsReadWriteSecretKey;
    	
    	String mvAccessKey, mvSecretKey;

    	String smtpPassword;
    	byte[] rsaPublicKey;
    	byte[] rsaPrivateKey;
    	
    	byte[] pgpPublicKey;
    	byte[] pgpPrivateKey;    	
    	
    	String stripeCardNumber, stripeCardExpMonth, stripeCardExpYear, stripeCardCVC;
    	String stripeTransactionID;
    	int paymentAmount = 1;
    	
    	SignUpInfo (
    		String name, String password, String captchaToken,
			JSResult<Boolean> callback
		)
		{
    		this.name = name;
    		this.password = password;
    		this.captchaToken = captchaToken;
    		this.callback = callback;
    		this.smtpPassword = "" + Math.abs(random.nextLong());
		}	
    	
    	void intializeStorageDropbox (
    		String appKey, String appSecret,
			String authToken, String authSecret
		)
    	{
    		this.storage = Storage.Dropbox;
    		this.dropboxAppKey = appKey;
    		this.dropboxAppSecret = appSecret;
    		this.dropboxAuthToken = authToken;
    		this.dropboxAuthSecret = authSecret;
		}
    	
    	void initializeStorageS3 (String storageRegion)
    	{
    		this.storage = Storage.S3;
    		this.storageRegion = storageRegion;
    	}
    	
    	void initializeStorageMailiverse ()
    	{
    		this.storage = Storage.Mailiverse;
    	}
    	
    	public void setDropboxInfo (String userToken, String userSecret)
    	{
    		this.dropboxUserToken = userToken;
    		this.dropboxUserSecret = userSecret;
    	}
    	
    	public void setAWSInfo (
    		String awsBucketName,  String awsBucketRegion,
    		String awsWriteAccessKey, String awsWriteSecretKey,
    		String awsReadWriteAccessKey, String awsReadWriteSecretKey)
    	{
    		this.awsBucketName = awsBucketName;
    		this.awsBucketRegion = awsBucketRegion;
    		this.awsWriteAccessKey = awsWriteAccessKey;
    		this.awsWriteSecretKey = awsWriteSecretKey;
    		this.awsReadWriteAccessKey = awsReadWriteAccessKey;
    		this.awsReadWriteSecretKey = awsReadWriteSecretKey;
    	}

    	public void setMailiverseInfo (String mvAccessKey, String mvSecretKey)
    	{
    		this.mvAccessKey = mvAccessKey;
    		this.mvSecretKey = mvSecretKey;
    	}
    	
    	
    	public void calculateRSA (Callback callback) throws NoSuchAlgorithmException
    	{
    		new CryptorRSAFactory().generate(2048, new CallbackDefault() {
				
				@Override
				public void onSuccess(Object... arguments) throws Exception {
					Pair<byte[], byte[]> pair = (Pair<byte[], byte[]>)arguments[0];	

					rsaPublicKey = pair.first;
		    		rsaPrivateKey = pair.second;
		    		
		    		callback.invoke();
				}
			}.setReturn(callback)
    		);
    	}
    	
    	public void calculatePGP (Callback callback) throws NoSuchAlgorithmException
    	{
    		new CryptorPGPFactory().generate(2048, name, password, new CallbackDefault() {
				
				@Override
				public void onSuccess(Object... arguments) throws Exception {
					Pair<byte[], byte[]> pair = (Pair<byte[], byte[]>)arguments[0];	

					pgpPublicKey = pair.first;
		    		pgpPrivateKey = pair.second;
		    		
		    		callback.invoke();
				}
			}.setReturn(callback)
    		);
    	}
    	
    	public void calculateEnvironmentDropbox ()
    	{
    		String handler = ConstantsStorage.HANDLER_DROPBOX;
    		String prefix = handler + "/";
    		
    		serverEnvironment = new Environment();
			serverEnvironment.put(ConstantsEnvironmentKeys.CONFIGURATION_VERSION, ConstantsVersion.CONFIGURATION);
    		serverEnvironment.put(ConstantsEnvironmentKeys.HANDLER, handler);
    		serverEnvironment.put(ConstantsEnvironmentKeys.SMTP_PASSWORD, smtpPassword);
    		serverEnvironment.put(prefix + ConstantsDropbox.DropboxUserPrefix, name);
    		serverEnvironment.put(prefix + ConstantsDropbox.DropboxAppKey, dropboxAppKey);
    		serverEnvironment.put(prefix + ConstantsDropbox.DropboxAppSecret, dropboxAppSecret);
    		serverEnvironment.put(prefix + ConstantsDropbox.DropboxTokenKey, dropboxUserToken);
    		serverEnvironment.put(prefix + ConstantsDropbox.DropboxTokenSecret, dropboxUserSecret);
    		serverEnvironment.put(
    				ConstantsEnvironmentKeys.PUBLIC_ENCRYPTION_KEY, 
    				Base64.encode(rsaPublicKey)
    			);
    		serverEnvironment.put(
    				ConstantsEnvironmentKeys.PGP_PUBLIC_KEY, 
    				Base64.encode(pgpPublicKey)
    			);

    		clientEnvironment = new Environment();
			clientEnvironment.put(ConstantsEnvironmentKeys.CONFIGURATION_VERSION, ConstantsVersion.CONFIGURATION);
    		clientEnvironment.put(ConstantsEnvironmentKeys.HANDLER, handler);
    		clientEnvironment.put(ConstantsEnvironmentKeys.SMTP_PASSWORD, smtpPassword);
    		clientEnvironment.put(prefix + ConstantsDropbox.DropboxUserPrefix, name);
    		clientEnvironment.put(prefix + ConstantsDropbox.DropboxAppKey, dropboxAppKey);
    		clientEnvironment.put(prefix + ConstantsDropbox.DropboxAppSecret, dropboxAppSecret);
    		clientEnvironment.put(prefix + ConstantsDropbox.DropboxTokenKey, dropboxUserToken);
    		clientEnvironment.put(prefix + ConstantsDropbox.DropboxTokenSecret, dropboxUserSecret);
    		clientEnvironment.put(
    				ConstantsEnvironmentKeys.PUBLIC_ENCRYPTION_KEY, 
    				Base64.encode(rsaPublicKey)
    			);
    		clientEnvironment.put(
    				ConstantsEnvironmentKeys.PRIVATE_DECRYPTION_KEY, 
    				Base64.encode(rsaPrivateKey)
    			);	
    		clientEnvironment.put(
    				ConstantsEnvironmentKeys.PGP_PUBLIC_KEY, 
    				Base64.encode(pgpPublicKey)
    			);
    		clientEnvironment.put(
    				ConstantsEnvironmentKeys.PGP_PRIVATE_KEY, 
    				Base64.encode(pgpPrivateKey)
    			);
    		
    		completeEnvironment = new Environment();
			completeEnvironment.put(ConstantsEnvironmentKeys.CONFIGURATION_VERSION, ConstantsVersion.CONFIGURATION);
			completeEnvironment.addChildEnvironment(ConstantsEnvironmentKeys.SERVER_ENVIRONMENT, serverEnvironment);
			completeEnvironment.addChildEnvironment(ConstantsEnvironmentKeys.CLIENT_ENVIRONMENT, clientEnvironment);
		}
    	
    	public void calculateEnvironmentAWS ()
    	{
    		String handler = ConstantsStorage.HANDLER_S3;
    		String prefix = handler + "/";
    		
    		serverEnvironment = new Environment();
			serverEnvironment.put(ConstantsEnvironmentKeys.CONFIGURATION_VERSION, ConstantsVersion.CONFIGURATION);
    		serverEnvironment.put(ConstantsEnvironmentKeys.HANDLER, handler);
    		serverEnvironment.put(ConstantsEnvironmentKeys.SMTP_PASSWORD, smtpPassword);
    		serverEnvironment.put(prefix + ConstantsS3.AWSAccessKeyId, awsWriteAccessKey);
    		serverEnvironment.put(prefix + ConstantsS3.AWSSecretKey, awsWriteSecretKey);
    		serverEnvironment.put(prefix + ConstantsS3.AWSBucketName, awsBucketName);
    		serverEnvironment.put(prefix + ConstantsS3.AWSBucketRegion, awsBucketRegion);
    		serverEnvironment.put(
    				ConstantsEnvironmentKeys.PUBLIC_ENCRYPTION_KEY, 
    				Base64.encode(rsaPublicKey)
    			);
    		serverEnvironment.put(
    				ConstantsEnvironmentKeys.PGP_PUBLIC_KEY, 
    				Base64.encode(pgpPublicKey)
    			);

    		clientEnvironment = new Environment();
			clientEnvironment.put(ConstantsEnvironmentKeys.CONFIGURATION_VERSION, ConstantsVersion.CONFIGURATION);
    		clientEnvironment.put(ConstantsEnvironmentKeys.HANDLER, handler);
    		clientEnvironment.put(ConstantsEnvironmentKeys.SMTP_PASSWORD, smtpPassword);
    		clientEnvironment.put(prefix + ConstantsS3.AWSAccessKeyId, awsReadWriteAccessKey);
    		clientEnvironment.put(prefix + ConstantsS3.AWSSecretKey, awsReadWriteSecretKey);
    		clientEnvironment.put(prefix + ConstantsS3.AWSBucketName, awsBucketName);
    		clientEnvironment.put(prefix + ConstantsS3.AWSBucketRegion, awsBucketRegion);
    		clientEnvironment.put(
    				ConstantsEnvironmentKeys.PUBLIC_ENCRYPTION_KEY, 
    				Base64.encode(rsaPublicKey)
    			);
    		clientEnvironment.put(
    				ConstantsEnvironmentKeys.PRIVATE_DECRYPTION_KEY, 
    				Base64.encode(rsaPrivateKey)
    			);	
    		clientEnvironment.put(
    				ConstantsEnvironmentKeys.PGP_PUBLIC_KEY, 
    				Base64.encode(pgpPublicKey)
    			);
    		clientEnvironment.put(
    				ConstantsEnvironmentKeys.PGP_PRIVATE_KEY, 
    				Base64.encode(pgpPrivateKey)
    			);
    		
    		completeEnvironment = new Environment();
			completeEnvironment.put(ConstantsEnvironmentKeys.CONFIGURATION_VERSION, ConstantsVersion.CONFIGURATION);
			completeEnvironment.addChildEnvironment(ConstantsEnvironmentKeys.SERVER_ENVIRONMENT, serverEnvironment);
			completeEnvironment.addChildEnvironment(ConstantsEnvironmentKeys.CLIENT_ENVIRONMENT, clientEnvironment);
		}
    	
    	public void calculateEnvironmentMailiverse ()
    	{
    		String handler = ConstantsStorage.HANDLER_MV;
    		String prefix = handler + "/";
    		
    		serverEnvironment = new Environment();
			serverEnvironment.put(ConstantsEnvironmentKeys.CONFIGURATION_VERSION, ConstantsVersion.CONFIGURATION);
    		serverEnvironment.put(ConstantsEnvironmentKeys.HANDLER, handler);
    		serverEnvironment.put(ConstantsEnvironmentKeys.SMTP_PASSWORD, smtpPassword);
    		serverEnvironment.put(prefix + ConstantsMvStore.AccessKeyId, mvAccessKey);
    		serverEnvironment.put(prefix + ConstantsMvStore.SecretKey, mvSecretKey);
    		serverEnvironment.put(
    				ConstantsEnvironmentKeys.PUBLIC_ENCRYPTION_KEY, 
    				Base64.encode(rsaPublicKey)
    			);
    		serverEnvironment.put(
    				ConstantsEnvironmentKeys.PGP_PUBLIC_KEY, 
    				Base64.encode(pgpPublicKey)
    			);

    		clientEnvironment = new Environment();
			clientEnvironment.put(ConstantsEnvironmentKeys.CONFIGURATION_VERSION, ConstantsVersion.CONFIGURATION);
    		clientEnvironment.put(ConstantsEnvironmentKeys.HANDLER, handler);
    		clientEnvironment.put(ConstantsEnvironmentKeys.SMTP_PASSWORD, smtpPassword);
    		clientEnvironment.put(prefix + ConstantsMvStore.AccessKeyId, mvAccessKey);
    		clientEnvironment.put(prefix + ConstantsMvStore.SecretKey, mvSecretKey);
    		clientEnvironment.put(
    				ConstantsEnvironmentKeys.PUBLIC_ENCRYPTION_KEY, 
    				Base64.encode(rsaPublicKey)
    			);
    		clientEnvironment.put(
    				ConstantsEnvironmentKeys.PRIVATE_DECRYPTION_KEY, 
    				Base64.encode(rsaPrivateKey)
    			);	
    		clientEnvironment.put(
    				ConstantsEnvironmentKeys.PGP_PUBLIC_KEY, 
    				Base64.encode(pgpPublicKey)
    			);
    		clientEnvironment.put(
    				ConstantsEnvironmentKeys.PGP_PRIVATE_KEY, 
    				Base64.encode(pgpPrivateKey)
    			);
    		
    		completeEnvironment = new Environment();
			completeEnvironment.put(ConstantsEnvironmentKeys.CONFIGURATION_VERSION, ConstantsVersion.CONFIGURATION);
			completeEnvironment.addChildEnvironment(ConstantsEnvironmentKeys.SERVER_ENVIRONMENT, serverEnvironment);
			completeEnvironment.addChildEnvironment(ConstantsEnvironmentKeys.CLIENT_ENVIRONMENT, clientEnvironment);
		}

    	public void calculateEnvironment ()
    	{
    		if (storage == Storage.Dropbox)
    			calculateEnvironmentDropbox();
    		else
    		if (storage == Storage.S3)
    			calculateEnvironmentAWS();
    		else
    		if (storage == Storage.Mailiverse)
    			calculateEnvironmentMailiverse();
    		
    	}
    };
    
    public void signUp (
    	String storage,
    	String storageInfo,
    	String name, String password, String captchaToken,
    	String dropboxUserKey, String dropboxUserSecret,
    	Object callback
    ) throws JSONException
    {
    	log.debug("signUp", name, password, captchaToken, dropboxUserKey, dropboxUserSecret);
    	
    	SignUpInfo info = new SignUpInfo(
    		name, password, captchaToken, 
    		new JSResult<Boolean>(callback)
    	);
    	
    	if (storage.equals("dropbox"))
    		info.intializeStorageDropbox(ConstantsClient.DROPBOX_APPKEY, ConstantsClient.DROPBOX_APPSECRET, dropboxUserKey, dropboxUserSecret);
    	else
    	if (storage.equals("s3"))
    		info.initializeStorageS3(JSON_.getString(JSON_.parse(storageInfo), "region"));
    	else
    	if (storage.equals("mailiverse"))
    		info.initializeStorageMailiverse();
    	
    	CallbackChain signUpChain = new CallbackChain();
    	
    	signUpChain.addCallback(new CallbackDefault(info) {
			public void onSuccess(Object... arguments) throws Exception {
		    	log.debug("signUp_step_createRSA");
		    	
				SignUpInfo info = (SignUpInfo)V(0);
				info.calculateRSA(callback);
			}
		});
    	
    	signUpChain.addCallback(new CallbackDefault(info) {
			public void onSuccess(Object... arguments) throws Exception {
		    	log.debug("signUp_step_createPGP");
		    	
				SignUpInfo info = (SignUpInfo)V(0);
				JSInvoker.invoke(info.callback.getCallback(), "progress", new Object[] { "Creating PGP key pair." });
				info.calculatePGP(callback);
			}
		});

    	signUpChain.addCallback(new CallbackDefault(info) {
			public void onSuccess(Object... arguments) throws Exception {
		    	log.debug("signUp_step_genKeyPair");
		    	
				SignUpInfo info = (SignUpInfo)V(0);
				JSInvoker.invoke(info.callback.getCallback(), "progress", new Object[] { "Creating verification key pair." });
				info.keyPair = new KeyPairFromPassword(info.password);
				info.keyPair.generate_().addCallback(callback).invoke();
			}
		});

    	signUpChain.addCallback(new CallbackDefault(info) {
			public void onSuccess(Object... arguments) throws Exception {
		    	log.debug("signUp_step_createStorage");
				
				SignUpInfo info = (SignUpInfo)V(0);
				JSInvoker.invoke(info.callback.getCallback(), "progress", new Object[] { "Creating storage space." });
				signUp_step_requestAccess(info, callback);
			}
		});
    	
    	signUpChain.addCallback(new CallbackDefault(info) {
			public void onSuccess(Object... arguments) throws Exception {
		    	log.debug("signUp_step_createEnvironments");

		    	SignUpInfo info = V(0);
				JSInvoker.invoke(info.callback.getCallback(), "progress", new Object[] { "Calculating environments." });
		    	info.calculateEnvironment();
		    	callback.invoke();
			}
		});

    	signUpChain.addCallback(new CallbackDefault(info) {
			public void onSuccess(Object... arguments) throws Exception {
		    	log.debug("signUp_step_createUser");

		    	SignUpInfo info = V(0);
				JSInvoker.invoke(info.callback.getCallback(), "progress", new Object[] { "Creating user account." });
		    	createUser(
		    		info.name, info.keyPair, info.captchaToken, 
		    		callback
		   		);
			}
		});

    	signUpChain.addCallback(new CallbackDefault(info) {
			public void onSuccess(Object... arguments) throws Exception {
		    	log.debug("signUp_step_enableServer");

		    	SignUpInfo info = V(0);
				JSInvoker.invoke(info.callback.getCallback(), "progress", new Object[] { "Enabling server." });
		    	putServerEnvironment(
	    			info.name, info.keyPair, info.serverEnvironment, 
	    			callback
		    	);
			}
		});

    	signUpChain.addCallback(new CallbackDefault(info) {
			public void onSuccess(Object... arguments) throws Exception {
		    	log.debug("signUp_step_enableServer");

		    	SignUpInfo info = V(0);
				JSInvoker.invoke(info.callback.getCallback(), "progress", new Object[] { "Enabling client." });
		    	putClientEnvironment(
	    			info.name, info.keyPair, info.completeEnvironment, 
	    			callback
		    	);
			}
		});
    	signUpChain.addCallback(new CallbackWithVariables(info) {

			@Override
			public void invoke(Object... arguments) {
				SignUpInfo info = (SignUpInfo)V(0);
				info.callback.invoke(arguments);
			}
    	});

    	main.eventPropagator.signal(
    		EventPropagator.INVOKE, 
    		signUpChain
    	);
    }
    
    //------------------------------------------------------------------------------------------
    
    protected void signUp_step_requestAccess (SignUpInfo info, Callback callback)
    {
    	if (info.storage == SignUpInfo.Storage.S3)
    		signUp_step_requestS3Bucket(info, callback);
    	else
    	if (info.storage == SignUpInfo.Storage.Mailiverse)
    		signUp_step_requestMailiverseBucket(info, callback);
    	else
    	if (info.storage == SignUpInfo.Storage.Dropbox)
    		signUp_step_requestDropboxAccessToken(info, callback);
    }

	protected void signUp_step_requestS3Bucket (SignUpInfo info, Callback callback)
	{
    	log.debug("signUp_step_requestS3Bucket");

		String url = 
				ConstantsClient.WEB_SERVER_TOMCAT + "CreateBucket" +
					"?email=" + info.name + "&captcha=" + info.captchaToken + "&region=" + info.storageRegion;
				
		JSHttpDelegate http = new JSHttpDelegate(main.delegate);
		http.execute(HttpDelegate.GET, url, null, false, false, null, 
			new CallbackDefault(info) {
				public void onSuccess(Object... arguments)
				{
			    	log.debug("signUp_step_requestS3Bucket callback");
					SignUpInfo info = V(0);
					String response = (String)arguments[0];
					String awsBucketName=null, awsBucketRegion=null,
						awsWriteAccessKey=null, awsWriteSecretKey=null,
						awsReadWriteAccessKey=null, awsReadWriteSecretKey=null;
					
					String[] parts = response.split("&");
					for (String part : parts)
					{
						String[] keyValue = part.split("!");
						String key = keyValue[0];
						String value = keyValue[1];
						
						if (key.equalsIgnoreCase("writeAccessKey"))
							awsWriteAccessKey = value;
						else
						if (key.equalsIgnoreCase("writeSecretKey"))
							awsWriteSecretKey = value;
						else
						if (key.equalsIgnoreCase("readWriteAccessKey"))
							awsReadWriteAccessKey = value;
						else
						if (key.equalsIgnoreCase("readWriteSecretKey"))
							awsReadWriteSecretKey = value;
						else
						if (key.equalsIgnoreCase("bucketName"))
							awsBucketName = value;
						else
						if (key.equalsIgnoreCase("bucketRegion"))
							awsBucketRegion = value;
					}
					
					info.setAWSInfo(awsBucketName, awsBucketRegion, awsWriteAccessKey, awsWriteSecretKey, awsReadWriteAccessKey, awsReadWriteSecretKey);
					next();
				}
			}.setReturn(callback)
		);
	}
    
	protected void signUp_step_requestMailiverseBucket (SignUpInfo info, Callback callback)
	{
    	log.debug("signUp_step_requestMailiverseBucket");

		String url = 
				ConstantsClient.WEB_SERVER_TOMCAT + "StoreEnable" +
					"?email=" + info.name + "&captcha=" + info.captchaToken;
				
		JSHttpDelegate http = new JSHttpDelegate(main.delegate);
		http.execute(HttpDelegate.GET, url, null, false, false, null, 
			new CallbackDefault(info) {
				public void onSuccess(Object... arguments)
				{
			    	log.debug("signUp_step_requestMailiverseBucket callback");
					SignUpInfo info = V(0);
					String response = (String)arguments[0];
					String mvAccessKey=null, mvSecretKey=null;
					
					String[] parts = response.split("&");
					for (String part : parts)
					{
						String[] keyValue = part.split("!");
						String key = keyValue[0];
						String value = keyValue[1];
						
						if (key.equalsIgnoreCase(ConstantsMvStore.AccessKeyId))
							mvAccessKey = value;
						else
						if (key.equalsIgnoreCase(ConstantsMvStore.SecretKey))
							mvSecretKey = value;
					}
					
					info.setMailiverseInfo(mvAccessKey, mvSecretKey);
					next();
				}
			}.setReturn(callback)
		);
	}

	protected void signUp_step_requestDropboxAccessToken (SignUpInfo info, Callback callback)
	{
    	log.debug("signUp_step_requestAccessToken");

    	String url = 
				"https://api.dropbox.com/1/oauth/access_token" +
					"?oauth_consumer_key=" + info.dropboxAppKey +
					"&oauth_token=" + info.dropboxAuthToken + "&" +
					"&oauth_signature_method=PLAINTEXT" + 
					"&oauth_signature=" +  info.dropboxAppSecret + "%26" + info.dropboxAuthSecret + 
					"&oauth_nonce=\"" + (new Date()).getTime() + "\"";
				
		JSHttpDelegate http = new JSHttpDelegate(main.delegate);
		http.execute(HttpDelegate.GET, url, null, false, false, null, 
			new CallbackDefault(info) {
				public void onSuccess(Object... arguments)
				{
			    	log.debug("signUp_step_requestAccessToken callback");
					SignUpInfo info = V(0);
					
					String response = (String)arguments[0];
					String token=null, tokenSecret=null;
					String[] parts = response.split("&");
					for (String part : parts)
					{
						String[] keyValue = part.split("=");
						String key = keyValue[0];
						String value = keyValue[1];
						
						if (key.equalsIgnoreCase("oauth_token_secret"))
							tokenSecret = value;
						else
						if (key.equalsIgnoreCase("oauth_token"))
							token = value;
					}
					
					info.setDropboxInfo(token, tokenSecret);
					next();
				}
			}.setReturn(callback)
		);
	}
    
    
    protected void createUser (String user, KeyPairFromPassword keyPair, String token, Callback callback) throws Exception
    {
		String url = ConstantsClient.MAIL_SERVER_WEBSOCKET;
		
		MailServerAuthenticatorNoThread.create_(
			user, keyPair, token,
			new JSStreamSessionWebSocket(url, main.delegate)
		).setReturn(callback).invoke();
    }
    
    protected void putServerEnvironment (String user, KeyPairFromPassword keyPair, Environment environment, Callback callback) throws Exception
    {
		String url = ConstantsClient.MAIL_SERVER_WEBSOCKET;
		
		MailServerAuthenticatorNoThread.put_(
			user, keyPair, environment,
			new JSStreamSessionWebSocket(url, main.delegate),
			JSSignUp.this
		).setReturn(callback).invoke(environment);
    }

    protected void putClientEnvironment (String user, KeyPairFromPassword keyPair, Environment environment, Callback callback)
    {
		try
    	{
			String url = ConstantsClient.KEY_SERVER_WEBSOCKET;
			
			KeyServerAuthenticatorNoThread.put_(
				user, keyPair, environment,
				new JSStreamSessionWebSocket(url, main.delegate),
				JSSignUp.this
			).setReturn(callback).invoke(environment);
    	}
    	catch (Exception e)
    	{
    		callback.invoke(e);
    	}
    }    
    
    public void onSRPStep (String event)
    {
    	main.eventPropagator.signal("onAuthenticationStep", event);
    }
    
}

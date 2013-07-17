/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import mail.client.model.Direction;

import core.callback.Callback;
import core.callback.CallbackChain;
import core.callback.CallbackDefault;
import core.callback.CallbackWithVariables;
import core.connector.FileInfo;
import core.connector.async.AsyncStoreConnector;
import core.connector.async.AsyncStoreConnectorBase64;
import core.connector.async.AsyncStoreConnectorEncrypted;
import core.connector.async.Lock;
import core.constants.ConstantsStorage;
import core.crypt.Cryptor;
import core.util.LogNull;

public class ArrivalsMonitorDefault extends ArrivalsMonitor
{
	static LogNull log = new LogNull(ArrivalsMonitorDefault.class);
	static final int NUM_FILES_BEFORE_CACHE = 10;
	static final int NUM_FILES_IN_CHECK = 15;
	AsyncStoreConnector store;

	boolean checking = false;
	Object in,out;
	List<FileInfo> files;
	int totalFilesFound=0;
	int numFilesWillCheck=0;
	Lock checkMailLock;
	
	public ArrivalsMonitorDefault (Cryptor cryptor, AsyncStoreConnector connector)
	{
		this.store = new AsyncStoreConnectorEncrypted(cryptor, connector);
		checkMailLock = 
			new Lock(
				new AsyncStoreConnectorBase64(connector), 
				ConstantsStorage.NEW_IN_JSON + "/checkMail.lock", 
				ConstantsStorage.MAIL_CHECK_LOCK_TIME_SECONDS,
				ConstantsStorage.MAIL_CHECK_LOCK_TIME_ALLOWED_BEFORE_RELOCK_SECONDS
			);
	}
	
	public boolean isChecking ()
	{
		return checking;
	}

	@Override
	public void check ()
	{
		if (checking)
			return;
		
		log.debug("check");

		in = null;
		out = null;
		totalFilesFound = numFilesWillCheck = 0;
		
		CallbackChain callbackChain = new CallbackChain()
			.addCallback(master.getEventPropagator().signal_(Events.CheckBegin, (Object[])null))
			.addCallback(master.getCacheManager().update_())
			.addCallback(checkMailLock.lock_())
			.addCallback(check_directory_(ConstantsStorage.NEW_IN_JSON, Direction.IN))
			.addCallback(check_directory_(ConstantsStorage.NEW_OUT_JSON, Direction.OUT))
			.addCallback(check_combine_())
			.addCallback(new CyclicalFileCheck(this))
			.addCallback(check_end_());
		
		// we do not unlock the checkMailLock, because I think this throws
		// too many variables into the equation
		
		checking = true;
		callbackChain.invoke();
	}
	
	public Callback check_end_()
	{
		return 
		new CallbackDefault() {

			@Override
			public void onSuccess(Object... arguments) throws Exception {
				checking = false;
				
				master.getEventPropagator().signal(Events.CheckSuccess, (Object[])null);
				master.getEventPropagator().signal(Events.CheckEnd, (Object[])null);
			}
			
			public void onFailure(Exception e) {
				checking = false;
				
				master.getEventPropagator().signal(Events.CheckFailure, "Failed");
				master.getEventPropagator().signal(Events.CheckEnd, (Object[])null);
			}
		};	
	}
	
	public Callback check_directory_ (String path, Direction direction)
	{
		log.debug("check directory", path);
		
		return store.list_(path + "/").addCallback(check_accumulate_(direction));
	}
	
	public Callback check_accumulate_ (Direction direction)
	{
		return new CallbackDefault(direction) {
			public void onSuccess(Object... arguments) throws Exception {
				Direction direction = V(0);
				if (direction == Direction.IN)
					in = arguments[0];
				else
					out = arguments[0];
				
				next();
			}
		};
	}
	
	public Callback check_combine_ ()
	{
		return new CallbackDefault() {
			public void onSuccess(Object...arguments) throws Exception {

				log.debug("check_combine");
				checkMailLock.testLock((List<FileInfo>)in);

				files = new ArrayList<FileInfo>();
				if (in instanceof List)
				{
					List<FileInfo> lin = (List<FileInfo>)in;
					log.debug("check_combine","in", lin.size());
					
					for (FileInfo file : lin)
					{
						if (file.path.endsWith(".lock"))
							continue;
						
						if (master.getArrivalsProcessor().alreadyProcessed(file.path))
							continue;
						
						log.debug("found file",file);
						
						files.add(file);
						file.user = Direction.IN;
					}
				}
				if (out instanceof List)
				{
					List<FileInfo> lout = (List<FileInfo>)out;
					log.debug("check_combine","out", lout.size());
					for (FileInfo file : lout)
					{
						if (master.getArrivalsProcessor().alreadyProcessed(file.path))
							continue;
						
						files.add(file);
						file.user = Direction.OUT;
					}
				}
			
				totalFilesFound = files.size();
				
				Collections.sort(files, new FileInfo.SortByDateAscending());
				List<FileInfo> segment = files.subList(0, Math.min(files.size(), NUM_FILES_IN_CHECK));
				files = segment;
				
				numFilesWillCheck = files.size();
				
				log.debug("check_combine","final", files.size());
				next();
			}
		};
	}
}

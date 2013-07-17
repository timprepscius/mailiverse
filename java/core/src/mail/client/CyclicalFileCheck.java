/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client;

import java.util.Date;

import mail.client.model.Direction;
import core.callback.Callback;
import core.callback.CallbackChain;
import core.callback.CallbackDefault;
import core.callback.CallbackWithVariables;
import core.connector.FileInfo;
import core.util.LogNull;
import core.util.LogOut;

public class CyclicalFileCheck extends CallbackDefault
{
	static LogNull log = new LogNull(CyclicalFileCheck.class);
	public int numChecked = 0;

	ArrivalsMonitorDefault monitor;
	
	public CyclicalFileCheck(ArrivalsMonitorDefault monitor)
	{	
		this.monitor = monitor;
	}
	
	public void onSuccess (Object...arguments) throws Exception {
		
		log.debug("check_next_file");
		ArrivalsProcessor arrivalsProcessor = monitor.master.getArrivalsProcessor();

		FileInfo file = null;
		if (!monitor.files.isEmpty())
		{
			file = monitor.files.get(0);
			monitor.files.remove(0);
		}
		
		log.debug("found file",file);
		
		numChecked++;
		
		arrivalsProcessor.getMaster().getEventPropagator().signal(
			Events.CheckStep, "" + numChecked + ":" + monitor.numFilesWillCheck + ":" + monitor.totalFilesFound
		);
		
		/*
		if (file == null || (++numChecked % monitor.NUM_FILES_BEFORE_CACHE == 0))
		{
			try
			{
				monitor.master.getCacheManager().flush();
			}
			catch (Exception e)
			{
				log.debug("ignoring during flush, because don't want to interrupt arrivals", e);
			}
		}
		*/
		
		if (file != null)
		{
			log.debug("going to process file",file.path);
			
			monitor.checkMailLock.relock_().invoke();
			
			monitor.store.get_()
				.addCallback(new CheckFileResult((Direction)file.user, file.path, file.date, this))
				.invoke(file.path);				
		}
		else
		{
			log.debug("no file, to proceeding in callback chain");
			next();
		}
	}
	
	class CheckFileResult extends CallbackDefault 
	{
		CheckFileResult (Direction direction, String path, Date date, Callback callback)
		{
			super(direction, path, date);
			this.callback = callback;
		}

		public void onSuccess(Object...arguments) throws Exception {
			Direction direction = V(0);
			String path = V(1);
			Date date = V(2);
			
			log.debug("check_file_result", path);
			ArrivalsProcessor arrivalsProcessor = monitor.master.getArrivalsProcessor();
		
			byte[] data = (byte[])arguments[0];
	
			log.debug ("handling ", path);
			
			try
			{
	    		arrivalsProcessor.processSuccess(
	    			direction, 
	    			path, 
	    			date,
	    			data
	    		);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				arrivalsProcessor.processFailure(direction, path, date, e);
			}
			
			next();
		}
	};
}

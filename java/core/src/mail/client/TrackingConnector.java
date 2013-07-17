/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.client;

import core.callback.Callback;
import core.callback.CallbackDefault;
import core.callback.CallbackWithVariables;
import core.connector.async.AsyncStoreConnector;
import core.util.LogNull;
import core.util.LogOut;

public class TrackingConnector extends Servent<Master> implements AsyncStoreConnector
{
	AsyncStoreConnector connector;
	LogNull log = new LogNull(TrackingConnector.class);
	
	public volatile int uploading = 0;
	public volatile int downloading = 0;
	
	public TrackingConnector (AsyncStoreConnector connector)
	{
		this.connector = connector;
	}
	
	protected synchronized void onUploadBegin()
	{
		uploading++;
		log.debug("onUploadBegin", uploading);
		
		if (uploading == 1)
			getMaster().getEventPropagator().signal(Events.UploadBegin, (Object[])null);
	}
	
	protected Callback onUploadBegin_()
	{
		return new Callback() {
			public void invoke (Object... arguments) {
				onUploadBegin();
				next(arguments);
			}
		};
	}
	
	protected synchronized void onUploadEnd()
	{
		log.debug("onUploadEnd", uploading);
		uploading--;
		
		if (uploading == 0)
			getMaster().getEventPropagator().signal(Events.UploadEnd, (Object[])null);
	}
	
	protected Callback onUploadEnd_()
	{
		return new Callback() {
			public void invoke (Object... arguments) {
				onUploadEnd();
				next(arguments);
			}
		};
	}

	protected synchronized void onDownloadBegin()
	{
		downloading++;
		log.debug("onDownloadBegin", downloading);

		if (downloading == 1)
			getMaster().getEventPropagator().signal(Events.DownloadBegin, (Object[])null);
	}
	
	protected Callback onDownloadBegin_()
	{
		return new Callback() {
			public void invoke (Object... arguments) {
				onDownloadBegin();
				next(arguments);
			}
		};
	}

	protected synchronized void onDownloadEnd ()
	{
		log.debug("onDownloadEnd", downloading);
		downloading--;

		if (downloading == 0)
			getMaster().getEventPropagator().signal(Events.DownloadEnd, (Object[])null);
	}
	
	protected Callback onDownloadEnd_()
	{
		return new Callback() {
			public void invoke (Object... arguments) {
				onDownloadEnd();
				next(arguments);
			}
		};
	}

	//--------------------------------------------------------------
	
	@Override
	public Callback list_(String path)
	{
		return onDownloadBegin_().addCallback(connector.list_(path)).addCallback(onDownloadEnd_()).setSlowFail();
	}

	@Override
	public Callback createDirectory_(String path)
	{
		return onUploadBegin_().addCallback(connector.createDirectory_(path)).addCallback(onUploadEnd_()).setSlowFail();
	}

	@Override
	public Callback ensureDirectories_(String[] directories)
	{
		return onUploadBegin_().addCallback(connector.ensureDirectories_(directories)).addCallback(onUploadEnd_()).setSlowFail();
	}

	@Override
	public Callback get_()
	{
		return onDownloadBegin_().addCallback(connector.get_()).addCallback(onDownloadEnd_()).setSlowFail();
	}

	@Override
	public Callback get_(String path)
	{
		return onDownloadBegin_().addCallback(connector.get_(path)).addCallback(onDownloadEnd_()).setSlowFail();
	}

	@Override
	public Callback put_(String path, byte[] bytes)
	{
		return onUploadBegin_().addCallback(connector.put_(path, bytes)).addCallback(onUploadEnd_()).setSlowFail();
	}

	@Override
	public Callback put_(String path)
	{
		return onUploadBegin_().addCallback(connector.put_(path)).addCallback(onUploadEnd_()).setSlowFail();
	}

	@Override
	public Callback move_(String from, String to)
	{
		return onUploadBegin_().addCallback(connector.move_(from, to)).addCallback(onUploadEnd_()).setSlowFail();
	}

	@Override
	public Callback delete_(String path)
	{
		return onUploadBegin_().addCallback(connector.delete_(path)).addCallback(onUploadEnd_()).setSlowFail();
	}
}

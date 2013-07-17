/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package key.streamserver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import key.server.KeyServerSessionDb;
import key.server.KeyServerUserSession;
import key.server.sql.KeyUserDb;
import mail.streamserver.MailServerSessionDb;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.service.IoHandler;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;

import core.constants.ConstantsServer;
import core.crypt.CryptorRSAAES;
import core.crypt.CryptorRSAJCE;
import core.io.IoChainBase64;
import core.io.IoChainFinishedException;
import core.io.IoChainNewLinePackets;
import core.io.IoChainAccumulator;
import core.srp.server.SRPServerUserSession;
import core.util.ExternalResource;
import core.util.InternalResource;
import core.util.LogNull;
import core.util.LogOut;


/**
 * {@link IoHandler} implementation for NetCat client.  This class extended
 * {@link IoHandlerAdapter} for convenience.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class SRPProtocolHandler extends IoHandlerAdapter 
{
	static final int TIMEOUT_SECONDS = ConstantsServer.AUTH_TIMEOUT;
	static LogOut log = new LogOut(SRPProtocolHandler.class);
	
	CryptorRSAAES cryptorRSA;
	KeyUserDb db;
	Map<IoSession, KeyServerUserSession> sessions = new HashMap<IoSession, KeyServerUserSession>();
	
	public SRPProtocolHandler () throws Exception
	{
		 db = new KeyUserDb();
		 cryptorRSA = new CryptorRSAAES(new CryptorRSAJCE(ExternalResource.getResourceAsStream(getClass(), "keystore.jks"), null));
	}
	
    @Override
    public void exceptionCaught(IoSession session, Throwable cause) 
    {
    	log.debug("exceptionCaught", cause);
        session.close(true);
    }

    @Override
    public void sessionOpened(IoSession session) 
    {
        log.debug("sessionOpened",session);
        
        // Set reader idle time to 10 seconds.
        // sessionIdle(...) method will be invoked when no data is read
        // for 10 seconds.
        session.getConfig().setIdleTime(IdleStatus.READER_IDLE, TIMEOUT_SECONDS);
        
        try
        {
        	KeyServerSessionDb sessionDb = new KeyServerSessionDb(db);
        	KeyServerUserSession userSession =
        		new KeyServerUserSession(
        			sessionDb,
					new SRPServerUserSession(cryptorRSA, sessionDb, 
						new IoChainBase64(
							new IoChainNewLinePackets(
								new IoChainAccumulator()
							)
						)
        			)
        		);
        	
        	userSession.run();
	        sessions.put(session, userSession);
        }
        catch (Exception e)
        {
        	log.debug("sessionOpened caught", e);
        	session.close(true);
        }
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception 
    {
        log.debug("sessionClosed ", session," Total ",session.getReadBytes()," byte(s)");
        KeyServerUserSession userSession = sessions.get(session);
        sessions.remove(session);

        if (userSession != null)
        	userSession.stop();
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) {
        // Close the connection if reader is idle.
        if (status == IdleStatus.READER_IDLE) {
            session.close(true);
        }
    }

    public void write (IoSession session, byte[] data)
    {
    	log.debug("writing");
    	
    	byte[] bytes = data;
        IoBuffer out = IoBuffer.allocate(bytes.length);
        out.setAutoExpand(true);
        out.put (bytes);
        out.flip();
        
        session.write(out);
    }
    
    @Override
    public void messageReceived(IoSession session, Object message) {
        log.debug("messageReceived");

        try
        {
	        IoChainAccumulator userSession = 
	        	(IoChainAccumulator)sessions.get(session).getFinalSender();

	        // add back the new line
            String str = message.toString() + "\n";
	        userSession.receive(str.getBytes());
	        
	        List<byte[]> packets = userSession.getAndClearPackets();
	        for(byte[] packet: packets)
	        	write(session, packet);
	        
	        Exception e = userSession.getAndClearException();
	        if (e != null)
	        	throw e;
	        
	        if (userSession.isClosed())
	        	throw new IoChainFinishedException();
        }
        catch (Exception e)
        {
        	log.debug("messageReceived caught", e);
        	session.close(true);
        } 
    }
}
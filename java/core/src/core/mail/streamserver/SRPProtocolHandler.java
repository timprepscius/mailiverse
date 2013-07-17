/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */
package mail.streamserver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mail.server.db.MailUserDb;

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
import core.util.LogOut;


/**
 * {@link IoHandler} implementation for NetCat client.  This class extended
 * {@link IoHandlerAdapter} for convenience.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class SRPProtocolHandler extends IoHandlerAdapter 
{
	static LogOut log = new LogOut(SRPProtocolHandler.class);
	static final int TIMEOUT_SECONDS = ConstantsServer.AUTH_TIMEOUT;
	
	MailUserDb db;
	Map<IoSession, MailServerUserSession> sessions = new HashMap<IoSession, MailServerUserSession>();
	CryptorRSAAES cryptorRSA;
	
	public SRPProtocolHandler () throws Exception
	{
		db = new MailUserDb();
		cryptorRSA = new CryptorRSAAES(new CryptorRSAJCE(ExternalResource.getResourceAsStream(getClass(), "keystore.jks"), null));
	}
	
    @Override
    public void exceptionCaught(IoSession session, Throwable cause) 
    {
    	log.debug("exceptionCaught", session, cause);
        session.close(true);
    }

    @Override
    public void sessionOpened(IoSession session) 
    {
    	log.debug("sessionOpened", session);
        
        // Set reader idle time to 10 seconds.
        // sessionIdle(...) method will be invoked when no data is read
        // for 10 seconds.
        session.getConfig().setIdleTime(IdleStatus.READER_IDLE, TIMEOUT_SECONDS);
        
        try
        {
        	MailServerSessionDb sessionDb = new MailServerSessionDb(db);
        	
        	MailServerUserSession userSession = 
        		new MailServerUserSession(sessionDb,
       				new SRPServerUserSession(
       					cryptorRSA, sessionDb,
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
    public void sessionClosed(IoSession session)
    {
        log.debug("sessionClosed", session, "Total", session.getReadBytes(), "byte(s)");
        sessions.remove(session);
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status) {
        // Close the connection if reader is idle.
        if (status == IdleStatus.READER_IDLE) {
            session.close(true);
        }
    }

    public void write (IoSession session, byte[] bytes)
    {
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
        	log.debug ("messageReceived caught", e);
        	e.printStackTrace();
        	session.close(true);
        } 
    }
}
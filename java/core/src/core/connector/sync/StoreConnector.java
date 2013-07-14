/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.connector.sync;

import java.util.List;

import core.connector.ConnectorException;
import core.connector.FileInfo;

public interface StoreConnector 
{	
	public void open () throws ConnectorException;
	public void close () throws ConnectorException;
	
	List<FileInfo> listDirectory (String path) throws ConnectorException;
	void createDirectory (String path) throws ConnectorException;
	
	byte[] get (String path) throws ConnectorException;
	byte[] get (String path, long size) throws ConnectorException;

	void put (String path, byte[] contents) throws ConnectorException;
	void move (String from, String to) throws ConnectorException;
	void delete (String path) throws ConnectorException;
	
	public boolean ensureDirectories (String ... folders);
}

/**
 * Author: Timothy Prepscius
 * License: GPLv3 Affero + keep my name in the code!
 */

package core.connector.s3.sync;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import core.connector.ConnectorException;
import core.connector.FileInfo;
import core.connector.s3.ClientInfoS3;
import core.connector.sync.StoreConnector;
import core.util.Streams;


public class S3Connector implements StoreConnector 
{
	AmazonS3 s3;
	ClientInfoS3 info;
	
	public S3Connector (ClientInfoS3 info)
	{
		this.info = info;
	}
	
	public void open () throws ConnectorException
	{
		try
		{
			s3 = new AmazonS3Client(new SimpleAWSCredentials(info.getAccessId(), info.getSecretKey()));
			s3.setEndpoint(info.getBucketEndpoint());
		}
		catch (Exception e)
		{
			throw new ConnectorException(e);
		}
	}
	
	public void close () 
	{
		s3 = null;
	}
	
	@Override
	public List<FileInfo> listDirectory(String path) throws ConnectorException
	{
		try
		{
			ObjectListing bucketListing = 
				s3.listObjects(
				    	new ListObjectsRequest()
				    		.withBucketName(info.getBucketName())
				    		.withPrefix(path)
				    );
	
			List<FileInfo> listing = new ArrayList<FileInfo>(bucketListing.getObjectSummaries().size());
			
			boolean finished = false;
			while (!finished)
			{
				for (S3ObjectSummary s3s : bucketListing.getObjectSummaries())
			    {
			    	String key = s3s.getKey();
			    	long size = s3s.getSize();
			    	Date date = s3s.getLastModified();
			    	
			    	listing.add(new FileInfo(key, key.substring(path.length()+1), size, date, s3s.getETag()));
			    }
				if (bucketListing.isTruncated())
					bucketListing = s3.listNextBatchOfObjects(bucketListing);
				else
					finished = true;
			}
			
			return listing;
		}
		catch (Exception e)
		{
			throw new ConnectorException(e);
		}
	}

	@Override
	public void createDirectory(String path) throws ConnectorException
	{
		// no need in s3
	}

	@Override
	public byte[] get(String path, long size) throws ConnectorException
	{
		try
		{
			GetObjectRequest request = new GetObjectRequest(info.getBucketName(), path);
			if (size >= 0)
				request.withRange(0,  size);
			
			return Streams.readFullyBytes(s3.getObject(request).getObjectContent());
		}
		catch (Exception e)
		{
			throw new ConnectorException(e);
		}
	}
	
	@Override
	public byte[] get(String path) throws ConnectorException
	{
		return get(path, -1);
	}

	@Override
	public void put(String path, byte[] contents) throws ConnectorException
	{
		try
		{
			s3.putObject(info.getBucketName(), path, new ByteArrayInputStream(contents), null);
		}
		catch (Exception e)
		{
			throw new ConnectorException(e);
		}
	}

	@Override
	public void move(String from, String to) throws ConnectorException
	{
		try
		{
	    	s3.copyObject(info.getBucketName(), from, info.getBucketName(), to);
	    	s3.deleteObject(info.getBucketName(), from);
		}
		catch (Exception e)
		{
			throw new ConnectorException(e);
		}	    	
	}

	@Override
	public void delete(String path) throws ConnectorException
	{
		try
		{
	    	s3.deleteObject(info.getBucketName(), path);
		}
		catch (Exception e)
		{
			throw new ConnectorException(e);
		}
	}
	
	public boolean ensureDirectories (String ... folders)
	{
		for (String folder : folders)
		{
			try
			{
				createDirectory(folder);
			}
			catch (Exception e) 
			{ 
				System.out.format("Folder[%s] already exists.\n", folder);
			}
		}
		
		return true;
	}
}

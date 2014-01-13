package au.com.addstar.rcon;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;

import org.bukkit.plugin.Plugin;

public class RConThread extends Thread
{
	private ServerSocket mSocket;
	private int mPort;
	
	private ArrayList<RconConnectionThread> mConnections = new ArrayList<RconConnectionThread>();
	
	private Plugin mPlugin;
	
	public RConThread(int port, Plugin plugin)
	{
		mPort = port;
		mPlugin = plugin;
	}
	
	public List<RconConnection> getConnections()
	{
		ArrayList<RconConnection> connections = new ArrayList<RconConnection>();
		
		synchronized(mConnections)
		{
			for(RconConnectionThread connection : mConnections)
			{
				if(connection.getRCON() != null)
					connections.add(connection.getRCON());
			}
		}
		
		return connections;
	}
	
	private ServerSocket setupSocket()
	{
		try
		{
			KeyStore keys = KeyStore.getInstance("JKS");
			keys.load(mPlugin.getResource("keystore.jks"), "BetterRCon".toCharArray());
			
			KeyManagerFactory factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			factory.init(keys, "BetterRCon".toCharArray());
			SSLContext context = SSLContext.getInstance("SSL");
			context.init(factory.getKeyManagers(), null, null);
			SSLServerSocketFactory socketFactory = context.getServerSocketFactory();
			
			ServerSocket socket = socketFactory.createServerSocket();
			socket.bind(new InetSocketAddress(mPort));
			socket.setSoTimeout(30);
			return socket;
		}
		catch(IOException e)
		{
			System.err.println("Cannot start RCon listener. Could not bind socket:");
			e.printStackTrace();
		}
		catch ( KeyStoreException e )
		{
			e.printStackTrace();
		}
		catch ( NoSuchAlgorithmException e )
		{
			System.err.println("Cannot start RCon listener. Encryption method is not supported by this setup: " + e.getMessage());
		}
		catch ( CertificateException e )
		{
			e.printStackTrace();
		}
		catch ( KeyManagementException e )
		{
			e.printStackTrace();
		}
		catch ( UnrecoverableKeyException e )
		{
			e.printStackTrace();
		}
		
		return null;
	}
	
	@Override
	public void run()
	{
		mSocket = setupSocket();
		if(mSocket == null)
			return;
		
		System.out.println("Better RCon listening on port " + mPort);
		
		try
		{
			while(true)
			{
				try
				{
					Socket socket = mSocket.accept();
					RconConnectionThread conObj = new RconConnectionThread(socket, this);
					
					synchronized(mConnections)
					{
						mConnections.add(conObj);
					}
					
					conObj.start();
				}
				catch(SocketException e)
				{
					break;
				}
				catch(IOException e)
				{
				}
			}
		}
		finally
		{
			if(!mSocket.isClosed())
			{
				synchronized(mConnections)
				{
					for(RconConnectionThread thread : mConnections)
						thread.terminate();
					mConnections.clear();
				}
				
				try
				{
					mSocket.close();
				}
				catch(IOException e) 
				{
				}
			}
		}
	}
	
	public void terminate()
	{
		if(!isAlive())
			return;
		
		synchronized(mConnections)
		{
			for(RconConnectionThread thread : mConnections)
				thread.terminate();
			mConnections.clear();
		}
		
		try
		{
			mSocket.close();
		}
		catch(IOException e) {}
	}
	
	public void remove(RconConnectionThread thread)
	{
		synchronized(mConnections)
		{
			mConnections.remove(thread);
		}
	}
}

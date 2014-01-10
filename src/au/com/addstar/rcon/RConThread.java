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
				connections.add(connection.getRCON());
		}
		
		return connections;
	}
	
	@Override
	public void run()
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
			
			mSocket = socketFactory.createServerSocket();
			mSocket.bind(new InetSocketAddress(mPort));
			mSocket.setSoTimeout(30);
			System.out.println("Better RCon listening on port " + mPort);
		}
		catch(IOException e)
		{
			System.err.println("Cannot start RCon listener. Could not bind socket:");
			e.printStackTrace();
			return;
		}
		catch ( KeyStoreException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		catch ( NoSuchAlgorithmException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		catch ( CertificateException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		catch ( KeyManagementException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		catch ( UnrecoverableKeyException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		
		try
		{
			while(true)
			{
				try
				{
					Socket socket = mSocket.accept();
					System.out.println("RCON connection from " + socket.getInetAddress());
					RconConnection con = new RconConnection(socket);
					RconConnectionThread conObj = new RconConnectionThread(con, this);
					con.setThread(conObj);
					
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

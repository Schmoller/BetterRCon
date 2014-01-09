package au.com.addstar.rcon;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class RConThread extends Thread
{
	private ServerSocket mSocket;
	private int mPort;
	
	private ArrayList<RconConnectionThread> mConnections = new ArrayList<RconConnectionThread>();
	
	public RConThread(int port)
	{
		mPort = port;
	}
	
	@Override
	public void run()
	{
		try
		{
			mSocket = new ServerSocket();
			mSocket.bind(new InetSocketAddress(mPort));
			mSocket.setSoTimeout(30);
		}
		catch(IOException e)
		{
			System.err.println("Cannot start RCon listener. Could not bind socket:");
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
					RconConnectionThread conObj = new RconConnectionThread(new RconConnection(socket), this);
					synchronized(mConnections)
					{
						mConnections.add(conObj);
					}
					
					conObj.start();
				}
				catch(IOException e)
				{
				}
			}
		}
		finally
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
			catch(IOException e) {}
			finally
			{
				mSocket = null;
			}
		}
	}
	
	public void remove(RconConnectionThread thread)
	{
		synchronized(mConnections)
		{
			mConnections.remove(thread);
		}
	}
}

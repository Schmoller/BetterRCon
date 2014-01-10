package au.com.addstar.rcon;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class RConThread extends Thread
{
	private ServerSocket mSocket;
	private int mPort;
	
	private ArrayList<RconConnectionThread> mConnections = new ArrayList<RconConnectionThread>();
	
	public RConThread(int port)
	{
		mPort = port;
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
			mSocket = new ServerSocket();
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

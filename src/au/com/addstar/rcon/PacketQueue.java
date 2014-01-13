package au.com.addstar.rcon;

import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.Map.Entry;

import au.com.addstar.rcon.packets.RConPacket;

public class PacketQueue extends Thread
{
	private ArrayDeque<Entry<RConPacket, RconConnection>> mQueue = new ArrayDeque<Entry<RConPacket, RconConnection>>();
	private Object mWaitObj = new Object();
	
	public void queueSend(RConPacket packet, RconConnection connection)
	{
		synchronized(mQueue)
		{
			mQueue.add(new AbstractMap.SimpleEntry<RConPacket, RconConnection>(packet, connection));
		}
		
		synchronized(mWaitObj)
		{
			mWaitObj.notifyAll();
		}
	}
	
	@Override
	public void run()
	{
		try
		{
			while(true)
			{
				Entry<RConPacket, RconConnection> next;
				
				synchronized(mQueue)
				{
					next = mQueue.poll();
				}
				
				if(next == null)
				{
					synchronized(mWaitObj)
					{
						mWaitObj.wait();
					}
					continue;
				}
				
				Socket socket = next.getValue().getThread().getSocket();
				
				if(socket.isClosed())
					continue;
				
				try
				{
					DataOutputStream stream = new DataOutputStream(socket.getOutputStream());
					next.getKey().write(stream);
					stream.flush();
				}
				catch(EOFException e) 
				{
					next.getValue().getThread().terminate();
				}
				catch(IOException e)
				{
					e.printStackTrace();
					next.getValue().getThread().terminate();
				}
				
			}
		}
		catch(InterruptedException e)
		{
		}
	}
}

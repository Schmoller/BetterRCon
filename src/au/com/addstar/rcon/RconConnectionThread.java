package au.com.addstar.rcon;

import java.io.DataInputStream;
import java.io.IOException;

import au.com.addstar.rcon.packets.RConPacket;

public class RconConnectionThread extends Thread
{
	private RconConnection mCon;
	private RConThread mParent;
	public RconConnectionThread(RconConnection connection, RConThread parent)
	{
		mCon = connection;
		mParent = parent;
	}
	
	@Override
	public void run()
	{
		try
		{
			DataInputStream input = new DataInputStream(mCon.getSocket().getInputStream());
			
			while(true)
			{
				RConPacket packet = RConPacket.load(input);
				
				if(packet == null)
					break;
				
				mCon.handle(packet);
			}
			
		}
		catch(IOException e)
		{
			System.err.println("Error in connection " + mCon.getSocket().getInetAddress());
			e.printStackTrace();
		}
		finally
		{
			mParent.remove(this);
			mCon.close();
		}
	}
	
	public void terminate()
	{
		mCon.close();
		interrupt();
	}
}

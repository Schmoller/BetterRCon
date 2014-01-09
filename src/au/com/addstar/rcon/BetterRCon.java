package au.com.addstar.rcon;

import org.bukkit.plugin.java.JavaPlugin;

import au.com.addstar.rcon.packets.RConPacket;

public class BetterRCon extends JavaPlugin
{
	private RConThread mThread;
	private PacketQueue mQueue;
	
	private static BetterRCon instance;
	
	@Override
	public void onEnable()
	{
		instance = this;
		mThread = new RConThread(8000);
		mThread.start();
		mQueue = new PacketQueue();
		mQueue.start();
	}
	
	@Override
	public void onDisable()
	{
		mQueue.interrupt();
		try
		{
			mQueue.join();
		}
		catch ( InterruptedException e )
		{
		}
		
		mThread.terminate();
		try
		{
			mThread.join();
		}
		catch(InterruptedException e)
		{
		}
	}
	
	public static void sendPacket(RConPacket packet, RconConnection connection)
	{
		instance.mQueue.queueSend(packet, connection);
	}
	
	public static boolean isValid(String username, long passwordHash)
	{
		return true;
	}
}

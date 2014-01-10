package au.com.addstar.rcon;

import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.plugin.java.JavaPlugin;

import au.com.addstar.rcon.packets.RConPacket;

public class BetterRCon extends JavaPlugin
{
	private RConThread mThread;
	private PacketQueue mQueue;
	
	private static BetterRCon instance;
	
	private RConsoleAppender mAppender;
	
	@Override
	public void onEnable()
	{
		instance = this;
		mThread = new RConThread(8000);
		mThread.start();
		mQueue = new PacketQueue();
		mQueue.start();
		
		// Install custom RConsole appender
		Logger log = (Logger)LogManager.getRootLogger();
		for(Appender appender : log.getAppenders().values())
		{
			if(appender instanceof RConsoleAppender)
				log.removeAppender(appender);
		}
		
		mAppender = new RConsoleAppender(new DefaultConfiguration());
		mAppender.start();
		log.addAppender(mAppender);
	}
	
	@Override
	public void onDisable()
	{
		Logger log = (Logger)LogManager.getRootLogger();
		log.removeAppender(mAppender);
		mAppender = null;
		
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
	
	public static void broadcastMessage(String message)
	{
		for(RconConnection connection : instance.mThread.getConnections())
			connection.sendRawMessage(message);
	}
	
	public static boolean isValid(String username, long passwordHash)
	{
		return true;
	}
	
	private static CommandMap mCommandMap = null;
	
	public static CommandMap getCommandMap()
	{
		if(mCommandMap != null)
			return mCommandMap;
		
		try
		{
			Method method = Bukkit.getServer().getClass().getMethod("getCommandMap");
			mCommandMap = (CommandMap)method.invoke(Bukkit.getServer());
		}
		catch(Exception e)
		{
			throw new RuntimeException(e);
		}
		return mCommandMap;
	}
	
}

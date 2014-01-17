package au.com.addstar.rcon;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.plugin.java.JavaPlugin;

import au.com.addstar.rcon.auth.AuthManager;
import au.com.addstar.rcon.auth.User;
import au.com.addstar.rcon.commands.AccountCommand;
import au.com.addstar.rcon.commands.GroupCommand;
import au.com.addstar.rcon.commands.PasswordCommand;
import au.com.addstar.rcon.commands.RootCommandDispatcher;
import au.com.addstar.rcon.compat.CommandHelperCompat;
import au.com.addstar.rcon.packets.RConPacket;

public class BetterRCon extends JavaPlugin
{
	private RConThread mThread;
	private PacketQueue mQueue;
	
	private static BetterRCon instance;
	
	private RConsoleAppender mAppender;
	private AuthManager mAuth;
	
	private Config mConfig;
	
	@Override
	public void onEnable()
	{
		instance = this;
		
		getDataFolder().mkdirs();
		
		if(!loadConfig())
		{
			die();
			return;
		}
		
		if(!loadAuth())
		{
			die();
			return;
		}
		
		if(!loadConnectionThread())
		{
			die();
			return;
		}
		
		if(!loadLogAppender())
		{
			die();
			return;
		}
		
		loadCommands();
		loadCompat();
	}
	
	private void die()
	{
		setEnabled(false);
		instance = null;
	}
	
	private boolean loadConfig()
	{
		mConfig = new Config(new File(getDataFolder(), "config.yml"));
	
		if(mConfig.load())
		{
			mConfig.save();
			return true;
		}
		
		return false;
	}
	
	private boolean loadAuth()
	{
		File authFile = new File(getDataFolder(), "auth.yml");
		
		if(!authFile.exists())
			saveResource("auth.yml", false);
		
		mAuth = new AuthManager(new File(getDataFolder(), "auth.yml"), this);
		try
		{
			mAuth.read();
		}
		catch(InvalidConfigurationException e)
		{
			getLogger().severe("Bad value in auth.yml: " + e.getMessage());
			return false;
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return false;
		}
		
		User consoleUser = mAuth.getUser("Console");
		if(consoleUser.getPassword() == null)
			getLogger().severe("ATTENTION! No password for the Console account has been set! Please user '/rcon password <password>' to set it.");
		
		return true;
	}
	
	private boolean loadConnectionThread()
	{
		mThread = new RConThread(mConfig.port, this);
		mThread.start();
		mQueue = new PacketQueue();
		mQueue.start();
		
		return true;
	}
	
	private boolean loadLogAppender()
	{
		Logger log = (Logger)LogManager.getRootLogger();
		for(Appender appender : log.getAppenders().values())
		{
			if(appender instanceof RConsoleAppender)
				log.removeAppender(appender);
		}
		
		mAppender = new RConsoleAppender(new DefaultConfiguration());
		mAppender.start();
		log.addAppender(mAppender);
		
		return true;
	}
	
	private void loadCommands()
	{
		RootCommandDispatcher dispatch = new RootCommandDispatcher("rcon", "Allows you to manage accounts/connections for the rcon");
		dispatch.registerCommand(new PasswordCommand());
		dispatch.registerCommand(new AccountCommand());
		dispatch.registerCommand(new GroupCommand());
		
		dispatch.registerAs(getCommand("rcon"));
	}
	
	private void loadCompat()
	{
		if(Bukkit.getPluginManager().isPluginEnabled("CommandHelper"))
			CommandHelperCompat.intitialize(this);
	}
	
	@Override
	public void onDisable()
	{
		if(mAppender != null)
		{
			Logger log = (Logger)LogManager.getRootLogger();
			log.removeAppender(mAppender);
			mAppender = null;
		}
		
		if(mQueue != null)
		{
			mQueue.interrupt();
			try
			{
				mQueue.join();
			}
			catch ( InterruptedException e )
			{
			}
		}
		
		if(mThread != null)
		{
			mThread.terminate();
			try
			{
				mThread.join();
			}
			catch(InterruptedException e)
			{
			}
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

	public static void sendLog( LogEvent event )
	{
		try
		{
			for(RconConnection connection : instance.mThread.getConnections())
			{
				if(connection.isSilent())
					continue;
				
				if(connection.noFormat())
					connection.sendRawMessage(event.getMessage().getFormattedMessage());
				else
					connection.sendRawMessage(new String(RConsoleAppender.layout.toByteArray(event), "UTF-8"));
			}
		}
		catch ( UnsupportedEncodingException e )
		{
		}
	}
	
	public static java.util.logging.Logger getLog()
	{
		return instance.getLogger();
	}
	
	public static AuthManager getAuth()
	{
		return instance.mAuth;
	}
	
	public static void runSync(Runnable runnable)
	{
		Bukkit.getScheduler().runTask(instance, runnable);
	}

	public static List<RconConnection> getAllConnections()
	{
		return instance.mThread.getConnections();
	}
}

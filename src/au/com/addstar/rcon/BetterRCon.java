package au.com.addstar.rcon;

import org.bukkit.plugin.java.JavaPlugin;

public class BetterRCon extends JavaPlugin
{
	private RConThread mThread;
	
	@Override
	public void onEnable()
	{
		mThread = new RConThread(8000);
		mThread.start();
	}
	
	@Override
	public void onDisable()
	{
		mThread.interrupt();
	}
}

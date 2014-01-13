package au.com.addstar.rcon.events;

import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RconCommandPreprocessEvent extends Event implements Cancellable
{
	private static final HandlerList handlers = new HandlerList();
	
	private RemoteConsoleCommandSender mSender;
	private String mMessage;
	private boolean mCancelled;
	
	public RconCommandPreprocessEvent(RemoteConsoleCommandSender sender, String message)
	{
		mSender = sender;
		mMessage = message;
	}
	
	public RemoteConsoleCommandSender getSender()
	{
		return mSender;
	}
	
	public String getMessage()
	{
		return mMessage;
	}
	
	public void setMessage(String message)
	{
		mMessage = message;
	}
	
	@Override
	public boolean isCancelled()
	{
		return mCancelled;
	}
	
	@Override
	public void setCancelled( boolean cancel )
	{
		mCancelled = cancel;
	}
	
	@Override
	public HandlerList getHandlers()
	{
		return handlers;
	}
	
	public static HandlerList getHandlerList()
	{
		return handlers;
	}

}

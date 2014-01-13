package au.com.addstar.rcon.compat;

import java.util.ArrayList;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import com.laytonsmith.abstraction.MCCommandSender;
import com.laytonsmith.abstraction.bukkit.BukkitMCCommandSender;
import com.laytonsmith.core.InternalException;
import com.laytonsmith.core.Script;
import com.laytonsmith.core.Static;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;

import au.com.addstar.rcon.events.RconCommandPreprocessEvent;

public class CommandHelperCompat implements Listener
{
	@EventHandler(priority=EventPriority.LOW, ignoreCancelled=true)
	private void onCommand(RconCommandPreprocessEvent event)
	{
		MCCommandSender sender = new BukkitMCCommandSender(new WrappedCommandSender(event.getSender()));
		boolean match = false;
		try
		{
			match = Static.getAliasCore().alias("/" + event.getMessage(), sender, new ArrayList<Script>());
		}
		catch(InternalException e)
		{
			
		}
		catch(ConfigRuntimeException e)
		{
			
		}
		catch(Throwable e)
		{
			event.getSender().sendMessage(ChatColor.RED + "An internal error occured while executing that command.");
			e.printStackTrace();
			return;
		}
		
		if(match)
			event.setMessage("commandhelper null");
	}
	
	public static void intitialize(Plugin plugin)
	{
		Bukkit.getPluginManager().registerEvents(new CommandHelperCompat(), plugin);
	}
	
	private static class WrappedCommandSender implements CommandSender
	{
		private CommandSender mSender;
		public WrappedCommandSender(CommandSender sender)
		{
			mSender = sender;
		}
		@Override
		public PermissionAttachment addAttachment( Plugin plugin )
		{
			return mSender.addAttachment(plugin);
		}
		@Override
		public PermissionAttachment addAttachment( Plugin plugin, int ticks )
		{
			return mSender.addAttachment(plugin, ticks);
		}
		@Override
		public PermissionAttachment addAttachment( Plugin plugin, String name, boolean value )
		{
			return mSender.addAttachment(plugin, name, value);
		}
		@Override
		public PermissionAttachment addAttachment( Plugin plugin, String name, boolean value, int ticks )
		{
			return mSender.addAttachment(plugin, name, value, ticks);
		}
		@Override
		public Set<PermissionAttachmentInfo> getEffectivePermissions()
		{
			return mSender.getEffectivePermissions();
		}
		@Override
		public boolean hasPermission( String name )
		{
			return mSender.hasPermission(name);
		}
		@Override
		public boolean hasPermission( Permission perm )
		{
			return mSender.hasPermission(perm);
		}
		@Override
		public boolean isPermissionSet( String name )
		{
			return mSender.isPermissionSet(name);
		}
		@Override
		public boolean isPermissionSet( Permission perm )
		{
			return mSender.isPermissionSet(perm);
		}
		@Override
		public void recalculatePermissions()
		{
			mSender.recalculatePermissions();
		}
		@Override
		public void removeAttachment( PermissionAttachment attachment )
		{
			mSender.removeAttachment(attachment);
		}
		@Override
		public boolean isOp()
		{
			return mSender.isOp();
		}
		@Override
		public void setOp( boolean value )
		{
			mSender.setOp(value);
		}
		@Override
		public String getName()
		{
			return "~" + mSender.getName();
		}
		@Override
		public Server getServer()
		{
			return mSender.getServer();
		}
		@Override
		public void sendMessage( String message )
		{
			mSender.sendMessage(message);
		}
		@Override
		public void sendMessage( String[] messages )
		{
			mSender.sendMessage(messages);
		}
	}
}

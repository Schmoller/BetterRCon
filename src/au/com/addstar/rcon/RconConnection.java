package au.com.addstar.rcon;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.permissions.PermissibleBase;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import au.com.addstar.rcon.auth.User;
import au.com.addstar.rcon.events.RconCommandPreprocessEvent;
import au.com.addstar.rcon.packets.PacketCommand;
import au.com.addstar.rcon.packets.PacketLogin;
import au.com.addstar.rcon.packets.PacketMessage;
import au.com.addstar.rcon.packets.PacketTabComplete;
import au.com.addstar.rcon.packets.PacketTabCompleteRequest;
import au.com.addstar.rcon.packets.RConPacket;

public class RconConnection implements RemoteConsoleCommandSender
{
	private RconConnectionThread mThread;
	private PermissibleBase perm = new PermissibleBase(this);
	
	private String format = "[%d %l]: %m";
	private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	
	private String mUsername;
	private boolean mNoFormat;
	private boolean mSilent;
	
	private User mUser;
	private PermissionAttachment mAttachment;
	
	public RconConnection(PacketLogin packet, RconConnectionThread thread)
	{
		mUsername = packet.username;
		mNoFormat = packet.noFormat;
		mSilent = packet.silentMode;
		mThread = thread;
	}
	
	public void setUser(User user)
	{
		mUser = user;
	}
	
	public User getUser()
	{
		return mUser;
	}
	
	public RconConnectionThread getThread()
	{
		return mThread;
	}
	
	public boolean isSilent()
	{
		return mSilent;
	}
	
	public boolean noFormat()
	{
		return mNoFormat;
	}
	
	public void send(RConPacket packet)
	{
		BetterRCon.sendPacket(packet, this);
	}
	
	@Override
	public String getName()
	{
		return mUsername;
	}

	@Override
	public Server getServer()
	{
		return Bukkit.getServer();
	}

	@Override
	public void sendMessage( String message )
	{
		if(!mNoFormat)
		{
			String date = dateFormat.format(System.currentTimeMillis());
			String formatted = format.replaceAll("\\%d", date);
			formatted = formatted.replaceAll("\\%l", "INFO");
			formatted = formatted.replaceAll("\\%m", message);
			message = formatted;
		}
				
		send(new PacketMessage(message));
	}
	
	public void sendRawMessage(String message)
	{
		send(new PacketMessage(message));
	}

	@Override
	public void sendMessage( String[] messages )
	{
		for(String message : messages)
			sendMessage(message);
	}

	@Override
	public PermissionAttachment addAttachment( Plugin plugin )
	{
		return perm.addAttachment(plugin);
	}

	@Override
	public PermissionAttachment addAttachment( Plugin plugin, int ticks )
	{
		return perm.addAttachment(plugin, ticks);
	}

	@Override
	public PermissionAttachment addAttachment( Plugin plugin, String name, boolean value )
	{
		return perm.addAttachment(plugin, name, value);
	}

	@Override
	public PermissionAttachment addAttachment( Plugin plugin, String name, boolean value, int ticks )
	{
		return perm.addAttachment(plugin, name, value, ticks);
	}

	@Override
	public Set<PermissionAttachmentInfo> getEffectivePermissions()
	{
		return perm.getEffectivePermissions();
	}

	@Override
	public boolean hasPermission( String permission )
	{
		return perm.hasPermission(permission);
	}

	@Override
	public boolean hasPermission( Permission permission )
	{
		return perm.hasPermission(permission);
	}

	@Override
	public boolean isPermissionSet( String permission )
	{
		return perm.isPermissionSet(permission);
	}

	@Override
	public boolean isPermissionSet( Permission permission )
	{
		return perm.isPermissionSet(permission);
	}

	@Override
	public void recalculatePermissions()
	{
		perm.recalculatePermissions();
	}

	@Override
	public void removeAttachment( PermissionAttachment attachment )
	{
		perm.removeAttachment(attachment);
	}

	@Override
	public boolean isOp()
	{
		return mUser == null ? true : mUser.isOp();
	}

	@Override
	public void setOp( boolean op )
	{
		if(mUser == null)
			throw new UnsupportedOperationException("Cannot change operator status of " + getName());
		
		mUser.setOp(op);
	}
	
	public void close()
	{
		perm.clearPermissions();
	}
	
	public void handle(RConPacket packet)
	{
		if(packet instanceof PacketCommand)
			handleCommand((PacketCommand)packet);
		else if(packet instanceof PacketTabCompleteRequest)
			handleTabComplete((PacketTabCompleteRequest)packet);
		
	}
	
	private void handleCommand(final PacketCommand packet)
	{
		BetterRCon.runSync(new Runnable()
		{
			@Override
			public void run()
			{
				RconCommandPreprocessEvent event = new RconCommandPreprocessEvent(RconConnection.this, packet.command);
				Bukkit.getPluginManager().callEvent(event);
				
				if(!event.isCancelled())
					Bukkit.dispatchCommand(RconConnection.this, event.getMessage());
			}
		});
		
	}
	
	private void handleTabComplete(PacketTabCompleteRequest packet)
	{
		List<String> results = BetterRCon.getCommandMap().tabComplete(this, packet.request);
		send(new PacketTabComplete(results));
	}
	
	public PermissionAttachment getPermissions(Plugin plugin)
	{
		if(mAttachment != null)
			return mAttachment;
		
		return (mAttachment = perm.addAttachment(plugin)); 
	}
	
	public void removePermissions()
	{
		if(mAttachment != null)
			perm.removeAttachment(mAttachment);
		mAttachment = null;
	}
}

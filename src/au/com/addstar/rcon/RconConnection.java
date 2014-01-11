package au.com.addstar.rcon;

import java.io.IOException;
import java.net.Socket;
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

import au.com.addstar.rcon.packets.PacketCommand;
import au.com.addstar.rcon.packets.PacketLogin;
import au.com.addstar.rcon.packets.PacketMessage;
import au.com.addstar.rcon.packets.PacketTabComplete;
import au.com.addstar.rcon.packets.PacketTabCompleteRequest;
import au.com.addstar.rcon.packets.RConPacket;

public class RconConnection implements RemoteConsoleCommandSender
{
	private Socket mSocket;
	private RconConnectionThread mThread;
	private PermissibleBase perm = new PermissibleBase(this);
	
	private String format = "[%d %l]: %m";
	private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	
	private String mUsername;
	private boolean mNoFormat;
	private boolean mSilent;
	
	private boolean mHasLoggedIn = false;
	
	public RconConnection(Socket socket)
	{
		mSocket = socket;
	}
	
	public Socket getSocket()
	{
		return mSocket;
	}
	
	public RconConnectionThread getThread()
	{
		return mThread;
	}
	public void setThread(RconConnectionThread thread)
	{
		mThread = thread;
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
		if(!mSocket.isClosed() && (mHasLoggedIn || packet instanceof PacketLogin))
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
		return true;
	}

	@Override
	public void setOp( boolean op )
	{
		throw new UnsupportedOperationException("Cannot change operator status of remote console");
	}
	
	public void close()
	{
		try
		{
			mSocket.close();
		}
		catch(IOException e) {}
		
		perm.clearPermissions();
	}
	
	public void handle(RConPacket packet)
	{
		if(packet instanceof PacketLogin)
			handleLogin((PacketLogin)packet);
		else if(!mHasLoggedIn)
			mThread.terminate();
		else if(packet instanceof PacketCommand)
			handleCommand((PacketCommand)packet);
		else if(packet instanceof PacketTabCompleteRequest)
			handleTabComplete((PacketTabCompleteRequest)packet);
		
	}
	
	private void handleLogin(PacketLogin packet)
	{
		if(BetterRCon.isValid(packet.username, packet.passwordHash))
		{
			mUsername = packet.username;
			mNoFormat = packet.noFormat;
			mSilent = packet.silentMode;
			
			if(!mSilent)
				BetterRCon.getLog().info(String.format("%s logged in from %s", mUsername, mSocket.getInetAddress()));
			
			mHasLoggedIn = true;
			send(new PacketLogin(packet.username, 0, false, false));
		}
		else
		{
			send(new PacketLogin("", 0, false, false));
			close();
		}
	}
	
	private void handleCommand(PacketCommand packet)
	{
		Bukkit.dispatchCommand(this, packet.command);
	}
	
	private void handleTabComplete(PacketTabCompleteRequest packet)
	{
		List<String> results = BetterRCon.getCommandMap().tabComplete(this, packet.request);
		send(new PacketTabComplete(results));
	}
}

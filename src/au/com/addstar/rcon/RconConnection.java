package au.com.addstar.rcon;

import java.io.IOException;
import java.net.Socket;
import java.util.Set;

import org.bukkit.Server;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.plugin.Plugin;

import au.com.addstar.rcon.packets.RConPacket;

public class RconConnection implements RemoteConsoleCommandSender
{
	private Socket mSocket;
	
	public RconConnection(Socket socket)
	{
		mSocket = socket;
	}
	
	public Socket getSocket()
	{
		return mSocket;
	}
	
	@Override
	public String getName()
	{
		return null;
	}

	@Override
	public Server getServer()
	{
		return null;
	}

	@Override
	public void sendMessage( String message )
	{
		
	}

	@Override
	public void sendMessage( String[] messages )
	{
	}

	@Override
	public PermissionAttachment addAttachment( Plugin plugin )
	{
		return null;
	}

	@Override
	public PermissionAttachment addAttachment( Plugin plugin, int arg1 )
	{
		return null;
	}

	@Override
	public PermissionAttachment addAttachment( Plugin plugin, String arg1, boolean arg2 )
	{
		return null;
	}

	@Override
	public PermissionAttachment addAttachment( Plugin plugin, String arg1, boolean arg2, int arg3 )
	{
		return null;
	}

	@Override
	public Set<PermissionAttachmentInfo> getEffectivePermissions()
	{
		return null;
	}

	@Override
	public boolean hasPermission( String permission )
	{
		return true;
	}

	@Override
	public boolean hasPermission( Permission permission )
	{
		return true;
	}

	@Override
	public boolean isPermissionSet( String permission )
	{
		return false;
	}

	@Override
	public boolean isPermissionSet( Permission permission )
	{
		return false;
	}

	@Override
	public void recalculatePermissions()
	{
	}

	@Override
	public void removeAttachment( PermissionAttachment attachment )
	{
	}

	@Override
	public boolean isOp()
	{
		return false;
	}

	@Override
	public void setOp( boolean op )
	{
	}
	
	public void close()
	{
		try
		{
			mSocket.close();
		}
		catch(IOException e) {}
	}
	
	public void handle(RConPacket packet)
	{
		
	}
}

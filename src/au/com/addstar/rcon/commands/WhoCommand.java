package au.com.addstar.rcon.commands;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import au.com.addstar.rcon.BetterRCon;
import au.com.addstar.rcon.RconConnection;

public class WhoCommand implements ICommand
{

	@Override
	public String getName()
	{
		return "who";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] {"list"};
	}

	@Override
	public String getPermission()
	{
		return "rcon.manage.who";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label;
	}

	@Override
	public String getDescription()
	{
		return "Checks who has logged in";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player, CommandSenderType.Console);
	}

	@Override
	public boolean onCommand( CommandSender sender, String label, String[] args )
	{
		if(args.length != 0)
			return false;
		
		String list = "";
		
		List<RconConnection> connections = BetterRCon.getAllConnections();
		for(RconConnection connection : connections)
		{
			if(!list.isEmpty())
				list += ChatColor.WHITE + ", ";
			list += ChatColor.WHITE + connection.getName() + ChatColor.GRAY + String.format("[%s]", connection.getThread().getSocket().getInetAddress());
		}
		
		sender.sendMessage(ChatColor.GOLD + String.format("There are %d connections.", connections.size()));
		sender.sendMessage(list);
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String label, String[] args )
	{
		return null;
	}

}

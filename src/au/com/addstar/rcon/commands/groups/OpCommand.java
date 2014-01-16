package au.com.addstar.rcon.commands.groups;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import au.com.addstar.rcon.BetterRCon;
import au.com.addstar.rcon.auth.Group;
import au.com.addstar.rcon.auth.AuthManager;
import au.com.addstar.rcon.commands.CommandSenderType;
import au.com.addstar.rcon.commands.ICommand;

public class OpCommand implements ICommand
{
	@Override
	public String getName()
	{
		return "setop";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] { "op" };
	}

	@Override
	public String getPermission()
	{
		return "rcon.groups.setop";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " <group> <isOP>";
	}

	@Override
	public String getDescription()
	{
		return "Sets whether the group is op";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player, CommandSenderType.Console);
	}

	@Override
	public boolean onCommand( CommandSender sender, String label, String[] args )
	{
		if(args.length != 2)
			return false;
		
		AuthManager manager = BetterRCon.getAuth();
		
		Group group = manager.getGroup(args[0]);
		
		if(group == null)
		{
			sender.sendMessage(ChatColor.RED + "There is no group by that name.");
			return true;
		}
		
		boolean op = Boolean.parseBoolean(args[1]);
		group.setOp(op);
		
		manager.recalculatePerms(group);
		
		if(op)
			sender.sendMessage(ChatColor.GREEN + "The group " + group.getName() + " is now OP by default.");
		else
			sender.sendMessage(ChatColor.GREEN + "The group " + group.getName() + " is now NOT OP by default.");
		
		try
		{
			manager.write();
		}
		catch(IOException e)
		{
			sender.sendMessage(ChatColor.RED + "WARNING! Auth file failed to save. Changes will NOT be perminent.");
			e.printStackTrace();
		}
		
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String label, String[] args )
	{
		return null;
	}

}

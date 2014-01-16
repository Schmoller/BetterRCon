package au.com.addstar.rcon.commands.groups;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import au.com.addstar.rcon.BetterRCon;
import au.com.addstar.rcon.auth.AuthManager;
import au.com.addstar.rcon.auth.Group;
import au.com.addstar.rcon.commands.CommandSenderType;
import au.com.addstar.rcon.commands.ICommand;

public class ParentCommand implements ICommand
{

	@Override
	public String getName()
	{
		return "parent";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "rcon.groups.parent";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " <groupName> [parent]";
	}

	@Override
	public String getDescription()
	{
		return "Sets or clears the parent group";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player, CommandSenderType.Console);
	}

	@Override
	public boolean onCommand( CommandSender sender, String label, String[] args )
	{
		if(args.length != 1 && args.length != 2)
			return false;
		
		AuthManager manager = BetterRCon.getAuth();
		
		Group group = manager.getGroup(args[0]);
		
		if(group == null)
		{
			sender.sendMessage(ChatColor.RED + "There is no group by that name.");
			return true;
		}
		
		if(args.length == 1)
		{
			// Clear parent
			group.setParent(null);
			sender.sendMessage("Group parent cleared");
		}
		else
		{
			Group other = manager.getGroup(args[1]);
			if(other == null)
			{
				sender.sendMessage(ChatColor.RED + "There is no group by the name of " + args[1]);
				return true;
			}
			
			group.setParent(args[1]);
			sender.sendMessage("Group parent changed to " + args[1]);
		}
		
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

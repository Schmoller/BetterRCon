package au.com.addstar.rcon.commands.accounts;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import au.com.addstar.rcon.BetterRCon;
import au.com.addstar.rcon.auth.AuthManager;
import au.com.addstar.rcon.auth.Group;
import au.com.addstar.rcon.auth.User;
import au.com.addstar.rcon.commands.CommandSenderType;
import au.com.addstar.rcon.commands.ICommand;

public class SetGroupCommand implements ICommand
{

	@Override
	public String getName()
	{
		return "setgroup";
	}

	@Override
	public String[] getAliases()
	{
		return new String[] {"group"};
	}

	@Override
	public String getPermission()
	{
		return "rcon.accounts.setgroup";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " <user> [groupName]";
	}

	@Override
	public String getDescription()
	{
		return "Sets or clears the users group";
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
		User user = manager.getUser(args[0]);
		
		if(user == null)
		{
			sender.sendMessage(ChatColor.RED + "No user by that name exists");
			return true;
		}
		
		if(args.length == 1)
		{
			user.setGroup(null);
			sender.sendMessage(ChatColor.GREEN + "Group cleared");
		}
		else
		{
			Group group = manager.getGroup(args[1]);
			if(group == null)
			{
				sender.sendMessage(ChatColor.RED + "No group by that name exists");
				return true;
			}
			
			user.setGroup(group.getName());
			sender.sendMessage(ChatColor.GREEN + "Group set to " + group.getName());
		}
		
		manager.recalculatePerms(user);
		
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

package au.com.addstar.rcon.commands.accounts;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import au.com.addstar.rcon.BetterRCon;
import au.com.addstar.rcon.auth.AuthManager;
import au.com.addstar.rcon.auth.User;
import au.com.addstar.rcon.commands.CommandSenderType;
import au.com.addstar.rcon.commands.ICommand;

public class AddPermCommand implements ICommand
{

	@Override
	public String getName()
	{
		return "addperm";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "rcon.accounts.addperm";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " <user> <permission>";
	}

	@Override
	public String getDescription()
	{
		return "Adds a permission to the user";
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
		
		User user = manager.getUser(args[0]);
		
		if(user == null)
		{
			sender.sendMessage(ChatColor.RED + "No user by that name exists");
			return true;
		}
		
		String perm = args[1];
		
		if(perm.startsWith("-"))
			user.addPerm(perm.substring(1), true);
		else
			user.addPerm(perm, false);
		
		manager.recalculatePerms(user);
		
		sender.sendMessage(ChatColor.GREEN + perm + " added to " + user.getName());
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String label, String[] args )
	{
		return null;
	}

}

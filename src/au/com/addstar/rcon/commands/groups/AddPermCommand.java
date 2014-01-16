package au.com.addstar.rcon.commands.groups;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import au.com.addstar.rcon.BetterRCon;
import au.com.addstar.rcon.auth.AuthManager;
import au.com.addstar.rcon.auth.Group;
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
		return "rcon.groups.addperm";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " <group> <permission>";
	}

	@Override
	public String getDescription()
	{
		return "Adds a permission to the group";
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
			sender.sendMessage(ChatColor.RED + "No group by that name exists");
			return true;
		}
		
		String perm = args[1];
		
		if(perm.startsWith("-"))
			group.addPerm(perm.substring(1), true);
		else
			group.addPerm(perm, false);
		
		manager.recalculatePerms(group);
		
		sender.sendMessage(ChatColor.GREEN + perm + " added to " + group.getName());
		return true;
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String label, String[] args )
	{
		return null;
	}

}

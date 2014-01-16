package au.com.addstar.rcon.commands.groups;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import au.com.addstar.rcon.BetterRCon;
import au.com.addstar.rcon.commands.CommandSenderType;
import au.com.addstar.rcon.commands.ICommand;

public class RemoveCommand implements ICommand
{

	@Override
	public String getName()
	{
		return "remove";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "rcon.groups.add";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " <groupName>";
	}

	@Override
	public String getDescription()
	{
		return "Removes a group";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player, CommandSenderType.Console);
	}

	@Override
	public boolean onCommand( CommandSender sender, String label, String[] args )
	{
		if(args.length != 1)
			return false;
		
		try
		{
			BetterRCon.getAuth().removeGroup(args[0]);
			sender.sendMessage(String.format("Group %s was successfully removed.", args[0]));
			
			BetterRCon.getAuth().write();
		}
		catch(IllegalArgumentException e)
		{
			sender.sendMessage(ChatColor.RED + e.getMessage());
		}
		catch(IOException e)
		{
			sender.sendMessage(ChatColor.RED + "An error occured while saving.");
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

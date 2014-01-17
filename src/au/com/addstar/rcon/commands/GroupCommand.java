package au.com.addstar.rcon.commands;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.command.CommandSender;

import au.com.addstar.rcon.commands.groups.AddCommand;
import au.com.addstar.rcon.commands.groups.AddPermCommand;
import au.com.addstar.rcon.commands.groups.OpCommand;
import au.com.addstar.rcon.commands.groups.ParentCommand;
import au.com.addstar.rcon.commands.groups.RemoveCommand;
import au.com.addstar.rcon.commands.groups.RemovePermCommand;

public class GroupCommand extends CommandDispatcher implements ICommand
{
	public GroupCommand()
	{
		super("group", "Allows you to manage groups");
		
		registerCommand(new AddCommand());
		registerCommand(new AddPermCommand());
		registerCommand(new OpCommand());
		registerCommand(new ParentCommand());
		registerCommand(new RemoveCommand());
		registerCommand(new RemovePermCommand());
	}
	
	@Override
	public String getName()
	{
		return "group";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return null;
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return null;
	}

	@Override
	public String getDescription()
	{
		return null;
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player, CommandSenderType.Console);
	}

	@Override
	public boolean onCommand( CommandSender sender, String label, String[] args )
	{
		return dispatchCommand(sender, label, args);
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String label, String[] args )
	{
		return tabComplete(sender, label, args);
	}

}

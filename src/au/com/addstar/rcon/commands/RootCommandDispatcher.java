package au.com.addstar.rcon.commands;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;

public class RootCommandDispatcher extends CommandDispatcher implements CommandExecutor, TabCompleter
{
	public RootCommandDispatcher( String commandName, String description )
	{
		super(commandName, description);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
	{
		return dispatchCommand(sender, label, args);
	}
	
	@Override
	public List<String> onTabComplete( CommandSender sender, Command command, String label, String[] args )
	{
		return tabComplete(sender, label, args);
	}
	
	public void registerAs(PluginCommand command)
	{
		command.setExecutor(this);
		command.setTabCompleter(this);
	}
}

package au.com.addstar.rcon.commands;

import java.util.EnumSet;
import java.util.List;

import org.bukkit.command.CommandSender;

import au.com.addstar.rcon.commands.accounts.AddCommand;
import au.com.addstar.rcon.commands.accounts.AddPermCommand;
import au.com.addstar.rcon.commands.accounts.CopyCommand;
import au.com.addstar.rcon.commands.accounts.OpCommand;
import au.com.addstar.rcon.commands.accounts.RemoveCommand;
import au.com.addstar.rcon.commands.accounts.RemovePermCommand;
import au.com.addstar.rcon.commands.accounts.SetGroupCommand;

public class AccountCommand extends CommandDispatcher implements ICommand
{
	public AccountCommand()
	{
		super("account", "Allows you to manage rcon accounts");
		
		registerCommand(new AddCommand());
		registerCommand(new AddPermCommand());
		registerCommand(new CopyCommand());
		registerCommand(new OpCommand());
		registerCommand(new au.com.addstar.rcon.commands.accounts.PasswordCommand());
		registerCommand(new RemoveCommand());
		registerCommand(new RemovePermCommand());
		registerCommand(new SetGroupCommand());
	}
	
	@Override
	public String getName()
	{
		return "account";
	}

	@Override
	public String[] getAliases()
	{
		return null;
	}

	@Override
	public String getPermission()
	{
		return "rcon.account.manage";
	}

	@Override
	public String getUsageString( String label, CommandSender sender )
	{
		return label + " <options>";
	}

	@Override
	public String getDescription()
	{
		return "Allows you to manage rcon accounts";
	}

	@Override
	public EnumSet<CommandSenderType> getAllowedSenders()
	{
		return EnumSet.of(CommandSenderType.Player, CommandSenderType.Console);
	}

	@Override
	public boolean onCommand( CommandSender sender, String label, String[] args )
	{
		return super.dispatchCommand(sender, label, args);
	}

	@Override
	public List<String> onTabComplete( CommandSender sender, String label, String[] args )
	{
		return super.tabComplete(sender, label, args);
	}

}

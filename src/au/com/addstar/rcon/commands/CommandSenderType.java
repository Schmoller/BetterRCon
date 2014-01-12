package au.com.addstar.rcon.commands;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;

public enum CommandSenderType
{
	/**
	 * Can be Player
	 */
	Player,
	/**
	 * Can be BlockCommandSender
	 */
	Block,
	/**
	 * Can be ConsoleCommandSender OR RemoteConsoleCommandSender
	 */
	Console;
	
	public boolean matches(CommandSender sender)
	{
		switch(this)
		{
		case Player:
			return (sender instanceof org.bukkit.entity.Player);
		case Block:
			return (sender instanceof BlockCommandSender);
		case Console:
			return (sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender);
		default:
			return false;
		}
	}
	
	public static CommandSenderType from(CommandSender sender)
	{
		if(sender instanceof org.bukkit.entity.Player)
			return Player;
		if(sender instanceof BlockCommandSender)
			return Block;
		if(sender instanceof ConsoleCommandSender || sender instanceof RemoteConsoleCommandSender)
			return Console;
		
		return null;
	}
	
	@Override
	public String toString()
	{
		switch(this)
		{
		case Block:
			return "Command Block";
		case Console:
			return "Console";
		case Player:
			return "Player";
		default:
			return name();
		}
	}
}

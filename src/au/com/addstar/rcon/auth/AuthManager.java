package au.com.addstar.rcon.auth;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.Plugin;

import au.com.addstar.rcon.RconConnection;

public class AuthManager
{
	private File mAuthFile;
	private FileConfiguration mConfig;
	
	private HashMap<String, User> mUsers;
	private HashMap<String, Group> mGroups;
	
	private Plugin mPlugin;
	
	private Field mPermissions;
	
	public AuthManager(File authFile, Plugin plugin)
	{
		mAuthFile = authFile;
		mPlugin = plugin;
		
		mConfig = new YamlConfiguration();
		
		mUsers = new HashMap<String, User>();
		mGroups = new HashMap<String, Group>();
		
		try
		{
			mPermissions = PermissionAttachment.class.getDeclaredField("permissions");
			mPermissions.setAccessible(true);
		}
		catch(NoSuchFieldException e)
		{
			throw new RuntimeException("The Bukkit API has changed. This plugin needs updating!");
		}
	}
	
	public synchronized void read() throws InvalidConfigurationException, FileNotFoundException, IOException
	{
		mConfig.load(mAuthFile);
		
		mUsers.clear();
		mGroups.clear();
		
		ConfigurationSection userSection = mConfig.getConfigurationSection("users");
		ConfigurationSection groupSection = mConfig.getConfigurationSection("groups");
		
		if(userSection != null)
		{
			for(String key : userSection.getKeys(false))
			{
				if(!userSection.isConfigurationSection(key))
					continue;
				
				User user = new User(userSection.getConfigurationSection(key), this);
				mUsers.put(user.getName(), user);
			}
		}
		
		if(groupSection != null)
		{
			for(String key : groupSection.getKeys(false))
			{
				if(!groupSection.isConfigurationSection(key))
					continue;
				
				Group group = new Group(groupSection.getConfigurationSection(key), this);
				mGroups.put(group.getName(), group);
			}
		}
		
		try
		{
			// Check for cycles in groups
			for(Group group : mGroups.values())
			{
				HashSet<Group> visited = new HashSet<Group>();
				Group current = group;
				
				String cycleString = "";
				
				while(current != null)
				{
					if(visited.contains(current))
						throw new InvalidConfigurationException("A permission group cycle was detected: " + cycleString);
					
					visited.add(current);
					
					if(!cycleString.isEmpty())
						cycleString += " -> ";
					
					cycleString += current.getName();
					
					current = current.getParentGroup();
				}
			}
		}
		catch(InvalidConfigurationException e)
		{
			// Dont use bad data!
			mGroups.clear();
			mUsers.clear();
			throw e;
		}
	}
	
	public void write() throws IOException
	{
		mConfig.save(mAuthFile);
	}
	
	public Collection<Group> getGroups()
	{
		return Collections.unmodifiableCollection(mGroups.values());  
	}
	
	public Group getGroup( String name )
	{
		return mGroups.get(name);
	}
	
	public synchronized void loadPermissions(RconConnection connection)
	{
		PermissionAttachment attachment = connection.addAttachment(mPlugin);
		
		if(connection.getName().equals("Console"))
		{
			// Full perms. Do nothing
		}
		else
		{
			User user = mUsers.get(connection.getName());
			
			if(user == null)
				return;
			
			connection.setUser(user);

			HashSet<String> allPerms = new HashSet<String>();

			Group group = user.getGroupObject();
			
			while(group != null)
			{
				for(String name : group.getDefinedPermissions())
				{
					if(name.startsWith("-"))
					{
						allPerms.remove(name.substring(1));
						allPerms.add(name);
					}
					else
					{
						allPerms.remove("-" + name);
						allPerms.add(name);
					}
				}
				
				group = group.getParentGroup();
			}
			
			for(String name : user.getDefinedPermissions())
			{
				if(name.startsWith("-"))
				{
					allPerms.remove(name.substring(1));
					allPerms.add(name);
				}
				else
				{
					allPerms.remove("-" + name);
					allPerms.add(name);
				}
			}
			
			
			try
			{
				@SuppressWarnings( "unchecked" )
				Map<String, Boolean> permissions = (Map<String, Boolean>)mPermissions.get(attachment);
				
				for(String perm : allPerms)
				{
					if(perm.startsWith("-"))
						permissions.put(perm.substring(1).toLowerCase(), false);
					else
						permissions.put(perm.toLowerCase(), true);
				}
				
				connection.recalculatePermissions();
			}
			catch ( IllegalArgumentException e )
			{
			}
			catch ( IllegalAccessException e )
			{
			}
		}
	}
}

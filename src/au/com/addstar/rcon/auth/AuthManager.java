package au.com.addstar.rcon.auth;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayDeque;
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

import au.com.addstar.rcon.BetterRCon;
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
				
				if(user.getPassword() == null)
					mPlugin.getLogger().warning(String.format("User %s has no password! Account disabled until one is set.", key));

				mUsers.put(user.getName(), user);
			}
		}
		
		if(!mUsers.containsKey("Console"))
		{
			if(userSection == null)
				userSection = mConfig.createSection("users");
			
			User console = new User(userSection.createSection("Console"), this);
			console.setOp(true);
			mUsers.put("Console", console);
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
			
			write();
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
		if(mAuthFile.exists())
			Files.copy(mAuthFile.toPath(), new File(mAuthFile.getParentFile(), mAuthFile.getName() + ".old").toPath(), StandardCopyOption.REPLACE_EXISTING);

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
	
	public User getUser( String name )
	{
		return mUsers.get(name);
	}
	
	public User createUser( String name ) throws IllegalArgumentException
	{
		if(mUsers.containsKey(name))
			throw new IllegalArgumentException("Username already exists");
		
		if(!isValidName(name))
			throw new IllegalArgumentException("Username contains invalid characters");
		
		User user = new User(mConfig.createSection(name), this);
		mUsers.put(name, user);
		return user;
	}
	
	public void removeUser( String name ) throws IllegalArgumentException
	{
		if(!mUsers.containsKey(name))
			throw new IllegalArgumentException("That username does not exist");
		
		mConfig.set(name, null);
		mUsers.remove(name);
	}
	
	public Group createGroup( String name ) throws IllegalArgumentException
	{
		if(mGroups.containsKey(name))
			throw new IllegalArgumentException("Group already exists");
		
		if(!isValidName(name))
			throw new IllegalArgumentException("Group name contains invalid characters");
		
		Group group = new Group(mConfig.createSection(name), this);
		mGroups.put(name, group);
		return group;
	}
	
	public void removeGroup( String name ) throws IllegalArgumentException
	{
		if(!mGroups.containsKey(name))
			throw new IllegalArgumentException("There is no group by that name");
		
		Group group = mGroups.remove(name);
		
		for(Group other : mGroups.values())
		{
			if(other.getParent().equals(group.getName()))
				other.setParent(null);
		}
		
		for(User user : mUsers.values())
		{
			if(user.getGroup().equals(group.getName()))
				user.setGroup(null);
		}
	}
	
	
	public void attemptLogin(String username, char[] password) throws IllegalArgumentException, IllegalAccessException
	{
		User user = mUsers.get(username);
		if(user == null)
			throw new IllegalArgumentException();
		
		StoredPassword existingPassword = user.getPassword();
		if(existingPassword == null)
			throw new IllegalAccessException();
		
		if(!existingPassword.matches(password))
			throw new IllegalAccessException();
	}
	
	public synchronized void loadPermissions(RconConnection connection)
	{
		connection.removePermissions();
		PermissionAttachment attachment = connection.getPermissions(mPlugin);
		
		if(connection.getName().equals("Console"))
		{
			// So this account can remove user accounts
			attachment.setPermission("rcon.account.manage.remove", true);
			attachment.setPermission("rcon.account.manage.password.others", true);
			
			// Otherwise, full permissions
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
				
				// *** Forced permissions: ***
				// This one allows the connection to manage its password
				permissions.put("rcon.account.manage", true);
				
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
	
	
	private static String allowedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890_";
	public static boolean isValidName(String name)
	{
		for(char c : name.toCharArray())
		{
			if(allowedChars.indexOf(c) == -1)
				return false;
		}
		
		return true;
	}

	public void recalculatePerms( PermissionObject permObj )
	{
		HashSet<User> toUpdate = new HashSet<User>();
		if(permObj instanceof User)
			toUpdate.add((User)permObj);
		else if(permObj instanceof Group)
		{
			ArrayDeque<Group> toSearch = new ArrayDeque<Group>();
			toSearch.add((Group)permObj);
			
			while(!toSearch.isEmpty())
			{
				Group group = toSearch.poll();
				
				for(User user : mUsers.values())
				{
					if(group.equals(user.getGroupObject()))
						toUpdate.add(user);
				}
				
				for(Group other : mGroups.values())
				{
					if(group.equals(other.getParent()))
						toSearch.add(other);
				}
			}
		}
		
		// Now we need to find the RconConnection objects and refresh them all
		for(RconConnection con : BetterRCon.getAllConnections())
		{
			if(toUpdate.contains(con.getUser()))
				loadPermissions(con);
		}
	}
}

package au.com.addstar.rcon.auth;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;

public abstract class PermissionObject
{
	private ConfigurationSection mSection;
	
	public PermissionObject(ConfigurationSection section)
	{
		mSection = section;
	}
	
	public ConfigurationSection getConfig()
	{
		return mSection;
	}
	
	public String getName()
	{
		return mSection.getName();
	}
	
	public boolean isOp()
	{
		return mSection.getBoolean("op", true);
	}
	
	public void setOp(boolean value)
	{
		mSection.set("op", value);
	}
	
	public Set<String> getDefinedPermissions()
	{
		if(!mSection.isConfigurationSection("permissions"))
			return Collections.emptySet();
		
		ConfigurationSection permSection = mSection.getConfigurationSection("permissions");
		HashSet<String> perms = new HashSet<String>();
		
		for(String key : permSection.getKeys(false))
		{
			boolean has = permSection.getBoolean(key);
			
			if(has)
				perms.add(key);
			else
				perms.add("-" + key);
		}
		
		return perms;
	}
	
	public void addPerm(String perm, boolean invert)
	{
		ConfigurationSection permSection = mSection.getConfigurationSection("permissions");
		if(permSection == null)
			permSection = mSection.createSection("permissions");
		
		permSection.set(perm, !invert);
	}
	
	public void removePerm(String perm)
	{
		ConfigurationSection permSection = mSection.getConfigurationSection("permissions");
		if(permSection == null)
			permSection = mSection.createSection("permissions");
		
		permSection.set(perm, null);
	}
}

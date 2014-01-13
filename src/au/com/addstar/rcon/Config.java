package au.com.addstar.rcon;

import java.io.File;

import org.bukkit.configuration.InvalidConfigurationException;

public class Config extends AutoConfig
{
	public Config(File file)
	{
		super(file);
	}
	
	@ConfigField
	public int port = 8000;

	@Override
	protected void onPostLoad() throws InvalidConfigurationException
	{
		if(port <= 0 || port > 65535)
			throw new InvalidConfigurationException("Port number must be between 1 and 65535");
	}
}

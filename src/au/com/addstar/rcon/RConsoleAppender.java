package au.com.addstar.rcon;
import java.io.UnsupportedEncodingException;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;

public class RConsoleAppender extends AbstractAppender
{
	public RConsoleAppender(Configuration config)
	{
		super("RConsoleAppender", null, PatternLayout.createLayout("[%d{HH:mm:ss} %level]: %msg", config, null, "UTF-8", "true"));
	}

	@Override
	public void append( LogEvent event )
	{
		try
		{
			BetterRCon.broadcastMessage(new String(getLayout().toByteArray(event), "UTF-8"));
		}
		catch ( UnsupportedEncodingException e )
		{
			e.printStackTrace();
		}
	}

}

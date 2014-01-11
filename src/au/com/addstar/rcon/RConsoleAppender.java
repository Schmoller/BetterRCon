package au.com.addstar.rcon;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;

public class RConsoleAppender extends AbstractAppender
{
	public static PatternLayout layout;
	public RConsoleAppender(Configuration config)
	{
		super("RConsoleAppender", null, null);
		layout = PatternLayout.createLayout("[%d{HH:mm:ss} %level]: %msg", config, null, "UTF-8", "true");
	}

	@Override
	public void append( LogEvent event )
	{
		BetterRCon.sendLog(event);
	}

}

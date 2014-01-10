package au.com.addstar.rcon.client;
import java.util.List;

import jline.Completor;


public class TabCompleter implements Completor
{
	private Connection mCon;
	public TabCompleter(Connection connection)
	{
		mCon = connection;
	}
	
	@SuppressWarnings( { "unchecked", "rawtypes" } )
	@Override
	public int complete( String buffer, int cursor, List candidates )
	{
		if(!mCon.isOpen())
			return cursor;
		
		List<String> results = mCon.doTabComplete(buffer);
		if(results == null)
			return cursor;
		
		candidates.addAll(results);
		
		 int lastSpace = buffer.lastIndexOf(' ');
         if (lastSpace == -1)
             return cursor - buffer.length();
         else
             return cursor - (buffer.length() - lastSpace - 1);
	}

}

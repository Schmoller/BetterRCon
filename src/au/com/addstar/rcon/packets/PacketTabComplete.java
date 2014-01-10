package au.com.addstar.rcon.packets;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PacketTabComplete extends RConPacket
{
	public List<String> results;
	
	public PacketTabComplete()
	{
		super(22);
		results = Collections.emptyList(); 
	}
	
	public PacketTabComplete(List<String> results)
	{
		super(22);
		this.results = results;
	}
	
	@Override
	public void write( DataOutput output ) throws IOException
	{
		super.write(output);
		
		if(results == null)
			output.writeShort(-1);
		else
		{
			output.writeShort(results.size());
			for(int i = 0; i < results.size(); ++i)
				output.writeUTF(results.get(i));
		}
	}
	
	@Override
	public void read( DataInput input ) throws IOException
	{
		int size = input.readShort();
		
		if(size == -1)
			results = null;
		else
		{
			results = new ArrayList<String>();
			for(int i = 0; i < size; ++i)
				results.add(input.readUTF());
		}
	}

}

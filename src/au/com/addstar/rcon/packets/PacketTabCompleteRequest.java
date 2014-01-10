package au.com.addstar.rcon.packets;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

public class PacketTabCompleteRequest extends RConPacket
{
	public String request;
	
	public PacketTabCompleteRequest()
	{
		super(21);
	}
	
	public PacketTabCompleteRequest(String request)
	{
		super(21);
		this.request = request;
	}
	
	@Override
	public void write( DataOutput output ) throws IOException
	{
		super.write(output);
		output.writeUTF(request);
	}
	
	@Override
	public void read( DataInput input ) throws IOException
	{
		request = input.readUTF();
	}

}

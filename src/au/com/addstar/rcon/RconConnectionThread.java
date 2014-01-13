package au.com.addstar.rcon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

import au.com.addstar.rcon.packets.PacketLogin;
import au.com.addstar.rcon.packets.RConPacket;

public class RconConnectionThread extends Thread
{
	private RconConnection mCon;
	private RConThread mParent;
	private Socket mSocket;
	
	public RconConnectionThread(Socket socket, RConThread parent)
	{
		mParent = parent;
		mSocket = socket;
	}
	
	public RconConnection getRCON()
	{
		return mCon;
	}
	
	private void send(RConPacket packet) throws IOException
	{
		if(mSocket.isClosed())
			return;
		
		DataOutputStream stream = new DataOutputStream(mSocket.getOutputStream());
		packet.write(stream);
		stream.flush();
	}
	
	private RconConnection handleLogin(PacketLogin packet) throws IOException
	{
		try
		{
			BetterRCon.getAuth().attemptLogin(packet.username, packet.password);
			
			RconConnection connection = new RconConnection(packet, this);
			
			if(!connection.isSilent())
				BetterRCon.getLog().info(String.format("%s logged in from %s", connection.getName(), mSocket.getInetAddress()));
			
			BetterRCon.getAuth().loadPermissions(connection);
			
			send(new PacketLogin(packet.username, new char[0], false, false));
			return connection;
		}
		catch(IllegalAccessException e)
		{
			// Bad password
			send(new PacketLogin("", new char[0], false, false));
		}
		catch(IllegalArgumentException e)
		{
			// Bad username
			send(new PacketLogin("", new char[0], false, false));
		}
		return null;
	}
	
	@Override
	public void run()
	{
		try
		{
			DataInputStream input = new DataInputStream(mSocket.getInputStream());
			
			// Handle login first
			RConPacket packet = RConPacket.load(input);
			if(!(packet instanceof PacketLogin))
				return;
			
			RconConnection con = handleLogin((PacketLogin)packet);
			
			if(con == null)
				return;
			
			mCon = con;
			
			// Now handle packets like normal
			while(true)
			{
				packet = RConPacket.load(input);
				
				if(packet == null)
					break;
				
				mCon.handle(packet);
			}
			
		}
		catch(EOFException e)
		{
			// Connection term. Silent shutdown
		}
		catch(IOException e)
		{
			System.err.println("Error in connection " + mSocket.getInetAddress() + ": " + e.getMessage());
		}
		finally
		{
			mParent.remove(this);
			silentClose();
			
			if(mCon != null)
				mCon.close();
		}
	}
	
	private void silentClose()
	{
		try
		{
			mSocket.close();
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void terminate()
	{
		mCon.close();
		silentClose();
		interrupt();
		mParent.remove(this);
	}

	public Socket getSocket()
	{
		return mSocket;
	}
}

package au.com.addstar.rcon.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayDeque;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import au.com.addstar.rcon.packets.*;


public class Connection extends Thread
{
	private Socket mSocket;
	private ArrayDeque<PacketWaiter<? extends RConPacket>> mWaiters = new ArrayDeque<PacketWaiter<? extends RConPacket>>();
	
	public Connection(String host, int port) throws IOException
	{
		try
		{
			KeyStore keyStore = KeyStore.getInstance("JKS");
			
			InputStream stream = Connection.class.getResourceAsStream("keystore.jks");
			if(stream == null) // For testing
				stream = new FileInputStream("../keystore.jks");
			
			keyStore.load(stream, "BetterRCon".toCharArray());
			
			stream.close();
			
			KeyManagerFactory keyFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
			keyFactory.init(keyStore, "BetterRCon".toCharArray());
			TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
			factory.init(keyStore);
			
			SSLContext context = SSLContext.getInstance("SSL");
			context.init(keyFactory.getKeyManagers(), factory.getTrustManagers(), null);
			
			mSocket = context.getSocketFactory().createSocket();
			mSocket.connect(new InetSocketAddress(host, port), 2000);
			
			start();
		}
		catch ( KeyStoreException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch ( NoSuchAlgorithmException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch ( CertificateException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch ( KeyManagementException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch ( UnrecoverableKeyException e )
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void send(RConPacket packet) throws IOException
	{
		synchronized(mSocket)
		{
			packet.write(new DataOutputStream(mSocket.getOutputStream()));
			mSocket.getOutputStream().flush();
		}
	}
	
	public synchronized void sendCommand(String command) throws IOException
	{
		send(new PacketCommand(command));
	}
	
	private RConPacket get() throws IOException
	{
		DataInputStream stream = new DataInputStream(mSocket.getInputStream());
		return RConPacket.load(stream);
	}
	
	public boolean login(String username, char[] password, boolean silent, boolean noFormat)
	{
		try
		{
			PacketLogin loginPacket = new PacketLogin(username, password, silent, noFormat);
			send(loginPacket);
			
			Future<PacketLogin> result = waitForPacket(PacketLogin.class);
			
			PacketLogin packet = result.get(2000, TimeUnit.MILLISECONDS);
			
			if(packet.username.equals(username))
			{
				ConsoleMain.printString("Connected to minecraft server. Type quit to exit");
				return true;
			}
			else
			{
				ConsoleMain.printString("Unable to authenticate, invalid password.");
				return false;
			}
		}
		catch(TimeoutException e)
		{
			ConsoleMain.printString("Unable to connect to server. Connection timeout.");
		}
		catch(IOException e)
		{
			ConsoleMain.printString("Connection error: " + e.getMessage());
		}
		catch ( InterruptedException e )
		{
		}
		catch ( ExecutionException e )
		{
		}
		
		return false;
	}
	
	@Override
	public void run()
	{
		try
		{
			while(true)
			{
				if(isInterrupted())
					break;

				RConPacket response = get();
				
				synchronized(mWaiters)
				{
					PacketWaiter<? extends RConPacket> waiter = mWaiters.peekFirst();
					
					if(waiter != null && response.getClass().equals(waiter.getPacketClass()))
					{
						mWaiters.poll();
						waiter.setData(response);
						continue;
					}
				}
				
				if(response instanceof PacketMessage)
					ConsoleMain.printString(((PacketMessage)response).message);
			}
		}
		catch(SocketException e)
		{
			if(!mSocket.isClosed())
			{
				ConsoleMain.printString("Connection error: " + e.getMessage());
				e.printStackTrace();
			}
		}
		catch(EOFException e)
		{
			// Con shutdown. Silent stop
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			try
			{
				mSocket.close();
			}
			catch(IOException e) {}
		}
	}
	
	public boolean isOpen()
	{
		synchronized(mSocket)
		{
			return mSocket != null && mSocket.isConnected();
		}
	}
	
	public <T extends RConPacket> Future<T> waitForPacket(Class<T> packetClass)
	{
		PacketWaiter<T> waiter = new PacketWaiter<T>(packetClass);
		synchronized(mWaiters)
		{
			mWaiters.add(waiter);
		}
		
		return waiter;
	}

	public List<String> doTabComplete( String buffer )
	{
		try
		{
			send(new PacketTabCompleteRequest(buffer));
		}
		catch ( IOException e )
		{
			return null;
		}

		Future<PacketTabComplete> result = waitForPacket(PacketTabComplete.class);
		
		try
		{
			PacketTabComplete packet = result.get(2000, TimeUnit.MILLISECONDS);
			return packet.results;
		}
		catch ( Exception e )
		{
			return null;
		}
	}
	
	public void close()
	{
		try
		{
			mSocket.close();
		}
		catch(IOException e) {}
		
		interrupt();
	}
	
	private static class PacketWaiter<T extends RConPacket> implements Future<T>
	{
		private Class<T> mClass;
		private Object mWaitObj = new Object();
		
		private T mData = null;
		private boolean mCancel = false;
		
		public PacketWaiter(Class<T> packetClass)
		{
			mClass = packetClass;
		}
		
		@SuppressWarnings( "unchecked" )
		public void setData(RConPacket data)
		{
			mData = (T)data;
			
			synchronized(mWaitObj)
			{
				mWaitObj.notifyAll();
			}
		}
		
		public Class<T> getPacketClass()
		{
			return mClass;
		}
		
		@Override
		public boolean cancel( boolean mayInterruptIfRunning )
		{
			mCancel = true;
			
			synchronized(mWaitObj)
			{
				mWaitObj.notifyAll();
			}
			return true;
		}

		@Override
		public boolean isCancelled()
		{
			return mCancel;
		}

		@Override
		public boolean isDone()
		{
			return mData != null;
		}

		@Override
		public T get() throws InterruptedException, ExecutionException
		{
			while(true)
			{
				synchronized(mWaitObj)
				{
					mWaitObj.wait();
				}
				
				if(mCancel)
					throw new InterruptedException();
				
				if(mData != null)
					return mData;
			}
		}

		@Override
		public T get( long timeout, TimeUnit unit ) throws InterruptedException, ExecutionException, TimeoutException
		{
			long waitTime = TimeUnit.MILLISECONDS.convert(timeout, unit);
			long endTime = System.currentTimeMillis() + waitTime;
			while(true)
			{
				synchronized(mWaitObj)
				{
					mWaitObj.wait(endTime - System.currentTimeMillis());
				}
				
				if(mCancel)
					throw new InterruptedException();
				
				if(mData != null)
					return mData;
				else if(System.currentTimeMillis() >= endTime)
					throw new TimeoutException();
			}
		}
		
	}

	
}

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;

public class UdpChat implements Runnable 
{
    public static String clientHostname = null;
    public static String destinationName = null;
    public static DatagramSocket clientSocket;
    static HashMap<String, ArrayList<Serializable>> table = new HashMap<String, ArrayList<Serializable>>();
    static InetAddress clientServerIP = null;
    static int clientServerPort = -1;
    static int clientPort = -1;
    static boolean isOnline = false;
    static boolean clientAck = false;
    static int counterA = 0;
    static String offlineChatMode = null;
    static String buffer = null;
	
    
    // main method checks mode 
    // if server, it calls corresponding constructor
    // if client, it calls the corresponding constructor 
    // called-constructor determined by parameter(s) provided
    // requires correct arguments to proceed 
    
	public static void main(String[] args) throws Exception 
	{
		
		try
		{
			String mode = null;
			mode = args[0].substring(1);


		   switch(mode){	
		   
		   case ("s"):
			
				int port = Integer.parseInt(args[1]);
				
				if (port <= 1024 || port >= 65535)
				{
					System.out.println(">>> error! server port must be in the range 1024-65535");
					System.exit(0);
				}

				
				UdpChat initiateServer = new UdpChat(port);	
			    
				break;
			
			
		   case ("c"):
			
				String hostname = args[1];
				InetAddress serverIP = InetAddress.getByName(args[2]);
				int serverPort = Integer.parseInt(args[3]);
				int clientPort = Integer.parseInt(args[4]);
				
				if (serverPort <= 1024 || serverPort >= 65535)
				{
					System.out.println(">>> error! server port must be in the range 1024-65535");
					System.exit(0);
				}
				
				if (clientPort <= 1024 || clientPort >= 65535)
				{
					System.out.println(">>> error! client port must be in the range 1024-65535");
					System.exit(0);
				}
				
				
				UdpChat initiateClient = new UdpChat(hostname, serverIP, serverPort, clientPort);	
			
		        break;
		
		}
		   }
		
		catch (ArrayIndexOutOfBoundsException e)
		{
			System.out.println(">>> please enter valid arguments to continue.");
		}
	}
	 
	//constructor to be called in client mode
	public UdpChat(String hostname, InetAddress ip, int srvPort, int clientPort ) throws Exception 
    { 
		DatagramSocket clientSocket = null; 
		
		try
		{
			clientSocket = new DatagramSocket( clientPort );
		}
		catch (BindException e)
		{
			System.out.println(">>> [Address already in use, exiting...]");
			System.exit(0);
		}
		
		String nickname = hostname;
		int myPort = clientPort;
		byte[] receiveData = new byte[1024];
		
		InetAddress serverIP = ip;
		int serverPort = srvPort;

		UdpChat sender = new UdpChat (clientSocket, nickname, serverIP, serverPort, myPort);
		Thread s = new Thread (sender);
		s.start();
		
		
		System.out.println(">>> [Registering...]");
		System.out.print(">>> ");
		
		while (true)
		{

				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length); 
	
				clientSocket.receive(receivePacket); 
				   
				String sentence = new String(receivePacket.getData(), 0, receivePacket.getLength());
				   

				if (sentence.startsWith(".hash, "))
				{
					clientAck = true;
					sender.updateHash(sentence);
				}
				

				else if (sentence.equals(".isAlive?"))
				{
					String out = ".isAlive!";
	                
		        	byte buffer[] = new byte [1024];
		        	buffer = out.getBytes();
		            InetAddress address = receivePacket.getAddress();
		            DatagramPacket ack = new DatagramPacket(buffer, buffer.length, address, receivePacket.getPort());
		                
		            clientSocket.send(ack);
				}
				  

				else if (sentence.equals("[You are Offline. Bye.]"))
				{
					clientAck = true;
					System.out.println(sentence);
					System.out.print(">>> ");
				}
				

				else if (sentence.equals("[Message received by the server and saved]"))
				{
					clientAck = true;
					System.out.println(sentence); 
					System.out.print(">>> ");
				}
				  
		
				else if (!sentence.startsWith("[Message received by "))
	            {
	            	System.out.println(sentence);
	            	System.out.print(">>> ");
		        	String out = "[Message received by " + clientHostname +".]";
		                
		        	byte buffer[] = new byte [1024];
		        	buffer = out.getBytes();
		            InetAddress address = receivePacket.getAddress();
		            DatagramPacket ack = new DatagramPacket(buffer, buffer.length, address, receivePacket.getPort());
		                
		            clientSocket.send(ack);
	            }

	
				else
				{
					clientAck = true;
					System.out.print(">>> ");
					System.out.println(sentence); 
					System.out.print(">>> ");
				}
			

		}
	}		
	
	   public UdpChat(DatagramSocket socket, String host, InetAddress serverIP, int serverPort, int userPort) 
	    {
	        clientSocket = socket;
	        clientPort = userPort;	        
	        clientHostname = host;	        
	        clientServerIP = serverIP;	        
	        clientServerPort = serverPort;
	 
	        try 
	        {
				register();			
			} 
	        catch (InterruptedException e) 
	        {
				e.printStackTrace();
			}	        
	    }


	    public void run() 
	    {
	        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	        while (true) 
	        {
	        	
	        	if (clientAck)
	        	{
	        		
	        		counterA = 0;
		        	try 
		        	{
		        		
		        		String line = in.readLine();
		        		
		        		
		        		if (isOnline || line.startsWith("reg") || line.startsWith("ctrl + c"))
		        		{
		        			
			        		if (line.startsWith("send"))
			        		{
			        			
				        			clientAck = false;	
				        			try
				        	    	{
				        				clientSocket.send(clientLookUp(line));
				        	    	}
				        			catch (ArrayIndexOutOfBoundsException e)
				        			{
				        				clientAck = true;
				        				System.out.println(">>> [Please enter a username]");
				        				System.out.print(">>> ");
				        			}
				        			catch (StringIndexOutOfBoundsException e)
				        			{
				        				clientAck = true;
				        				System.out.println(">>> [Please enter a message]");
				        				System.out.print(">>> ");
				        			}
				        			catch (NullPointerException e)
				        			{
				        				clientAck = true;
				        				System.out.println(">>> [The requested client was not found. To see available clients, type <table>.]");
				        				System.out.print(">>> ");
				        			}	
			        			}
			        		
			
			        		
			        		else if (line.equals("table"))
			        		{
			  				  System.out.println(">>> Table");
							  Iterator<String> iterator = table.keySet().iterator(); 
							  while (iterator.hasNext())
								{
									String value = table.get(iterator.next().toString()).toString();		
									String output = value.substring(1, value.length()-1);  						
									System.out.println(">>> " + output);		
								}
							  System.out.print(">>> ");
			        		}
			        		
			        		
			        		else if (line.startsWith("dereg"))
			        		{
			        			if (!isOnline)
			        			{
			        				System.out.println(">>> [You are currently offline.]");
			        			}
			        			else
			        			{
			        				System.out.print(">>> ");
			        				deregister();
			        				isOnline = false;
			        			}
			        			
			        		}
			        		

			        		else if (line.startsWith("reg"))
			        		{
			        			if (isOnline)
			        				System.out.println(">>> [You are currently online.]");
			        			else
			        			{
			        				System.out.print(">>> ");
			        				String[] values = line.split(" ");
			        				try
			        				{
			        					clientHostname = values[1];
			        					register();
				        				isOnline = true;
			        				}
			        				catch (ArrayIndexOutOfBoundsException e)
			        				{
				        				System.out.println("[Please enter a clientname]");
				        				System.out.print(">>> ");
			        				}
			        				
			        			}
			        		}
			        		

			        		else if (line.equals("ctrl + c"))
			        		{
			        			if (isOnline)
			        			{
			        				deregister();
			        			}
			        			System.out.print(">>> ");
			        			
			        			Thread.sleep(500); 
			        			
			        			System.out.println("[Exiting]");
			        			System.exit(0);
			        		}
			        		
			        		else
			        		{
			        			System.out.println(">>> [Command not recognized, please try again.]");
			        			System.out.print(">>> ");
			        		}

		        		}
		

						else
						{
							System.out.println(">>> [You are currently offline.]");
							System.out.println(">>> [Please Register or exit the application.]");
							System.out.print(">>> ");
						}
					} 
		        	
		        	catch (Exception e) 
		        	{
						e.printStackTrace();
					}
	        	}
	        	

	        	else
	        	{
	        		if (offlineChatMode.equals("server"))
	        			serverACK();
	        		else
	        			clientACK();
	        	}
	        }
	    }

        // method updates table of users each time a reg or dereg is done
		public void updateHash (String sentence)
		{
			String[] values = sentence.split(", ");
			
			ArrayList<Serializable> list = new ArrayList<Serializable>();
			int i = 1;
			while ( i < values.length)
			{
				list.add (values[i]);
				i++;
			}
	  
			table.put(values[1], list);
			
			list = new ArrayList<Serializable>();	
		}
	    

		
		private static DatagramPacket clientLookUp(String input)
		{
			
			String[] list = input.split(" ");		
			String client = list[1];
			destinationName = client;

			int index = 6 + list[1].length();
			
			String message = clientHostname + ": " + input.substring(index);


			ArrayList<Serializable> values = table.get(client);
			
			String ip = (String) values.get(1);
			ip = ip.substring(1);
			
			String status = (String) values.get(3);

			
			if (status.equalsIgnoreCase("on"))
			{
				offlineChatMode = "client";
				

				InetAddress clientIP = null;

				try 
				{
					clientIP = InetAddress.getByName(ip);
				} 
				catch (UnknownHostException e) 
				{
					e.printStackTrace();
				}	
				
	
				int clientPort = Integer.parseInt((String) values.get(2)); 


				byte[] sendData  = new byte[1024];		
				sendData = message.getBytes(); 
				buffer = ".off " + client + " " + clientHostname + " " + input.substring(index);
				
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientIP, clientPort); 

				return sendPacket;
			}
			

			else
			{
				offlineChatMode = "server";
				
				System.out.println(">>> [" + client + " is currently off-line, message sent to server.]");
				System.out.print(">>> ");
				
				message = ".off " + client + " " + clientHostname + " " + input.substring(index);
				byte[] sendData  = new byte[1024];	
				
				sendData = message.getBytes();  
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientServerIP, clientServerPort); 
		
				return sendPacket;

			}
		}
	    
        // registration method..
		public void register() throws InterruptedException
		{
			
			
	        String sentence = ".register " + clientHostname + " " + clientPort + " " + "on";
	        
	        byte[] sendData = new byte[1024];
			sendData = sentence.getBytes();
	   
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientServerIP, clientServerPort); 
			
			try 
			{
				clientAck = false;   
				offlineChatMode = "server";
				clientSocket.send(sendPacket);
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			
			isOnline = true;
	  
		}

		//deregistration...
		private static void deregister() throws SocketException
		{
		
	        String sentence = ".deregister " + clientHostname + " " + clientPort + " " + "off";
	        
	        byte[] sendData = new byte[1024];
			sendData = sentence.getBytes();
	   
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientServerIP, clientServerPort); 
			
			try 
			{
				clientAck = false;
				offlineChatMode = "server";
				clientSocket.send(sendPacket);
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}

			
			isOnline = false;
		}
	   

		private static void serverACK()
		{
			if (counterA < 5)
			{
				try 
				{
					Thread.sleep(500);
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
				counterA++;
			}
			else
			{
				System.out.println("[Server not responding]");
				System.out.println(">>> [Exiting]");
				System.exit(0);
			}
		}
		

		private static void clientACK()
		{
			if (counterA < 1)
			{
				try 
				{
					Thread.sleep(500);
				} 
				catch (InterruptedException e) 
				{
					e.printStackTrace();
				}
				counterA++;
			}
			else
			{
				System.out.println(">>> [No ACK from " + destinationName + ", message sent to server.]");
				System.out.print (">>> ");
				
				byte[] sendData  = new byte[1024];
				sendData = buffer.getBytes();
				
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientServerIP, clientServerPort); 
		
				try 
				{
					clientAck = false;
					offlineChatMode = "server";
					clientSocket.send(sendPacket);
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}
		}	
		
		
			

			
		
		static HashMap<String, ArrayList<Serializable>> tableHashMap = new HashMap<String, ArrayList<Serializable>>();

	
		static DatagramSocket serverSocket = null;
		static byte[] receiveData = new byte[1024]; 
		
		// constructor to be called in server mode
		public UdpChat (int port) throws Exception 
		{ 
			System.out.println(">>> [Server initiated...]");
			
			try  
			{
				serverSocket = new DatagramSocket(port);
			}
			catch (BindException e)
			{
				System.out.println(">>> [Socket Already in use, exiting...]");
				System.exit(0);
			}

			while(true) 
			{ 

				DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length); 
				serverSocket.receive(receivePacket); 
		
				String sentence = new String(receivePacket.getData(), 0, receivePacket.getLength());
				InetAddress IPAddress = receivePacket.getAddress(); 
				

				if (sentence.startsWith(".register"))
				{
					
					String[] values = sentence.split(" ");
					boolean on = false;
					

					if (tableHashMap.get(values[1]) != null)
					{
						ArrayList<Serializable> list = tableHashMap.get(values[1]);
						
						InetAddress toIP = (InetAddress) list.get(1);	
						int toPort = Integer.parseInt((String) list.get(2)); 


						if (checkAlive(values[1], toIP, toPort))
						{
							String output = "[Nickname already taken.]";
							byte[] sendData  = new byte[1024];
							
							sendData = output.getBytes();  
						
							DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, receivePacket.getPort()); 
							try 
							{
								serverSocket.send(sendPacket);
							} 
							catch (IOException e) 
							{
								e.printStackTrace();
							}
							on = true;
						}
						

						else
						{
							on = false;
						}
											
					}
					

					if (!on)
					{
						registration (sentence, IPAddress);
						
						String output = "[Welcome, You are registered.]";
						byte[] sendData  = new byte[1024];
						
						sendData = output.getBytes();  
						
						DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, receivePacket.getPort()); 
						try 
						{
							serverSocket.send(sendPacket);
						} 
						catch (IOException e) 
						{
							e.printStackTrace();
						} 
						
						readMessage(sentence.split(" ")[1]);
					}
				}
				

				if (sentence.startsWith(".deregister"))
				{
					registration (sentence, IPAddress);
					
					String output = "[You are Offline. Bye.]";
					byte[] sendData  = new byte[1024];
					
					sendData = output.getBytes();  
					
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, receivePacket.getPort()); 
					try 
					{
						serverSocket.send(sendPacket);
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					} 
					
				}
				

				if (sentence.startsWith(".off"))
				{
					clientLookUp(sentence, IPAddress, receivePacket.getPort());
				}
				

				if (sentence.equals(".isAlive!"))
				{
					serverAck= true;
				}
			} 
		} 
		
		
		private static void registration(String sentence, InetAddress IPAddress)
		{
			String[] values = sentence.split(" ");
	  
			ArrayList<Serializable> list = new ArrayList<Serializable>();
			
			list.add (values[1]);
			list.add(IPAddress);
			list.add (values[2]);
			list.add(values[3]);
	           
			tableHashMap.put(values[1], list);
	          
			list = new ArrayList<Serializable>();
	  
			Iterator<String> iterator = tableHashMap.keySet().iterator();          
			while (iterator.hasNext()) 
			{  
				
				String key = iterator.next().toString();

				list = (ArrayList<Serializable>) tableHashMap.get(key);	
				

				String status = (String) list.get(3);

				if (status.equalsIgnoreCase("on"))
				{

					InetAddress ip = (InetAddress) list.get(1);	
					

					int p = Integer.parseInt((String) list.get(2)); 

					Iterator<String> iterator2 = tableHashMap.keySet().iterator(); 
					while (iterator2.hasNext())
					{
						String value = tableHashMap.get(iterator2.next().toString()).toString();		
						String output = ".hash, " + value.substring(1, value.length()-1);  
						
						byte[] sendData  = new byte[1024];
						
						sendData = output.getBytes();  
						
						DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip, p); 
						try 
						{
							
							serverSocket.send(sendPacket);
						} 
						catch (IOException e) 
						{
							e.printStackTrace();
						} 
					}
					
					byte[] sendData  = new byte[1024];
					String output = "[Client table updated.]";
					sendData = output.getBytes();  
					
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip, p); 
					try 
					{
						serverSocket.send(sendPacket);
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					}
		
				}
			}
		}
	  

		private static void sendTable(String client, InetAddress ip, int p)
		{
			String output = "[Client " + client + " exists!!]";
			
			byte[] sendData  = new byte[1024];
			sendData = output.getBytes();  
			
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip, p); 
			try 
			{
				serverSocket.send(sendPacket);
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			
			Iterator<String> iterator = tableHashMap.keySet().iterator(); 
			while (iterator.hasNext())
			{
				String value = tableHashMap.get(iterator.next().toString()).toString();		
				output = ".hash, " + value.substring(1, value.length()-1);  
				
				sendData  = new byte[1024];
				sendData = output.getBytes();  
				
				sendPacket = new DatagramPacket(sendData, sendData.length, ip, p); 
				try 
				{
					serverSocket.send(sendPacket);
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				} 
			}
			
			sendData  = new byte[1024];
			output = "[Client table updated.]";
			sendData = output.getBytes();  
			
			sendPacket = new DatagramPacket(sendData, sendData.length, ip, p); 
			try 
			{
				serverSocket.send(sendPacket);
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}


		private static void clientLookUp(String sentence, InetAddress IPAddress, int port) throws IOException
		{
			String[] values = sentence.split(" ");
			String toClient = values[1];
					
			ArrayList<Serializable> list = tableHashMap.get(toClient);
			
			String status = (String) list.get(3);

			InetAddress toIP = (InetAddress) list.get(1);	
			
			int toPort = Integer.parseInt((String) list.get(2)); 
			

			if (status.equals("off"))
			{

				if (!checkAlive (toClient, toIP, toPort))
				{
					saveMessage (sentence);
					
					String output = "[Message received by the server and saved]";
					byte[] sendData  = new byte[1024];
					sendData = output.getBytes();  
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port); 
					try 
					{
						serverSocket.send(sendPacket);
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					}
				}
				

				else
				{
					String output = "[Client " + toClient + " exists!!]";
					
					byte[] sendData  = new byte[1024];
					sendData = output.getBytes();  
					
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port); 
					try 
					{
						serverSocket.send(sendPacket);
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					}
					
					updateHash(toClient, "on");

				}
				
			}

			
			else
			{
	
				if (checkAlive (toClient, toIP, toPort))
					sendTable (toClient, IPAddress, port);	
				

				else 
				{
					saveMessage (sentence);
					
					String output = "[Message received by the server and saved]";
					byte[] sendData  = new byte[1024];
					sendData = output.getBytes();  
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port); 
					try 
					{
						serverSocket.send(sendPacket);
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					}
					
					updateHash(toClient, "off");
				}

			}
		}

		
		public static void saveMessage(String output) throws IOException
		{
			String[] values = output.split(" ");
			String toClient = values[1];
			String fromClient = values[2];
			
			int index = 7 + values[1].length()+ values[2].length();

			DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
			Date date = new Date();
			
			String message = fromClient + ": <" +dateFormat.format(date) + "> " + output.substring(index);
			
			String filename = toClient+".txt";
			
			File txt = null;
			BufferedWriter writer = null;
			try
			{
				 writer = new BufferedWriter(new FileWriter(filename, true));
			}
			
			catch (FileNotFoundException e)
			{
				txt = new File(filename);
				writer = new BufferedWriter (new FileWriter(txt, true));
				
			}

			writer.write(message);
			writer.newLine();
			writer.close();
			
		}
		

		public static void readMessage(String client) throws IOException
		{
			String filename = client+".txt";

	
			try 
			{
				BufferedReader in = new BufferedReader(new FileReader(filename));
				
				
				ArrayList<Serializable> list = tableHashMap.get(client);
				
			
				InetAddress ip = (InetAddress) list.get(1);	
				
		
				int p = Integer.parseInt((String) list.get(2));
				
				String output = "You Have Messages";
				byte[] sendData  = new byte[1024];
				sendData = output.getBytes();  
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip, p); 
				try 
				{
					serverSocket.send(sendPacket);
				} 
				catch (IOException e) 
				{
					e.printStackTrace();
				} 
				

				String message;
				while ((message = in.readLine()) != null)
				{
					sendData = message.getBytes();  
					sendPacket = new DatagramPacket(sendData, sendData.length, ip, p); 
					
					try 
					{
						serverSocket.send(sendPacket);
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					} 
					
				}
				
				in.close();
				
				File txt = new File (filename);
				txt.delete();
				
				
			} 
			

			catch (FileNotFoundException e) 
			{

			}
		}
		

		public static boolean checkAlive(String toClient, InetAddress toIP, int toPort)
		{
			String output = ".isAlive?";
			byte[] sendData  = new byte[1024];
			sendData = output.getBytes();  
			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, toIP, toPort); 

			
			try 
			{
				serverAck= false;
				serverSocket.send(sendPacket);
			} 
			catch (IOException e) 
			{
				e.printStackTrace();
			}
			
			
			boolean notYet = true;
			while (notYet)
			{
				if (serverAck)
				{
					notYet = true;	
				}
				else
				{

					if (serverCounter < 1)
					{
						try 
						{
							Thread.sleep(500);
						} 
						catch (InterruptedException e) 
						{
							e.printStackTrace();
						}
						serverCounter++;
					}
					else
						notYet = false;
				}
			}
			
			serverCounter = 0;
			
			return serverAck;	
		}
		
        // updates table of users
		public static void updateHash(String client, String updatedStatus)
		{
			ArrayList<Serializable> list = tableHashMap.get(client);
			
			list.set(3, updatedStatus);
	           
			tableHashMap.put(client, list);
	          
			Iterator<String> iterator = tableHashMap.keySet().iterator();          
			while (iterator.hasNext()) 
			{  

				String key = iterator.next().toString();
				

				list = (ArrayList<Serializable>) tableHashMap.get(key);	
				

				String status = (String) list.get(3);
				
				if (status.equalsIgnoreCase("on"))
				{

					InetAddress ip = (InetAddress) list.get(1);	
					

					int p = Integer.parseInt((String) list.get(2)); 

	
					Iterator<String> iterator2 = tableHashMap.keySet().iterator(); 
					while (iterator2.hasNext())
					{
						String value = tableHashMap.get(iterator2.next().toString()).toString();		
						String output = ".hash, " + value.substring(1, value.length()-1);  
						
						byte[] sendData  = new byte[1024];
						
						sendData = output.getBytes();  
						
						DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip, p); 
						try 
						{
							serverSocket.send(sendPacket);
						} 
						catch (IOException e) 
						{
							e.printStackTrace();
						} 
					}
					
					byte[] sendData  = new byte[1024];
					String output = "[Client table updated.]";
					sendData = output.getBytes();  
					
					DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, ip, p); 
					try 
					{
						serverSocket.send(sendPacket);
					} 
					catch (IOException e) 
					{
						e.printStackTrace();
					}
		
				}
			}
		}

		static int serverCounter = 0;
		static boolean serverAck= false;
}

package mtgengine.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import mtgengine.Game;
import mtgengine.Xml;
import mtgengine.card.CardFactory;
import mtgengine.cost.ManaCost;

public class MtgeServer {
	static Vector<Client> clients = new Vector<Client>();
	
	private static Client findClientBySocketChannel(SocketChannel sc) {
		for (Client c : clients) {
			if (c.getSocketChannel() == sc) {
				return c;
			}
		}
		return null;
	}
	
	public static void main(String[] args) {
		Selector selector;
		try {
			// open selector
			System.out.print("Opening Selector...");
			selector = Selector.open();
			System.out.println("OK");

			// open server socket
			System.out.print("Opening ServerSocket...");
			ServerSocketChannel serverSocket = ServerSocketChannel.open();
			System.out.println("OK");

			// bind address
			InetSocketAddress hostAddress = new InetSocketAddress("localhost", 7152);
			System.out.print("Binding address : " + hostAddress + "...");
	        serverSocket.bind(hostAddress);
	        System.out.println("OK");
			
	        // configure socket
	        serverSocket.configureBlocking(false);
			int ops = serverSocket.validOps();
	        serverSocket.register(selector, ops, null);

	        // load card definition file
	        System.out.print("Loading card definition from XML file : " + Xml.CARD_DEFINITION_FILE + "...");
	        CardFactory.loadCardDefinition(Xml.CARD_DEFINITION_FILE);
	        System.out.println("OK");
	        
	        // load token definition file
	        System.out.print("Loading token definition from XML file : " + Xml.TOKEN_DEFINITION_FILE + "...");
	        CardFactory.loadCardDefinition(Xml.TOKEN_DEFINITION_FILE);
	        System.out.println("OK");
	        
	        // load mana definition
	        ManaCost.initialize();
	        
	        // Export to XML file
//	        System.out.print("Exporting cards to output XML file : " + Xml.OUTPUT_FILE + "...");
//	        Xml.dumpXml(CardFactory.extractCardsFromDefinition(), Xml.OUTPUT_FILE);
//	        System.out.println("OK");
	        
	        // all systems ready
	        System.out.println("MTGE server started.");
	        
	        while (true) {
				Game g = new Game();
				String subdir1 = "/";
				String subdir2 = "/";
				g.addPlayer("Svetlana", subdir1 + "all_cards.txt");
				g.addPlayer("Natacha", subdir2 + "all_cards.txt");
				g.startGame();
				
				boolean bGameIsOver = false;
		
				int iclient = 0;
				
		        while (bGameIsOver == false) {
		        	selector.select();
		        	
		        	Set<SelectionKey> selectedKeys = selector.selectedKeys();
		        	Iterator<SelectionKey> iter = selectedKeys.iterator();
		        	
		        	while (iter.hasNext())
		        	{
		        		SelectionKey ky = (SelectionKey) iter.next();
		        		if (ky.isAcceptable()) {
							SocketChannel client = serverSocket.accept();
							client.configureBlocking(false);
							client.register(selector, SelectionKey.OP_READ);
							int id = g.getPlayers().get(iclient).getID();
							Client cl = new Client(client, id, g);
							clients.add(cl);
							iclient = (iclient + 1) % 2; 
		
							// Send welcome message which is actually the first game status
							cl.writeStatus();
		        		}
		        		else if (ky.isReadable())
		        		{
							Client cl = findClientBySocketChannel((SocketChannel) ky.channel());
							if (cl.read() == -1) {
								cl.close();
								clients.remove(cl);
							}
							else
							{
								g.printContinuousEffects();
								if (g.isOver())
									bGameIsOver = true;
								// update all other clients
								for (Client c : clients) 
								{
									if (bGameIsOver)
										c.close();
									else if (c != cl) {
										c.writeStatus();
									}
								}
							}
		        		}
						iter.remove();
		        	}
		        }
			}
		} catch (IOException e) {
			System.err.println("Could not open socket.");
			e.printStackTrace();
		}
	}
}


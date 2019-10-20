package mtgengine.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.UIManager;

import mtgengine.Game;
import mtgengine.Game.State;

public class MtgeClient extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1569547039340140080L;
	private static Board _board;
	private State _state;
	String _cardInfo;
	String _manaChoices;
	private DataOutputStream _dos;
	private boolean _bAutoPassPriority;
	
	// last clicked items
	private UIPanel _lastPanelClicked;
	private UICard _lastCardClicked;
	
	public MtgeClient() {
		ManaSymbols.initialize();
		initUI();
    }
	
	/**
	 * 
	 */
    private void initUI() {
        try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
		} catch (Exception e) {}
        
		_board = new Board();
		add(_board);
		_board.setApp(this);
		_board.setFocusable(true);
		_board.addMouseListener(new MtgeGuiMouseAdapter(_board));
		_board.addMouseMotionListener(new MtgeGuiMouseAdapter(_board));
		_board.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent arg0) {
			}
			
			@Override
			public void keyReleased(KeyEvent arg0) {
			}
			
			@Override
			public void keyPressed(KeyEvent arg0) {
				int code = arg0.getKeyCode();
				switch (code) {
				
				// F2 : pass priority
				case 113:
					F2pressed();
					break;
				
				// F4 : pass priority until there is something to respond to OR declare attackers
				case 115:
					F4pressed();
					break;
					
				// F6
				case 117:
					//F6pressed();
					break;
					
				default:
					break;
				}
			}
		});
        setSize(Board.WINDOW_WIDTH, Board.WINDOW_HEIGHT);
        setTitle("MTGE client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        JMenuItem optionsMenuItem = new JMenuItem("Options");
        optionsMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				OptionsDialog optionsFrame = new OptionsDialog(_board);
				optionsFrame.setVisible(true);
			}
		});
        
        JMenuItem toggleDebugModeMenuItem = new JMenuItem("Debug mode");
        toggleDebugModeMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				send(Game.COMMAND_TOGGLE_DEBUG_MODE);
			}
		});
        
        JMenuItem toggleDisplayImagesMenuItem = new JMenuItem("Toggle display images");
        toggleDisplayImagesMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (_board.bDisplayImages)
					_board.bDisplayImages = false;
				else
					_board.bDisplayImages = true;
			}
		});
        
        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
        
        fileMenu.add(optionsMenuItem);
        fileMenu.add(toggleDebugModeMenuItem);
        fileMenu.add(toggleDisplayImagesMenuItem);
        fileMenu.add(exitMenuItem);
        menuBar.add(fileMenu);
        this.setJMenuBar(menuBar);
    }
    
    public void computeInput(String input) {
		synchronized (_board.lock) {
			while (_board.bReadyToCompute == false) {
				try {
					_board.lock.wait();
				} catch (InterruptedException e) {}
			}
			compute(input);
		}
    }
    
    public void connectToServer() {
    	InputStreamReader isr;
		BufferedReader br;
		String input;
    	
    	try {
			Socket client = new Socket("localhost", 7152);
			isr = new InputStreamReader(client.getInputStream());
			br = new BufferedReader(isr);
			_dos = new DataOutputStream(client.getOutputStream());
			
			do {
				input = br.readLine();
				if (input == null) {
					client.close();
					return;
				}
				if (_board.bInitializationOK == false)
					compute(input);
				else
					computeInput(input);	
				_board.repaint();
			} while (input != null);
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    private void compute(String input) {
    	String[] packets = input.split("%");
    	String packet;
    	Boolean bLibManip = false;
    	Boolean bOppHand = false;

    	String response = packets[0].substring("Response=".length(), packets[0].length()-1);
   		_board.setResponse(response);
   		_board.clearCounters();
   		_board.clearAuras();
   		_manaChoices = null;
   		
    	for (int iPacket = 1; iPacket < packets.length; iPacket++) {
    		packet = packets[iPacket];
    		
    		if (packet.startsWith("Status="))
    			_board.updateStatus(packet);
    		else if (packet.startsWith("Players="))
    			_board.updatePlayers(packet);		
    		else if (packet.startsWith("ID="))
    			_board.updateID(packet);
    		else if (packet.startsWith("Stack="))
    			_board.updateUIpublicZone(packet, _board._stack);
    		else if (packet.startsWith("Actions="))
    			_board.updateActions(packet);
    		else if (packet.startsWith("ManaChoices="))
    			_board.updateManaChoices(packet);
    		else if (packet.startsWith("Attacking="))
    			_board.updateAttacking(packet);
    		else if (packet.startsWith("Attackers="))
    			_board.updateAtkDecl(packet);
    		else if (packet.startsWith("Attackables="))
    			_board.updateAttackables(packet);
    		else if (packet.startsWith("Blocking="))
    			_board.updateBlocks(packet);
    		else if (packet.startsWith("Blockers="))
    			_board.updateBloDecl(packet);
    		else if (packet.startsWith("Blockables="))
    			_board.updateBlockables(packet);
    		else if (packet.startsWith("BlockOrder="))
    			_board.updateBlockOrder(packet);
    		else if (packet.startsWith("Highlight="))
    			_board.updateHighlight(packet);
    		else if (packet.startsWith("Counters="))
    			_board.updateCounters(packet);
    		else if (packet.startsWith("Library="))
    			_board.updateLibrary(packet);
    		else if (packet.startsWith("LibManip=")) {
    			_board.updateLibManipulation(packet);
    			bLibManip = true;
    		}
    		else if (packet.startsWith("OpponentHand=")) {
    			_board.updateOpponentHand(packet);
    			bOppHand = true;
    		}
    		else if (packet.startsWith("CardInfo="))
    			_board.updateCardInfo(packet);
    		else if (packet.startsWith("Hand") ||
    				packet.startsWith("Graveyard") ||
    				packet.startsWith("Exile") ||
    				packet.startsWith("Command") ||
    				packet.startsWith("Lands") ||
    				packet.startsWith("Creatures") ||
    				packet.startsWith("Other"))
    		{
    			int idxPound = packet.indexOf("#");
    			int idxEqualSign = packet.indexOf("=");
    			int idPlayer = Integer.parseInt(packet.substring(idxPound + 1, idxEqualSign));
    			_board.updateUIprivateZone(packet, idPlayer);
    		}
    	}
    	
    	_board.updateAuras();
    	
    	if (!bLibManip)
    		_board.closeUpdateLibPanel();
    	
    	if (!bOppHand)
    		_board.closeOppHandPanel();
    	
    	_board.doFkeys();
    	_board.bInitializationOK = true;
	}

	public static void main(String[] args) {
    	MtgeClient ex = new MtgeClient();
        ex.setVisible(true);
        while(true) {
        	ex.connectToServer();
        }
    }

	public void send(String string) {
		try {
			//System.out.println("out:[" + string + "]");
			_dos.writeBytes(string + "\r\n");
			_dos.flush();
		} catch (IOException e) {}	
	}
	
	public void send(int idCard) {
		send(Integer.toString(idCard));
	}

	public void F2pressed() {
		if (_state == State.Ready)
			send(Game.COMMAND_PASS_PRIORITY);
		else if ((_state == State.WaitDeclareAttackers) || (_state == State.WaitDeclareBlockers))
			send(Game.COMMAND_DONE);
	}

	public void F4pressed() {
		_board.activateF4();
		send(Game.COMMAND_PASS_PRIORITY);
	}


	public State getGameState() {
		return _state;
	}
	
	public void setGameState(State state) {
		_state = state;
	}

	public String getCardInfo() {
		return _cardInfo;
	}

	public void setCardInfo(String cardInfo) {
		_cardInfo = cardInfo;
	}

	public void setManaChoices(String manaChoices) {
		_manaChoices = manaChoices;
	}
	
	public UIPanel getLastPanelClicked() {
		return _lastPanelClicked;
	}

	public void setLastPanelClicked(UIPanel _lastPanelClicked) {
		this._lastPanelClicked = _lastPanelClicked;
	}

	public UICard getLastCardClicked() {
		return _lastCardClicked;
	}

	public void setLastCardClicked(UICard _lastCardClicked) {
		this._lastCardClicked = _lastCardClicked;
	}

	public boolean isAutoPassPriority() {
		return _bAutoPassPriority;
	}

	public void setAutoPassPriority(boolean _bAutoPassPriority) {
		this._bAutoPassPriority = _bAutoPassPriority;
	}

}

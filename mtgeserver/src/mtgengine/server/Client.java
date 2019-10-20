package mtgengine.server;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import mtgengine.Game;
import mtgengine.Game.Response;

public class Client {
	private SocketChannel _socketChannel;
	private int _id;
	private Interpreter _shell;
	
	public Client(SocketChannel s, int idPlayer, Game g) {
		_socketChannel = s;
		_id = idPlayer;
		_shell = new Interpreter(g, idPlayer);
	}
	
	public void write(String str) {
		try {
			_socketChannel.write(ByteBuffer.wrap(str.getBytes()));
		} catch (IOException e) {}
	}

	public int read() {
		ByteBuffer buffer = ByteBuffer.allocate(16);
		String query = "";
		String buf = null;
		boolean bContinue = true;
		
		int nbRead = 0;
			
		while (bContinue)
		{
			try {
				nbRead = _socketChannel.read(buffer);
			} catch (IOException e) {
				return -1;
			}
			if (nbRead == -1) {
				bContinue = false;
			}
			else {
				buf = new String(buffer.array());
				if (buf.contains("\r\n")) {
					bContinue = false;
					query = buf.substring(0, buf.indexOf("\r\n"));
				}
				else {
					
				}
			}
		}
		
		if (nbRead == -1)
			return -1;

		System.out.println(_id + "> in:[" + query + "]");
		
		if (query.length() != 0)
		{
			Response response = _shell.computeQuery(_id + " " + query);
			String output = _shell.computeResponse(response);
			write(output);
			System.out.println(_id + "> out:[" + output.substring(0, output.length()-2) + "]");
		}
		return 0;
	}
	
	public SocketChannel getSocketChannel() {
		return _socketChannel;
	}

	public void close() throws IOException {
		_socketChannel.close();
	}

	public void writeStatus() {
		write(_shell.computeResponse(Response.OK));
	}
}

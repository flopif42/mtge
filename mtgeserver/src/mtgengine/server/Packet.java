package mtgengine.server;

import java.util.Vector;

public class Packet {
	private final static String PACKET_SEPARATOR = "%";
	
	public static String print(String name, Vector<String> elements) {
		String ret = name + "=";
		if ((elements != null) && (elements.size() > 0)) {
			for (int i = 0; i < elements.size(); i++)
				ret += elements.get(i) + ";";
		}
		ret += PACKET_SEPARATOR;
		return ret;
	}
}

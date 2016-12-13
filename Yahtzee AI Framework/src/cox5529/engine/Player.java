package cox5529.engine;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class Player {
	
	private Process p;
	private int id;
	private String name;
	private BufferedReader out;
	private BufferedWriter in;
	private ArrayList<Integer>[] decks;
	
	public Player(Process p) {
		this.p = p;
		out = new BufferedReader(new InputStreamReader(p.getInputStream()));
		in = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
		decks = new ArrayList[3];
		for(int i = 0; i < decks.length; i++)
			decks[i] = new ArrayList<Integer>();
	}
	
	public ArrayList<Integer>[] getDecks() {
		return decks;
	}
	
	private String readLine() {
		try {
			String in = out.readLine();
			System.out.println(id + ": " + in);
			return in;
		} catch(IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private boolean send(String data) {
		try {
			System.out.println(data);
			in.write(data + "\n");
			in.flush();
			return true;
		} catch(IOException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	public int getId() {
		return id;
	}
	
	public void initialize(int id, int[][] rooms, int playerCount, ArrayList<String> roomList, ArrayList<String> weaponList) {
		this.id = id;
		send("ID " + id);
		send("Playercount " + playerCount);
		send("Board " + rooms.length + " " + rooms[0].length);
		for(int i = 0; i < rooms.length; i++) {
			String w = "";
			for(int j = 0; j < rooms[i].length; j++) {
				w += rooms[i][j] + (j == rooms[i].length - 1 ? "": " ");
			}
			send(w);
		}
		send("Name");
		name = readLine();
		for(int i = 0; i < roomList.size(); i++) {
			send("Roomname " + i + " " + roomList.get(i));
		}
		for(int i = 0; i < weaponList.size(); i++) {
			send("Weaponname " + weaponList.size() + " " + i + " " + weaponList.get(i));
		}
	}
	
	public void sendMove(int id, int[] move) { // y, x
		send("Opponent " + id + " " + move[0] + " " + move[1]);
	}
	
	public void give(int type, int id) {
		if(!decks[type].contains(id))
			decks[type].add(id);
		send("Card " + type + " " + id);
	}
	
	public void accusation(int player, int p, int w, int r) {
		send("Accusation " + player + " " + p + " " + w + " " + r);
	}
	
	public void guess(int player, int p, int w, int r) {
		send("Guess " + player + " " + p + " " + w + " " + r);
	}
	
	public int disprove(int player, int p, int w, int r, boolean send) {
		int re = -1;
		if(send) {
			send("Disprove " + player + " " + p + " " + w + " " + r);
			String in = readLine();
			String[] data = in.split(" "); // Disprove [-1, 0, 1, or 2]
			re = Integer.parseInt(data[0]);
		}
		if(re == -1 && (decks[0].contains(p) || decks[1].contains(w) || decks[2].contains(r))) {
			return -2;
		} else if(re < -1 || re > 2) {
			return -2;
		} else if(re == 0 && !decks[0].contains(p)) {
			return -2;
		} else if(re == 1 && !decks[1].contains(w)) {
			return -2;
		} else if(re == 2 && !decks[2].contains(r)) {
			return -2;
		}
		return re;
	}
	
	public void showDisprove(int player, int type, int id) {
		send("Card From " + player + " " + type + " " + id);
	}
	
	public void disproved(int player) {
		send("Disproved " + player);
	}
	
	public int[] getMove(long time, int moves) {
		int[] re = new int[0];
		send("Move " + time + " " + moves);
		long start = System.nanoTime();
		String in = readLine();
		if(System.nanoTime() - start > time)
			return null;
		if(in.startsWith("Accusation")) { // player weapon room
			re = new int[4];
			String[] data = in.split(" ");
			for(int i = 0; i < data.length - 1; i++) {
				try {
					re[i] = Integer.parseInt(data[i + 1]);
				} catch(Exception e) {
					return null;
				}
			}
			re[3] = 1;
		} else if(in.startsWith("Move Guess")) { // y x player weapon room
			re = new int[5];
			String[] data = in.split(" ");
			for(int i = 0; i < data.length - 2; i++) {
				try {
					re[i] = Integer.parseInt(data[i + 2]);
				} catch(Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		} else if(in.startsWith("Move")) { // y x
			re = new int[2];
			String[] data = in.split(" ");
			for(int i = 0; i < data.length - 1; i++) {
				try {
					re[i] = Integer.parseInt(data[i + 1]);
				} catch(Exception e) {
					return null;
				}
			}
		} else if(in.startsWith("Guess")) { // player weapon room
			re = new int[4];
			String[] data = in.split(" ");
			for(int i = 0; i < data.length - 1; i++) {
				try {
					re[i] = Integer.parseInt(data[i + 1]);
				} catch(Exception e) {
					return null;
				}
			}
			re[3] = 0;
		}
		return re;
	}
	
	public void sendWin(int id) {
		send("Win " + id);
		try {
			end();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public void end() throws IOException {
		in.close();
		out.close();
		p.destroy();
	}
	
	public String getName() {
		return name;
	}
	
	public void sendPlayers(ArrayList<String> playerList) {
		for(int i = 0; i < playerList.size(); i++) {
			send("Personname " + playerList.size() + " " + i + " " + playerList.get(i));
		}
	}
}

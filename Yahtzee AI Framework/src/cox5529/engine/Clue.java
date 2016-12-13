package cox5529.engine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class Clue {
	
	public static String[] roomNames = { "Cooper's Lab", "Physics Lab", "Cafeteria", "Boardroom", "Alderdice's Office", "Dean Gregory's Office", "Krakowiak's Lab", "Rhuele's Lab", "Buzen's Lab", "Amstud Room", "WorldStud Room", "Comp Lab A", "Comp Lab B", "French Room", "Spanish Room", "Speech Room", "Monson's Room", "3rd Floor Lounge", "J Waddell's Room", "L Waddell's Room", "Cooper's Room", "Thompson's Room", "Krakowiak's Room", "Rhuele's Room", "Boole Lab", "5th Floor Lounge", "D Gregory's Room", "Nikki's Room", "Ulrey's Room", "Walt's Room", "Grisham's Room", "Moix's Office", "Seward's Lab", "6th floor Pine", "Frank's Lab", "Mac Lab", "2nd Floor Landing", "3rd Floor Landing" };
	public static String[] weaponNames = { "physics pendulum accelerometer", "computer keyboard", "paper", "laptop", "pen", "knife", "revolver", "rope", "wrench", "lead pipe", "cafeteria fork", "cafeteria spoon", "cafeteria knife", "car", "fire extinguisher", "door knob" };
	
	private Player[] players;
	private int[][] rooms;
	private int[][] pLocs;
	private ArrayList<String> roomList;
	private ArrayList<String> playerList;
	private ArrayList<String> weaponList;
	private int playerBase;
	
	public Clue(int size, String... players) throws IOException {
		Runtime rt = Runtime.getRuntime();
		playerList = new ArrayList<String>();
		String[] n = { "Director Alderdice", "Dean Gregory", "Kim McKean", "Dr. Ruehle", "Dr. Kostopulos", "Dr. Leigh", "Brian Isbell", "Dr. Oatsvall", "Bryan Adams", "Señor Mac", "James Katowich", "Dr. Monson", "Dr. J Waddell", "Dr. L Waddell", "Dr. Krakowiak", "Shane Thompson", "Jill Cooper", "Denise Gregory", "Nikki Zhang", "Daniel Moix", "Teacher Walt", "Caleb Grisham", "Josh Ulrey", "Nick Seward", "Carl Frank", "Fred Zipkes", "Olivia Jarrell", "Penny Lock", "Saul Broussard", "Kevin Abbott", "Nurse White", "Jason Riley", "Tim Heron, Jr.", "Alex Browning", "James Wesson", "Ashley Clayborn", "Deana Hughes", "Roxanne Easter", "Dean Currier", "Sharon Brown", "Ron Luckow", "Briana Crowe", "Raynetta Newton", "Kayla Roop" };
		playerList.addAll(Arrays.asList(n));
		playerBase = n.length;
		weaponList = new ArrayList<String>();
		weaponList.addAll(Arrays.asList(weaponNames));
		roomList = new ArrayList<String>();
		rooms = new int[size][size];
		pLocs = new int[size][size];
		ArrayList<String> temp = new ArrayList<String>(Arrays.asList(roomNames));
		int maxRoom = size;
		int i = 1;
		while(i <= maxRoom) {
			int x = (int) (Math.random() * size);
			int y = (int) (Math.random() * size);
			if(rooms[y][x] != 0) {
				continue;
			}
			rooms[y][x] = i;
			if(i == 1) {
				pLocs[y][x] = (int) (Math.pow(2, players.length) - 1);
			}
			String name = temp.remove((int) (Math.random() * temp.size()));
			roomList.add(name);
			i++;
		}
		
		this.players = new Player[players.length];
		for(int j = 0; j < players.length; j++) {
			Player p = new Player(rt.exec(players[j]));
			p.initialize(j, rooms, players.length, roomList, weaponList);
			playerList.add(p.getName());
			this.players[j] = p;
		}
		for(int j = 0; j < players.length; j++) {
			this.players[j].sendPlayers(playerList);
		}
	}
	
	public String play(long time, boolean debug) {
		int randPlayer = (int) (Math.random() * playerList.size());
		int randWeap = (int) (Math.random() * weaponList.size());
		int randRoom = (int) (Math.random() * roomList.size());
		int[] key = { randPlayer, randWeap, randRoom };
		playerList.remove(randPlayer);
		weaponList.remove(randWeap);
		roomList.remove(randRoom);
		deal(0, playerList.size() + 1, randPlayer, 0);
		deal(0, weaponList.size() + 1, randWeap, 1);
		deal(0, roomList.size() + 1, randRoom, 2);
		while(true) {
			if(players.length == 1) {
				win(players[0].getId());
				return players[0].getName();
			}
			for(int i = 0; i < players.length; i++) {
				int range = (int) (12 * Math.random()) + 1;
				int[] move = players[i].getMove(time, range);
				if(move == null) {
					removePlayer(i, "Invalid move.");
					i--;
					continue;
				}
				if(move.length == 2 || move.length == 5) {
					int y = move[0];
					int x = move[1];
					int id = players[i].getId();
					int[] cur = getCurrentPos(id);
					if(isValidMove(range, cur[0], cur[1], x, y)) {
						pLocs[cur[1]][cur[0]] -= Math.pow(2, id);
						pLocs[y][x] += Math.pow(2, id);
					} else {
						removePlayer(i, "Invalid move.");
						i--;
						continue;
					}
					for(int j = 0; j < players.length; j++) {
						if(j != i)
							players[j].sendMove(players[i].getId(), new int[] { y, x });
					}
				}
				if(move.length == 4 || move.length == 5) {
					int player = 0;
					int weap = 0;
					int room = 0;
					if(move.length == 4) {
						player = move[0];
						weap = move[1];
						room = move[2];
					} else if(move.length == 5) {
						player = move[2];
						weap = move[3];
						room = move[4];
					}
					if((room != getCurrentRoom(i) && move.length == 4 && move[3] == 0) || (player < 0 || player > playerList.size()) || (weap < 0 || weap > weaponList.size()) || (room < 0 || room > roomList.size())) {
						removePlayer(i, "Invalid guess.");
						i--;
						continue;
					}
					if(move[3] == 1 && move.length == 4) {
						if(key[0] == player && key[1] == weap && key[2] == room) {
							win(i);
							return players[i].getName();
						} else {
							removePlayer(i, "False accusation.");
							i--;
							for(int j = 0; j < players.length; j++) {
								players[j].accusation(i + 1, player, weap, room);
							}
							continue;
						}
					} else {
						ArrayList<Integer> bad = new ArrayList<Integer>();
						for(int j = 0; j < players.length; j++) {
							if(player >= playerBase && players.length > player - playerBase) {
								int[] toLoc = getRoomLocation(room);
								players[j].sendMove(player - playerBase, toLoc);
								int id = players[player - playerBase].getId();
								int[] cur = getCurrentPos(id);
								pLocs[cur[1]][cur[0]] -= Math.pow(2, id);
								pLocs[toLoc[0]][toLoc[1]] += Math.pow(2, id);
							}
							if(j != i) {
								players[j].guess(i, player, weap, room);
							}
						}
						int j = i + 1;
						if(j == players.length)
							j = 0;
						while(j != i) {
							int incor = players[j].disprove(i, player, weap, room, true);
							if(incor == 0) {
								players[i].showDisprove(j, 0, player);
							} else if(incor == 1) {
								players[i].showDisprove(j, 1, weap);
							} else if(incor == 2) {
								players[i].showDisprove(j, 2, room);
							} else if(incor == -2) {
								bad.add(j);
							}
							if(incor != -1 && incor != -2) {
								for(int k = 0; k < players.length; k++) {
									if(k != j && k != i)
										players[k].disproved(j);
								}
								break;
							}
							j++;
							if(j == players.length)
								j = 0;
						}
						for(j = 0; j < bad.size(); j++) {
							removePlayer(bad.get(j), "Gave bad response to \"Disprove\" command.");
							if(i > j) {
								i--;
							}
						}
					}
				}
			}
		}
	}
	
	public void win(int id) {
		for(int j = 0; j < players.length; j++) {
			players[j].sendWin(id);
		}
	}
	
	private void removePlayer(int id, String reason) {
		System.out.println("Player " + players[id].getId() + " was removed for reason: " + reason);
		Player[] p = new Player[players.length - 1];
		ArrayList<Integer>[] decks = players[id].getDecks();
		for(int i = 0; i < id; i++) {
			p[i] = players[i];
		}
		try {
			players[id].end();
		} catch(IOException e) {
			e.printStackTrace();
		}
		for(int i = id + 1; i < players.length; i++) {
			p[i - 1] = players[i];
		}
		players = p;
		for(int i = 0; i < decks.length; i++) {
			ArrayList<Integer> deck = decks[i];
			int play = 0;
			for(int j = 0; j < deck.size(); j++) {
				players[play].give(i, deck.get(j));
				play++;
				if(play == players.length)
					play = 0;
			}
		}
	}
	
	private int[] getRoomLocation(int id) {
		for(int i = 0; i < rooms.length; i++) {
			for(int j = 0; j < rooms.length; j++) {
				if(rooms[i][j] == id + 1)
					return new int[] { i, j };
			}
		}
		return new int[] { 0, 0 };
	}
	
	private int getCurrentRoom(int id) {
		int[] pos = getCurrentPos(id);
		int y = pos[1];
		int x = pos[0];
		return rooms[y][x] - 1;
	}
	
	private int[] getCurrentPos(int id) {
		int curX = 0;
		int curY = 0;
		for(int i = 0; i < pLocs.length; i++) {
			for(int j = 0; j < pLocs.length; j++) {
				String b = Integer.toBinaryString(pLocs[i][j]);
				if(b.length() > id && b.charAt(b.length() - id - 1) == '1') {
					curX = j;
					curY = i;
					break;
				}
			}
		}
		return new int[] { curX, curY };
	}
	
	private boolean isValidMove(int range, int curX, int curY, int x, int y) {
		return Math.abs(curX - x) + Math.abs(curY - y) <= range;
	}
	
	public static void shuffle(int[] arr) {
		for(int i = 0; i < arr.length; i++) {
			int temp = arr[i];
			int r = (int) (Math.random() * arr.length);
			arr[i] = arr[r];
			arr[r] = temp;
		}
	}
	
	private void deal(int min, int max, int skip, int type) {
		int[] cards = new int[max - min - 1];
		int val = min;
		for(int i = min; i < max - 1; i++) {
			if(val != skip)
				cards[i - min] = val++;
			else {
				cards[i - min] = ++val;
				val++;
			}
		}
		Clue.shuffle(cards);
		int player = 0;
		for(int i = 0; i < cards.length; i++) {
			players[player].give(type, cards[i]);
			player++;
			if(player == players.length)
				player = 0;
		}
	}
}

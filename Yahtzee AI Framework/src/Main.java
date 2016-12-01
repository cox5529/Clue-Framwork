import java.io.IOException;

import cox5529.engine.Clue;

public class Main {
	
	public static void main(String[] args) {
		try { 
			Clue game = new Clue(10, args);
			System.out.println(game.play(100000000000L, true));
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
}

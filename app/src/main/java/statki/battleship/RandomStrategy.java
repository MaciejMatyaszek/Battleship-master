package statki.battleship;

import java.io.Serializable;
import java.util.Random;



class RandomStrategy implements StrategyInterface, Serializable{


    static final String strategyName = "RANDOM STRATEGY";


    public String getStrategyName(){
        return strategyName;
    }


    public Place pickStrategyMove(Board board) {
        if (board == null || board.isAllHit()) {
            return null;
        }
        Random rng = new Random();
        int boardSize = board.size();

        Place toHit = null;

        while (toHit == null || toHit.isHit()){

            toHit = board.placeAt(rng.nextInt(boardSize), rng.nextInt(boardSize));
        }

        return toHit;
    }


    public void afterHit(boolean shipHit, boolean shipSunk, int x, int y){

    }
}

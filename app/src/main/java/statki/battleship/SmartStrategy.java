package statki.battleship;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.io.Serializable;


class SmartStrategy implements StrategyInterface, Serializable{






    static final String strategyName = "SMART STRATEGY";


    private Random rng = new Random();


    private int[] lastMoveHitShip = null;


    private final int UNKNOWN = 0;
    private final int UP = 1;
    private final int RIGHT = 2;
    private final int DOWN = 3;
    private final int LEFT = 4;


    private List<Integer> directionsToTry = new LinkedList<>();



    private int lastDirectionTried = UNKNOWN;



    public String getStrategyName(){
        return strategyName;
    }


    public Place pickStrategyMove(Board board){
        if(lastMoveHitShip == null || lastMoveHitShip.length < 2){
            return hunt(board);
        }
        return target(board);
    }




    private Place target(Board board){
        int lastX = lastMoveHitShip[0];
        int lastY = lastMoveHitShip[1];
        Place toHit = null;


        if(directionsToTry.size() == 0){
            toHuntMode();
            return hunt(board);
        }


        int randomDirection = directionsToTry.get(rng.nextInt(directionsToTry.size()));
        lastDirectionTried = randomDirection;

        directionsToTry.remove(Integer.valueOf(randomDirection)); //Done to prevent calling Linked list remove(int) method given index.  Intended call is method(Object)

        switch (randomDirection){
            case UP: toHit = goDirection(board, lastX, lastY, 0, -1); break;
            case RIGHT: toHit = goDirection(board, lastX, lastY, 1, 0); break;
            case DOWN: toHit = goDirection(board, lastX, lastY, 0 , 1); break;
            case LEFT: toHit = goDirection(board, lastX, lastY, -1, 0); break;
        }


        if(toHit == null){

            return target(board);
        }
        return toHit;
    }


    private Place hunt(Board board){

        lastDirectionTried = UNKNOWN;
        int boardSize = board.size();

        Place toHit = null;

        for (int i = 0; i < (boardSize*boardSize*4) && (toHit == null || toHit.isHit() || !isCheckerboardPlace(toHit)); i++){
            toHit = board.placeAt(rng.nextInt(boardSize), rng.nextInt(boardSize));
        }

        if(toHit == null || toHit.isHit() || !isCheckerboardPlace(toHit)){
            for(int i = 0; i < (boardSize*boardSize*4) && (toHit == null || toHit.isHit()); i++ ){
                toHit = board.placeAt(rng.nextInt(boardSize), rng.nextInt(boardSize));
            }
        }
        return toHit;
    }


    private boolean isCheckerboardPlace(Place place){
        return (place.getX()+place.getY()) % 2 == 0;
    }


    private void fillDirectionsList(){
        directionsToTry.add(UP);
        directionsToTry.add(DOWN);
        directionsToTry.add(LEFT);
        directionsToTry.add(RIGHT);
    }


    private Place goDirection(Board board, int x, int y, int dx, int dy){
        int newX = x+dx;
        int newY = y+dy;

        if(board.isOutOfBounds(newX, newY)){
            return target(board);
        }

        if(board.placeAt(newX,newY).isHit()){
            if(board.placeAt(newX,newY).hasShip()){
                return goDirection(board, newX, newY, dx, dy);
            }
            else{
                return target(board);
            }
        }
        return board.placeAt(newX, newY);
    }


    private void toHuntMode(){
        directionsToTry.clear();
        lastDirectionTried = UNKNOWN;
        lastMoveHitShip = null;
    }


    public void afterHit(boolean shipHit, boolean shipSunk, int x, int y){


        if(shipSunk){
            toHuntMode();
        }
        else if(shipHit) {


            if(lastDirectionTried == UNKNOWN){
                lastMoveHitShip = new int[]{x, y};
                fillDirectionsList();
            }
            else{
                directionsToTry.add(lastDirectionTried);
            }
        }

    }

}

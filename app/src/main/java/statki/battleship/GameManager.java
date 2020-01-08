package statki.battleship;

import android.util.Log;

import java.io.Serializable;



public class GameManager implements Serializable{


    private Player activePlayer;


    private Player player;


    private ComputerPlayer opponent;


    GameManager(){
        player = new Player();
        opponent = new ComputerPlayer();
        activePlayer = player;
    }


    GameManager(Board playerBoard){
        player = new Player(playerBoard);

        opponent = new ComputerPlayer();
        activePlayer = player;
    }


    GameManager(Board playerBoard, Board opponentBoard, boolean playerTurn){

        Log.d("wifiMe", "Player board is null? " + (playerBoard == null));
        player = new Player(playerBoard);
        opponent = new ComputerPlayer(opponentBoard);
        //opponent = new ComputerPlayer();
        activePlayer = player;
        if(playerTurn){
            changeTurn();
        }
    }


    int getShipsSunkCount(Player player){
        return opponent.shipsSunk();
    }


    public int getShipShots(Player player){
        return opponent.getShipHitPlaces().size();
    }


    public void setOpponentBoard(Board x){
        opponent.setBoard(x);
    }


    Player getActivePlayer(){
        return activePlayer;
    }


    private Player getInactivePlayer(){
        if(activePlayer == player){
            return opponent;
        }
        return player;
    }


    Player getPlayer(){
        return player;
    }


    Player getOpponentPlayer(){
        return opponent;
    }


    void computerPlay(Place place){
        Board opponentBoard = player.getBoard();
        boolean hitShip = false;
        boolean sunkShip = false;
        if(opponentBoard.hit(place)){
            if(place.hasShip()){

                hitShip = true;
                //Then computer sunk a ship
                if(place.getShip().isShipSunk()){
                    sunkShip = true;
                }
            }
            opponent.getStrategyInterface().afterHit(hitShip, sunkShip, place.getX(), place.getY());
            return;
        }
        Log.d("", "Computer opponent tried to hit invalid place");
    }


    Place computerPickPlace(){
        Board opponentBoard = player.getBoard();
        return opponent.pickPlace(opponentBoard);
    }


    void changeStrategy(String strategyName){
        opponent.changeStrategy(strategyName);
    }


    boolean hitPlace(int x, int y){
        Board board = getInactivePlayer().getBoard();
        return board.hit(board.placeAt(x,y));
    }

     void changeTurn(){
        if(activePlayer == player){
            activePlayer = opponent;
        }
        else{
            activePlayer = player;
        }
    }
}

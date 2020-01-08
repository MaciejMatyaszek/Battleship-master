package statki.battleship;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class Board implements Serializable {



    private final int size;


    private Place[][] board = null;


    private int placesShot = 0;

    public Board(){
        this(10);
    }


    public Board(int size) {
        this.size = size;
        board = new Place[size()][size()];
        createBoard(board);
    }


    private void createBoard(Place[][] board){

        for(int y = 0; y < board.length; y++){
            for(int x = 0; x < board[0].length; x++){
                board[y][x] = new Place(x, y);
            }
        }
    }


    boolean placeShip(Ship ship, int x, int y, boolean dir){

        if(ship == null){
            return false;
        }

        removeShip(ship);

        List<Place> shipPlaces = new ArrayList<Place>();
        Place place;

        //Goes through places where ship will be placed.*/
        for(int i = 0; i < ship.getSize(); i++){

            if(dir){
                place = placeAt(x+i, y);
            }
            else{
                place = placeAt(x, y+i);
            }


            if(place == null || place.hasShip()) {
                return false;
            }


            for (int k=place.getX()-1; k<=place.getX()+1; k++){


                    for (int j = place.getY() - 1; j <= place.getY() + 1; j++) {



                            Place placeTest;
                        System.out.println(k+"|"+j);
                            placeTest = placeAt(k, j);
                            if (placeTest == null) {

                                continue;
                            }
                            if (placeTest.hasShip()) {
                                return false;

                            }


                    }

            }







            shipPlaces.add(place);
        }


        for(Place placeWithShip: shipPlaces){
            placeWithShip.setShip(ship);
        }

        ship.setDir(dir);
        ship.placeShip(shipPlaces);

        return true;
    }


    private void removeShip(Ship ship){
        for(int i = 0; i < board.length; i++){
            for(int j = 0; j < board[0].length; j++){
                if(board[i][j].hasShip(ship)){
                    board[i][j].removeShip();
                }
            }
        }
        ship.removeShip();
    }


    Place placeAt(int x, int y){
        if(board == null || isOutOfBounds(x,y) || board[y][x] == null){
            return null;
        }

        return board[y][x];
    }


    boolean hit(Place placeToHit){
        if(placeToHit == null){
            return false;
        }
        //If place hasn't been hit before, then hits the place.
        if(!placeToHit.isHit()){
            placesShot++;
            placeToHit.hit();
            return true;
        }
        return false;
    }


    boolean isOutOfBounds(int x, int y){
        return x >= size() || y >= size() || x < 0 || y < 0;
    }


    int numOfShots(){
        return placesShot;
    }

    int size() {
        return size;
    }


    List<Place> getShipHitPlaces() {

        List<Place> boardPlaces = getPlaces();
        List<Place> shipHitPlaces = new ArrayList<Place>();

        for (Place place : boardPlaces) {
            if (place.isHit() && place.hasShip()) {
                shipHitPlaces.add(place);
            }
        }
        return shipHitPlaces;
    }


    private List<Place> getPlaces(){
        List<Place> boardPlaces = new LinkedList<Place>();
        for(int i = 0; i < size(); i++){
            for(int j = 0; j < size(); j++){
                boardPlaces.add(board[i][j]);
            }
        }
        return boardPlaces;
    }


    boolean isAllSunk(){
        for(int i = 0; i < size(); i++){
            for(int j = 0; j < size(); j++){
                Place place = board[i][j];
                if(place.hasShip() && !place.isHit()){
                    return false;
                }
            }
        }
        return true;
    }


    boolean isAllHit(){
        for(int i = 0; i < size(); i++){
            for(int j = 0; j < size(); j++){
                if(!board[i][j].isHit()){
                    return false;
                }
            }
        }
        return true;
    }

    /*public Board(String msg){
        size = 10;
        board = new Place[size][size];
        int i = 0;
        int j = 0;
        for(int k = 0; k < msg.length(); k++){
            char letter = msg.charAt(k);
            if(letter == '0' || letter == '1' || letter == '2'){
                board[j][i] = new Place(i,j);
            }
            if(letter == '1'){
                board[j][i].setShip(new Ship(3));
            }
            if(letter == '2'){
                board[j][i].hit();
            }

        }
    }*/

    /*@Override
    public String toString(){
        String boardString = "";
        if(board == null){
            return "Board is null";
        }
        for(int i = 0; i < board[0].length; i++){
            for(int j = 0; j < board.length; j++){
                Place place = board[i][j];
                if(place == null){
                    boardString = boardString + "?";
                }
                else if(place.hasShip()){
                    boardString = boardString + "1";
                }
                else if(place.isHit()){
                    boardString = boardString + "2";
                }
                else{
                    boardString = "0";
                }
            }
            boardString = boardString + "n";
        }

        return boardString;
    }*/

    @Override
    public String toString(){
        String boardString = "";
        if(board == null){
            return "Board is null";
        }
        for(int i = 0; i < board[0].length; i++){
            for(int j = 0; j < board.length; j++){
                Place place = board[i][j];
                Ship ghost = place.getShip();

                if(ghost != null){
                    String shipType = ghost.getName();
                    if(shipType.contains("aircraftcarrier"))
                        boardString += "5";
                    else if(shipType.contains("battleship"))
                        boardString += "4";
                    else if(shipType.contains("submarine"))
                        boardString += "3";
                    else if(shipType.contains("frigate"))
                        boardString += "2";
                    else //Sweeper
                        boardString += "1";
                }
                //empty place
                else{
                    boardString += "0";
                }
            }
        }

        return boardString;
    }

}

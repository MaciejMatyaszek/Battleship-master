package statki.battleship;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;



class Ship implements Serializable {


    private String name;


    private int size;


    private boolean isSunk = false;


    private int amountShot = 0;


    private boolean dir = true;


    private List<Place> placed = new ArrayList<>();

    public Ship(int size){
        this.size = size;
        name = " ";
    }
    Ship(String name, int size){
        this.name = name;
        this.size = size;
    }


    int getSize(){
        return size;
    }

    public String getName(){
        return name;
    }


    void placeShip(List<Place> places){
        placed = places;
    }


    public int getAmountShot(){
        updateAmountShot();
        return amountShot;
    }


    private void updateAmountShot(){
        amountShot = 0;
        for (Place place : placed) {
            if(place.isHit()) {
                amountShot++;
            }
        }
    }


    void setDir(boolean newDir){
        dir = newDir;
    }


    boolean getDir(){
        return dir;
    }


    boolean isPlaced(){
        return !placed.isEmpty();
    }


    boolean isShipSunk(){


        if(isSunk){
            return true;
        }

        if(placed == null){
            return false;
        }
        updateAmountShot();

        isSunk = (size <= amountShot);
        return isSunk;
    }


    List<Place> getPlacement(){
        return placed;
    }


    void removeShip(){
        placed.clear();
    }
}

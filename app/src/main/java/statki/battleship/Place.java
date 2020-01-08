package statki.battleship;

import java.io.Serializable;


public class Place implements Serializable {


    private int x = 0;


    private int y = 0;


    private boolean isHit = false;


    private Ship ship = null;



    public Place(int x, int y){
        this.x = x;
        this.y = y;
    }


    boolean isHit(){
        return isHit;
    }


    void hit() {
        isHit = true;
    }


    public int getX(){
        return x;
    }


    public int getY(){
        return y;
    }


    boolean hasShip() {
        return ship != null;
    }


    boolean hasShip(Ship shipToCheck) {
        return ship == shipToCheck;
    }


    void removeShip(){
        ship = null;
    }


    protected void setShip(Ship ship){
        this.ship = ship;
    }


    public Ship getShip(){
        return ship;
    }

}
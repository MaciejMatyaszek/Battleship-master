package statki.battleship;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;



class Player implements Serializable {



    private Board playerBoard = null;


    private List<Ship> fleet = new LinkedList<>();


    Player() {
        playerBoard = new Board();

        placeShipRandomly(playerBoard, new Ship("Minesweeper", 2));
        placeShipRandomly(playerBoard, new Ship("Frigate", 3));
        placeShipRandomly(playerBoard, new Ship("Submarine", 3));
        placeShipRandomly(playerBoard, new Ship("Battleship", 4));
        placeShipRandomly(playerBoard, new Ship("Aircraft carrier", 5));
    }


    Player(Board board) {
        setBoard(board);
    }


    private Ship placeShipRandomly(Board board, Ship ship) {
        Random rng = new Random();
        boolean dir = rng.nextBoolean();


        int[] maxCoordinates = findMaxLocation(board.size(), ship.getSize(), dir);


        if (maxCoordinates == null) {
            return null;
        }

        int maxX = maxCoordinates[0];
        int maxY = maxCoordinates[1];

        boolean placedShip = false;

        while (!placedShip) {

            int x = rng.nextInt(maxX);
            int y = rng.nextInt(maxY);


            if (board.placeShip(ship, x, y, dir)) {
                placedShip = true;
            }
        }

        fleet.add(ship);
        return ship;
    }


    private int[] findMaxLocation(int boardSize, int shipSize, boolean dir) {

        int maxX = boardSize;
        int maxY = boardSize;
        if (dir) {
            maxX = boardSize - shipSize;
        } else {
            maxY = boardSize - shipSize;
        }

        if (maxX < 0 || maxY < 0) {
            return null;
        }

        return new int[]{maxX, maxY};
    }


    public Board getBoard() {
        return playerBoard;
    }


    List<Place> getShipHitPlaces() {
        List<Place> shipHitPlaces = new LinkedList<>();

        for (Ship ship : fleet) {
            List<Place> shipPlaces = ship.getPlacement();
            for (Place shipPlace : shipPlaces) {
                if (shipPlace.isHit()) {
                    shipHitPlaces.add(shipPlace);
                }
            }

        }
        return shipHitPlaces;
    }


    int shipsSunk() {
        int sunkShips = 0;

        for (Ship ship : fleet) {
            if (ship.isShipSunk()) {
                sunkShips++;
            }
        }
        return sunkShips;
    }


    public void setBoard(Board newBoard) {
        playerBoard = newBoard;
        updateShipsFromBoard(newBoard);
    }


    private void updateShipsFromBoard(Board board) {
        Place place;
        for (int i = 0; i < board.size(); i++) {
            for (int j = 0; j < board.size(); j++) {
                place = board.placeAt(i, j);
                if (place != null && place.hasShip()) {
                    addShip(place.getShip());
                }
            }
        }
    }



    private void addShip(Ship shipToAdd) {
        for (Ship ship : fleet) {

            if (ship == shipToAdd) {
                return;
            }
        }
        fleet.add(shipToAdd);
    }


    boolean areAllShipsSunk() {

        return getBoard().isAllSunk();
        /*for (Ship ship : fleet) {
            if(!ship.isShipSunk()){   //TODO remove after testing is done
                return false;
            }
        }
        return true;*/

    }
}

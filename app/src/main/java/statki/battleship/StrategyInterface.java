package statki.battleship;


interface StrategyInterface {


    String getStrategyName();


    Place pickStrategyMove(Board board);


    void afterHit(boolean shipHit, boolean shipSunk, int x, int y);

}

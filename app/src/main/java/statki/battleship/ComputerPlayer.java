package statki.battleship;

import java.io.Serializable;



class ComputerPlayer extends Player implements Serializable {


    private StrategyInterface strategyInterface;

    ComputerPlayer(){
        super();
        strategyInterface = new RandomStrategy();
    }

    ComputerPlayer(Board board){
        super();
        super.setBoard(board);
        strategyInterface = new RandomStrategy();
    }


    Place pickPlace(Board opponentBoard){
        return strategyInterface.pickStrategyMove(opponentBoard);
    }


    void changeStrategy(String strategyName){
        if(strategyName == null){
            return;
        }
        if(strategyName.equals(SmartStrategy.strategyName)){
            strategyInterface = new SmartStrategy();
        }
        else if(strategyName.equals(RandomStrategy.strategyName)){
            strategyInterface = new RandomStrategy();
        }
    }


    StrategyInterface getStrategyInterface(){
        return strategyInterface;
    }

}

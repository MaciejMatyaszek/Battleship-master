package statki.battleship;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import statki.battleship.R;

import static java.lang.Thread.sleep;


public class MainActivity extends AppCompatActivity {



    private BoardView playerBoardView;


    private BoardView opponentBoardView;


    private GameManager game;


    private TextView strategyDescription;


    private TextView gameStatus;


    private Button opponentSelect;


    private MediaPlayer mp;


    private boolean soundEnabled = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();

        //Gets Game Manager from previous activity or makes a new one
        if (intent == null) {
            game = new GameManager();
        } else {
            Bundle oldBundle = intent.getBundleExtra("gameManager");
            if (oldBundle == null) {
                game = new GameManager();
            } else {
                game = (GameManager) oldBundle.getSerializable("gameManager");
                Log.d("wifiMe", "is Game null? " + (game == null));
                if (game == null) {
                    game = new GameManager();
                }
            }
        }


        opponentBoardView = (BoardView) findViewById(R.id.opponentBoardView);
        playerBoardView = (BoardView) findViewById(R.id.playerBoardView);
        strategyDescription = (TextView) findViewById(R.id.strategy_description);
        gameStatus = (TextView) findViewById(R.id.gameStatus);
        opponentSelect = (Button) findViewById(R.id.opponentSelect);

        //Gives board references to the BoardViews
        setNewBoards(playerBoardView, opponentBoardView, game.getPlayer().getBoard(), game.getOpponentPlayer().getBoard());
        updateTurnDisplay();

        if (NetworkAdapter.hasConnection()) {
            //if there is a multiplayer game, disable the AI difficulty change setting
            opponentSelect.setEnabled(false);
            strategyDescription.setText(getString(R.string.wifi_p2p_opponent));
            startReadingNetworkMessages();
        } else {
//            toast("No connection with opponent"); //TODO used for debugging remove before submission, or add something else to indicate not connected
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        //Restores Game State for after an orientation happens
        game = (GameManager) savedInstanceState.getSerializable("game");
        setNewBoards(playerBoardView, opponentBoardView, game.getPlayer().getBoard(), game.getOpponentPlayer().getBoard());
        updateTurnDisplay();
        updateBoards();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {

        super.onSaveInstanceState(bundle);
        bundle.putSerializable("game", game);
    }


    private void setNewBoards(BoardView playerBoardView, BoardView opponentBoardView, Board playerBoard, Board opponentBoard) {

        playerBoardView.setBoard(playerBoard);
        opponentBoardView.setBoard(opponentBoard);

        playerBoardView.displayBoardsShips(true);
//        opponentBoardView.displayBoardsShips(true); //TODO REMOVE TO PREVENT CHEATING
        opponentBoardView.addBoardTouchListener(new BoardView.BoardTouchListener() {
            @Override
            public void onTouch(int x, int y) {
                boardTouched(x, y);
            }
        });

        updateBoards();
    }


    void startReadingNetworkMessages() {

        Thread readMessages = new Thread(new Runnable() {
            public void run() {

                while (true) {
                    String msg = NetworkAdapter.readMessage();
                    Log.d("wifiMe", "Message received: " + msg);
                    Log.d("wifiMe", msg);
                    if (msg == null) {
                        //Connection lost handler
                        Log.d("wifiMe", "Connection Lost!, in");
                        toast("Connection Lost! Now playing single player game against computer");
                        //allow user to change AI difficulty again
                        opponentSelect.setEnabled(true);
                        return;
                    } else if (msg.startsWith(NetworkAdapter.PLACED_SHIPS)) {
                        Log.d("wifiMe", "Received place ships message?? Shouldn't have found one, debug");
                    } else if (msg.startsWith(NetworkAdapter.NEW_GAME)) {
                        Log.d("wifiMe", "New game requested, dialog given with yes or no options to accept or reject request"); //should send accept message message and reset game
                        resetPromptDialog(getString(R.string.reset_game_connected_prompt), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                if (NetworkAdapter.hasConnection()) {
                                    NetworkAdapter.writeAcceptNewGameMessage();
                                    NetworkAdapter.writeStopReadingMessage();
                                }
                                segueToPlaceShipsActivity();
                            }
                        }, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        NetworkAdapter.writeRejectNewGameMessage();
                                    }
                                }).start();

                            }
                        });
                    } else if (msg.startsWith(NetworkAdapter.REJECT_NEW_GAME_REQUEST)) {
                        toast("New game request rejected  by other player");
                    } else if (msg.startsWith(NetworkAdapter.ACCEPT_NEW_GAME_REQUEST)) {
                        Log.d("wifiMe", "Accepted new game request");  //should send accept message message

                        if (NetworkAdapter.hasConnection()) {
                            NetworkAdapter.writeStopReadingMessage();
                        }
                        segueToPlaceShipsActivity();
                    } else if (msg.contains(NetworkAdapter.STOP_READING)) { //TODO remove if everything is broken
                        Log.d("wifiMe", "STOPPED READING MESSAGE IN MAINACTIVITY CLASS");
                        return;
                    } else if (msg.contains(NetworkAdapter.PLACE_SHOT)) {
                        Log.d("wifiMe", "Place was shot message received, message: " + msg);
                        int[] placeShot = NetworkAdapter.decipherPlaceShot(msg);
                        if (placeShot == null) {
                            Log.d("wifiMe", "Found no coordinates");
                            return;
                        }
                        Log.d("wifiMe", "Placed shot on: " + placeShot[0] + ", " + placeShot[1]);
                        p2pOpponentPlay(placeShot[0], placeShot[1]);

                    }

                }
            }
        });
        readMessages.start();
    }


    public void resetGame(View view) {
        System.out.println(NetworkAdapter.hasConnection());
        if (NetworkAdapter.hasConnection()) {
            NetworkAdapter.writeNewGameMessage();
            toast("New game will start when other player accepts request");
        }

        else if (game.getActivePlayer().getBoard().numOfShots() == 0 || game.getActivePlayer().areAllShipsSunk()) {
            resetGame();
        } else {
            resetPromptDialog(getString(R.string.reset_game_prompt), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent menu = new Intent(MainActivity.this, MainMenu.class);
                    finish();
                    startActivity(menu);
                }
            }, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                }
            });
        }




    }


    public void resetPromptDialog(final String message, final DialogInterface.OnClickListener acceptListener, final DialogInterface.OnClickListener rejectListener) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                alertDialog.setTitle(getString(R.string.reset_game_title));
                alertDialog.setMessage(message);//(getString(R.string.reset_game_prompt)


                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "YES", acceptListener);


                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "NO", rejectListener);
                alertDialog.show();
            }
        });

    }



    public void resetGame() {
        game = new GameManager();
        if (NetworkAdapter.hasConnection()) {
            strategyDescription.setText(getString(R.string.wifi_p2p_opponent));
            setNewBoards(playerBoardView, opponentBoardView, game.getPlayer().getBoard(), game.getOpponentPlayer().getBoard());
        }
        if (strategyDescription.getText().equals(getString(R.string.random_opponent))) {

            Intent test= new Intent(MainActivity.this, PlaceShipsActivity.class);
            finish();
            startActivity(test);
        } else {
            game.changeStrategy(SmartStrategy.strategyName);
            Intent test= new Intent(MainActivity.this, PlaceShipsActivity.class);
            finish();
            startActivity(test);
        }

    }


    public void playSound(int soundResourceId) {
        if (!soundEnabled) {
            return;
        }
        if (mp != null) {
            mp.stop();
            mp.reset();
            mp.release();
            mp = null;
        }

        mp = MediaPlayer.create(this, soundResourceId);
        mp.start();
    }


    public void boardTouched(final int x, final int y) {

        Place placeToHit = game.getOpponentPlayer().getBoard().placeAt(x, y);

        boolean isGameOver = game.getPlayer().areAllShipsSunk() || game.getOpponentPlayer().areAllShipsSunk();
        boolean computersTurn = game.getActivePlayer() != game.getPlayer();
        boolean placeAlreadyHit = placeToHit.isHit();


        if (isGameOver || computersTurn || placeAlreadyHit) {

            return;
        }

        game.hitPlace(x, y);



        if (placeToHit.hasShip()) {
            playSound(R.raw.shiphit);
        }

        else {
            game.changeTurn();
            playSound(R.raw.miss);
        }
        updateTurnDisplay();
        updateBoards();
        boolean playerWon = game.getOpponentPlayer().areAllShipsSunk();

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (NetworkAdapter.hasConnection()) {
                    Log.d("wifiMe", "Wrote message to opponent for placing shot");
                    NetworkAdapter.writePlaceShotMessage(x, y);
                }
            }
        }).start();

        if (playerWon) {
            updateWinDisplay(true);
            resultsDialog(true, game.getShipsSunkCount(game.getPlayer()));
            return;
        }


        if (!NetworkAdapter.hasConnection()) {
            Log.d("wifiMe", "Computer made a play");
            boolean isComputersTurn = game.getActivePlayer() != game.getPlayer();
            if (isComputersTurn) {
                computerTurn();
            }
        }
    }


    public void updateTurnDisplay() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (game.getOpponentPlayer() == game.getActivePlayer()) {
                    if (NetworkAdapter.hasConnection()) {
                        gameStatus.setText(getString(R.string.opponent_turn_status));
                    } else {
                        gameStatus.setText(getString(R.string.computer_turn_status));
                    }
                } else {
                    gameStatus.setText(getString(R.string.player_turn_status));
                }
            }
        });
    }


    public void updateWinDisplay(final boolean playerWon) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (playerWon) {
                    gameStatus.setText(getString(R.string.win_status));
                } else {
                    gameStatus.setText(getString(R.string.lose_status));
                }
            }
        });
    }


    public void p2pOpponentPlay(int x, int y) {
        Place placeToHit = game.getPlayer().getBoard().placeAt(x, y);

        game.hitPlace(x, y);
        updateBoards();


        if (placeToHit.hasShip()) {
            playSound(R.raw.shiphit);
        }

        else {
            game.changeTurn();
            updateTurnDisplay();
            playSound(R.raw.miss);
        }


        boolean p2pOpponentWon = game.getPlayer().areAllShipsSunk();
        if (p2pOpponentWon) {
            updateWinDisplay(false);
            resultsDialog(true, game.getShipsSunkCount(game.getPlayer()));
            return;
        }
    }


    private void segueToPlaceShipsActivity() {
        final Context activity = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(activity, PlaceShipsActivity.class);
                startActivity(intent);
            }
        });

    }


    public void computerTurn() {

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Place placeToHit = game.computerPickPlace();

                    //Prevents computer from placing immediately after player places
                    sleep(450);

                    game.computerPlay(placeToHit);

                    boolean hitShip = placeToHit.hasShip();


                    if (hitShip) {
                        playSound(R.raw.shiphit);

                        boolean sunkShip = placeToHit.getShip().isShipSunk();
                        if (sunkShip) {

                        }
                    } else {
                        game.changeTurn();
                        updateTurnDisplay();
                        playSound(R.raw.miss);
                    }
                    final boolean computerWon = game.getPlayer().areAllShipsSunk();
                    updateBoards();
                    if (computerWon) {
                        updateWinDisplay(false);
                        resultsDialog(false, game.getShipsSunkCount(game.getPlayer()));
                    }

                    if (computerWon) {
                        return;
                    }
                    sleep(250); //Prevents player from place immediately after the computer places

                    if (placeToHit.hasShip()) {
                        computerTurn();
                    }
                } catch (InterruptedException e) {
                    Log.d("", "Exception thrown in computerTurn() method in MainActivity");
                    computerTurn();
                }
            }
        });

        thread.start();
    }


    public void updateBoards() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                opponentBoardView.invalidate();
                playerBoardView.invalidate();
            }
        });
    }


    public void showOpponentSelectPopup(View v) {
        PopupMenu popup = new PopupMenu(this, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.opponent_selection, popup.getMenu());
        popup.show();

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                String newStrategyName = null;
                String newStrategyDescription = "";
                switch (item.getItemId()) {
                    case R.id.smart_opponent:
                        newStrategyName = SmartStrategy.strategyName;
                        newStrategyDescription = getString(R.string.smart_opponent);
                        break;
                    case R.id.random_opponent:
                        newStrategyName = RandomStrategy.strategyName;
                        newStrategyDescription = getString(R.string.random_opponent);
                        break;
                    default:
                        break;
                }
                game.changeStrategy(newStrategyName);
                strategyDescription.setText(newStrategyDescription);
                return true;
            }
        });
    }


    private void resultsDialog(final boolean winner, final int shipsSunk) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                String title, description;
                if (winner) {
                    title = getString(R.string.winner_title);
                    description = getString(R.string.winner_description);
                } else {
                    title = getString(R.string.loser_title);
                    description = getString(R.string.loser_description);
                }
                alertDialog.setTitle(title);
                alertDialog.setMessage(description);

                //Ok button
                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                alertDialog.show();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_bar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            //Sound is a toggle for when to disable and enable
            case R.id.sound:
                soundEnabled = !soundEnabled;
                if (soundEnabled) {
                    item.setTitle(getString(R.string.disable_sound));
                } else {
                    item.setTitle(getString(R.string.enable_sound));
                }
                return true;
            case R.id.menu:

                finish();
                Intent i = new Intent(this, MainMenu.class);
                startActivity(i);
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void toast(final String s) {
        //Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
        final Context context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

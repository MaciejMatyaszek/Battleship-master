package statki.battleship;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.List;

import statki.battleship.R;

import static java.lang.Thread.sleep;

public class PlaceShipsActivity extends AppCompatActivity {


    private BoardView boardView;


    private Board playerBoard;


    private ShipView shipBeingDragged = null;


    private List<ShipView> fleetView = new LinkedList<>();


    private Button placeButton;



    private Board opponentBoard = null;


    private boolean donePlacingShips = false;

    private Thread readMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RelativeLayout layout = (RelativeLayout) getLayoutInflater().inflate(R.layout.content_place_ships, null);
        setContentView(layout);

        boardView = (BoardView) findViewById(R.id.placeShipsBoardView);
        playerBoard = new Board();
        boardView.setBoard(playerBoard);
        boardView.displayBoardsShips(true);

        placeButton = (Button) findViewById(R.id.placeButton);
        enablePlaceButton(false);


        ImageView minesweeper = (ImageView) findViewById(R.id.minesweeperStatus);
        ImageView frigate = (ImageView) findViewById(R.id.frigate);
        ImageView submarine = (ImageView) findViewById(R.id.submarine);
        ImageView battleship = (ImageView) findViewById(R.id.battleship);
        ImageView aircraftcarrier = (ImageView) findViewById(R.id.aircraftcarrier);


        fleetView.add(new ShipView(minesweeper, new Ship("minesweeper", 2)));
        fleetView.add(new ShipView(frigate, new Ship("frigate", 3)));
        fleetView.add(new ShipView(submarine, new Ship("submarine", 3)));
        fleetView.add(new ShipView(battleship, new Ship("battleship", 4)));
        fleetView.add(new ShipView(aircraftcarrier, new Ship("aircraftcarrier", 5)));

        for (ShipView shipView : fleetView) {
            setShipImage(shipView);
        }

        setContentView(layout);

        setBoardDragListener(boardView, playerBoard);

        boardView.invalidate();

        Log.d("wifiMe", "Is Socket null? " + (NetworkAdapter.getSocket() == null));
        if (NetworkAdapter.hasConnection()) {
            startReadingMessage();
        } else {
//            toast("No connection with opponent"); //TODO used for debugging remove before submission, or add something else to indicate not connected
        }

    }



    public void setBoardDragListener(final BoardView boardView, final Board board) {
        boardView.setOnDragListener(new View.OnDragListener() {
            @Override
            public boolean onDrag(View v, DragEvent event) {
                switch (event.getAction()) {
                    case DragEvent.ACTION_DRAG_STARTED:
                        break;

                    case DragEvent.ACTION_DRAG_ENTERED:
                        break;

                    case DragEvent.ACTION_DRAG_EXITED:
                        break;

                    case DragEvent.ACTION_DRAG_LOCATION:
                        break;

                    case DragEvent.ACTION_DRAG_ENDED:
                        break;

                    case DragEvent.ACTION_DROP:


                        float x = event.getX();
                        float y = event.getY();
                        int width;
                        int height;

                        if (!shipBeingDragged.getShip().getDir()) {
                            width = shipBeingDragged.getShipImage().getHeight();
                            height = shipBeingDragged.getShipImage().getWidth();

                        } else {
                            width = shipBeingDragged.getShipImage().getWidth();
                            height = shipBeingDragged.getShipImage().getHeight();
                        }

                        //x and y coordinates of top-left of image, relative to the board
                        float boardX = x - (width / 2);
                        float boardY = y - (height / 2);

                        int xy = boardView.locatePlace(boardX, boardY);
                        if (xy == -1) {
                            return true;
                        }
                        int xGrid = xy / 100;
                        int yGrid = xy % 100;

                        if (!board.placeShip(shipBeingDragged.getShip(), xGrid, yGrid, shipBeingDragged.getShip().getDir())) {
                            return true;
                        }

                        if (!shipBeingDragged.getShip().getDir()) {
                            shipBeingDragged.getShipImage().setX(v.getX() + (xGrid * (v.getWidth() / 10)) - (height / 2) + (width / 2));
                            shipBeingDragged.getShipImage().setY(v.getY() + (yGrid * (v.getHeight() / 10)) + (height / 2) - (width / 2));

                        } else {
                            shipBeingDragged.getShipImage().setX(v.getX() + (xGrid * (v.getWidth() / 10)));
                            shipBeingDragged.getShipImage().setY(v.getY() + (yGrid * (v.getHeight() / 10)));
                        }


                        boardView.invalidate();
                        if (allShipsPlaced()) {
                            enablePlaceButton(true);
                        }
                        break;

                    default:
                        break;
                }
                return true;
            }
        });
    }


    void startReadingMessage() {

        readMessages = new Thread(new Runnable() {
            public void run() {

                try {
                    sleep(1000); //Waits 1 seconds before starting to read messages, done to ensure stopReadingMessages is taken and applied to correct thread.
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                while (true) {
                    String msg = NetworkAdapter.readMessage();
                    Log.d("wifiMe", "Message received IN PLACE SHIP ACTIVITY: " + msg);

                    if (msg == null) {
                        //Connection lost handler
                        Log.d("wifiMe", "Connection Lost!");
                        toast("Connection Lost! Now playing single player game against computer");
                        Log.d("wifiMe", "Has connection? " + NetworkAdapter.hasConnection());
                        return;
                    }
                    else if(msg.contains(NetworkAdapter.STOP_READING)){
                        return;
                    }
                    else if (msg.startsWith(NetworkAdapter.PLACED_SHIPS)) {
                        Log.d("wifiMe", "Found board message");


                        //Gets board
                        opponentBoard = NetworkAdapter.decipherPlaceShips(msg);
                        Log.d("wifiMe", "Decipher done"); //Why does it sometimes not reach this message, if donePlacingShips is true?
                        //If you are already done placing ships, and you have received your opponent's board, then startActivity
                        if (donePlacingShips) {
                            //readMessages.interrupt();
                            NetworkAdapter.writeStopReadingMessage();

                            GameManager game = new GameManager(playerBoard, opponentBoard, true);
                            segueToActivity(game);

                            //return;
                        }
                    }


                }
            }
        });
        readMessages.start();
    }


    /*private void readTheirBoard() {

        int timeout = 20;
        while (timeout > 0) {
            String msg = NetworkAdapter.readMessage();
            Log.d("wifiMe", "Message received: " + msg);

            if (msg == null) {
                toast("Waiting on other player to place their ships");

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                return;
            } else if (msg.startsWith(NetworkAdapter.PLACED_SHIPS)) {
                Log.d("wifiMe", "Found board message");

                //Gets board
                opponentBoard = NetworkAdapter.decipherPlaceShips(msg);
                Log.d("wifiMe", "Decipher done"); //Why does it sometimes not reach this message, if donePlacingShips is true?
                //If you are already done placing ships, and you have received your opponent's board, then startActivity
            } else {
                toast("Opponent timed out.");
                //TODO: Throw some error, since message is not what we expected it to be.
            }

            timeout--;
        }
    }*/



    public boolean allShipsPlaced() {
        for (ShipView ship : fleetView) {
            if (ship.getShip() == null) {
                return false;
            }

            if (!ship.getShip().isPlaced()) {
                return false;
            }
        }
        return true;
    }


    public void segueToActivity(final GameManager game) {
        final PlaceShipsActivity activity = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(activity, MainActivity.class);

                Bundle bundle = new Bundle();

                bundle.putSerializable("gameManager", game);
                i.putExtra("gameManager", bundle);
                startActivity(i);
            }
        });


    }



    public void segueToPlayActivity(View view) {

        donePlacingShips = true;

        new Thread(new Runnable() {
            @Override
            public void run() {
                //If there is a p2p connection
                if (NetworkAdapter.getSocket() != null) {
                    NetworkAdapter.writeBoardMessage(playerBoard);
                    Log.d("wifiMe", "Board was sent");
                    //If other player has given us their board
                    if (opponentBoard != null) {
                        NetworkAdapter.writeStopReadingMessage();
                        GameManager game = new GameManager(playerBoard, opponentBoard, false);
                        segueToActivity(game);
                    } else {
                        toast("Game will start when the other player places their ships");
                        return;
                    }
                }
                else {
                    Log.d("wifiMe", "Not playing wifi game");
                    GameManager game = new GameManager(playerBoard);
                    segueToActivity(game);
                }
            }
        }).start();


    }



    private void setShipImage(final ShipView shipView) {
        setImageScaling(shipView.getShipImage());
        setTouchListener(shipView);
    }


    private void setTouchListener(final ShipView shipView) {
        final ImageView image = shipView.getShipImage();
        image.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {

                    ClipData data = ClipData.newPlainText("", "");


                    double rotationRad = Math.toRadians(image.getRotation());
                    final int w = (int) (image.getWidth() * image.getScaleX());
                    final int h = (int) (image.getHeight() * image.getScaleY());
                    double s = Math.abs(Math.sin(rotationRad));
                    double c = Math.abs(Math.cos(rotationRad));
                    final int width = (int) (w * c + h * s);
                    final int height = (int) (w * s + h * c);
                    View.DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(image) {
                        @Override
                        public void onDrawShadow(Canvas canvas) {
                            canvas.scale(image.getScaleX(), image.getScaleY(), width / 2,
                                    height / 2);
                            canvas.rotate(image.getRotation(), width / 2, height / 2);
                            canvas.translate((width - image.getWidth()) / 2,
                                    (height - image.getHeight()) / 2);
                            super.onDrawShadow(canvas);
                        }

                        @Override
                        public void onProvideShadowMetrics(Point shadowSize,
                                                           Point shadowTouchPoint) {
                            shadowSize.set(width, height);
                            shadowTouchPoint.set(shadowSize.x / 2, shadowSize.y / 2);
                        }
                    };

                    image.startDrag(data, shadowBuilder, image, 0);
                    //image.setVisibility(View.INVISIBLE);
                    shipBeingDragged = shipView;
                    deselectAllShipViews();
                    select(shipView);

                    return true;
                } else {
                    return false;
                }
            }

        });
    }


    public void rotateButtonTapped(View v) {
        ShipView shipToRotate = findSelectedShip();
        if(shipToRotate != null) {
            rotateShip(shipToRotate);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int height = displayMetrics.heightPixels;
            int width = displayMetrics.widthPixels;
            shipToRotate.getShipImage().setX(width / 3 + 10);
            shipToRotate.getShipImage().setY((height / 4) - 20);

            enablePlaceButton(false);

            if (shipToRotate.getShip() != null) {
                for (Place place : shipToRotate.getShip().getPlacement()) {
                    place.setShip(null);
                }
                shipToRotate.getShip().removeShip();
            }

            shipToRotate.getShipImage().setOnTouchListener(null);
            setTouchListener(shipToRotate);
            boardView.invalidate();
        }
        }



    private void enablePlaceButton(Boolean enable) {
        if (enable) {
            placeButton.setEnabled(true);
            placeButton.setTextColor(Color.WHITE);
            placeButton.setBackgroundColor(Color.rgb(102, 153, 0));
        } else {
            placeButton.setBackgroundColor(Color.rgb(75, 120, 30));
            placeButton.setTextColor(Color.rgb(115, 115, 115));
            placeButton.setEnabled(false);
        }
    }


    private ShipView findSelectedShip() {
        for (ShipView shipView : fleetView) {
            if (shipView.isSelected()) {
                return shipView;
            }
        }
        return null;
    }


    private void rotateShip(ShipView shipToRotate) {
        if (shipToRotate.getShip().getDir()) {
            shipToRotate.getShipImage().setRotation(90);
            shipToRotate.getShip().setDir(false);
        } else {
            shipToRotate.getShipImage().setRotation(0);
            shipToRotate.getShip().setDir(true);
        }
    }


    public void select(ShipView shipView) {
        shipView.setSelected(true);
        shipView.getShipImage().setBackgroundColor(Color.GREEN);
    }


    public void deselectAllShipViews() {
        for (ShipView shipView : fleetView) {
            shipView.setSelected(false);
            shipView.getShipImage().setBackgroundColor(Color.TRANSPARENT);
        }
    }


    private void setImageScaling(final ImageView image) {

        image.setAdjustViewBounds(true);

        ViewTreeObserver vto = image.getViewTreeObserver();
        vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {

                image.setMaxHeight(boardView.getMeasuredHeight() / 10);
            }

        });
    }


    private void toast(final String s) {
        final Context context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, s, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

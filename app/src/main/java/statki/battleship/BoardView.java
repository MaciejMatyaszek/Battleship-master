package statki.battleship;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;


public class BoardView extends View {


    public interface BoardTouchListener {


        void onTouch(int x, int y);
    }


    private final List<BoardTouchListener> listeners = new ArrayList<>();


    private final int boardColor = Color.rgb(255, 255, 255);


    private final Paint boardPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    {
        boardPaint.setColor(boardColor);
    }


    private final int boardLineColor = Color.BLACK;


    private final Paint boardLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    {
        boardLinePaint.setColor(boardLineColor);
        boardLinePaint.setStrokeWidth(2);
    }


    private boolean displayShips = false;


    private Board board;


    private int boardSize = 10;


    public BoardView(Context context) {
        super(context);
    }


    public BoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public BoardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }


    public void setBoard(Board board) {
        this.board = board;
        this.boardSize = board.size();

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                int xy = locatePlace(event.getX(), event.getY());
                if (xy >= 0) {
                    notifyBoardTouch(xy / 100, xy % 100);
                }
                break;
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_CANCEL:
                break;
        }
        return true;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawGrid(canvas);
        drawShotPlaces(canvas);
        if(displayShips){
            drawShips(canvas);
        }
        drawShipHitPlaces(canvas);

    }


    private void drawShips(Canvas canvas){

        for(int x = 0; x < boardSize; x++){
            for(int y = 0; y < boardSize; y++){
                if(board.placeAt(x, y).hasShip()){
                    drawSquare(canvas, Color.argb(126, 183,155,255), x, y);
                }
            }
        }
    }

    private void drawShotPlaces(Canvas canvas) {
        // check the state of each place of the board and draw it.
    if(board == null){
        return;
    }
        for(int x = 0; x < boardSize; x++){
            for(int y = 0; y < boardSize; y++){
                if(board.placeAt(x, y).isHit()){
                    drawSquare(canvas, Color.RED, x, y);
                }
            }
        }
    }


    public void drawSquare(Canvas canvas, int color, int x, int y){
        boardPaint.setColor(color);
        int length = 98;
        float viewSize = maxCoord();
        float tileSize = viewSize / 10;  //10 Is how many tiles there are
        float offSet = 8;
        canvas.drawRect((tileSize* x) + offSet, (tileSize*y) + offSet, ((tileSize * x)+tileSize) - offSet, (((viewSize/10) * y)+tileSize) - offSet, boardPaint);
    }


    public void drawShipHitPlaces(Canvas canvas){
        if(board == null){
            return;
        }
        List<Place> shipHitPlaces = board.getShipHitPlaces();
        for(Place places : shipHitPlaces){
            drawSquare(canvas, Color.GREEN, places.getX(), places.getY());
        }

    }


    private void drawGrid(Canvas canvas) {
        final float maxCoord = maxCoord();
        final float placeSize = lineGap();
        boardPaint.setColor(Color.WHITE);
        canvas.drawRect(0, 0, maxCoord, maxCoord, boardPaint);
        for (int i = 0; i < numOfLines(); i++) {
            float xy = i * placeSize;
            canvas.drawLine(0, xy, maxCoord, xy, boardLinePaint); // horizontal line
            canvas.drawLine(xy, 0, xy, maxCoord, boardLinePaint); // vertical line
        }
    }


    protected float lineGap() {
        return Math.min(getMeasuredWidth(), getMeasuredHeight()) / (float) boardSize;
    }


    private int numOfLines() {
        return boardSize + 1;
    }


    protected float maxCoord() {
        return lineGap() * (numOfLines() - 1);
    }


    public int locatePlace(float x, float y) {
        if (x <= maxCoord() && y <= maxCoord()) {
            final float placeSize = lineGap();
            int ix = (int) (x / placeSize);
            int iy = (int) (y / placeSize);
            return ix * 100 + iy;
        }
        return -1;
    }


    public void addBoardTouchListener(BoardTouchListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }


    public void removeBoardTouchListener(BoardTouchListener listener) {
        listeners.remove(listener);
    }


    private void notifyBoardTouch(int x, int y) {
        for (BoardTouchListener listener: listeners) {
            listener.onTouch(x, y);
        }
    }

    public void displayBoardsShips(boolean display){
        displayShips = display;
    }
}

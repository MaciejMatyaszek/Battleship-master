package statki.battleship;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;



public class NetworkAdapter {


    private static Socket socket;


    private static PrintWriter out;

    private static BufferedReader in;


    static final String PLACED_SHIPS = "SHIPS PLACED";


    static final String NEW_GAME = "NEW GAME REQUEST";


    static final String ACCEPT_NEW_GAME_REQUEST = "ACCEPTED NEW GAME REQUEST";


    static final String REJECT_NEW_GAME_REQUEST = "REJECT NEW GAME REQUEST";


    static final String PLACE_SHOT = "PLACE SHOT";


    static final String STOP_READING = "STOP READING";



    private NetworkAdapter(){}


    static void setSocket(Socket s){
        try {
            Log.d("wifiMe", "socket set, is socket null? " + (s == null));
            if(s == null){
                return;
            }
            socket = s;
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

            Log.d("wifiMe", "is 'in' null?  " + (in == null) + " is 'out' null? "  + (out == null) );
        }
        catch(IOException e){
            Log.d("wifiMe", "Exception thrown in NetworkAdapter constructor");
            e.printStackTrace();
        }
    }


    static Socket getSocket(){
        return socket;
    }




    static String readMessage(){

        try {
            Log.d("wifiMe", "Going to read messages part 1");
            if(in == null){
                //Only returns null if sockets aren't set correctly

                if(socket == null){
                    return null;
                }
                else{
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                }
            }


            Log.d("wifiMe", "Going to read messages part 2");

            String msg;

            while ((msg = in.readLine()) != null) {
                Log.d("wifiMe", "Got message");
                if(msg.equals("") || msg.equals(" ")){ //Checking " " probably unnecessary
                    continue;
                }
                return msg;
            }

        } catch (IOException e) {
            Log.d("wifiMe", "IOException ON NETWORK ADAPTER CLASS, READ MESSAGES METHOD");
        }
        Log.d("wifiMe", "Return msg null");
        return null;
    }


    static Board decipherPlaceShips(String opponentBoard){
        if(opponentBoard == null || !opponentBoard.startsWith(PLACED_SHIPS)){
            return null;
        }
        opponentBoard = opponentBoard.substring(NetworkAdapter.PLACED_SHIPS.length());

        Log.d("wifiMe", "Attempting to convert string to board, the string: " + opponentBoard);
        Board b = new Board(10);
        int traverseString = 0;
        char[] tb = opponentBoard.toCharArray();
        for(int i = 0; i < b.size(); i++){
            for(int j = 0; j < b.size(); j++){
                int shipType = tb[traverseString];
                Place place = b.placeAt(j, i);

                if(shipType == '5')
                    place.setShip(new Ship("aircraftcarrier", 5));
                else if(shipType == '4')
                    place.setShip(new Ship("battleship", 4));
                else if(shipType == '3')
                    place.setShip(new Ship("submarine", 3));
                else if(shipType == '2')
                    place.setShip(new Ship("frigate", 2));
                else if(shipType == '1')
                    place.setShip(new Ship("minesweeper", 1));
                else{
                    //Don't set a ship
                }

                traverseString++;
            }
        }

        Log.d("wifiMe", "Deciphered board " + b.toString());
        return b;
    }





    public static int[] decipherPlaceShot(String msg){
        if(msg == null || !msg.startsWith(PLACE_SHOT)){
            return null;
        }
        int[] coordinatesShot = new int[2];
        boolean firstDigitFound = false;
        for(int i = 0; i < msg.length(); i++){
            char letter = msg.charAt(i);

            if(isDigit(letter)){
                int digitFound = Character.getNumericValue(letter);

                if(firstDigitFound){
                    coordinatesShot[1] = digitFound;
                    return coordinatesShot;
                }
                else{
                    coordinatesShot[0] = digitFound;
                }
                firstDigitFound = true;
            }
        }

        return coordinatesShot;
    }


    static void writeBoardMessage(Board board){
        out.println(PLACED_SHIPS + board.toString());
        out.flush();
        Log.d("wifiMe", "Board being sent: " + board.toString());
    }


    static void writePlaceShotMessage(int x, int y){
        out.println(PLACE_SHOT + " " + x + "," + y);
        out.flush();//flush clears the message you just wrote
    }


    static void writeStopReadingMessage(){
        out.println(STOP_READING + " ");
        out.flush();
    }


    static void writeNewGameMessage(){
        out.println(NEW_GAME + " ");
        out.flush();
    }


    static void writeAcceptNewGameMessage(){
        out.println(ACCEPT_NEW_GAME_REQUEST + " ");
        out.flush();
    }


    static void writeRejectNewGameMessage(){
        out.println(REJECT_NEW_GAME_REQUEST + " ");
        out.flush();
    }


    static boolean hasConnection(){
        return (socket != null);
    }


    private static boolean isDigit(char l){
        return (l >= '0' && l <= '9');
    }
}

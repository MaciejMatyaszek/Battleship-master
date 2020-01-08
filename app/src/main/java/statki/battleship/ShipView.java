package statki.battleship;


import android.widget.ImageView;


class ShipView {


    private ImageView shipImage;


    private Ship ship;


    private boolean isSelected = false;


    ShipView(ImageView image, Ship newShip){
        shipImage = image;
        ship = newShip;
    }


    boolean isSelected(){
        return isSelected;
    }


    void setSelected(boolean selected){
        isSelected = selected;
    }


    ImageView getShipImage(){
        return shipImage;
    }


    public Ship getShip(){
        return ship;
    }


}

package statki.battleship;

import android.net.wifi.p2p.WifiP2pDevice;



public class CustomDevice{
    public WifiP2pDevice device;

    public CustomDevice(WifiP2pDevice d){
        device = d;
    }

    public String toString(){
        if(device == null)
            return "No devices found.";
        return device.deviceName;
    }
}

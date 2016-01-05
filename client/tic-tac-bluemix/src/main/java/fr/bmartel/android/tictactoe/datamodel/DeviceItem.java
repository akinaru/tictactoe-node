package fr.bmartel.android.tictactoe.datamodel;

import fr.bmartel.android.tictactoe.constant.GameStates;

/**
 * @author Bertrand Martel
 */
public class DeviceItem {

    private String deviceId = "";

    private String deviceName = "";

    private GameStates state = GameStates.NONE;

    public DeviceItem(String deviceId, String deviceName, GameStates state) {
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.state = state;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }
}

package fr.bmartel.android.tictactoe.datamodel;

/**
 * @author Bertrand Martel
 */
public class ClientConnectionEvent extends MessageObject {

    private String deviceId = "";

    private String deviceName = "";

    public ClientConnectionEvent(GameMessageTopic messageType, String deviceId, String deviceName) {
        super(messageType);
        this.deviceId = deviceId;
        this.deviceName = deviceName;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }
}


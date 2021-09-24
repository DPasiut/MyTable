package com.example.mytable.service.bluetooth;

public enum BluetoothCommunicationMessageType {
    CHANGE_STATE(0),
    ERROR(1),
    TEXT(2);

    private int value;

    BluetoothCommunicationMessageType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

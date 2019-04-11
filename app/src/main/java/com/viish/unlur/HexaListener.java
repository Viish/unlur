package com.viish.unlur;

public interface HexaListener {
    void onHexaCreated(HexaView hexa, int q, int r);
    boolean onHexaSelected(HexaView hexa, int q, int r);
}

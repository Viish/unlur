package com.viish.unlur;

/*
Game.java
Copyright (C) 2019 Sylvain Berfini, Grenoble, France
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>
*/

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;

public class Game extends Activity implements HexaListener {
    enum EndGame {
        NOPE,
        VICTORY,
        DEFEAT;
    }

    private GameView mGame;
    private Button mStayBlack, mUndo;

    private int mSize, mLines;
    private boolean mIsBlackTurn;
    private boolean mIsBlackChosen;
    private Map<Point, HexaView> mHexas;
    private ArrayList<HexaView> mLastMoves;
    private boolean mIsGameFinished;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.game);

        mGame = findViewById(R.id.game_board);
        mGame.setListener(this);

        mSize = getIntent().getExtras().getInt("BoardSize");
        mLines = mSize * 2 - 1;
        mHexas = new HashMap<>();

        mGame.setSize(mSize);

        mIsBlackTurn = true;
        mIsBlackChosen = false;
        mIsGameFinished = false;
        mLastMoves = new ArrayList<>();

        mStayBlack = findViewById(R.id.black_button);
        mStayBlack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mStayBlack.setEnabled(false);
                mIsBlackChosen = true;
                mIsBlackTurn = false;

                // Special meaning for staying black move
                mLastMoves.add(null);
                mUndo.setEnabled(true);

                // Enable sides
                enableSides(true);
            }
        });

        mUndo = findViewById(R.id.undo_button);
        mUndo.setEnabled(false);
        mUndo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mLastMoves.isEmpty()) {
                    return;
                }

                int lastMove = mLastMoves.size() - 1;
                HexaView lastHexa = mLastMoves.get(lastMove);
                mLastMoves.remove(lastMove);
                if (lastHexa == null) {
                    // Special meaning for staying black move
                    mStayBlack.setEnabled(true);
                    mIsBlackChosen = false;
                    mIsBlackTurn = true;
                    enableSides(false);
                } else {
                    lastHexa.setColor(Color.GRAY);
                    lastHexa.setEnabled(true);
                    if (mIsBlackChosen) {
                        mIsBlackTurn = !mIsBlackTurn;
                    }
                }
                mUndo.setEnabled(!mLastMoves.isEmpty());
            }
        });
    }

    private void enableSides(boolean enable) {
        for (HexaView hexa : mHexas.values()) {
            int q = hexa.getQ();
            int r = hexa.getR();
            if (r == mSize - 1) { // Bottom side
                hexa.setEnabled(enable);
            } else if (r == -(mSize - 1)) { // Top side
                hexa.setEnabled(enable);
            } else if (q == -(mSize - 1)) { // Bottom left side
                hexa.setEnabled(enable);
            } else if (q == mSize - 1) { // Top right side
                hexa.setEnabled(enable);
            } else if (r + q == mSize - 1) { // Bottom right side
                hexa.setEnabled(enable);
            } else if (r + q == -(mSize - 1)) { // Top left side
                hexa.setEnabled(enable);
            }
        }
    }

    @Override
    public boolean onHexaSelected(HexaView hexa, int q, int r) {
        if (mIsGameFinished) return false;

        mLastMoves.add(hexa);
        mUndo.setEnabled(true);
        if (mIsBlackTurn) {
            hexa.setColor(Color.BLACK);
        } else {
            hexa.setColor(Color.WHITE);
        }
        hexa.setEnabled(false);

        EndGame end = checkVictory(hexa);
        if (end != EndGame.NOPE) {
            mIsGameFinished = true;
            mUndo.setEnabled(false);
            Toast.makeText(this, (mIsBlackTurn ? "Black" : "White") + " " + (end == EndGame.VICTORY ? "Wins" : "Looses"), Toast.LENGTH_LONG).show();
        } else {
            if (mIsBlackChosen) {
                mIsBlackTurn = !mIsBlackTurn;
            }
        }
        return true;
    }

    @Override
    public void onHexaCreated(HexaView hexa, int q, int r) {
        Point p = new Point(q, r);
        mHexas.put(p, hexa);
        // Disable sides at the beginning of the game
        if (r == mSize - 1) { // Bottom side
            hexa.setEnabled(false);
        } else if (r == -(mSize - 1)) { // Top side
            hexa.setEnabled(false);
        } else if (q == -(mSize - 1)) { // Bottom left side
            hexa.setEnabled(false);
        } else if (q == mSize - 1) { // Top right side
            hexa.setEnabled(false);
        } else if (r + q == mSize - 1) { // Bottom right side
            hexa.setEnabled(false);
        } else if (r + q == -(mSize - 1)) { // Top left side
            hexa.setEnabled(false);
        }
    }

    private EndGame checkVictory(HexaView hexa) {
        boolean top, top_left, top_right, bottom, bottom_left, bottom_right;
        top = top_left = top_right = bottom_right = bottom_left = bottom = false;

        ArrayList<HexaView> connectedHexas = new ArrayList<>();
        getNearbyHexasWithSameColor(hexa, connectedHexas);
        for (HexaView hex : connectedHexas) {
            int q = hex.getQ();
            int r = hex.getR();
            if (r == mSize - 1) { // Bottom side
                bottom = true;
                if (q == 0) {
                    bottom_right = true;
                } else if (q == -(mSize - 1)) {
                    bottom_left = true;
                }
            } else if (r == -(mSize - 1)) { // Top side
                top = true;
                if (q == 0) {
                    top_left = true;
                } else if (q == mSize - 1) {
                    top_right = true;
                }
            } else if (q == -(mSize - 1)) { // Bottom left side
                bottom_left = true;
                if (r == 0) {
                    top_left = true;
                }
            } else if (q == mSize - 1) { // Top right side
                top_right = true;
                if (r == 0) {
                    bottom_right = true;
                }
            } else if (r + q == mSize - 1) { // Bottom right side
                bottom_right = true;
            } else if (r + q == -(mSize - 1)) { // Top left side
                top_left = true;
            }
        }

        boolean blackWins = (top && bottom_left && bottom_right) || (bottom && top_left && top_right);
        boolean whiteWins = (top && bottom) || (top_left && bottom_right) || (top_right && bottom_left);
        if (hexa.getColor() == Color.BLACK) {
            if (blackWins) {
                return EndGame.VICTORY;
            } else if (whiteWins) {
                return EndGame.DEFEAT;
            }
        } else {
            if (whiteWins) {
                return EndGame.VICTORY;
            } else if (blackWins) {
                return EndGame.DEFEAT;
            }
        }
        return EndGame.NOPE;
    }

    private ArrayList<HexaView> getNearbyHexasWithSameColor(HexaView parent, ArrayList<HexaView> found) {
        found.add(parent);

        int q = parent.getQ();
        int r = parent.getR();

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                HexaView hex = getHexaAt(q + x, r + y);
                if (hex != null && !(x == 0 && y == 0) && hex.getColor() == parent.getColor() && !found.contains(hex)) {
                    Log.e("Unlur", "Nearby Hexa q=" + hex.getQ() + ", r=" + hex.getR() + ", color=" + hex.getColor());
                    getNearbyHexasWithSameColor(hex, found);
                }
            }
        }

        return found;
    }

    private HexaView getHexaAt(int q, int r) {
        Point p = new Point(q, r);
        if (mHexas.containsKey(p)) {
            return mHexas.get(p);
        }
        return null;
    }
}

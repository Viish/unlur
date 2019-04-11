package com.viish.unlur;

/*
GameView.java
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

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

public class GameView extends ViewGroup {
    public static int HEX_COUNT = 6;

    private Context mContext;
    private int mBoardGameSize;
    private int mHexaWidth, mHexaHeight, mHexaSide, mLines;
    private boolean mSidesEnabled;
    private HexaListener mListener;

    public GameView(Context context, int size) {
        super(context);
        mContext = context;
        mBoardGameSize = size;
        init();
    }

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mBoardGameSize = HEX_COUNT;
        init();
    }

    public GameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        mBoardGameSize = HEX_COUNT;
        init();
    }

    public GameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        mBoardGameSize = HEX_COUNT;
        init();
    }

    public void setSize(int newSize) {
        mBoardGameSize = newSize;
        init();
        invalidate();
    }

    public void setListener(HexaListener listener) {
        mListener = listener;
    }

    private void init() {
        // This can probably be improved...
        int max = mBoardGameSize * 2 - 1;
        int sum = 0;
        for (int i = mBoardGameSize; i <= max; i++) {
            sum += i;
        }
        for (int i = max - 1; i >= mBoardGameSize; i--) {
            sum += i;
        }

        for (int i = 0; i < sum; i++) {
            addView(new HexaView(mContext));
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final int childLeft = this.getPaddingLeft();
        final int childTop = this.getPaddingTop();
        final int childRight = this.getMeasuredWidth() - this.getPaddingRight();
        final int childBottom = this.getMeasuredHeight() - this.getPaddingBottom();
        final int childWidth = childRight - childLeft;
        final int childHeight = childBottom - childTop;

        mLines = mBoardGameSize * 2 - 1;
        final int hexaMaxSize = childWidth / mLines;

        HexaView hexa = (HexaView) getChildAt(0);
        hexa.measure(MeasureSpec.makeMeasureSpec(hexaMaxSize, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.AT_MOST));

        mHexaWidth = hexa.getMeasuredWidth();
        mHexaHeight = hexa.getMeasuredHeight();
        mHexaSide = Math.round(Math.round(mHexaWidth / Math.sqrt(3)));
        // Since we don't want any space between the hex, the real size of a hex is 3/2 * it's side
        // (top hat + a side)
        int hexaFakeHeight = 3 * mHexaSide / 2;

        int marginTop = (childHeight - (hexaFakeHeight * mLines)) / 2;

        int childIndex = 0;
        for (int i = 0; i < mBoardGameSize; i++) {
            int widthCount = mBoardGameSize + i;
            int marginLeft = (childWidth - (widthCount * mHexaWidth)) / 2;
            for (int j = 0; j < widthCount; j++) {
                hexa = (HexaView) getChildAt(childIndex);
                hexa.setListener(mListener);

                hexa.measure(MeasureSpec.makeMeasureSpec(hexaMaxSize, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.AT_MOST));

                int spaceToRemove = i * (mHexaHeight - hexaFakeHeight);

                hexa.layout(marginLeft + j * mHexaWidth,
                        marginTop - spaceToRemove + i * mHexaHeight,
                        marginLeft + (j + 1) * mHexaWidth,
                        marginTop - spaceToRemove + (i + 1) * mHexaHeight);
                childIndex += 1;

                hexa.setCoords(mBoardGameSize - widthCount + j, i - mBoardGameSize + 1);
            }
        }

        marginTop += mBoardGameSize * hexaFakeHeight;

        for (int i = mBoardGameSize - 1; i > 0; i--) {
            int widthCount = mLines - (mBoardGameSize - i);
            int marginLeft = (childWidth - (widthCount * mHexaWidth)) / 2;
            for (int j = widthCount; j > 0; j--) {
                hexa = (HexaView) getChildAt(childIndex);
                hexa.setListener(mListener);

                hexa.measure(MeasureSpec.makeMeasureSpec(hexaMaxSize, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.AT_MOST));

                int spaceToRemove = (mBoardGameSize - 1 - i) * (mHexaHeight - hexaFakeHeight);

                hexa.layout(marginLeft + (widthCount - j) * mHexaWidth,
                        marginTop - spaceToRemove + (mBoardGameSize - 1 - i) * mHexaHeight,
                        marginLeft + ((widthCount - j) + 1) * mHexaWidth,
                        marginTop - spaceToRemove + (mBoardGameSize - i) * mHexaHeight);

                childIndex += 1;

                hexa.setCoords(1 - mBoardGameSize + (widthCount - j), mBoardGameSize - i);
            }
        }
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int desiredWidth = 200;
        int desiredHeight = 250;

        int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int width;
        int height;

        if (widthMode == MeasureSpec.EXACTLY) {
            width = widthSize;
        } else if (widthMode == MeasureSpec.AT_MOST) {
            width = Math.min(desiredWidth, widthSize);
        } else {
            width = desiredWidth;
        }

        if (heightMode == MeasureSpec.EXACTLY) {
            height = heightSize;
        } else if (heightMode == MeasureSpec.AT_MOST) {
            height = Math.min(desiredHeight, heightSize);
        } else {
            height = desiredHeight;
        }

        // The board game is a big hexagon so the following applies
        // Radius of inside circle
        int ri = Math.round(Math.min(width, height)) / 2;
        // Radius of circumscribed circle
        int rc = (int) Math.round(ri * 2 / Math.sqrt(3));
        setMeasuredDimension(ri * 2, rc * 2);
    }
}

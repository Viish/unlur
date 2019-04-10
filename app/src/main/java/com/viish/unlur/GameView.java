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
import android.util.AttributeSet;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

public class GameView extends ViewGroup {
    public static int HEX_COUNT = 6;

    private Context mContext;

    public GameView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public GameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    public GameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        init();
    }

    private void init() {
        int count = 91; //TODO Compute
        for (int i = 0; i < count; i++) {
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

        int hexCountHeight = HEX_COUNT * 2 - 1;
        final int hexaMaxSize = childWidth / hexCountHeight;

        HexaView hexa = (HexaView) getChildAt(0);
        hexa.measure(MeasureSpec.makeMeasureSpec(hexaMaxSize, MeasureSpec.EXACTLY),
                MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.AT_MOST));

        int hexaWidth = hexa.getMeasuredWidth();
        int hexaHeight = hexa.getMeasuredHeight();
        int hexaSide = Math.round(Math.round(hexaWidth / Math.sqrt(3)));
        // Since we don't want any space between the hex, the real size of a hex is 3/2 * it's side
        // (top hat + a side)
        int hexaFakeHeight = 3 * hexaSide / 2;

        int marginTop = (childHeight - (hexaFakeHeight * hexCountHeight)) / 2;

        int childIndex = 0;
        for (int i = 0; i < HEX_COUNT; i++) {
            int widthCount = HEX_COUNT + i;
            int marginLeft = (childWidth - (widthCount * hexaWidth)) / 2;
            for (int j = 0; j < widthCount; j++) {
                hexa = (HexaView) getChildAt(childIndex);
                hexa.measure(MeasureSpec.makeMeasureSpec(hexaMaxSize, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.AT_MOST));

                int spaceToRemove = i * (hexaHeight - hexaFakeHeight);

                hexa.layout(marginLeft + j * hexaWidth,
                        marginTop - spaceToRemove + i * hexaHeight,
                        marginLeft + (j + 1) * hexaWidth,
                        marginTop - spaceToRemove + (i + 1) * hexaHeight);
                childIndex += 1;
            }
        }

        marginTop += HEX_COUNT * hexaFakeHeight;

        for (int i = HEX_COUNT - 1; i > 0; i--) {
            int widthCount = hexCountHeight - (HEX_COUNT - i);
            int marginLeft = (childWidth - (widthCount * hexaWidth)) / 2;
            for (int j = widthCount; j > 0; j--) {
                hexa = (HexaView) getChildAt(childIndex);
                hexa.measure(MeasureSpec.makeMeasureSpec(hexaMaxSize, MeasureSpec.EXACTLY),
                        MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.AT_MOST));

                int spaceToRemove = (HEX_COUNT - 1 - i) * (hexaHeight - hexaFakeHeight);

                hexa.layout(marginLeft + (widthCount - j) * hexaWidth,
                        marginTop - spaceToRemove + (HEX_COUNT - 1 - i) * hexaHeight,
                        marginLeft + ((widthCount - j) + 1) * hexaWidth,
                        marginTop - spaceToRemove + (HEX_COUNT - i) * hexaHeight);

                childIndex += 1;
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

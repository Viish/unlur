package com.viish.unlur;

/*
HexaView.java
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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.Region;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class HexaView extends View {
    private Context mContext;
    private Path hexagonPath, hexagonContourPath;
    private int mColor;
    private int mQ, mR;
    private HexaListener mListener;

    public HexaView(Context context) {
        super(context);
        mContext = context;
        init();
    }

    public HexaView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        init();
    }

    public HexaView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        init();
    }

    public HexaView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
        init();
    }

    private void init() {
        hexagonPath = new Path();
        hexagonContourPath = new Path();
        mColor = Color.GRAY;
        mQ = 0;
        mR = 0;
    }

    public void setCoords(int q, int r) {
        mQ = q;
        mR = r;
        if (mListener != null) {
            mListener.onHexaCreated(this, q, r);
        }
    }

    public int getQ() {
        return mQ;
    }

    public int getR() {
        return mR;
    }
    
    public void setListener(HexaListener listener) {
        mListener = listener;
    }

    public void setColor(int color) {
        if (color != mColor) {
            mColor = color;
            invalidate();
        }
    }

    public int getColor() {
        return mColor;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isEnabled()) return false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                setColor(Color.GREEN);
                break;
            case MotionEvent.ACTION_MOVE:
                if (isMotionEventInsideView(event)) {
                    setColor(Color.GREEN);
                } else {
                    setColor(Color.GRAY);
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!isMotionEventInsideView(event)) {
                    setColor(Color.GRAY);
                } else {
                    if (mListener != null) {
                        boolean ok = mListener.onHexaSelected(this, mQ, mR);
                        if (!ok) {
                            setColor(Color.GRAY);
                        }
                    }
                }
                break;
        }
        return true;
    }

    private boolean isMotionEventInsideView(MotionEvent event) {
        Rect viewRect = new Rect(getLeft(), getTop(), getRight(), getBottom());

        return viewRect.contains(getLeft() + (int) event.getX(),getTop() + (int) event.getY());
    }

    private void calculatePath(float radius) {
        float halfRadius = radius / 2;
        float triangleHeight = (float) (Math.sqrt(3) * radius / 2);
        float centerX = getMeasuredWidth() / 2;
        float centerY = getMeasuredHeight() / 2;

        this.hexagonContourPath.reset();
        this.hexagonContourPath.moveTo(centerX, centerY + radius);
        this.hexagonContourPath.lineTo(centerX - triangleHeight, centerY + halfRadius);
        this.hexagonContourPath.lineTo(centerX - triangleHeight, centerY - halfRadius);
        this.hexagonContourPath.lineTo(centerX, centerY - radius);
        this.hexagonContourPath.lineTo(centerX + triangleHeight, centerY - halfRadius);
        this.hexagonContourPath.lineTo(centerX + triangleHeight, centerY + halfRadius);
        this.hexagonContourPath.close();

        // 2 pixels margin
        halfRadius = radius / 2 - 2;
        triangleHeight = (float) (Math.sqrt(3) * radius / 2) - 2;

        this.hexagonPath.reset();
        this.hexagonPath.moveTo(centerX, centerY + radius);
        this.hexagonPath.lineTo(centerX - triangleHeight, centerY + halfRadius);
        this.hexagonPath.lineTo(centerX - triangleHeight, centerY - halfRadius);
        this.hexagonPath.lineTo(centerX, centerY - radius);
        this.hexagonPath.lineTo(centerX + triangleHeight, centerY - halfRadius);
        this.hexagonPath.lineTo(centerX + triangleHeight, centerY + halfRadius);
        this.hexagonPath.close();
        invalidate();
    }

    @Override
    public void onDraw(Canvas canvas) {
        canvas.clipPath(hexagonContourPath, Region.Op.INTERSECT);
        canvas.drawColor(Color.BLACK);
        canvas.clipPath(hexagonPath, Region.Op.INTERSECT);
        canvas.drawColor(mColor);
        super.onDraw(canvas);
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

        // Radius of inside circle
        int ri = Math.round(Math.min(width, height) / 2);
        // Radius of circumscribed circle
        int rc = (int) Math.round(ri * 2 / Math.sqrt(3));
        setMeasuredDimension(ri * 2, rc * 2);
        calculatePath(rc);
    }
}

package com.example.jonathan.androidkvm;

import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.inputstick.api.basic.InputStickMouse;

public class TouchHandler implements  View.OnTouchListener{

    Float lastX=null;
    Float lastY=null;

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        float newX = motionEvent.getX();
        float newY=motionEvent.getY();

        if(lastX!=null && lastY!= null){
            Float diffX=(lastX-newX)*-1;
            Float diffY=(lastY-newY)*-1;
            InputStickMouse.move(diffX.byteValue(),diffY.byteValue());

        }
        lastY=newY;
        lastX=newX;
        return  true;
    }
}

package com.example.jonathan.androidkvm;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.os.Bundle;


import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;

import android.view.View;


import com.inputstick.api.ConnectionManager;
import com.inputstick.api.basic.InputStickHID;
import com.inputstick.api.layout.GermanLayout;
import com.inputstick.api.utils.remote.KeyboardSupport;
import com.inputstick.api.utils.remote.ModifiersSupport;
import com.inputstick.api.utils.remote.RemotePreferences;
import com.inputstick.api.utils.remote.RemoteSupport;

import android.view.Menu;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ToggleButton;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity {

    private Camera mCamera;
    private CameraPreview mPreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        // Create our Preview view and set it as the content of our activity.
       // mPreview = new CameraPreview(this);
       // F//rameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
       // preview.addView(mPreview);

        View touch = findViewById(R.id.touchview);

        TouchHandler touchHandler = new TouchHandler();
        touch.setOnTouchListener(touchHandler);

        RemoteSupport r = new RemoteSupport(new RemotePreferences());
        Group group = findViewById(R.id.fncgroup);
        ToggleButton btn = findViewById(R.id.toggleButton2);
        ToggleButton shift = findViewById(R.id.shift);
        ToggleButton ctrl = findViewById(R.id.ctrlBtn);
        ToggleButton alt = findViewById(R.id.altBtn);
        Button btnc = findViewById(R.id.button);
        ModifiersSupport m = new ModifiersSupport(r,group,ctrl,shift,alt,btn,btn,btnc);

        //getSupportFragmentManager()

        findViewById(R.id.fncbtn).setOnClickListener((e)->{
            KeyboardSupport.getFunctionKeysDialog(MainActivity.this, r, m, "title");
        });
        LinearLayout layout = (LinearLayout) findViewById(R.id.linearlayout);

        LinearLayout lines =createLayouRow() ;

        GermanLayout gl = GermanLayout.getInstance();
        int skipCt=0;
        for(int[] row : gl.LUT){
            if(row[0]==-1){
                skipCt++;
                if(skipCt==2){
                    layout.addView(lines);
                    lines = createLayouRow();
                }
                continue;
            }


            skipCt=0;
            Button btnTag = new Button(this);

           AtomicReference<Key> currentKey=new AtomicReference<>(null);
            Consumer<Void> update=(s)->{
                Key k=gl.getText(m,row);
                currentKey.set(k);
                if(k.code.length()==0){
                    btnTag.setTextColor(Color.WHITE);
                }else{
                    btnTag.setTextColor(Color.BLACK);
                }
                btnTag.setText(k.displayValue);
            };

            btnTag.setMinWidth(0);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1,-1,1);
            btnTag.setLayoutParams(params);
            btnTag.setMinimumWidth(4);
            btnTag.setText(new String(new char[]{(char) row[1]}));

            m.shiftListeners.add((val)->{
                update.accept(null);
            });
            m.ctrlListeners.add((val)->{
                update.accept(null);
            });
            m.altListeners.add((val)->{
                update.accept(null);
            });
            btnTag.setAllCaps(false);
            btnTag.setOnClickListener(a->{
                String code=currentKey.get().code;
                if(code.length()>0){
                    gl.type(code);
                }
            });
            lines.addView(btnTag);
        }
        layout.addView(lines);

        InputStickHID.addStateListener(state->{
            if(state== ConnectionManager.STATE_READY){
                layout.setBackgroundColor(Color.GREEN);
            }else if(state== ConnectionManager.STATE_CONNECTING){
                layout.setBackgroundColor(Color.YELLOW);
            }
        });

        InputStickHID.connect(getApplication());
       /* findViewById(R.id.).setOnClickListener((e)->{
            InputStickKeyboard.type("test","de-DE");
        });

        findViewById(R.id.).setOnClickListener((e)->{
            InputStickKeyboard.pressAndRelease(HIDKeycodes.KEY_BACKSPACE);
        });*/

    }

    private LinearLayout createLayouRow() {
        LinearLayout lines = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1,-1,1);
        lines.setLayoutParams(params);
        lines.setOrientation(LinearLayout.HORIZONTAL);
        return lines;
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    public static Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }


}
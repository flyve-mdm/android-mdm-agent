package org.flyve.mdm.agent.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TableRow;

import org.flyve.mdm.agent.R;
import org.flyve.mdm.agent.utils.FlyveLog;

public class OptionsEnrollmentActivity extends Activity {

    public final static int REQUEST_DRAWOVERLAY_CODE = 54987;

    public static boolean needOptions() {
        //use for lock option
        return Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Switch switchDrawOverlay = findViewById(R.id.swtDrawOverlay);
        //use for lock option
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            switchDrawOverlay.setChecked(Settings.canDrawOverlays(getApplicationContext()));
        }
    }

    /**
     * Called when the activity is starting
     * It shows the UI to start the enrollment
     * @param savedInstanceState if the activity is being re-initialized, it contains the data it most recently supplied
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_option_enrollment);

        TableRow rowDrawOverlay = findViewById(R.id.rowDrawOverlay);
        Switch switchDrawOverlay = findViewById(R.id.swtDrawOverlay);
        LinearLayout lnButtons = findViewById(R.id.lnButtons);

        if(needOptions()){
            lnButtons.setVisibility(View.VISIBLE);
        }

        //use for lock option
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M){
            rowDrawOverlay.setVisibility(View.VISIBLE);
            switchDrawOverlay.setChecked(Settings.canDrawOverlays(getApplicationContext()));
            switchDrawOverlay.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getApplicationContext().getPackageName()));
                    startActivityForResult(intent, REQUEST_DRAWOVERLAY_CODE);
                }
            });
        }else{
            rowDrawOverlay.setVisibility(View.INVISIBLE);
        }

        Button btnContinue = findViewById(R.id.btnContinueOptions);
        btnContinue.setVisibility(View.VISIBLE);
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent miIntent = new Intent(OptionsEnrollmentActivity.this, PermissionEnrollmentActivity.class);
                OptionsEnrollmentActivity.this.startActivity(miIntent);
                OptionsEnrollmentActivity.this.finish();
            }
        });

    }
}

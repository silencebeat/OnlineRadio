package silencebeat.onlineradio.View.View;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;

import silencebeat.onlineradio.Presenter.PlayerWireframe;
import silencebeat.onlineradio.R;
import silencebeat.onlineradio.Support.SimpleDB;
import silencebeat.onlineradio.Support.StaticVariable;
import silencebeat.onlineradio.Support.TextReader;
import silencebeat.onlineradio.databinding.ActivityMainBinding;

public class MainActivity extends Activity implements TextReader.ReadTextListener{

    private static final int REQUEST_CODE = 1;

    ActivityMainBinding content;
    TextReader textReader;
    SimpleDB simpleDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = DataBindingUtil.setContentView(this, R.layout.activity_main);
        textReader = new TextReader(this, this);
        simpleDB = new SimpleDB(this);
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(
                this,
                new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.MODIFY_AUDIO_SETTINGS},
                REQUEST_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            boolean bothGranted = true;
            for (int i = 0; i < permissions.length; i++) {
                if (Manifest.permission.RECORD_AUDIO.equals(permissions[i]) || Manifest.permission.MODIFY_AUDIO_SETTINGS.equals(permissions[i])) {
                    bothGranted &= grantResults[i] == PackageManager.PERMISSION_GRANTED;
                }
            }
            if (bothGranted) {
                simpleDB.putBoolean(StaticVariable.RECORD_PERMISSION, true);
            } else {
                simpleDB.putBoolean(StaticVariable.RECORD_PERMISSION, false);
            }
            PlayerWireframe.getInstance().toView(this);
        }
    }

    @Override
    public void onFinishReadingText() {
        content.txtText.setText("Synchronizing data...");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.MODIFY_AUDIO_SETTINGS) == PackageManager.PERMISSION_GRANTED) {

                    simpleDB.putBoolean(StaticVariable.RECORD_PERMISSION, true);
                    PlayerWireframe.getInstance().toView(MainActivity.this);
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.RECORD_AUDIO)
                            || ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.MODIFY_AUDIO_SETTINGS)) {
                        AlertDialog.OnClickListener onClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == DialogInterface.BUTTON_POSITIVE) {
                                    requestPermissions();
                                }
                            }
                        };
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Need permission")
                                .setMessage("Need record permission to show wave sound animation")
                                .setPositiveButton("Next", onClickListener)
                                .setNegativeButton("Cancel", onClickListener)
                                .show();
                    } else {
                        requestPermissions();
                    }
                }

            }
        },2000);
    }

    @Override
    public void onErrorReadingText() {
        content.txtText.setText("Error reading data. File is missing (Please reinstall this app from Play Store)");
    }
}

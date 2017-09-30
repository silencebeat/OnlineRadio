package silencebeat.onlineradio.Support;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;

/**
 * Created by Candra Triyadi on 04/06/2017.
 */

public class HardButtonReceiver extends BroadcastReceiver {

    private final static String TAG = "hbr";

    private HardButtonListener mButtonListener;

    public HardButtonReceiver(HardButtonListener buttonListener) {
        super();

        mButtonListener = buttonListener;
    }

    public HardButtonReceiver(){}

    @Override
    public void onReceive(Context context, Intent intent)
    {

        if(mButtonListener != null) {

            abortBroadcast();

            KeyEvent key = (KeyEvent) intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

            if(key.getAction() == KeyEvent.ACTION_UP)
            {
                int keycode = key.getKeyCode();

                if(keycode == KeyEvent.KEYCODE_MEDIA_NEXT)
                {
                    mButtonListener.onNextButtonPress();
                }
                else if(keycode == KeyEvent.KEYCODE_MEDIA_PREVIOUS)
                {
                    mButtonListener.onPrevButtonPress();
                }
                else if(keycode == KeyEvent.KEYCODE_HEADSETHOOK)
                {
                    mButtonListener.onPlayPauseButtonPress();
                }
            }
        }
    }

    public interface HardButtonListener {
        void onPrevButtonPress();
        void onNextButtonPress();
        void onPlayPauseButtonPress();
    }
}
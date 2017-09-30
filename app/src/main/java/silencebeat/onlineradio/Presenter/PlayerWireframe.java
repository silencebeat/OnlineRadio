package silencebeat.onlineradio.Presenter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import silencebeat.onlineradio.View.View.PlayerActivity;

/**
 * Created by Candra Triyadi on 30/09/2017.
 */

public class PlayerWireframe {

    private PlayerWireframe(){
    }

    private static class SingleTonHelper{
        private static final PlayerWireframe INSTANCE = new PlayerWireframe();
    }

    public static PlayerWireframe getInstance() {
        return SingleTonHelper.INSTANCE;
    }

    public void toView(Context context){
        Intent intent = new Intent(context, PlayerActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        ((Activity) context).finish();
    }
}

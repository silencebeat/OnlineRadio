package silencebeat.onlineradio.View.View;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.KeyEvent;
import android.view.View;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.cleveroad.audiovisualization.AudioVisualization;
import com.cleveroad.audiovisualization.DbmHandler;
import com.cleveroad.audiovisualization.GLAudioVisualizationView;
import com.cleveroad.audiovisualization.VisualizerDbmHandler;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import co.mobiwise.library.RadioListener;
import co.mobiwise.library.RadioManager;
import silencebeat.onlineradio.Model.RadioData;
import silencebeat.onlineradio.Model.RadioDatas;
import silencebeat.onlineradio.Presenter.ParseImagePresenter;
import silencebeat.onlineradio.Presenter.RadioOnclickListener;
import silencebeat.onlineradio.R;
import silencebeat.onlineradio.Support.Adapter;
import silencebeat.onlineradio.Support.HardButtonReceiver;
import silencebeat.onlineradio.Support.SimpleDB;
import silencebeat.onlineradio.Support.StaticVariable;
import silencebeat.onlineradio.View.ViewHolder.RadioViewHolder;
import silencebeat.onlineradio.databinding.ActivityPlayerBinding;

/**
 * Created by Candra Triyadi on 30/09/2017.
 */

public class PlayerActivity extends Activity implements RadioListener, View.OnClickListener, RadioOnclickListener, HardButtonReceiver.HardButtonListener, Observer{

    ActivityPlayerBinding content;
    RadioManager mRadioManager;
    SimpleDB simpleDB;
    private AudioVisualization audioVisualization;
    HardButtonReceiver mButtonReceiver;
    IntentFilter iF;
    ParseImagePresenter parseImagePresenter;

    ArrayList<RadioData> radioDatas = new ArrayList<>();
    int position = 0;
    boolean isPaused = false;
    String title, artist;

    @Override
    protected void onStart() {
        super.onStart();
        mRadioManager.connect();
    }

    @Override
    protected void onDestroy() {
        mRadioManager.disconnect();
        audioVisualization.release();
        super.onDestroy();

    }

    @Override
    protected void onStop() {
        try {
            unregisterReceiver(mButtonReceiver);
        }catch (Exception e){
            registerReceiver(mButtonReceiver, iF);
        }
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        isPaused = false;
        audioVisualization.onResume();
    }

    @Override
    public void onPause() {
        audioVisualization.onPause();
        isPaused = true;
        super.onPause();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        content = DataBindingUtil.setContentView(this, R.layout.activity_player);
        final StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        parseImagePresenter = new ParseImagePresenter();
        parseImagePresenter.addObserver(this);
        simpleDB = new SimpleDB(this);
        setView();
        setListRadio();

        content.btnForward.setOnClickListener(this);
        content.btnLike.setOnClickListener(this);
        content.btnPlay.setOnClickListener(this);
        content.btnRewind.setOnClickListener(this);
        content.btnShare.setOnClickListener(this);

        mButtonReceiver = new HardButtonReceiver(this);
        iF = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);
        iF.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY + 1);
        registerReceiver(mButtonReceiver, iF);
    }

    void setView(){
        content.viewList.setAnimationRes(R.anim.in_animation, R.anim.out_animation);
        audioVisualization = (AudioVisualization) content.visualizerView;
        if (simpleDB.getBoolean(StaticVariable.RECORD_PERMISSION)){
            VisualizerDbmHandler visualizerHandler = DbmHandler.Factory.newVisualizerHandler(this, 0);
            audioVisualization.linkTo(visualizerHandler);
        }else{
            content.visualizerView.setVisibility(View.INVISIBLE);
        }

        mRadioManager = RadioManager.with(this);
        mRadioManager.registerListener(this);
        mRadioManager.enableNotification(true);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                initRadio(position);
            }
        },1000);

    }

    private void updateAudioVisualization(int color){

        GLAudioVisualizationView.ColorsBuilder colorsBuilder = new GLAudioVisualizationView.ColorsBuilder(this);
        colorsBuilder.setLayerColors(R.array.custom_av_colors);
        colorsBuilder.setBackgroundColor(color);
        content.visualizerView.getRenderer().updateConfiguration(colorsBuilder);
    }

    private void updateNotification(String artist, String title, Bitmap bitmap){
        if (bitmap != null){
            mRadioManager.updateNotification(artist,title, bitmap);
        }else{
            mRadioManager.updateNotification(artist,title, R.mipmap.ic_launcher);
        }
    }

    void setListRadio(){
        simpleDB = new SimpleDB(this);
        RadioDatas datas = simpleDB.getObject(StaticVariable.URLs, RadioDatas.class);
        radioDatas = datas.getRadioDatas();

        Adapter<RadioData, RadioViewHolder> adapter = new Adapter<RadioData, RadioViewHolder>(R.layout.item_radio, RadioViewHolder.class,
                RadioData.class, radioDatas) {
            @Override
            protected void bindView(RadioViewHolder holder, RadioData model, int position) {
                holder.onBind(model, PlayerActivity.this);
            }
        };

        final LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);

        content.list.setLayoutManager(manager);
        content.list.setAdapter(adapter);
    }

    private void initRadio(final int position){
        this.position = position;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                content.txtRadioname.setText(radioDatas.get(position).getTitle());
                mRadioManager.startRadio(radioDatas.get(position).getUrl());

            }
        },1000);
    }

    private void toogleList(){

        if (content.viewList.isShown()){
            content.viewList.setVisibility(View.GONE);
        }else{
            content.viewList.setVisibility(View.VISIBLE);
        }
    }

    private void toogleRadio(){
        if (mRadioManager.isPlaying()){
            mRadioManager.stopRadio();
        }else{
            initRadio(position);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_like:
                toogleList();
                break;

            case R.id.btn_rewind:

                prevRadio();
                break;
            case R.id.btn_play:

                toogleRadio();
                break;
            case R.id.btn_forward:

                nextRadio();
                break;

            case R.id.btn_share:

                break;
        }
    }

    @Override
    public void onRadioConnected() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                content.txtArtist.setText("Loading...");
                content.txtTitle.setText("Loading...");
            }
        });
    }

    @Override
    public void onRadioStarted() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                content.btnPlay.setImageResource(R.drawable.pause);
            }
        });
    }

    @Override
    public void onRadioStopped() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                content.btnPlay.setImageResource(R.drawable.play);
            }
        });
    }

    @Override
    public void onMetaDataReceived(String s, String s1) {
        if (s!= null && s1 != null){
            if (s.equalsIgnoreCase("StreamTitle")){
                String[] data = s1.split(" - ");
                if (data.length <= 1) {
                    artist = "Unknown";
                    title = "Unknown";
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            content.txtArtist.setText(artist);
                            content.txtTitle.setText(title);
                            Glide.with(getApplicationContext())
                                    .load(R.mipmap.ic_launcher).asBitmap()
                                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                                    .centerCrop()
                                    .dontAnimate()
                                    .into(new SimpleTarget<Bitmap>() {
                                        @Override
                                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                            changeBackgroundColor(resource);
                                        }
                                    });

                            updateNotification(artist, title, null);
                        }
                    });

                    return;
                }

                artist = data[0];
                title = data[1];
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        content.txtArtist.setText(artist);
                        content.txtTitle.setText(title);
                        mRadioManager.updateNotification(artist,title, R.mipmap.ic_launcher);
                        parseImagePresenter.getImage(artist + " - "+ title);
                    }
                });
            }
        }
    }

    @Override
    public void radioOnclick(int position) {
        toogleList();
        initRadio(position);
    }

    @Override
    public void onPrevButtonPress() {
        prevRadio();
    }

    @Override
    public void onNextButtonPress() {
        nextRadio();
    }

    @Override
    public void onPlayPauseButtonPress() {
        toogleRadio();
    }

    private void nextRadio(){
        if (position < radioDatas.size()-1){
            position++;
        }else{
            position = 0;
        }
        if (mRadioManager.isPlaying())
            mRadioManager.stopRadio();

        initRadio(position);
    }

    private void prevRadio(){
        if (position > 0){
            position--;
        }else{
            position = radioDatas.size() - 1;
        }
        if (mRadioManager.isPlaying())
            mRadioManager.stopRadio();

        initRadio(position);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof String){
            String a = (String) arg;
            Glide.with(this)
                    .load(a).asBitmap()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .centerCrop()
                    .dontAnimate()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            content.imgArtist.setImageBitmap(resource);
                            updateNotification(artist, title, resource);
                            changeBackgroundColor(resource);
                        }
                    });
        }
    }

    private void changeBackgroundColor(Bitmap bitmap){
        final int newColor = getDominantColor(bitmap);
        int duration = 1000;
        ObjectAnimator.ofObject(content.background, "backgroundColor", new ArgbEvaluator(), content.background.getDrawingCacheBackgroundColor(), newColor)
                .setDuration(duration)
                .start();

        updateAudioVisualization(newColor);

    }

    public int getDominantColor(Bitmap bitmap) {
        Bitmap newBitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true);
        final int color = newBitmap.getPixel(0, 0);
        newBitmap.recycle();
        return color;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (content.viewList.isShown()){
                    toogleList();
                    return true;
                }
                if (isTaskRoot() && mRadioManager.isPlaying()) {
                    Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                    homeIntent.addCategory(Intent.CATEGORY_HOME);
                    startActivity(homeIntent);
                    return true;
                } else {
                    super.onKeyDown(keyCode, event);
                    return true;
                }

            default:
                super.onKeyDown(keyCode, event);
                return true;
        }
    }
}

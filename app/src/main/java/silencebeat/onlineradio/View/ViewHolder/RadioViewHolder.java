package silencebeat.onlineradio.View.ViewHolder;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import silencebeat.onlineradio.Model.RadioData;
import silencebeat.onlineradio.Presenter.RadioOnclickListener;
import silencebeat.onlineradio.databinding.ItemRadioBinding;

/**
 * Created by Candra Triyadi on 30/09/2017.
 */

public class RadioViewHolder extends RecyclerView.ViewHolder {

    ItemRadioBinding content;

    public RadioViewHolder(View itemView) {
        super(itemView);
        content = DataBindingUtil.bind(itemView);
    }

    public void onBind(RadioData model, final RadioOnclickListener listener){

        content.txtTitle.setText(model.getTitle());
        content.txtGenres.setText(model.getGenres());

        content.getRoot().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.radioOnclick(getAdapterPosition());
            }
        });
    }
}

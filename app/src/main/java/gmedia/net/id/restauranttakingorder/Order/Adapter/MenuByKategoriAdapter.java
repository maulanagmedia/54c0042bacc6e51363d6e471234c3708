package gmedia.net.id.restauranttakingorder.Order.Adapter;

/**
 * Created by Shin on 3/21/2017.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ImageUtils;
import com.maulana.custommodul.ItemValidation;
import com.maulana.custommodul.SessionManager;

import java.util.List;

import gmedia.net.id.restauranttakingorder.R;

public class MenuByKategoriAdapter extends RecyclerView.Adapter<MenuByKategoriAdapter.MyViewHolder> {

    private Context context;
    private List<CustomItem> masterList;
    public static int position = 0;
    private ItemValidation iv = new ItemValidation();
    public static int selectedPosition = 0;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public ImageView ivThumbnail;
        public LinearLayout llThumbnail;
        public TextView tvThumbnail, tvItem1, tvItem2;

        public MyViewHolder(View view) {

            super(view);

            ivThumbnail = (ImageView) view.findViewById(R.id.iv_thumbnail);
            llThumbnail = (LinearLayout) view.findViewById(R.id.ll_thumbnail);
            tvThumbnail = (TextView) view.findViewById(R.id.tv_text_thumbnail);
            tvItem1 = (TextView) view.findViewById(R.id.tv_item1);
            tvItem2 = (TextView) view.findViewById(R.id.tv_item2);
        }
    }

    public MenuByKategoriAdapter(Context context, List masterlist){
        this.context = context;
        this.masterList = masterlist;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_menu_by_kategori, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        final CustomItem cli = masterList.get(position);

        if(position == selectedPosition){

        }else{

        }

        holder.tvItem1.setText(cli.getItem2());
        holder.tvItem2.setText(cli.getItem3());
        if(cli.getItem4().equals("")){

            String firstWord = cli.getItem2().substring(0,0);
            holder.tvThumbnail.setText(firstWord.toUpperCase());
        }
    }

    @Override
    public int getItemCount() {
        return masterList.size();
    }

}
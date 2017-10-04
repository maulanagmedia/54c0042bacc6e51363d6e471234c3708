package gmedia.net.id.restauranttakingorder.RiwayatPemesanan.Adapter;

/**
 * Created by Shin on 3/21/2017.
 */

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;

import java.util.List;

import gmedia.net.id.restauranttakingorder.Order.MainOrder;
import gmedia.net.id.restauranttakingorder.R;

public class MenuByTransaksiAdapter extends RecyclerView.Adapter<MenuByTransaksiAdapter.MyViewHolder> {

    private Context context;
    private List<CustomItem> masterList;
    public static int position = 0;
    private ItemValidation iv = new ItemValidation();
    public static int selectedPosition = 0;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public RelativeLayout rlContainer;
        public ImageView ivThumbnail;
        public LinearLayout llThumbnail;
        public TextView tvThumbnail, tvItem1, tvItem2, tvItem3, tvItem4;

        public MyViewHolder(View view) {

            super(view);

            rlContainer = (RelativeLayout) view.findViewById(R.id.rl_container);
            ivThumbnail = (ImageView) view.findViewById(R.id.iv_thumbnail);
            llThumbnail = (LinearLayout) view.findViewById(R.id.ll_thumbnail);
            tvThumbnail = (TextView) view.findViewById(R.id.tv_text_thumbnail);
            tvItem1 = (TextView) view.findViewById(R.id.tv_item1);
            tvItem2 = (TextView) view.findViewById(R.id.tv_item2);
            tvItem3 = (TextView) view.findViewById(R.id.tv_item3);
            tvItem4 = (TextView) view.findViewById(R.id.tv_item4);
        }
    }

    public MenuByTransaksiAdapter(Context context, List masterlist){
        this.context = context;
        this.masterList = masterlist;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_menu_by_transaksi, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        final CustomItem cli = masterList.get(position);

        if(position == selectedPosition){

        }else{

        }

        holder.tvItem1.setText(cli.getItem2());
        holder.tvItem2.setText("@ "+iv.ChangeToRupiahFormat(iv.parseNullDouble(cli.getItem3())));
        holder.tvItem3.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(cli.getItem6())));
        holder.tvItem4.setText(cli.getItem5());
        if(cli.getItem4().equals("")){

            String firstWord = cli.getItem2().substring(0,1);
            holder.tvThumbnail.setText(firstWord.toUpperCase());
        }

        holder.rlContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return masterList.size();
    }

}
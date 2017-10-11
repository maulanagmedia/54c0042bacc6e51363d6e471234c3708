package gmedia.net.id.restauranttakingorder.Order.Adapter;

/**
 * Created by Shin on 3/21/2017.
 */

import android.content.Context;
import android.content.Intent;
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

import gmedia.net.id.restauranttakingorder.Order.DetailOrder;
import gmedia.net.id.restauranttakingorder.Order.MainOrder;
import gmedia.net.id.restauranttakingorder.R;

public class ListMejaAdapter extends RecyclerView.Adapter<ListMejaAdapter.MyViewHolder> {

    private Context context;
    private List<CustomItem> masterList;
    public static int position = 0;
    private ItemValidation iv = new ItemValidation();
    public static int selectedPosition = 0;

    public class MyViewHolder extends RecyclerView.ViewHolder {

        public RelativeLayout rlContainer;
        public TextView tvItem1, tvItem2, tvItem3;

        public MyViewHolder(View view) {

            super(view);

            rlContainer = (RelativeLayout) view.findViewById(R.id.rl_container);
            tvItem1 = (TextView) view.findViewById(R.id.tv_item1);
            tvItem2 = (TextView) view.findViewById(R.id.tv_item2);
            tvItem3 = (TextView) view.findViewById(R.id.tv_item3);
        }
    }

    public ListMejaAdapter(Context context, List masterlist){
        this.context = context;
        this.masterList = masterlist;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.adapter_meja, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int position) {

        final CustomItem cli = masterList.get(position);

        if(position == selectedPosition){

        }else{

        }

        holder.tvItem1.setText(cli.getItem2());
        if(cli.getItem3().equals("1")){
            holder.rlContainer.setBackgroundColor(context.getResources().getColor(R.color.color_table_active));
            holder.tvItem2.setText(cli.getItem5());
            holder.tvItem3.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(cli.getItem6())));
        }else{
            holder.rlContainer.setBackgroundColor(context.getResources().getColor(R.color.color_table));
            holder.tvItem2.setText("");
            holder.tvItem3.setText("");
        }

        holder.rlContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(context, DetailOrder.class);
                intent.putExtra("kdmeja", cli.getItem1());
                intent.putExtra("nomeja", cli.getItem2());
                intent.putExtra("statusmeja", cli.getItem3());
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return masterList.size();
    }

}
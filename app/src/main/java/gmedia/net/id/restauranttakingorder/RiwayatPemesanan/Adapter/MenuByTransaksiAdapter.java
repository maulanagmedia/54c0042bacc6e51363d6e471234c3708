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

public class MenuByTransaksiAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<CustomItem> masterList;
    public static int position = 0;
    private ItemValidation iv = new ItemValidation();
    public static int selectedPosition = 0;

    public class MyViewHolder0 extends RecyclerView.ViewHolder {

        public LinearLayout llNote;
        public TextView tvItem1, tvItem2, tvItem3;

        public MyViewHolder0(View view) {

            super(view);
            tvItem1 = (TextView) view.findViewById(R.id.tv_item1);
        }
    }

    public class MyViewHolder1 extends RecyclerView.ViewHolder {

        public LinearLayout llNote;
        public TextView tvItem1, tvItem2, tvItem3, tvNote;

        public MyViewHolder1(View view) {

            super(view);

            llNote = (LinearLayout) view.findViewById(R.id.ll_note);
            tvItem1 = (TextView) view.findViewById(R.id.tv_item1);
            tvItem2 = (TextView) view.findViewById(R.id.tv_item2);
            tvItem3 = (TextView) view.findViewById(R.id.tv_item3);
            tvNote = (TextView) view.findViewById(R.id.tv_note);
        }
    }

    public MenuByTransaksiAdapter(Context context, List masterlist){
        this.context = context;
        this.masterList = masterlist;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View itemView = null ;
        if(viewType == 0 ){
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_menu_by_transaksi_title, parent, false);
            return new MyViewHolder0(itemView);
        }else{
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.adapter_menu_by_transaksi, parent, false);
            return new MyViewHolder1(itemView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final CustomItem cli = masterList.get(position);

        if(holder.getItemViewType() == 0){

            MyViewHolder0 holder0 = (MyViewHolder0) holder;
            if(cli.getItem3().equals("1")){

                holder0.tvItem1.setText("Order pertama ("+ cli.getItem2()+")");
            }else{
                holder0.tvItem1.setText("Upselling "+ cli.getItem3() +" ("+ cli.getItem2()+")");
            }
        }else{

            MyViewHolder1 holder1 = (MyViewHolder1) holder;

            holder1.tvItem1.setText(cli.getItem2());
            holder1.tvItem2.setText(cli.getItem5());
            if(cli.getItem4().length()>0 || cli.getItem9().length() > 0){
                holder1.llNote.setVisibility(View.VISIBLE);
                holder1.tvItem3.setText((cli.getItem9().length() > 0 ? cli.getItem9()+ " ": "")+cli.getItem4());
            }else{
                holder1.llNote.setVisibility(View.GONE);
            }

            if(cli.getItem7().equals("0") || cli.getItem8().equals("0")){

                holder1.tvItem1.setTextColor(context.getResources().getColor(R.color.color_blue));
                holder1.tvItem2.setTextColor(context.getResources().getColor(R.color.color_blue));
                holder1.tvItem3.setTextColor(context.getResources().getColor(R.color.color_blue));
                holder1.tvNote.setTextColor(context.getResources().getColor(R.color.color_blue));
            }else{

                holder1.tvItem1.setTextColor(context.getResources().getColor(R.color.color_red));
                holder1.tvItem2.setTextColor(context.getResources().getColor(R.color.color_red));
                holder1.tvItem3.setTextColor(context.getResources().getColor(R.color.color_red));
                holder1.tvNote.setTextColor(context.getResources().getColor(R.color.color_red));
            }

        }
    }

    @Override
    public int getItemViewType(int position) {

        int hasil = 0;
        final CustomItem item = masterList.get(position);
        if(item.getItem1().equals("H")){
            hasil = 0;
        }else{
            hasil = 1;
        }
        return hasil;
    }

    @Override
    public int getItemCount() {
        return masterList.size();
    }

}
package gmedia.net.id.restauranttakingorder.RiwayatPemesanan.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;

import java.util.List;

import gmedia.net.id.restauranttakingorder.R;
import gmedia.net.id.restauranttakingorder.RiwayatPemesanan.MainRiwayatPemesanan;
import gmedia.net.id.restauranttakingorder.Utils.FormatItem;


/**
 * Created by Shin on 1/8/2017.
 */

public class ListTransaksiAdapter extends ArrayAdapter{

    private Activity context;
    private List<CustomItem> items;
    private ItemValidation iv = new ItemValidation();

    public ListTransaksiAdapter(Activity context, List<CustomItem> items) {
        super(context, R.layout.adapter_list_transaksi, items);
        this.context = context;
        this.items = items;
    }

    private static class ViewHolder {
        private LinearLayout llContainer, llLine;
        private TextView tvItem1, tvItem2, tvItem3, tvItem4, tvItem5;
        private TextView tvTitle1, tvTitle2, tvTitle3, tvTitle4, tvTitle5;
    }

    public void addMoreData(List<CustomItem> moreData){

        items.addAll(moreData);
        notifyDataSetChanged();
    }

    /*@Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {

        int hasil = 0;
        final CustomItem item = items.get(position);
        if(!item.getItem9().equals("1") || !item.getItem10().equals("1") || !item.getItem11().equals("1")){
            hasil = 0;
        }else{
            hasil = 1;
        }
        return hasil;
    }*/

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder = new ViewHolder();
        //int tipeViewList = getItemViewType(position);

        if(convertView == null){

            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            convertView = inflater.inflate(R.layout.adapter_list_transaksi, null);

            holder.llContainer = (LinearLayout) convertView.findViewById(R.id.ll_container);
            holder.llLine= (LinearLayout) convertView.findViewById(R.id.ll_line);
            holder.tvItem1 = (TextView) convertView.findViewById(R.id.tv_item1);
            holder.tvItem2 = (TextView) convertView.findViewById(R.id.tv_item2);
            holder.tvItem3 = (TextView) convertView.findViewById(R.id.tv_item3);
            holder.tvItem4 = (TextView) convertView.findViewById(R.id.tv_item4);
            holder.tvItem5 = (TextView) convertView.findViewById(R.id.tv_item5);
            holder.tvTitle1 = (TextView) convertView.findViewById(R.id.tv_title1);
            holder.tvTitle2 = (TextView) convertView.findViewById(R.id.tv_title2);
            holder.tvTitle3 = (TextView) convertView.findViewById(R.id.tv_title3);
            holder.tvTitle4 = (TextView) convertView.findViewById(R.id.tv_title4);
            holder.tvTitle5 = (TextView) convertView.findViewById(R.id.tv_title5);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        final CustomItem itemSelected = items.get(position);
        holder.tvItem1.setText(itemSelected.getItem6());
        holder.tvItem2.setText(iv.ChangeFormatDateString(itemSelected.getItem5(), FormatItem.formatTimestamp, FormatItem.formatDataAndTime));
        holder.tvItem3.setText(itemSelected.getItem3());
        //holder.tvItem3.setText(itemSelected.getItem7()); pramusaji
        holder.tvItem4.setText(itemSelected.getItem2());
        holder.tvItem5.setText(itemSelected.getItem8());

        holder.llContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                MainRiwayatPemesanan.selectTransaksi(itemSelected);
            }
        });

        return convertView;
    }
}

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
        private TextView tvItem1, tvItem2, tvItem3;
    }

    public void addMoreData(List<CustomItem> moreData){

        items.addAll(moreData);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return super.getCount();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder holder = new ViewHolder();

        if(convertView == null){
            LayoutInflater inflater = context.getLayoutInflater();
            convertView = inflater.inflate(R.layout.adapter_list_transaksi, null);
            holder.tvItem1 = (TextView) convertView.findViewById(R.id.tv_item1);
            holder.tvItem2 = (TextView) convertView.findViewById(R.id.tv_item2);
            holder.tvItem3 = (TextView) convertView.findViewById(R.id.tv_item3);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        final CustomItem itemSelected = items.get(position);
        holder.tvItem1.setText(itemSelected.getItem3());
        holder.tvItem2.setText(iv.ChangeFormatDateString(itemSelected.getItem5(), FormatItem.formatTimestamp, FormatItem.formatDataAndTime));
        holder.tvItem3.setText(iv.ChangeToRupiahFormat(iv.parseNullDouble(itemSelected.getItem4())));
        return convertView;
    }
}

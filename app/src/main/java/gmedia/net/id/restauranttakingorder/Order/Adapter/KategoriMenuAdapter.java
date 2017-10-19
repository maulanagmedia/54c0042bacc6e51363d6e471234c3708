package gmedia.net.id.restauranttakingorder.Order.Adapter;

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

public class KategoriMenuAdapter extends ArrayAdapter{

    private Activity context;
    private List<CustomItem> items;
    private ItemValidation iv = new ItemValidation();
    public static int selectedPosition = 0;

    public KategoriMenuAdapter(Activity context, List<CustomItem> items) {
        super(context, R.layout.adapter_kategori_menu, items);
        this.context = context;
        this.items = items;
    }

    private static class ViewHolder {
        private TextView tvItem1;
        private LinearLayout llContainer;
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
            convertView = inflater.inflate(R.layout.adapter_kategori_menu, null);
            holder.tvItem1 = (TextView) convertView.findViewById(R.id.tv_item1);
            holder.llContainer = (LinearLayout) convertView.findViewById(R.id.ll_container);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        if(position == selectedPosition){
            holder.llContainer.setBackgroundColor(context.getResources().getColor(R.color.color_red));
            holder.tvItem1.setTextColor(context.getResources().getColor(R.color.color_white));
            notifyDataSetChanged();
        }else{
            holder.llContainer.setBackgroundColor(context.getResources().getColor(R.color.color_white));
            holder.tvItem1.setTextColor(context.getResources().getColor(R.color.color_text));
            notifyDataSetChanged();
        }

        final CustomItem itemSelected = items.get(position);
        holder.tvItem1.setText(itemSelected.getItem2());
        return convertView;

    }
}

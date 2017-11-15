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


/**
 * Created by Shin on 1/8/2017.
 */

public class ListUpsellingAdapter extends ArrayAdapter{

    private Activity context;
    private List<CustomItem> items;
    private ItemValidation iv = new ItemValidation();

    public ListUpsellingAdapter(Activity context, List<CustomItem> items) {
        super(context, R.layout.adapter_list_upselling, items);
        this.context = context;
        this.items = items;
    }

    private static class ViewHolder {
        private TextView tvItem1;
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
            convertView = inflater.inflate(R.layout.adapter_list_upselling, null);
            holder.tvItem1 = (TextView) convertView.findViewById(R.id.tv_item1);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        final CustomItem itemSelected = items.get(position);
        holder.tvItem1.setText(itemSelected.getItem2());
        if(itemSelected.getItem3().equals("1")){

            holder.tvItem1.setText("Order pertama ("+ itemSelected.getItem2()+")");
        }else{
            holder.tvItem1.setText("Upselling "+ itemSelected.getItem3() +" ("+ itemSelected.getItem2()+")");
        }

        return convertView;
    }
}

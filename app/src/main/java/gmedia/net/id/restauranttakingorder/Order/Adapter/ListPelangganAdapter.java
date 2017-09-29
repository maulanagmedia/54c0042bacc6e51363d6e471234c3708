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


/**
 * Created by Shin on 1/8/2017.
 */

public class ListPelangganAdapter extends ArrayAdapter{

    private Activity context;
    private List<CustomItem> items;
    private ItemValidation iv = new ItemValidation();
    public static int selectedPosition = 0;

    public ListPelangganAdapter(Activity context, List<CustomItem> items) {
        super(context, R.layout.adapter_list_pelanggan, items);
        this.context = context;
        this.items = items;
    }

    private static class ViewHolder {
        private LinearLayout llContainer;
        private TextView tvItem1, tvItem2;
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
            convertView = inflater.inflate(R.layout.adapter_list_pelanggan, null);
            holder.llContainer = (LinearLayout) convertView.findViewById(R.id.ll_container);
            holder.tvItem1 = (TextView) convertView.findViewById(R.id.tv_item1);
            holder.tvItem2 = (TextView) convertView.findViewById(R.id.tv_item2);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }
        final CustomItem itemSelected = items.get(position);
        holder.tvItem1.setText(itemSelected.getItem2().charAt(0)+"");
        holder.tvItem2.setText(itemSelected.getItem2());

        /*holder.llContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });*/
        return convertView;

    }
}

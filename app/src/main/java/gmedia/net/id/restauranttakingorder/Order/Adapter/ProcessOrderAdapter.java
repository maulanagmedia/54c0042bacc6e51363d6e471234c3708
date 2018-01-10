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

public class ProcessOrderAdapter extends ArrayAdapter{

    private Activity context;
    private List<CustomItem> items;
    private ItemValidation iv = new ItemValidation();
    private String flag = "";

    public ProcessOrderAdapter(Activity context, List<CustomItem> items, String flag) {
        super(context, R.layout.adapter_process_order, items);
        this.context = context;
        this.items = items;
        this.flag = flag;
    }

    private static class ViewHolder {
        private TextView tvItem1, tvItem2, tvItem3, tvItem4, tvNote;
        private LinearLayout llNote;
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
            convertView = inflater.inflate(R.layout.adapter_process_order, null);
            holder.tvItem1 = (TextView) convertView.findViewById(R.id.tv_item1);
            holder.tvItem2 = (TextView) convertView.findViewById(R.id.tv_item2);
            holder.tvItem3 = (TextView) convertView.findViewById(R.id.tv_item3);
            holder.tvItem4 = (TextView) convertView.findViewById(R.id.tv_item4);
            holder.tvNote = (TextView) convertView.findViewById(R.id.tv_note);
            holder.llNote = (LinearLayout) convertView.findViewById(R.id.ll_note);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        final CustomItem itemSelected = items.get(position);
        holder.tvItem1.setText(itemSelected.getItem2());
        if(itemSelected.getItem4().equals("TA")){
            holder.tvItem1.setText(itemSelected.getItem2() + " ("+ itemSelected.getItem4()+")");
        }

        holder.tvItem2.setText(itemSelected.getItem3());
        if(flag.equals("0")){
            holder.tvItem3.setText(itemSelected.getItem6());
        }else{

            LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
            p.weight = (float) 0.2;

            LinearLayout.LayoutParams p2 = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
            p2.weight = (float) 0.8;

            holder.tvItem1.setLayoutParams(p2);
            holder.tvItem2.setLayoutParams(p);
        }

        if(itemSelected.getItem5().length() > 0){
            holder.llNote.setVisibility(View.VISIBLE);
            holder.tvItem4.setText(itemSelected.getItem5());
        }else{
            holder.llNote.setVisibility(View.GONE);
        }

        if(itemSelected.getItem7().equals("0")){

            holder.tvItem1.setTextColor(context.getResources().getColor(R.color.color_blue));
            /*holder.tvItem4.setTextColor(context.getResources().getColor(R.color.color_blue));
            holder.tvNote.setTextColor(context.getResources().getColor(R.color.color_blue));*/

        }else{

            holder.tvItem1.setTextColor(context.getResources().getColor(R.color.color_red));
            /*holder.tvItem4.setTextColor(context.getResources().getColor(R.color.color_red));
            holder.tvNote.setTextColor(context.getResources().getColor(R.color.color_red));*/
        }

        return convertView;
    }
}

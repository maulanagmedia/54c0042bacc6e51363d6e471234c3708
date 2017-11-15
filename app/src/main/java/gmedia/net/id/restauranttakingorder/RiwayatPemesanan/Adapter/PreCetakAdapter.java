package gmedia.net.id.restauranttakingorder.RiwayatPemesanan.Adapter;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ItemValidation;

import java.util.ArrayList;
import java.util.List;

import gmedia.net.id.restauranttakingorder.R;


/**
 * Created by Shin on 1/8/2017.
 */

public class PreCetakAdapter extends ArrayAdapter{

    private Activity context;
    private List<CustomItem> items;
    private ItemValidation iv = new ItemValidation();

    public PreCetakAdapter(Activity context, List<CustomItem> items) {
        super(context, R.layout.adapter_list_pre_cetak, items);
        this.context = context;
        this.items = items;
    }

    private static class ViewHolder {
        private TextView tvItem2, tvItem3, tvNote;
        private LinearLayout llNote;
        private CheckBox cbPrint;
    }

    public List<CustomItem> getItems(){
        List<CustomItem> newItem = new ArrayList<>();

        for(CustomItem item : items){
            if(item.getItem15() != null && item.getItem15().equals("1")) newItem.add(item);
        }

        return  newItem;
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
            convertView = inflater.inflate(R.layout.adapter_list_pre_cetak, null);
            holder.tvItem2 = (TextView) convertView.findViewById(R.id.tv_item2);
            holder.tvItem3 = (TextView) convertView.findViewById(R.id.tv_item3);
            holder.tvNote = (TextView) convertView.findViewById(R.id.tv_note);
            holder.llNote = (LinearLayout) convertView.findViewById(R.id.ll_note);
            holder.cbPrint = (CheckBox) convertView.findViewById(R.id.cb_print);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        final CustomItem itemSelected = items.get(position);

        if(position == 0){

            holder.cbPrint.setText("Check All");
            holder.tvItem2.setText("");

            holder.cbPrint.setTextColor(context.getResources().getColor(R.color.color_black));
            holder.tvItem2.setTextColor(context.getResources().getColor(R.color.color_black));

            holder.llNote.setVisibility(View.GONE);
            boolean checkAll = true;
            for(CustomItem item: items) {
                if(item.getItem15() != null && !item.getItem15().equals("1")) checkAll = false;
            }
            holder.cbPrint.setChecked(checkAll);
        }else{

            holder.cbPrint.setText(itemSelected.getItem2());
            holder.tvItem2.setText(itemSelected.getItem5());
            if(itemSelected.getItem8().length() > 0){
                holder.llNote.setVisibility(View.VISIBLE);
                holder.tvItem3.setText(itemSelected.getItem8());
            }else{
                holder.llNote.setVisibility(View.GONE);
            }

            if(itemSelected.getItem13().equals("0") || itemSelected.getItem14().equals("0")){

                holder.cbPrint.setTextColor(context.getResources().getColor(R.color.color_blue));
                holder.tvItem2.setTextColor(context.getResources().getColor(R.color.color_blue));
                holder.tvItem3.setTextColor(context.getResources().getColor(R.color.color_blue));
                holder.tvNote.setTextColor(context.getResources().getColor(R.color.color_blue));
            }else{

                holder.cbPrint.setTextColor(context.getResources().getColor(R.color.color_red));
                holder.tvItem2.setTextColor(context.getResources().getColor(R.color.color_red));
                holder.tvItem3.setTextColor(context.getResources().getColor(R.color.color_red));
                holder.tvNote.setTextColor(context.getResources().getColor(R.color.color_red));
            }

            if(itemSelected.getItem15().equals("1")){

                holder.cbPrint.setChecked(true);
            }else{
                holder.cbPrint.setChecked(false);
            }
        }

        final ViewHolder finalHolder = holder;

        holder.cbPrint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean b = finalHolder.cbPrint.isChecked();
                if(position == 0){
                    if(b){

                        int x = 0;
                        for(CustomItem item: items) {
                            if(item.getItem15() != null) {
                                items.get(x).setItem15("1");
                            }
                            x++;
                        }
                    }else{

                        int x = 0;
                        for(CustomItem item: items) {
                            if(item.getItem15() != null) {
                                items.get(x).setItem15("0");
                            }
                            x++;
                        }
                    }
                }else{

                    if(b){

                        items.get(position).setItem15("1");
                    }else{
                        items.get(position).setItem15("0");
                    }
                }

                notifyDataSetChanged();
            }
        });

        return convertView;
    }
}

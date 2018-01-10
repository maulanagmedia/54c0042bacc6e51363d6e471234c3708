package gmedia.net.id.restauranttakingorder.Order.Adapter;

import android.app.Activity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.maulana.custommodul.CustomItem;
import com.maulana.custommodul.ImageUtils;
import com.maulana.custommodul.ItemValidation;

import java.util.List;

import gmedia.net.id.restauranttakingorder.R;


/**
 * Created by Shin on 1/8/2017.
 */

public class SoldOutMenuAdapter extends ArrayAdapter{

    private Activity context;
    private List<CustomItem> items;
    private ItemValidation iv = new ItemValidation();

    public SoldOutMenuAdapter(Activity context, List<CustomItem> items) {
        super(context, R.layout.adapter_menu_sold_out, items);
        this.context = context;
        this.items = items;
    }

    private static class ViewHolder {
        private TextView tvItem1, tvItem2, tvThumbnail;
        private LinearLayout llThumbnail;
        private ImageView ivThumbnail;
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
            convertView = inflater.inflate(R.layout.adapter_menu_sold_out, null);
            holder.ivThumbnail = (ImageView) convertView.findViewById(R.id.iv_thumbnail);
            holder.llThumbnail = (LinearLayout) convertView.findViewById(R.id.ll_thumbnail);
            holder.tvThumbnail = (TextView) convertView.findViewById(R.id.tv_text_thumbnail);
            holder.tvItem1 = (TextView) convertView.findViewById(R.id.tv_item1);
            holder.tvItem2 = (TextView) convertView.findViewById(R.id.tv_item2);
            convertView.setTag(holder);
        }else{
            holder = (ViewHolder) convertView.getTag();
        }

        final CustomItem cli = items.get(position);

        holder.tvItem1.setText(cli.getItem2());
        holder.tvItem2.setText( "@ " + iv.ChangeToRupiahFormat(iv.parseNullDouble(cli.getItem3())));

        if(cli.getItem4().equals("")){

            holder.llThumbnail.setVisibility(View.VISIBLE);
            holder.ivThumbnail.setVisibility(View.GONE);
            String firstWord = cli.getItem2().substring(0,1);
            holder.tvThumbnail.setText(firstWord.toUpperCase());
        }else{
            holder.llThumbnail.setVisibility(View.GONE);
            holder.ivThumbnail.setVisibility(View.VISIBLE);
            ImageUtils iu = new ImageUtils();
            iu.LoadCircleRealImage(context, cli.getItem4(),holder.ivThumbnail);
        }

        return convertView;
    }
}

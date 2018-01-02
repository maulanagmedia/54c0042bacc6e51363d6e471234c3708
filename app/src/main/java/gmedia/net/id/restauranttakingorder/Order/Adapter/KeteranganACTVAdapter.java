package gmedia.net.id.restauranttakingorder.Order.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.maulana.custommodul.CustomItem;

import java.util.ArrayList;
import java.util.List;

import gmedia.net.id.restauranttakingorder.Utils.ItemLength;

/**
 * Created by Shin on 2/2/2017.
 */

public class KeteranganACTVAdapter extends ArrayAdapter {

    private Context context;
    private int resource;
    private List<CustomItem> items, tempItems, suggestions;
    private int rowPerAutocompleteItem;

    public KeteranganACTVAdapter(Context context, List<CustomItem> items) {
        super(context, android.R.layout.simple_list_item_1, items);
        this.context = context;
        this.resource = resource;
        this.items = items;
        tempItems = new ArrayList<CustomItem>(items);
        suggestions = new ArrayList<CustomItem>();
        rowPerAutocompleteItem = ItemLength.autocompleteLength;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
        }

        CustomItem list = items.get(position);
        if (list != null) {
            TextView autocompleteText = (TextView) view.findViewById(android.R.id.text1);
            if (autocompleteText != null)
                autocompleteText.setText(list.getItem2());
        }
        return view;
    }

    @Override
    public Filter getFilter() {
        return nameFilter;
    }

    /**
     * Custom Filter implementation for custom suggestions we provide.
     */
    Filter nameFilter = new Filter() {
        @Override
        public CharSequence convertResultToString(Object resultValue) {
            String str = "";
            str = ((CustomItem) resultValue).getItem2();
            return str;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (constraint != null) {
                suggestions.clear();
                int x = 0;
                for (CustomItem list : tempItems) {
                    if (list.getItem2().toLowerCase().contains(constraint.toString().toLowerCase())) {
                        suggestions.add(list);
                        x++;
                    }
                    if(x >= rowPerAutocompleteItem) break;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = suggestions;
                filterResults.count = suggestions.size();
                return filterResults;
            } else {
                return new FilterResults();
            }
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            List<CustomItem> filterList = (ArrayList<CustomItem>) results.values;
            if (results != null && results.count > 0) {
                clear();
                addAll(filterList);
                notifyDataSetChanged();
            }else{
                notifyDataSetInvalidated();
            }
        }
    };
}

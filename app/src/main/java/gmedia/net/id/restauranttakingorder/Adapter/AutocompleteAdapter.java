package gmedia.net.id.restauranttakingorder.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.TextView;

import com.maulana.custommodul.CustomItem;

import gmedia.net.id.restauranttakingorder.Utils.ItemLength;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shin on 2/2/2017.
 */

public class AutocompleteAdapter extends ArrayAdapter {

    private Context context;
    private int resource;
    private List<CustomItem> items, tempItems, suggestions;
    private int rowPerAutocompleteItem; // list 50 item only
    private String flagDisplay = "C"; // custom

    public AutocompleteAdapter(Context context, List<CustomItem> items, String flagDisplay) {
        super(context, android.R.layout.simple_list_item_1, items);
        this.context = context;
        this.resource = resource;
        this.items = items;
        tempItems = new ArrayList<CustomItem>(items);
        suggestions = new ArrayList<CustomItem>();
        rowPerAutocompleteItem = ItemLength.autocompleteLength;
        this.flagDisplay = flagDisplay;
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
                if(flagDisplay.toUpperCase().equals("C")){
                    autocompleteText.setText(list.getItem2() +" ("+list.getItem3()+")");
                }else{
                    autocompleteText.setText(list.getItem2());
                }
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
            if(!flagDisplay.toUpperCase().equals("C")){
                str = ((CustomItem) resultValue).getItem2();
            }else{
                str = ((CustomItem) resultValue).getItem2() +" ("+((CustomItem) resultValue).getItem3()+")";
            }
            return str;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (constraint != null) {
                suggestions.clear();
                int x = 0;
                for (CustomItem list : tempItems) {
                    if(flagDisplay.toUpperCase().equals("C")){
                        if (list.getItem2().toLowerCase().contains(constraint.toString().toLowerCase()) || list.getItem3().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            suggestions.add(list);
                            x++;
                        }
                    }else{
                        if (list.getItem2().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            suggestions.add(list);
                            x++;
                        }
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

package fr.bmartel.android.tictactoe.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import fr.bmartel.android.tictactoe.R;
import fr.bmartel.android.tictactoe.datamodel.DeviceItem;

public class DeviceAdapter extends ArrayAdapter<DeviceItem> {

    List<DeviceItem> scanningList = new ArrayList<>();

    private static LayoutInflater inflater = null;

    public DeviceAdapter(Context context, int textViewResourceId,
                         List<DeviceItem> objects) {
        super(context, textViewResourceId, objects);

        this.scanningList = objects;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View vi = convertView;

        final ViewHolder holder;
        try {
            if (convertView == null) {
                vi = inflater.inflate(R.layout.listview_item, null);
                holder = new ViewHolder();
                holder.deviceName = (TextView) vi.findViewById(R.id.text);

                vi.setTag(holder);
            } else {
                holder = (ViewHolder) vi.getTag();
            }
            holder.deviceName.setText(scanningList.get(position).getDeviceName());

        } catch (Exception e) {
            e.printStackTrace();
        }
        return vi;
    }

    public List<DeviceItem> getDeviceList() {
        return scanningList;
    }

    @Override
    public long getItemId(int position) {

        return position;
    }

    public int getCount() {
        return scanningList.size();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public static class ViewHolder {
        public TextView deviceName;
    }

}
package com.vcontrol.vcontroliot.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vcontrol.vcontroliot.R;

import java.util.List;

public class CustomAdapter extends BaseAdapter
{
    private List<String> l;
    private Context context;

    public CustomAdapter(Context context, List<String> l)
    {
        this.context = context;
        this.l = l;
    }

    @Override
    public int getCount()
    {
        return l.size();
    }

    @Override
    public Object getItem(int position)
    {
        return position;
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        Holder holder = null;
        if (convertView == null)
        {
            holder = new Holder();
            convertView = LayoutInflater.from(context).inflate(R.layout.item,
                    null);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            convertView.setTag(holder);
        }
        else
        {
            holder = (Holder) convertView.getTag();
        }

        holder.name.setText(l.get(position).toString());
        return convertView;
    }

    class Holder
    {
        TextView name;
    }

}

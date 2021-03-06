package com.defaup.newsgateway;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.Map;

class SourceAdapter extends ArrayAdapter<Source>
{
    private List<Source> sources;
    private Map<String,Integer> colorMap;

    SourceAdapter(@NonNull Context context, int layoutID, @NonNull List<Source> sources, Map<String,Integer> colorMap)
    {
        super(context, layoutID, sources);
        this.sources = sources;
        this.colorMap = colorMap;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
    {
        convertView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.drawer_layout_item, parent, false);

        String sourceName = sources.get(position).getName();
        TextView drawerItem = convertView.findViewById(R.id.drawerEntry);
        drawerItem.setText(sourceName);
        drawerItem.setTextColor(colorMap.get(sources.get(position).getCategory()));
        return convertView;
    }
}

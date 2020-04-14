package com.defaup.newsgateway;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

class SourceAdapter extends ArrayAdapter<Source>
{
    private List<Source> sources;
    public SourceAdapter(@NonNull Context context, int resource, @NonNull List<Source> sources)
    {
        super(context, resource, sources);
        this.sources = sources;
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
        return convertView;
    }
}

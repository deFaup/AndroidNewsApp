package com.defaup.newsgateway;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FragmentArticle extends Fragment
{
    private static final String TAG = "Greg_FragmentArticle";
    private String articleUrl;

    public FragmentArticle(){};

    static public FragmentArticle newInstance
            (Article article, int index, int max)
    {
        FragmentArticle fragment = new FragmentArticle();
        Bundle bundle = new Bundle();
        bundle.putSerializable("ARTICLE_OBJECT", article);

        String indexString = String.format("%d of %d",index, max);
        bundle.putString("ARTICLE_INDEX", indexString);

        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState)
    {
        View fragmentView = inflater.inflate(R.layout.fragment_layout, container, false);
        Bundle args = getArguments();
        if (args == null) return null;

        final Article article = (Article) args.getSerializable("ARTICLE_OBJECT");
        if (article == null) return null;
        this.articleUrl = article.url;

        LinearLayout linearLayout = fragmentView.findViewById(R.id.fragmentLinearLayout);
        TextView textView;

        if (article.title != null)
        {
            textView = new TextView(getContext());
            textView.setTextColor(Color.BLACK);
            textView.setText(article.title);
            textView.setTextSize(24);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            setOnClick(textView);
            linearLayout.addView(textView);
        }
        if (article.publishedAt != null)
        {
            // we have this pattern "2013-03-05T18:05:05Z"
            String parsedDate = article.publishedAt;
            try
            {
                Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXX").parse(article.publishedAt);
                parsedDate = date.toString();
            }
            catch (ParseException e) { e.printStackTrace();}

            textView = new TextView(getContext());
            textView.setTextColor(Color.BLACK);
            textView.setText(parsedDate);
            textView.setTextSize(14);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);

            linearLayout.addView(textView);
        }
        if (article.author != null)
        {
            textView = new TextView(getContext());
            textView.setTextColor(Color.BLACK);
            textView.setText(article.author);
            textView.setTextSize(14);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);

            linearLayout.addView(textView);
        }
        if(article.urlToImage != null)
        {
            Picasso picasso = new Picasso.Builder(getContext()).build();

            ImageView imageView = new ImageView(getContext());
            picasso.load(article.urlToImage).error(R.drawable.brokenimage)
                    .placeholder(R.drawable.placeholder)
                    .into(imageView);
            setOnClick(imageView);
            linearLayout.addView(imageView);
        }
        if (article.description != null)
        {
            textView = new TextView(getContext());
            textView.setTextColor(Color.BLACK);
            textView.setText(article.description);
            textView.setTextSize(18);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            setOnClick(textView);
            linearLayout.addView(textView);
        }

        if (args.getString("ARTICLE_INDEX") != null)
        {
            textView = new TextView(getContext());
            textView.setTextColor(Color.BLACK);
            textView.setText(args.getString("ARTICLE_INDEX"));
            textView.setTextSize(15);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
            linearLayout.addView(textView);
        }
        return fragmentView;
    }

    private void setOnClick(View view)
    {
        view.setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(articleUrl));
            startActivity(intent);
        }
    });
    }

}

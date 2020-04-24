package com.defaup.newsgateway;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FragmentArticle extends Fragment
{
    private static final String TAG = "Greg_FragmentArticle";
    private String articleUrl;
    private String articleIndex;

    public FragmentArticle(){};

    static public FragmentArticle newInstance
            (Article article, int index, int max)
    {
        Log.d(TAG, "newInstance: " + index + " of " + max);
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

        this.articleIndex = args.getString("ARTICLE_INDEX");
        Log.d(TAG, "onCreateView: " + args.getString("ARTICLE_INDEX") + " " + article.title);

        LinearLayout linearLayout = fragmentView.findViewById(R.id.fragmentLinearLayout);
        TextView textView;

        if (article.title != null && !article.title.isEmpty())
        {
            textView = new TextView(getContext());
            textView.setTextColor(Color.BLACK);
            textView.setText(article.title);
            textView.setTextSize(24);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            textView.setTypeface(Typeface.DEFAULT_BOLD);
            textView.setPadding(0,0,0,5);
            setOnClick(textView);
            linearLayout.addView(textView);
        }
        if (article.publishedAt != null && !article.publishedAt.isEmpty())
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
            textView.setPadding(0,12,0,0);
            textView.setTextColor(Color.BLACK);
            textView.setText(parsedDate);
            textView.setTextSize(14);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            linearLayout.addView(textView);
        }
        if (article.author != null && !article.author.isEmpty())
        {
            textView = new TextView(getContext());
            textView.setPadding(0,8,0,0);
            textView.setTextColor(Color.BLACK);
            textView.setText(article.author);
            textView.setTextSize(14);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            textView.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
            linearLayout.addView(textView);
        }
        {
            ImageView separator = new ImageView(getContext());
            separator.setPadding(0,8,0,0);
            separator.setImageDrawable(getContext().getDrawable(R.drawable.separator));
            linearLayout.addView(separator);
        }
        if(article.urlToImage != null && !article.urlToImage.isEmpty())
        {
            Picasso picasso = new Picasso.Builder(getContext()).build();

            ImageView imageView = new ImageView(getContext());
            imageView.setPadding(0,28,0,0);

            Display display = getActivity().getWindowManager().getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            int screenHeight = size.y;

            imageView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, (int)(screenHeight*0.30)));
            //imageView.setScaleType(ImageView.ScaleType.FIT_START);

            picasso.load(article.urlToImage).error(R.drawable.brokenimage)
                    .placeholder(R.drawable.placeholder)
                    .into(imageView);
            setOnClick(imageView);
            linearLayout.addView(imageView);
        }
        if (article.description != null && !article.description.isEmpty())
        {
            textView = new TextView(getContext());
            textView.setPadding(0,12,0,0);
            textView.setTextColor(Color.BLACK);
            textView.setTextSize(18);
            textView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
            setOnClick(textView);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                textView.setText(Html.fromHtml(article.description,Html.FROM_HTML_MODE_LEGACY));
            } else {
                textView.setText(Html.fromHtml(article.description));
            }
            linearLayout.addView(textView);
        }

        if (args.getString("ARTICLE_INDEX") != null)
        {
            TextView pagerIndex = fragmentView.findViewById(R.id.pagerIndex);
            pagerIndex.setText(args.getString("ARTICLE_INDEX"));
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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: " + getArguments().getString("ARTICLE_INDEX"));
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState: "+ articleIndex);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //MainActivity mainActivity = getActivity();


    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: " + articleIndex);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: "+ articleIndex);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: " + articleIndex);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach: "+ articleIndex);
    }

}

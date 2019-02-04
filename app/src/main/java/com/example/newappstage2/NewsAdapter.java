package com.example.newappstage2;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class NewsAdapter extends ArrayAdapter<News> {

    private static final String LOG_TAG = NewsAdapter.class.getSimpleName();
    private Context context;

    public NewsAdapter(@NonNull Context context, @NonNull ArrayList<News> newsList) {
        super(context, 0, newsList);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {

        News currentNewsArticle = getItem(position);

        View listItemView = convertView;

        ViewHolder viewHolder;
        if (listItemView == null) {
            viewHolder = new ViewHolder();
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.news_list_item, parent, false);
            viewHolder.articleTitle = listItemView.findViewById(R.id.tv_title);
            viewHolder.articleSection = listItemView.findViewById(R.id.tv_section);
            viewHolder.articleAuthor = listItemView.findViewById(R.id.tv_author);
            viewHolder.articleDate = listItemView.findViewById(R.id.tv_date);
            viewHolder.articleThumbnail = listItemView.findViewById(R.id.img_thumbnail);
            listItemView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) listItemView.getTag();
        }

        if (currentNewsArticle != null) {
            viewHolder.articleTitle.setText(currentNewsArticle.getArticleTitle());
            viewHolder.articleSection.setText(currentNewsArticle.getArticleSection());
            viewHolder.articleDate.setText(formatDateAndTime(currentNewsArticle.getArticleDate()));
            viewHolder.articleThumbnail.setImageBitmap(currentNewsArticle.getArticleThumbnail());
            viewHolder.articleAuthor.setText(convertAuthorArrayToString(currentNewsArticle.getArticleAuthors()));
        }
        return listItemView;
    }

    //Format publication date to the desired format
    private String formatDateAndTime(final String time) {

        String returnFormat = context.getResources().getString(R.string.pub_date_na);
        if ((time != null) && (!time.isEmpty())) {
            try {
                //Format in which the date is available
                SimpleDateFormat availableFormat = new SimpleDateFormat(context.getResources().getString(R.string.available_date_format), Locale.ENGLISH);
                //New format to which it has to be converted
                SimpleDateFormat newFormat = new SimpleDateFormat(context.getResources().getString(R.string.new_date_format), Locale.ENGLISH);
                returnFormat = newFormat.format(availableFormat.parse(time));
            } catch (ParseException pe) {
                returnFormat = context.getResources().getString(R.string.pub_date_na);
                Log.e(LOG_TAG, context.getResources().getString(R.string.error_parse_date), pe);
            }
        }
        return returnFormat;
    }

    private String convertAuthorArrayToString(ArrayList authorsList) {
        StringBuilder authorString = new StringBuilder();
        for (int i = 0; i < authorsList.size(); i++) {
            authorString.append(authorsList.get(i));
            if ((i + 1) < authorsList.size()) {
                if ((i + 2) == authorsList.size()) {
                    authorString.append(" and ");
                } else {
                    authorString.append(", ");
                }
            }
        }
        return authorString.toString();
    }

    private static class ViewHolder {
        TextView articleTitle;
        TextView articleSection;
        TextView articleDate;
        TextView articleAuthor;
        ImageView articleThumbnail;
    }
}

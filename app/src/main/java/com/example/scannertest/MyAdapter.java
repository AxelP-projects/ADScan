package com.example.scannertest;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private List<String> mDataset;

    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView textView;
        public ImageView imageView;
        private String path;
        public MyViewHolder(View v) {
            super(v);
            v.setOnClickListener(this);
            textView = v.findViewById(R.id.tvNomFichier);
            imageView = v.findViewById(R.id.ivIconFichier);
        }

        @Override
        public void onClick(View view) {
            path = textView.getContentDescription().toString();
            //CHeck if file has default file extension, to see if it's a folder or not
            if(path.substring(path.length() - AppSettings.APP_DEFAULT_PHOTO_EXTENSION.length()).equals(AppSettings.APP_DEFAULT_PHOTO_EXTENSION)){
                Intent intent = new Intent(view.getContext(), pdfView.class);
                Log.v("Axel", path);
                intent.putExtra("path", textView.getContentDescription().toString());
                view.getContext().startActivity(intent);
            }else{
                Intent intent = new Intent(view.getContext(),ScansBrowserActivity.class);
                intent.putExtra("path", textView.getContentDescription().toString());
                view.getContext().startActivity(intent);
            }

        }
    }

    public MyAdapter(List<String> myDataset) {
        mDataset = myDataset;
    }

    @Override
    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_scans_browser_list_item, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String fileName = mDataset.get(position);
        Uri uri = Uri.parse(fileName);
        holder.textView.setText(uri.getLastPathSegment());
        holder.textView.setContentDescription(mDataset.get(position));
        if(fileName.length() > AppSettings.APP_DEFAULT_PHOTO_EXTENSION.length() && fileName.substring(fileName.length() - AppSettings.APP_DEFAULT_PHOTO_EXTENSION.length()).equals(AppSettings.APP_DEFAULT_PHOTO_EXTENSION)){
            holder.imageView.setImageResource(R.drawable.pdf_default);
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}

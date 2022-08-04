package com.example.instagramclone.view.adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.instagramclone.databinding.RecyclerRowBinding;
import com.example.instagramclone.view.model.Post;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostHolder> {

    ArrayList<Post> postArrayList;

    public PostAdapter(ArrayList<Post> postArrayList){
        this.postArrayList = postArrayList;
    }

    @NonNull
    @Override
    public PostHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        RecyclerRowBinding recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new PostHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull PostHolder holder, int position) {

        holder.binding.recyclerViewUserEmailText.setText(postArrayList.get(position).email);
        holder.binding.recyclerViewCommentText.setText(postArrayList.get(position).comment);
        Picasso.get().load(postArrayList.get(position).downloadUrl).into(holder.binding.recyclerViewImageView);

    }

    @Override
    public int getItemCount() {
        return postArrayList.size();
    }

    public static class PostHolder extends RecyclerView.ViewHolder{

        RecyclerRowBinding binding;

        public PostHolder(RecyclerRowBinding recyclerRowBinding) {

            super(recyclerRowBinding.getRoot());
            this.binding = recyclerRowBinding;
        }
    }


}

package com.example.instagramclone.view.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.instagramclone.R;
import com.example.instagramclone.databinding.ActivityFeedBinding;
import com.example.instagramclone.view.adapter.PostAdapter;
import com.example.instagramclone.view.model.Post;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Map;

public class FeedActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseFirestore firebaseFirestore;
    ArrayList<Post> postArrayList;
    ActivityFeedBinding binding;
    PostAdapter postAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFeedBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
        postArrayList = new ArrayList<>();
        getData();

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postAdapter = new PostAdapter(postArrayList);
        binding.recyclerView.setAdapter(postAdapter);
    }

    public void getData(){


        firebaseFirestore.collection("Posts").orderBy("date", Query.Direction.DESCENDING).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null){
                    Toast.makeText(FeedActivity.this,error.getMessage(),Toast.LENGTH_SHORT).show();
                }
                if (value != null){

                    for (DocumentSnapshot snapshot:value.getDocuments()){

                        Map<String,Object> data = snapshot.getData();
                        if (data != null){
                            String userEmail = (String) data.get("useremail");
                            String comment = (String) data.get("comment");
                            String downloadUrl = (String) data.get("downloadurl");

                            Post post = new Post(userEmail,comment,downloadUrl);
                            postArrayList.add(post);

                        }else {
                            Toast.makeText(FeedActivity.this,"Getting data failed!",Toast.LENGTH_SHORT).show();
                        }




                    }
                    postAdapter.notifyDataSetChanged();

                }
            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.options_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.add_post){
            Intent intent = new Intent(FeedActivity.this,UploadActivity.class);
            startActivity(intent);

        }else if (item.getItemId() == R.id.signout){

            auth.signOut();
            Intent intent = new Intent(FeedActivity.this,MainActivity.class);
            startActivity(intent);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


}
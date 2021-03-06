package com.company.lahacks.lahacks2018;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

import static java.lang.Math.toIntExact;

import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.GrayscaleTransformation;
import jp.wasabeef.glide.transformations.gpu.SepiaFilterTransformation;
import jp.wasabeef.glide.transformations.gpu.SwirlFilterTransformation;

import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class GalleryActivity extends AppCompatActivity {

    private String[] mUrls;
    private boolean isHost;
    private String newURL = "https://upload.wikimedia.org/wikipedia/en/c/c7/Bonobos_Lana_%26_Kesi_2006_CALVIN_IMG_1301.JPG";
    private int[] mValues = new int[6];
    private String lobbyName;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myRef = database.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Bundle extras = getIntent().getExtras();
        mUrls = extras.getStringArray("mUrls");
        isHost = extras.getBoolean("isHost");
        lobbyName = extras.getString("lobbyName");

        for (int i = 0; i < 6; i++) {
            mValues[i] = 0;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_images);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(layoutManager);

        GalleryActivity.ImageGalleryAdapter adapter = new GalleryActivity.ImageGalleryAdapter(this);
        recyclerView.setAdapter(adapter);

    }

    private class ImageGalleryAdapter extends RecyclerView.Adapter<ImageGalleryAdapter.MyViewHolder>  {

        @Override
        public ImageGalleryAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            // Inflate the layout
            View photoView = inflater.inflate(R.layout.item_photo, parent, false);

            ImageGalleryAdapter.MyViewHolder viewHolder = new ImageGalleryAdapter.MyViewHolder(photoView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ImageGalleryAdapter.MyViewHolder holder, int position) {

            MyPhoto mPhoto = mPhotos[position];
            ImageView imageView = holder.mPhotoImageView;

            Glide.with(mContext)
                    .load(mPhoto.getUrl())
//                    .placeholder(R.drawable.ic_cloud_off_red)
                    .into(imageView);
        }

        @Override
        public int getItemCount() {
            return (mPhotos.length);
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            public ImageView mPhotoImageView;

            public MyViewHolder(View itemView) {

                super(itemView);
                mPhotoImageView = (ImageView) itemView.findViewById(R.id.iv_photo);
                itemView.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View view) {
                        int position = getAdapterPosition();
                        String url;
                        url = mUrls[position];
                        //FIX LAG LATER
                        if(position != RecyclerView.NO_POSITION) {
                            if(mValues[position] > 0) {
                                Glide.with(mContext)
                                        .load(url)
//                                      .placeholder(R.drawable.ic_cloud_off_red)
                                        .into(mPhotoImageView);
                                mValues[position] = 0;
                            } else {
                                Glide.with(mContext)
                                        .load(url)
//                                      .placeholder(R.drawable.ic_cloud_off_red)
                                        .apply(bitmapTransform(new BlurTransformation()))
                                        .into(mPhotoImageView);
                                mValues[position] = 1;
                            }
                        }
                    }
                });
                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    public boolean onLongClick(View view) {
                        int position = getAdapterPosition();
                        String url;
                        url = mUrls[position];
                        if(position != RecyclerView.NO_POSITION) {
                            if(mValues[position] > 0) {
                                Glide.with(mContext)
                                        .load(url)
//                                      .placeholder(R.drawable.ic_cloud_off_red)
                                        .into(mPhotoImageView);
                                mValues[position] = 0;
                            } else {
                                Glide.with(mContext)
                                        .load(url)
//                                      .placeholder(R.drawable.ic_cloud_off_red)
                                        .apply(bitmapTransform(new SepiaFilterTransformation()))
                                        .into(mPhotoImageView);
                                mValues[position] = 2;
                            }
                        }
                        return true;
                    }
                });
            }
        }

        private MyPhoto[] mPhotos = new MyPhoto[6];
        private Context mContext;

        public ImageGalleryAdapter(Context context) {
            mContext = context;

            for (int i = 0; i < 6; i++) {
                mPhotos[i] = new MyPhoto(mUrls[i]);
            }
        }
    }

    public void reportPhotos(View view) {
        Toast.makeText(GalleryActivity.this, "Thanks for reporting! We will review the images, and if they are inappropriate images, you will be rewarded with coins!",
                Toast.LENGTH_LONG).show();
    }

    public void submitPhotos(View view) {
        //do some submitting magic here with mValues

        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(int i = 0; i < 6; i++){
                    String j = "Photo " + i + " score";
                    int newVal = ((Long) dataSnapshot.child("parties").child(lobbyName).child(j).getValue()).intValue();
                    newVal += mValues[i];
                    myRef.child("parties").child(lobbyName).child(j).setValue(newVal);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if(isHost) {
            Intent intent = new Intent(this, HostActivity.class);
            intent.putExtra("lobbyName", lobbyName);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, ClientActivity.class);
            intent.putExtra("lobbyName", lobbyName);
            startActivity(intent);
        }
    }
}

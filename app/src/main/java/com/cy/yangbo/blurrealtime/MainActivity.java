package com.cy.yangbo.blurrealtime;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.cy.yangbo.blur_realtime_library.GPUImageBlurView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private RecyclerView mContentRV;
    private GPUImageBlurView mBarBV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mContentRV = (RecyclerView) findViewById(R.id.rv_content);
        mBarBV = (GPUImageBlurView) findViewById(R.id.bv_bar);
        mBarBV.setBackgroundView(mContentRV);

        mContentRV.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mContentRV.setAdapter(new ContentAdapter());
    }

    public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ViewHolder>{

        private int[] drawableArr = {R.drawable.image_1, R.drawable.image_2, R.drawable.image_3,
                R.drawable.image_1, R.drawable.image_2, R.drawable.image_3};

        private int totalItem = 30;

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(MainActivity.this).inflate(R.layout.item_content, null));
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            int index = position % drawableArr.length;
            holder.mContentIV.setImageResource(drawableArr[index]);
        }

        @Override
        public int getItemCount() {
            return totalItem;
        }

        public class ViewHolder extends RecyclerView.ViewHolder{

            ImageView mContentIV;

            public ViewHolder(View itemView) {
                super(itemView);

                mContentIV = (ImageView) itemView.findViewById(R.id.iv_content);
            }
        }
    }
}

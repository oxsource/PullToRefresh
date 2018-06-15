package com.pizzk.refresh;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;

import pizzk.android.ptr.constant.RefreshOwner;
import pizzk.android.ptr.api.RefreshListener;
import pizzk.android.ptr.view.RefreshLayout;

public class NormalListActivity extends AppCompatActivity implements RefreshListener {
    private MovieListAdapter adapter;
    private RefreshLayout layout;
    private RefreshOwner owner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("今日热映");
        setContentView(R.layout.activity_one_level);
        RecyclerView recyclerView = findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //防止数据更新完成后闪屏
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);

        adapter = new MovieListAdapter();
        recyclerView.setAdapter(adapter);
        //刷新布局
        layout = findViewById(R.id.layout);
        layout.startRefresh(RefreshOwner.HEADER);
        layout.getKernel().setListener(this::onRefresh);
    }

    @Override
    public void onRefresh(RefreshOwner owner) {
        this.owner = owner;
        layout.postDelayed(runnable, 1500);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            layout.stopRefresh(true);
            boolean flag = false;
            if (RefreshOwner.HEADER == owner) {
                adapter.setItemCount(0);
                flag = true;
                layout.getFooter().setLess(false);
            }
            int startPos = adapter.getItemCount();
            int APPEND_COUNT = 10;
            adapter.setItemCount(startPos + APPEND_COUNT);
            layout.getFooter().setLess(adapter.getItemCount() >= 3 * APPEND_COUNT);
            if (flag) {
                adapter.notifyDataSetChanged();
            } else {
                adapter.notifyItemRangeInserted(startPos, APPEND_COUNT);
            }
        }
    };

    @Override
    protected void onDestroy() {
        layout.removeCallbacks(runnable);
        super.onDestroy();
    }
}

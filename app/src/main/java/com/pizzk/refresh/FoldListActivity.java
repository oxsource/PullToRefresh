package com.pizzk.refresh;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;

import pizzk.android.ptr.api.RefreshControl;
import pizzk.android.ptr.api.RefreshListener;
import pizzk.android.ptr.constant.RefreshOwner;
import pizzk.android.ptr.view.RefreshLayout;

public class FoldListActivity extends AppCompatActivity implements RefreshListener {
    private MovieListAdapter adapter;
    private RefreshLayout layout;
    private RefreshOwner owner;
    private AppBarLayout appBarLayout;
    private RefreshControl control;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Pizzk PullToRefresh");
        setContentView(R.layout.activity_two_level);
        appBarLayout = findViewById(R.id.appBarLayout);
        RecyclerView recyclerView = findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        //防止数据更新完成后闪屏
        ((SimpleItemAnimator) recyclerView.getItemAnimator()).setSupportsChangeAnimations(false);
        adapter = new MovieListAdapter();
        recyclerView.setAdapter(adapter);
        //刷新布局
        layout = findViewById(R.id.layout);
        layout.setTwoLevel(recyclerView);
        control = layout.getKernel().getControl();
        control.startRefresh(RefreshOwner.HEADER);
        control.setListener(this::onRefresh);
        layout.getFooter().getView().setEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        appBarLayout.addOnOffsetChangedListener(offsetListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        appBarLayout.removeOnOffsetChangedListener(offsetListener);
    }

    private AppBarLayout.OnOffsetChangedListener offsetListener = (appBarLayout, verticalOffset) -> {
        layout.setEnabled(verticalOffset == 0);
    };

    @Override
    public void onRefresh(RefreshOwner owner) {
        this.owner = owner;
        layout.postDelayed(runnable, 1500);
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            control.stopRefresh(true);
            boolean flag = false;
            if (RefreshOwner.HEADER == owner) {
                adapter.setItemCount(0);
                flag = true;
                control.setFootLess(false);
            }
            int startPos = adapter.getItemCount();
            int APPEND_COUNT = 10;
            adapter.setItemCount(startPos + APPEND_COUNT);
            control.setFootLess(adapter.getItemCount() >= 3 * APPEND_COUNT);
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

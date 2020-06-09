package com.baato.baatoandroiddemo.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.baato.baatoandroiddemo.models.HomeMenu;
import com.example.baatoandroiddemo.R;
import com.baato.baatoandroiddemo.adapters.HomeMenuAdapter;
import com.baato.baatoandroiddemo.helpers.ItemOffsetDecoration;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.app_name));

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerView.addItemDecoration(new ItemOffsetDecoration(this, R.dimen.normal_margin));
        recyclerView.setAdapter(new HomeMenuAdapter(getHomeListItems(), this));
    }

    private List<HomeMenu> getHomeListItems() {
        List<HomeMenu> list = new ArrayList<>();
        list.add(new HomeMenu("Search", HomeMenu.SERVICE_TYPE, R.drawable.ic_search));
        list.add(new HomeMenu("Reverse", HomeMenu.SERVICE_TYPE, R.drawable.ic_choose_on_map));
        list.add(new HomeMenu("Navigation", HomeMenu.SERVICE_TYPE, R.drawable.ic_route));
        list.add(new HomeMenu("Mock Navigation", HomeMenu.SERVICE_TYPE, R.drawable.ic_route));
//        list.add(new HomeMenu("Map Styles", HomeMenu.SERVICE_TYPE, R.drawable.ic_map_black_24dp));
        list.add(new HomeMenu("Map Styles", HomeMenu.TEXT_TYPE, R.drawable.ic_search));
        list.add(new HomeMenu("", HomeMenu.TEXT_TYPE, R.drawable.ic_search));

        list.add(new HomeMenu("Breeze Map", HomeMenu.MAP_TYPE, R.drawable.breeze));
        list.add(new HomeMenu("Monochrome Map", HomeMenu.MAP_TYPE, R.drawable.monochrome));
        list.add(new HomeMenu("Retro Map", HomeMenu.MAP_TYPE, R.drawable.retro));
        return list;
    }
}

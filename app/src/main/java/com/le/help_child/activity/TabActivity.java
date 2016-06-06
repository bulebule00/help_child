package com.le.help_child.activity;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import com.le.help_child.R;
import com.le.help_child.adapter.FragmentTabAdapter;

import java.util.ArrayList;
import java.util.List;

public class TabActivity extends AppCompatActivity {
    private final List<Fragment> fragments = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

        Intent intent=getIntent();
        int get_para = intent.getIntExtra("para", 0);
        fragments.add(new CameraActivity());
        fragments.add(new RecordActivity());
        fragments.add(new PlaceActivity());
        fragments.add(new HelpActivity());
        RadioGroup rgs = (RadioGroup) findViewById(R.id.tabs_rg);
        RadioButton camera_Rb = (RadioButton) findViewById(R.id.bottom_camera);
        RadioButton record_Rb = (RadioButton) findViewById(R.id.bottom_record);
        RadioButton place_Rb = (RadioButton) findViewById(R.id.bottom_place);
        RadioButton help_Rb = (RadioButton) findViewById(R.id.bottom_help);
        switch (get_para){
            case 0:
                camera_Rb.setChecked(true);
                break;
            case 1:
                record_Rb.setChecked(true);
                break;
            case 2:
                place_Rb.setChecked(true);
                break;
            case 3:
                assert help_Rb != null;
                help_Rb.setChecked(true);
                break;
        }
        FragmentTabAdapter tabAdapter = new FragmentTabAdapter(this, fragments, rgs, get_para);
        tabAdapter.setOnRgsExtraCheckedChangedListener(new FragmentTabAdapter.OnRgsExtraCheckedChangedListener()
        {
            @Override
            public void OnRgsExtraCheckedChanged() {
                //System.out.println("Extra---- " + index + " checked!!! ");
            }
        });
    }


}


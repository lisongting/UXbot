/*
 * Copyright 2017 lisongting
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.iscas.xlab.uxbot.mvp.robot_state;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

import cn.iscas.xlab.uxbot.Config;
import cn.iscas.xlab.uxbot.R;
import cn.iscas.xlab.uxbot.customview.CustomSeekBar;
import cn.iscas.xlab.uxbot.customview.PercentCircleView;


/**
 * Created by lisongting on 2017/11/14.
 */

public class RobotStateFragment extends Fragment implements RobotStateContract.View {
    private static final String TAG = "RobotStateFragment";

    private PercentCircleView batteryView;
    private CustomSeekBar yawDegreeSeekBar;
    private CustomSeekBar pitchDegreeSeekBar;
    private RobotStateContract.Presenter presenter;
    private Switch switcher;
    private Button btReset;
    private Button btThreeDimension;

    public RobotStateFragment() {

    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_robot_state, container, false);
        batteryView = view.findViewById(R.id.battery_view);
        yawDegreeSeekBar =  view.findViewById(R.id.seekbar_yaw_cloud_degree);
        pitchDegreeSeekBar =  view.findViewById(R.id.seekbar_pitch_cloud_degree);
        switcher =  view.findViewById(R.id.switcher);
        btReset = view.findViewById(R.id.bt_reset);
        btThreeDimension = view.findViewById(R.id.bt_three_dimension);

        btThreeDimension.setVisibility(View.GONE);
        initListeners();

        return view;
    }

    @Override
    public void initView() {
        if (Config.isRosServerConnected) {
            batteryView.startAnimation();
        } else {
            batteryView.stopAnimation();
        }
        pitchDegreeSeekBar.setValue(1);
    }

    private void initListeners() {
        switcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (presenter!=null && Config.isRosServerConnected) {
                    if (!switcher.isChecked()) {
                        presenter.publishElectricMachineryMsg(true);
                    } else {
                        presenter.publishElectricMachineryMsg(false);
                    }
                } else {
                    Toast.makeText(getActivity(), "Ros服务器未连接", Toast.LENGTH_SHORT).show();
                }
            }
        });

        yawDegreeSeekBar.setOnSeekChangeListener(new CustomSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(int value) {
            }

            @Override
            public void onProgressChangeCompleted(int value) {
                log("yawDegreeSeekBar value change complete :" + value);
                if (presenter != null && Config.isRosServerConnected ) {
                    presenter.publishYawPlatFormMsg(value);
                }else {
                    Toast.makeText(getActivity(), "Ros服务器未连接", Toast.LENGTH_SHORT).show();
                }
            }
        });

        pitchDegreeSeekBar.setOnSeekChangeListener(new CustomSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(int value) {
            }

            @Override
            public void onProgressChangeCompleted(int value) {
                log("pitchDegreeSeekBar value change complete:" + value);
                if (presenter != null && Config.isRosServerConnected ) {
                    presenter.publishPitchPlatFormMsg(value);
                }else {
                    Toast.makeText(getContext(), "Ros服务器未连接", Toast.LENGTH_SHORT).show();
                }

            }
        });

        btReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (presenter != null) {
                    presenter.reset();
                }
                switcher.setChecked(true);
                pitchDegreeSeekBar.setValue(1);
                yawDegreeSeekBar.setValue(0);
            }
        });
        btThreeDimension.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), RobotActivity.class));
            }
        });

    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        log("onCreate()");
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onResume() {
        log("onResume()");
        super.onResume();
        initView();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        log("isHidden:" + hidden);
        super.onHiddenChanged(hidden);
        //如果没被隐藏
        if (!hidden) {
            initView();
        }
    }

    //当Ros服务器连接状态变化时，从外部通知该Fragment
    public void notifyRosConnectionStateChange(boolean isConnected) {
        if (isConnected) {
            batteryView.startAnimation();
        } else {
            batteryView.stopAnimation();
        }
    }



    @Override
    public void setPresenter(RobotStateContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void updateYawPlatForm(final int degree) {
        if (yawDegreeSeekBar.isIndicatorDragged()) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                yawDegreeSeekBar.setValue(degree);

            }
        });
    }

    @Override
    public void updatePitchPlatForm(final int degree) {
        if (pitchDegreeSeekBar.isIndicatorDragged()) {
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                pitchDegreeSeekBar.setValue(degree);

            }
        });
    }

    @Override
    public void updateBattery(int percent) {
        batteryView.setPercent(percent);
    }

    @Override
    public void onStop() {
        super.onStop();
        batteryView.stopAnimation();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.unSubscribeRobotState();
            presenter.destroy();
        }

    }

    private void log(String s){
        Log.i(TAG,TAG+" -- "+ s);
    }
}

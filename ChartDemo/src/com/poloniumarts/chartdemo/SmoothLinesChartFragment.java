package com.poloniumarts.chartdemo;

import com.polonium.linechart.Line;
import com.polonium.linechart.LineChartView;
import com.polonium.linechart.LinePoint;
import com.poloniumarts.chartdemo.DefaultChartFragment.OnFragmentInteractionListener;

import android.app.Activity;
import android.graphics.DashPathEffect;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SmoothLinesChartFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private LineChartView chart;
    private String title;

    public static SmoothLinesChartFragment newInstance(String title) {
        SmoothLinesChartFragment fragment = new SmoothLinesChartFragment();
        fragment.title = title;
        return fragment;
    }

    public SmoothLinesChartFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().getActionBar().setTitle(title);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ((TextView) view.findViewById(R.id.description)).setText(R.string.description_smooth);

        chart = (LineChartView) view.findViewById(R.id.chart);
        chart.setViewPort(0, 0, 100, 160);
        chart.setGridSize(10, 0, 20, 0);
        chart.addLine(generateLine(0, 360, 5, 10, 50).setFilled(true).smoothLine(4));
        chart.addLine(generateLine(0, 360, 5, 60, 90).setColor(0xffff8800).smoothLine(1));
        chart.addLine(generateLine(0, 360, 5, 100, 150).setColor(0xff669900).smoothLine(8));
        setHasOptionsMenu(true);
        return view;
    }

    private Line generateLine(int startX, int endX, int step, int minY, int maxY) {
        Line line = new Line(getActivity());
        for (int i = startX; i <= endX; i += step) {
            line.addPoint(new LinePoint(getActivity(), i, (int) (Math.random() * (maxY - minY) + minY)));
        }
        return line;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}

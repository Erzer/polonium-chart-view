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

public class MultilineChartFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private LineChartView chart;
    private String title;

    public static MultilineChartFragment newInstance(String title) {
        MultilineChartFragment fragment = new MultilineChartFragment();
        fragment.title = title;
        return fragment;
    }

    public MultilineChartFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().getActionBar().setTitle(title);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        chart = (LineChartView) view.findViewById(R.id.chart);
        ((TextView) view.findViewById(R.id.description)).setText(R.string.description_multiline);

        chart.setViewPort(0, 0, 100, 150);
        chart.setGridSize(10, 0, 20, 0);
        // default line
        chart.addLine(generateLine(-100, 360, 10, 50, 90));
        // fat orange line with under fill
        chart.addLine(generateLine(-100, 360, 20, 20, 50).setColor(0xffff8800)
                                                         .setFilled(true)
                                                         .setFilledColor(0x44ff8800)
                                                         .setStrokeWidth(4));
        // dashed line
        chart.addLine(generateLine(-100, 360, 5, 90, 140).setColor(0xff669900)
                                                         .setStrokeWidth(0)
                                                         .setPathEffect(new DashPathEffect(new float[] { 3, 3 }, 0)));
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

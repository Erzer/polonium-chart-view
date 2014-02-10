package com.poloniumarts.chartdemo;

import com.polonium.linechart.Line;
import com.polonium.linechart.LineChartView;
import com.polonium.linechart.LineChartView.OnChartPointClickListener;
import com.polonium.linechart.LinePoint;
import com.polonium.linechart.LinePoint.Type;
import com.poloniumarts.chartdemo.DefaultChartFragment.OnFragmentInteractionListener;

import android.app.Activity;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MarkedPointsChartFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private LineChartView chart;
    private String title;

    public static MarkedPointsChartFragment newInstance(String title) {
        MarkedPointsChartFragment fragment = new MarkedPointsChartFragment();
        fragment.title = title;
        return fragment;
    }

    public MarkedPointsChartFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().getActionBar().setTitle(title);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        ((TextView) view.findViewById(R.id.description)).setText(R.string.description_marked_points);

        chart = (LineChartView) view.findViewById(R.id.chart);
        chart.setViewPort(0, 0, 100, 160);
        chart.setGridSize(10, 0, 20, 0);
        // default line
        Line line = generateLine(-100, 360, 10, 50, 90);
        for (LinePoint point : line.getPoints()) {
            point.setVisible(true);
            point.setType(Type.SQUARE);
        }
        chart.addLine(line);
        // fat orange line with under fill
        Line line2 = generateLine(-100, 360, 20, 20, 50).setColor(0xffff8800)
                                                        .setFilled(true)
                                                        .setFilledColor(0x44ff8800)
                                                        .setStrokeWidth(4);
        for (LinePoint point : line2.getPoints()) {
            point.setVisible(true);
            point.setType(Type.CIRCLE);
            point.getStrokePaint().setColor(0xffff8800);
        }
        chart.addLine(line2);
        //
        Line line3 = generateLine(-100, 360, 5, 100, 150).setColor(0xff009966);
        for (LinePoint point : line3.getPoints()) {
            point.setVisible(true);
            point.setType(Type.TRIANGLE);
            point.getStrokePaint().setColor(0xff009966);
        }
        chart.addLine(line3);
        
        chart.setOnPointClickListener(new OnChartPointClickListener() {

            @Override
            public void onPointClick(LinePoint point, Line line) {
                for (LinePoint p: line.getPoints()){
                    p.setRadius(5);
                    p.setTextVisible(false);
                }
                point.setRadius(10);
                point.setTextVisible(true);
                point.setText(String.valueOf(point.getY()));
                point.getTextPaint().setColor(line.getPaint().getColor());
            }
        });
        
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

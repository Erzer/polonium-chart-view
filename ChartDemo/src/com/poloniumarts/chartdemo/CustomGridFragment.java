package com.poloniumarts.chartdemo;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import com.polonium.linechart.Line;
import com.polonium.linechart.LineChartView;
import com.polonium.linechart.LinePoint;
import com.poloniumarts.chartdemo.DefaultChartFragment.OnFragmentInteractionListener;

import android.app.Activity;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class CustomGridFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private LineChartView chart;
    private String title;

    public static CustomGridFragment newInstance(String title) {
        CustomGridFragment fragment = new CustomGridFragment();
        fragment.title = title;
        return fragment;
    }

    public CustomGridFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().getActionBar().setTitle(title);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        chart = (LineChartView) view.findViewById(R.id.chart);
        ((TextView) view.findViewById(R.id.description)).setText(R.string.description_custom);

        chart.addLine(generateLine(0, 12 * 50, 10, 10, 90));

        chart.setViewPort(0, 0, 100, 100);
        chart.setGridSize(10, 3, 20, 3);
        chart.setBackgroundColor(0xff000000);

        Paint main = new Paint();
        main.setColor(0x88fdd074);
        main.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                                                      1,
                                                      getResources().getDisplayMetrics()));
        Paint sub = new Paint();
        sub.setColor(0x44ffbb33);
        sub.setStrokeWidth(0);
        chart.setHorizontalGridStyle(main, sub);
        chart.setVerticalGridStyle(main, sub);
        chart.setVerValuesMarginsDP(8, 30, 8, 8);
        chart.setHorValuesMarginsDP(0, 8, 8, 0);
        chart.setViewPortMarginsDP(30, 30, 8, 8);

        Paint horValPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        horValPaint.setColor(0xffffffff);
        horValPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14, getResources().getDisplayMetrics()));
        Paint verValPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        verValPaint.setColor(0xffffffff);
        verValPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10, getResources().getDisplayMetrics()));
        chart.setMainValuesStyle(horValPaint, verValPaint);

        SparseArray<String> horValues = new SparseArray<String>();
        Calendar c = new GregorianCalendar();
        for (int i = 0; i < 12; i++) {
            c.set(Calendar.MONTH, i);
            horValues.put(i * 50, c.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.US));
        }

        chart.setHorValuesText(horValues);
        
        SparseArray<String> verValues = new SparseArray<String>();
        for (int i = 10; i <= 150 ; i+=10) {
            verValues.put(i, i+"K");
        }
        chart.setVerValuesText(verValues);
        
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

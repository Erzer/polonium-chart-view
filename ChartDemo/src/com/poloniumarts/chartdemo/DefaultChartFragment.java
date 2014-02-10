package com.poloniumarts.chartdemo;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.polonium.linechart.Line;
import com.polonium.linechart.LineChartView;
import com.polonium.linechart.LinePoint;

public class DefaultChartFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private LineChartView chart;
   
    public static DefaultChartFragment newInstance() {
        DefaultChartFragment fragment = new DefaultChartFragment();
        return fragment;
    }

    public DefaultChartFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().getActionBar().setTitle("Default");
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        chart = (LineChartView) view.findViewById(R.id.chart);
        ((TextView) view.findViewById(R.id.description)).setText(R.string.description_default);
        Line line = new Line(getActivity());
        for (int i = 0; i < 300; i += 10) {
            line.addPoint(new LinePoint(getActivity(), i, (int) (Math.random() * 50 + 20)));
        }
        chart.addLine(line);
        setHasOptionsMenu(true);
        return view;
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

    /**
     * This interface must be implemented by activities that contain this fragment to allow an interaction in this
     * fragment to be communicated to the activity and potentially other fragments contained in that activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html" >Communicating with Other
     * Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        public void openDrawer();

        public void closeDrawer();
    }

}

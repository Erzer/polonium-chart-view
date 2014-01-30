package com.polonium.linechart;

/**
 * Interface holds constants for text align. Used by {@link com.polonium.linechart.LineChartView LineChartView} onDraw.
 */
public interface TextAlign {
    public final static int LEFT = 0x00000001;
    public final static int RIGHT = 0x00000002;
    public final static int TOP = 0x00000004;
    public final static int BOTTOM = 0x00000008;
    public final static int HORIZONTAL_CENTER = 0x00000010;
    public final static int VERTICAL_CENTER = 0x00000020;
}

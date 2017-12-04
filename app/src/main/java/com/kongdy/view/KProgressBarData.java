package com.kongdy.view;

/**
 * @author kongdy
 * @date 2017/12/4 14:24
 * @describe TODO
 **/
public class KProgressBarData {

    // progress color
    private int color;
    // progress real value,not percent,
    private float value;

    private float percentValue;

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    /**
     * this value will init after {@link PercentProgressBar} setDataArray
     * @return percentValue
     */
    public float getPercentValue() {
        return percentValue;
    }

    protected void setPercentValue(float percentValue) {
        this.percentValue = percentValue;
    }
}

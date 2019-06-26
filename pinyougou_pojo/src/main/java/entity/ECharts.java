package entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author ：Jin
 * @date ：Created in 2019/6/25 16:19:54
 */
public class ECharts implements Serializable {
    private List<String> day;
    private List<Double> salesCount;

    @Override
    public String toString() {
        return "ECharts{" +
                "day=" + day +
                ", salesCount=" + salesCount +
                '}';
    }

    public List<String> getDay() {
        return day;
    }

    public void setDay(List<String> day) {
        this.day = day;
    }

    public List<Double> getSalesCount() {
        return salesCount;
    }

    public void setSalesCount(List<Double> salesCount) {
        this.salesCount = salesCount;
    }
}

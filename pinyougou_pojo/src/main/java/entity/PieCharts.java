package entity;

import java.io.Serializable;

/**
 * @author ：Jin
 * @date ：Created in 2019/6/26 18:04:16
 */
public class PieCharts implements Serializable {
    private String value;
    private String name;

    @Override
    public String toString() {
        return "PieCharts{" +
                "value='" + value + '\'' +
                ", name='" + name + '\'' +
                '}';
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

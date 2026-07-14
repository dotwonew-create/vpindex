package me.vihnya.vpindex.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class Parameter {

    private String data;

    private boolean enabled;

    private double points;

    private long time;

    private double minimum;

    private long reset;

    private double demotion;

    private long maxtime;

    public String getData() {
        return data == null ? "null" : data;
    }
}

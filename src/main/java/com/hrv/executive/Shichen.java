package com.hrv.executive;

/**
 * 中国子午流注 12 时辰（每个时辰 2 小时）。
 */
public enum Shichen {
    ZI("子时", "23:00-01:00"),
    CHOU("丑时", "01:00-03:00"),
    YIN("寅时", "03:00-05:00"),
    MAO("卯时", "05:00-07:00"),
    CHEN("辰时", "07:00-09:00"),
    SI("巳时", "09:00-11:00"),
    WU("午时", "11:00-13:00"),
    WEI("未时", "13:00-15:00"),
    SHEN("申时", "15:00-17:00"),
    YOU("酉时", "17:00-19:00"),
    XU("戌时", "19:00-21:00"),
    HAI("亥时", "21:00-23:00");

    private final String cnName;
    private final String range;

    Shichen(String cnName, String range) {
        this.cnName = cnName;
        this.range = range;
    }

    public String cnName() {
        return cnName;
    }

    public String range() {
        return range;
    }
}

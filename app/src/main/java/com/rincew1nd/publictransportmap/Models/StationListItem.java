package com.rincew1nd.publictransportmap.Models;

public class StationListItem {
    public static final int ITEM = 0;
    public static final int SECTION = 1;

    public final int type;
    public final String text;
    public final int id;

    public int sectionPosition;
    public int listPosition;

    public StationListItem(int type, String text, int id) {
        this.type = type;
        this.text = text;
        this.id = id;
    }

    @Override public String toString() {
        return text;
    }
}

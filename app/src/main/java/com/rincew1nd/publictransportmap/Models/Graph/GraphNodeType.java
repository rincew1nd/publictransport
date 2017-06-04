package com.rincew1nd.publictransportmap.Models.Graph;

public enum GraphNodeType {
    Unscheduled("Без расписания"),
    Scheduled("С расписанием"),
    Walking("Пешие маршруты"),
    None("None");

    final String text;
    GraphNodeType(final String text) {
        this.text = text;
    }

    /* (non-Javadoc)
     * @see java.lang.Enum#toString()
     */
    @Override
    public String toString() {
        return text;
    }
}

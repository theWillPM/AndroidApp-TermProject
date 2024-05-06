package com.example.wwce;

/**
 * The ItemRow object class
 */
public class ItemRow {
    private String name;
    private String value1;
    private int value2;
    private String name2;
    private boolean swiped; // Flag to indicate whether the item has been swiped

    public ItemRow(String name, String value1, int value2, String name2) {
        this.name = name;
        this.value1 = value1;
        this.value2 = value2;
        this.name2 = name2;
        this.swiped = false;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue1() {
        return value1;
    }

    public void setValue1(String value1) {
        this.value1 = value1;
    }

    public int getValue2() {
        return value2;
    }

    public void setValue2(int value2) {
        this.value2 = value2;
    }

    public String getName2() {
        return name2;
    }

    public void setName2(String name2) {
        this.name2 = name2;
    }

    public boolean isSwiped() {
        return swiped;
    }

    public void setSwiped(boolean swiped) {
        this.swiped = swiped;
    }

}

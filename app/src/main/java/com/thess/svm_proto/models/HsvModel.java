package com.thess.svm_proto.models;

public class HsvModel {
    private int id;
    private float h;
    private float s;
    private float v;
    private int label;

    public float getH() {
        return h;
    }

    public void setH(float h) {
        this.h = h;
    }

    public float getS() {
        return s;
    }

    public void setS(float s) {
        this.s = s;
    }

    public float getV() {
        return v;
    }

    public void setV(float v) {
        this.v = v;
    }

    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
    }

    public HsvModel(int id, float h, float s, float v, int label) {
        this.id = id;
        this.h = h;
        this.s = s;
        this.v = v;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}

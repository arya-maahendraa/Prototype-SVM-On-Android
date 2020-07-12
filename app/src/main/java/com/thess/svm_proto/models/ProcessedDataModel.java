package com.thess.svm_proto.models;

import org.jscience.mathematics.vector.Float64Matrix;

import java.util.ArrayList;

public class ProcessedDataModel {
    private ArrayList<Float64Matrix>X;
    private ArrayList<Double> Y;

    public ArrayList<Float64Matrix> getX() {
        return X;
    }

    public ArrayList<Double> getY() {
        return Y;
    }

    public ProcessedDataModel(ArrayList<Float64Matrix> x, ArrayList<Double> y) {
        X = x;
        Y = y;
    }
}

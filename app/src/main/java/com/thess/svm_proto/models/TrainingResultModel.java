package com.thess.svm_proto.models;

import java.util.List;

public class TrainingResultModel {
    private List<Double> alpha;

    public List<Double> getAlpha() {
        return alpha;
    }

    public double getB() {
        return b;
    }

    private double b;
    private double sigma;

    public TrainingResultModel(List<Double> alpha, double b, double sigma) {
        this.alpha = alpha;
        this.b = b;
        this.sigma = sigma;
    }

    public double getSigma() {
        return sigma;
    }
}

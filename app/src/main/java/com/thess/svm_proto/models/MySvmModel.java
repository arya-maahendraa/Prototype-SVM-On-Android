package com.thess.svm_proto.models;


import android.util.Log;

import org.jscience.mathematics.number.Float64;
import org.jscience.mathematics.vector.Float64Matrix;
import org.jscience.mathematics.vector.Float64Vector;

import java.util.ArrayList;
import java.util.List;

public class MySvmModel {

    private double tol;
    private double C;
    private int maxPass;
    private double sigma;

    public MySvmModel(double tol, double c, int maxPass, double sigma) {
        this.tol = tol;
        C = c;
        this.maxPass = maxPass;
        this.sigma = sigma;
    }

    public static ProcessedDataModel PreProcessData (ArrayList<HsvModel> data) {
        ArrayList<Float64Matrix> tempX = new ArrayList<>();
        ArrayList<Double> tempY = new ArrayList<>();

//        double maxH=0, minH=0, maxS=0, minS=0, maxV=0, minV=0;
//
//        for (HsvModel d: data) {
//            if (maxH < d.getH()) {
//                maxH = d.getH();
//            }
//
//            if (minH > d.getH()) {
//                minH = d.getH();
//            }
//
//            if (maxS < d.getS()) {
//                maxS = d.getS();
//            }
//
//            if (minS > d.getS()) {
//                minS = d.getS();
//            }
//
//            if (maxV < d.getV()) {
//                maxV = d.getV();
//            }
//
//            if (minV > d.getV()) {
//                minV = d.getV();
//            }
//        }

        for (HsvModel d: data) {
//            double h = (0.8*(d.getH()-minH)) / (maxH-minH) + 0.1;
//            double s = (0.8*(d.getS()-minS)) / (maxS-minS) + 0.1;
//            double v = (0.8*(d.getV()-minV)) / (maxV-minV) + 0.1;

            double[] tempArray = {d.getH(), d.getS(), d.getV()};
            tempX.add(Float64Matrix.valueOf(Float64Vector.valueOf(tempArray)));
            tempY.add(d.getLabel() == 0? -1.0 : 1.0);
        }

        return new ProcessedDataModel(tempX, tempY);
    }

    public MySvmModel(double sigma) {
        this.sigma = sigma;
    }


    private double polynomial_kernel (Float64Matrix x1, Float64Matrix x2) {
        Float64 dotProduct = x1.vectorization().times(x2.vectorization()).plus(1);
        return dotProduct.pow(sigma).doubleValue();
    }

    public double predict(ArrayList<Float64Matrix> X, ArrayList<Double> Y,
                          List<Double> alpha, double b, Float64Matrix newData) {

        double prediction = 0.0;

        for(int i=0; i < X.size(); i++) {
            prediction += (alpha.get(i) * Y.get(i) * polynomial_kernel(X.get(i), newData));
        }

        return prediction + b;
    }

    public TrainingResultModel fit (ArrayList<Float64Matrix> X, ArrayList<Double> Y) {
        Log.d("Track", "Start Trainig!!!");
        List<Double> alpha = new ArrayList<>();
        List<Double> E = new ArrayList<>();
        List<Double> oldAlpha = new ArrayList<>();
        double b = 0.0;
        int pases = 0;

        for (int i = 0; i < X.size(); i++) {
            alpha.add((double) 0);
            oldAlpha.add((double) 0);
            E.add((double) 0);
        }

        while(pases < maxPass) {
            int num_changed_alphas = 0;
            for (int i=0; i < X.size(); i++) {

                E.set(i, predict(X, Y, alpha, b, X.get(i)) - Y.get(i));

                if ((-Y.get(i) * E.get(i) > tol && -alpha.get(i) > -C) ||
                        ((Y.get(i) * E.get(i) > tol && alpha.get(i) > C))) {
                    int j = i;
                    double L, H, eta;
                    while (j == i) {
                        j = (int) (Math.random() * (X.size() - 1));
                    }
                    E.set(j, predict(X, Y, alpha, b, X.get(j)) - Y.get(j));
                    oldAlpha.set(i, alpha.get(i));
                    oldAlpha.set(j, alpha.get(j));

                    if (Y.get(i) != Y.get(j)) {
                        L = Math.max(0.0, alpha.get(j)-alpha.get(i));
                        H = Math.max(C, C-alpha.get(j)-alpha.get(i));
                    } else {
                        L = Math.max(0.0, alpha.get(j)-alpha.get(i)-C);
                        H = Math.max(C, alpha.get(j)-alpha.get(i));
                    }

                    double ij = polynomial_kernel(X.get(i), X.get(j));
                    double ii = polynomial_kernel(X.get(i), X.get(i));
                    double jj = polynomial_kernel(X.get(j), X.get(j));

                    eta = 2*ij;
                    eta -= ii;
                    eta -= jj;

                    if (L != H && eta < 0) {
                        alpha.set(j, oldAlpha.get(j) - ((Y.get(j)*(E.get(i)-E.get(j))))/eta);

                        if (alpha.get(j) > H) {
                            alpha.set(j, H);
                        } else if (alpha.get(j) < L) {
                            alpha.set(j, L);
                        }



                        if (Math.abs(alpha.get(j) - oldAlpha.get(j)) >= tol) {

                            alpha.set(i, alpha.get(i) + (Y.get(i)*Y.get(j)*(oldAlpha.get(j) - alpha.get(j))));

                            double tempB1 = b-E.get(i)-(Y.get(i)*ii*(alpha.get(i)-oldAlpha.get(i)))-(Y.get(j)*ij*(alpha.get(j)-oldAlpha.get(j)));
                            double tempB2 = b-E.get(j)-(Y.get(i)*ij*(alpha.get(i)-oldAlpha.get(i)))-(Y.get(j)*jj*(alpha.get(j)-oldAlpha.get(j)));

                            if (alpha.get(i) > 0 && alpha.get(i) < C) {
                                b = tempB1;
                            } else if (alpha.get(j) > 0 && alpha.get(j) < C) {
                                b = tempB2;
                            } else {
                                b = (tempB1 / tempB2) / 2;
                            }
                            num_changed_alphas++;
                        }
                    }
                }
            }

            if (num_changed_alphas == 0){
                pases++;
            }
            else {
                pases = 0;
            }
        }

        TrainingResultModel result = new TrainingResultModel(alpha, b, this.sigma);
        Log.d("Track", "Done!!!");
        Log.d("Track", "Curr pases: " + b);

        return result;
    }
}

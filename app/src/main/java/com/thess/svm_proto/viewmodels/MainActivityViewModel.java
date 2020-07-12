package com.thess.svm_proto.viewmodels;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.thess.svm_proto.models.HsvModel;
import com.thess.svm_proto.models.TrainingResultModel;
import com.thess.svm_proto.repositories.SvmProtoDbHelper;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

public class MainActivityViewModel extends AndroidViewModel {
    SvmProtoDbHelper svmProtoDbHelper;

    public MainActivityViewModel(@NonNull Application application) {
        super(application);
        svmProtoDbHelper = new SvmProtoDbHelper(application.getApplicationContext());

    }

    public ArrayList<HsvModel> GetAlldata() {
        return svmProtoDbHelper.GetAllData();
    }

    public TrainingResultModel GetModel() {return svmProtoDbHelper.GetModel();}

    public boolean AddAlphaAndBias(TrainingResultModel data) {
        return svmProtoDbHelper.AddAlphaAndBias(data);
    }

    public HsvModel ProsesPredictData(String path) {
        Bitmap bitmap = getBitmap(path);
        float[] avgHsv = getAvgHsv(bitmap);
        return new HsvModel(0, avgHsv[0], avgHsv[1], avgHsv[2], -1);
    }

    public ArrayList<ArrayList<String>> prosesData(String path){
        ArrayList<ArrayList<String>> imagePaths = new ArrayList<ArrayList<String>>();
        if (dir_exists(path + "/0") && dir_exists(path + "/1")){
            imagePaths.add(GetImages(path + "/0"));
            imagePaths.add(GetImages(path + "/1"));

            ArrayList<HsvModel> data = new ArrayList<>();
            for (int i = 0; i < imagePaths.size(); i++){
                for (String imagePath: imagePaths.get(i)) {
                    Bitmap bitmap = getBitmap(imagePath);
                    float[] avgHsv = getAvgHsv(bitmap);
                    HsvModel hsvModel = new HsvModel(0, avgHsv[0], avgHsv[1], avgHsv[2], i);
                    data.add(hsvModel);
                }
            }

            svmProtoDbHelper.AddRawData(data);
        }
        return imagePaths;
    }

    private float[] getAvgHsv(Bitmap image) {
        float[] avgHsv = new float[3];
        avgHsv[0] = 0;
        avgHsv[1] = 0;
        avgHsv[2] = 0;
        for (int i = 0; i < image.getWidth(); i++){
            for (int j = 0; j < image.getHeight(); j++){
                float[] hsv = new float[3];
                int colour = image.getPixel(i, j);
                Color.colorToHSV(colour, hsv);
                avgHsv[0] += hsv[0];
                avgHsv[1] += (hsv[1]*255);
                avgHsv[2] += (hsv[2]*255);
            }
        }
        int countPixel = image.getHeight() * image.getWidth();
        avgHsv[0] /= countPixel;
        avgHsv[1] /= countPixel;
        avgHsv[2] /= countPixel;

        return avgHsv;
    }

    private ArrayList<String> GetImages(String path) {

        ArrayList<String> filePath = new ArrayList<String>();

        File directory = new File(path);
        File[] files = directory.listFiles();

        assert files != null;
        for (File file : files) {
            String fileName = path + "/" + file.getName();
            String fileType = fileName.substring(fileName.length() - 4);
            if (fileType.equals(".png") || fileType.equals(".jpg")) {
                filePath.add(fileName);
            }
        }
        return filePath;
    }

    private boolean dir_exists(String dir_path)
    {
        File dir = new File(dir_path);
        return dir.exists() && dir.isDirectory();
    }

    private Bitmap getBitmap(String path) {
        Bitmap image=null;
        try {
            File f= new File(path);
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            image = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resize(image, 256, 256) ;
    }

    private Bitmap resize(Bitmap image, int maxWidth, int maxHeight) {
        if (maxHeight > 0 && maxWidth > 0) {
            int width = image.getWidth();
            int height = image.getHeight();
            float ratioBitmap = (float) width / (float) height;
            float ratioMax = (float) maxWidth / (float) maxHeight;

            int finalWidth = maxWidth;
            int finalHeight = maxHeight;
            if (ratioMax > 1) {
                finalWidth = (int) ((float)maxHeight * ratioBitmap);
            } else {
                finalHeight = (int) ((float)maxWidth / ratioBitmap);
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);
            return image;
        } else {
            return image;
        }
    }
}

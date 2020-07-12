package com.thess.svm_proto.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codekidlabs.storagechooser.StorageChooser;
import com.thess.svm_proto.R;
import com.thess.svm_proto.models.HsvModel;
import com.thess.svm_proto.models.MySvmModel;
import com.thess.svm_proto.models.ProcessedDataModel;
import com.thess.svm_proto.models.TrainingResultModel;
import com.thess.svm_proto.viewmodels.MainActivityViewModel;

import org.jscience.mathematics.vector.Float64Matrix;
import org.jscience.mathematics.vector.Float64Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView tvJmlClass0, tvJmlClass1;
    private EditText edTol, edC, edMaxPass, edSigma;
    private MainActivityViewModel vmMainActivity;
    private Button btnAddData, btnTrain, btnPredict;
    private RelativeLayout progressBar;
    public static final int FOLDERPICKER_PERMISSIONS = 1;
    private ListView lvShowData;
    private Handler prosesImageHendler = new Handler();
    private double sigma, tol, C, maxPass;
    ArrayList<ArrayList<String>> dataPath;
    ProcessedDataModel data;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvJmlClass0 = findViewById(R.id.tv_class0);
        tvJmlClass1 = findViewById(R.id.tv_class1);
        progressBar = findViewById(R.id.progress_bar);
        btnTrain = findViewById(R.id.btn_train);
        btnAddData = findViewById(R.id.btn_add_data);
        btnPredict = findViewById(R.id.btn_predict);
        lvShowData = findViewById(R.id.lv_show_data);

        edTol = findViewById(R.id.ed_tol);
        edC = findViewById(R.id.ed_C);
        edMaxPass = findViewById(R.id.ed_max_pass);
        edSigma = findViewById(R.id.ed_sigma);

        vmMainActivity = ViewModelProviders.of(MainActivity.this).get(MainActivityViewModel.class);

        btnAddData.setOnClickListener(this);
        btnTrain.setOnClickListener(this);
        btnPredict.setOnClickListener(this);

        ShowLvData();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_add_data:
                String[] PERMISSIONS = {
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                };

                if(hasPermissions(MainActivity.this, PERMISSIONS)){
                    ShowDirectoryPicker();

                }else{
                    ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, FOLDERPICKER_PERMISSIONS);
                }
                break;

            case R.id.btn_train:
                progressBar.setVisibility(View.VISIBLE);
                ToogleButton(true);

                tol = Double.parseDouble(edTol.getText().toString());
                C = Double.parseDouble(edC.getText().toString());
                maxPass = Double.parseDouble(edMaxPass.getText().toString());
                sigma = Double.parseDouble(edSigma.getText().toString());

                data = MySvmModel.PreProcessData(vmMainActivity.GetAlldata());
                ProsesTrainingData train = new ProsesTrainingData(data.getX(), data.getY());
                new Thread(train).start();
                break;

            case R.id.btn_predict:
                ShowFilePicker();
                break;
        }
    }

    public void ShowLvData() {
        ArrayList<HsvModel> allHsvModel = vmMainActivity.GetAlldata();
        List<String> stringHSv = new ArrayList<>();
        for (HsvModel hsvModel : allHsvModel) {
            stringHSv.add("h: " + hsvModel.getH() +
                    ", s: " + hsvModel.getS() + ", v: " + hsvModel.getV() +
                    ", label: " + hsvModel.getLabel());
        }
        ArrayAdapter lvShowDataAdapter = new ArrayAdapter<>(
                MainActivity.this, android.R.layout.simple_list_item_1, stringHSv);
        lvShowData.setAdapter(lvShowDataAdapter);
    }

    public void ShowFilePicker(){
        // 1. Initialize dialog
        final StorageChooser chooser = new StorageChooser.Builder()
                .withActivity(MainActivity.this)
                .withFragmentManager(getFragmentManager())
                .withMemoryBar(true)
                .allowCustomPath(true)
                .setType(StorageChooser.FILE_PICKER)
                .build();

        // 2. Retrieve the selected path by the user and show in a toast !

        chooser.setOnSelectListener(new StorageChooser.OnSelectListener() {
            @Override
            public void onSelect(String path) {
                progressBar.setVisibility(View.VISIBLE);
                ToogleButton(false);

                ProsesPredictNewData runProsesImage = new ProsesPredictNewData(path);
                new Thread(runProsesImage).start();
            }
        });

        // 3. Display File Picker !
        chooser.show();
    }

    public void ShowDirectoryPicker(){
        // 1. Initialize dialog
        final StorageChooser chooser = new StorageChooser.Builder()
                .withActivity(MainActivity.this)
                .withFragmentManager(getFragmentManager())
                .withMemoryBar(true)
                .allowCustomPath(true)
                .setType(StorageChooser.DIRECTORY_CHOOSER)
                .build();

        // 2. Retrieve the selected path by the user and show in a toast !

        chooser.setOnSelectListener(new StorageChooser.OnSelectListener() {
            @Override
            public void onSelect(String path) {
                progressBar.setVisibility(View.VISIBLE);
                ToogleButton(false);


                ProsesImageRunnable runProsesImage = new ProsesImageRunnable(path);
                new Thread(runProsesImage).start();
            }
        });

        // 3. Display File Picker !
        chooser.show();
    }

    public void ToogleButton(Boolean state) {
        btnAddData.setEnabled(state);
        btnTrain.setEnabled(state);
        btnPredict.setEnabled(state);
    }


    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == FOLDERPICKER_PERMISSIONS) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                        MainActivity.this,
                        "Permission granted :)",
                        Toast.LENGTH_SHORT
                ).show();
            } else {
                Toast.makeText(
                        MainActivity.this,
                        "Permission denied to read your External storage :(",
                        Toast.LENGTH_SHORT
                ).show();
            }
        }
    }

    class ProsesImageRunnable implements Runnable {

        private String path;

        ProsesImageRunnable(String path) {
            this.path = path;
        }

        @Override
        public void run() {
            final long timeStart = System.nanoTime();
            dataPath = vmMainActivity.prosesData(path);
            final long timeStop = System.nanoTime();
            prosesImageHendler.post(new Runnable() {
                @Override
                public void run() {
                    if (dataPath.size() > 0){
                        tvJmlClass0.setText("Jumlah Class 0 : " + dataPath.get(0).size());
                        tvJmlClass1.setText("Jumlah Class 1 : " + dataPath.get(1).size());
                    } else {
                        Toast.makeText(
                                MainActivity.this,
                                "Folder Class 0 dan Class 1 Not Exist",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                    ShowLvData();
                    ToogleButton(true);
                    progressBar.setVisibility(View.GONE);

                    long time = TimeUnit.SECONDS.convert(timeStop - timeStart, TimeUnit.NANOSECONDS);

                    Toast.makeText(
                            MainActivity.this,
                            "Waktu Pemrosesan: "+time+"s",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        }
    }

    class ProsesPredictNewData implements Runnable {

        private String path;

        ProsesPredictNewData(String path) {
            this.path = path;
        }

        @Override
        public void run() {
            data = MySvmModel.PreProcessData(vmMainActivity.GetAlldata());
            HsvModel newData = vmMainActivity.ProsesPredictData(path);
            TrainingResultModel model = vmMainActivity.GetModel();

            double[] tempArray = {newData.getH(), newData.getS(), newData.getV()};
            Float64Matrix newX = Float64Matrix.valueOf(Float64Vector.valueOf(tempArray));

            MySvmModel svm = new MySvmModel(model.getSigma());
            final double predict = svm.predict(data.getX(), data.getY(), model.getAlpha(), model.getB(), newX);
            prosesImageHendler.post(new Runnable() {
                @Override
                public void run() {
                    ToogleButton(true);
                    progressBar.setVisibility(View.GONE);

                    String h = predict > 1? "Positif" : "Negatif";

                    Toast.makeText(
                            MainActivity.this,
                            "Hasil Prediksi: "+h+" Formalin",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });
        }
    }


    class ProsesTrainingData implements Runnable {

        private ArrayList<Float64Matrix> X;
        private ArrayList<Double> Y;

        public ProsesTrainingData(ArrayList<Float64Matrix> x, ArrayList<Double> y) {
            X = x;
            Y = y;
        }

        @Override
        public void run() {
            Process.setThreadPriority(Thread.MAX_PRIORITY);
            final long timeStart = System.nanoTime();
            List<Double> result = new ArrayList<>();

            MySvmModel svm = new MySvmModel(tol, C, (int)maxPass, sigma);
            final TrainingResultModel model = svm.fit(X, Y);

            for (int i=0; i < X.size(); i++) {
                double predict = svm.predict(X, Y, model.getAlpha(), model.getB(), X.get(i));
                result.add(predict > 0? 1.0 : -1.0);
            }

            double count = 0;

            for (int i=0; i < Y.size(); i++) {
                if (Y.get(i).equals(result.get(i))) {
                    count++;
                }
            }

            final long timeStop = System.nanoTime();
            final double finalCount = count;
            prosesImageHendler.post(new Runnable() {
                @Override
                public void run() {
                    boolean success = vmMainActivity.AddAlphaAndBias(model);
                    if (!success) {
                        Toast.makeText(
                                MainActivity.this,
                                "Error menyimpan Model",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                    ToogleButton(true);
                    progressBar.setVisibility(View.GONE);
                    double akurasi = (finalCount *100) / (double) X.size();
                    long time = TimeUnit.SECONDS.convert(timeStop - timeStart, TimeUnit.NANOSECONDS);
                    tvJmlClass0.setText("Waktu Training: "+time+"s");
                    tvJmlClass1.setText("Akurasi: "+akurasi+"%");
                }
            });
        }
    }
}

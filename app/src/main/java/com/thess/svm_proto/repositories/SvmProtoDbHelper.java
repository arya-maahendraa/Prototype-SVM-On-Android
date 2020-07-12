package com.thess.svm_proto.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import com.thess.svm_proto.models.HsvModel;
import com.thess.svm_proto.models.TrainingResultModel;

import java.util.ArrayList;
import java.util.List;

import static com.thess.svm_proto.repositories.SvmProtoContract.RawData;

public class SvmProtoDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "SvmProto.db";

    public SvmProtoDbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.beginTransaction();
        try {

            String createTableRawData = "CREATE TABLE "+ RawData.TABLE_NAME +" (" +
                    RawData.COLUMN_ID+" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    RawData.COLUMN_H+" REAL NOT NULL, " +
                    RawData.COLUMN_S+" REAL NOT NULL, " +
                    RawData.COLUMN_V+" REAL NOT NULL, " +
                    RawData.COLUMN_LABEL+" BLOB)";

            db.execSQL(createTableRawData);

            String createTableAlpha = "CREATE TABLE "+ SvmProtoContract.Alpha.TABLE_NAME +" (" +
                    SvmProtoContract.Alpha.COLUMN_ID+" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    SvmProtoContract.Alpha.COLUM_VALUE+" REAL NOT NULL)";

            db.execSQL(createTableAlpha);

            String createTableBias = "CREATE TABLE "+ SvmProtoContract.Bias.TABLE_NAME +" (" +
                    SvmProtoContract.Bias.COLUMN_ID+" INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                    SvmProtoContract.Bias.COLUM_VALUE+" REAL NOT NULL, "+
                    SvmProtoContract.Bias.COLUM_SIGMA+" REAL NOT NULL)";

                    db.execSQL(createTableBias);

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String dropRawData = "DROP TABLE IF EXISTS " + RawData.TABLE_NAME;

        db.execSQL(dropRawData);
    }

    public ArrayList<HsvModel> GetAllData() {
        ArrayList<HsvModel> data = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM "+RawData.TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do{
                HsvModel hsvModel = new HsvModel(
                        cursor.getInt(0), cursor.getFloat(1), cursor.getFloat(2),
                        cursor.getFloat(3), cursor.getInt(4));
                data.add(hsvModel);
            }while(cursor.moveToNext());
        }

        cursor.close();

        return data;
    }

    public TrainingResultModel GetModel() {
        List<Double> alpha = new ArrayList<>();
        double sigma = 0, b = 0;
        SQLiteDatabase db = this.getReadableDatabase();

        db.beginTransaction();
        try {
            String query = "SELECT * FROM "+ SvmProtoContract.Alpha.TABLE_NAME;
            Cursor cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do{
                    alpha.add(cursor.getDouble(1));
                }while(cursor.moveToNext());
            }

            query = "SELECT * FROM "+ SvmProtoContract.Bias.TABLE_NAME;
            cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                b = cursor.getDouble(1);
                sigma = cursor.getDouble(2);
            }

            cursor.close();
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        return new TrainingResultModel(alpha, b, sigma);
    }

    public boolean AddAlphaAndBias(TrainingResultModel data) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            truncate(SvmProtoContract.Alpha.TABLE_NAME);
            truncate(SvmProtoContract.Bias.TABLE_NAME);

            for(double d : data.getAlpha()) {
                ContentValues values = new ContentValues();
                values.put(SvmProtoContract.Alpha.COLUM_VALUE, d);

                long row = db.insert(SvmProtoContract.Alpha.TABLE_NAME, null, values);
                if (row == -1) {
                    db.endTransaction();
                    return false;
                }
            }

            ContentValues values = new ContentValues();
            values.put(SvmProtoContract.Bias.COLUM_VALUE, data.getB());
            values.put(SvmProtoContract.Bias.COLUM_SIGMA, data.getSigma());

            long row = db.insert(SvmProtoContract.Bias.TABLE_NAME, null, values);
            if (row == -1) {
                db.endTransaction();
                return false;
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return true;
    }

    public boolean AddRawData(ArrayList<HsvModel> data) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            truncate(RawData.TABLE_NAME);
            for(HsvModel d : data) {
                ContentValues values = new ContentValues();
                values.put(RawData.COLUMN_H, d.getH());
                values.put(RawData.COLUMN_S, d.getS());
                values.put(RawData.COLUMN_V, d.getV());
                values.put(RawData.COLUMN_LABEL, d.getLabel());

                long row = db.insert(RawData.TABLE_NAME, null, values);
                if (row == -1) {
                    db.endTransaction();
                    return false;
                }
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
        return true;
    }

    private void truncate(String tbName) {
        SQLiteDatabase db = this.getWritableDatabase();
        String truncateDb = "DELETE FROM "+tbName;
        db.execSQL(truncateDb);
    }
}

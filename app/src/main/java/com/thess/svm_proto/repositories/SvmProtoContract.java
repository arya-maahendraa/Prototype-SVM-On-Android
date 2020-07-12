package com.thess.svm_proto.repositories;

import android.provider.BaseColumns;

public final class SvmProtoContract {

    private SvmProtoContract() {}

    public static class RawData implements BaseColumns {
        public static final String TABLE_NAME = "raw_data";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_H = "h";
        public static final String COLUMN_S = "s";
        public static final String COLUMN_V = "v";
        public static final String COLUMN_LABEL = "label";
    }

    public static class Alpha implements BaseColumns {
        public static final String TABLE_NAME = "alpha";
        public static final String COLUMN_ID = "id";
        public static final String COLUM_VALUE = "value";

    }

    public static class Bias implements BaseColumns {
        public static final String TABLE_NAME = "bias";
        public static final String COLUMN_ID = "id";
        public static final String COLUM_VALUE = "value";
        public static final String COLUM_SIGMA = "sigma";

    }
}


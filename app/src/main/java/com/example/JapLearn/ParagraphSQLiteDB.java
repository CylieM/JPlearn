package com.example.JapLearn;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ParagraphSQLiteDB extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "paragraphs.db";
    private static final int DATABASE_VERSION = 18; // Incremented database version

    public static final String TABLE_PARAGRAPHS = "paragraphs";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_PARAGRAPH = "paragraph";
    public static final String COLUMN_ROMAJI = "romaji";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_SENTENCES = "sentences";

    private static final String TABLE_CREATE =
            "CREATE TABLE " + TABLE_PARAGRAPHS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_PARAGRAPH + " TEXT NOT NULL, " +
                    COLUMN_ROMAJI + " TEXT NOT NULL, " +
                    COLUMN_CATEGORY + " TEXT NOT NULL, " +
                    COLUMN_SENTENCES + " INTEGER NOT NULL);";

    public ParagraphSQLiteDB(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
        initializeDatabase(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PARAGRAPHS);
        onCreate(db);
    }

    private void initializeDatabase(SQLiteDatabase db) {
        Object[][] initialParagraphs = {
                {"こ ん し ゅ う  の  セ  ー  ル  は  と う ふ  と  チ ョ こ　レ ー ト  で す", "ko n sh u u no se e ru wa to u fu to cho ko re e to de su", "Mixed", 2}, // 2 sentence, mixed
                {"き ょ う  は  す  ー ぱ  ー  に か い も の  に い き ま し た  ま め の  か ん づ め と  ぱ ん  を  か い ま し た", "k yo u wa su u pa a ni ka i mo no ni i ki ma shi ta ma me no ka n zu me to pa n o ka i ma shi ta", "Hiragana", 2}, //2 sentence, pure hiragana
                {"き ょ う  と  あ し た  は が っ こ う  が  や す み  で す  き ょ う  は  と も だ ち  と  こ う え ん  に  い き ま す", "k yo u to a shi ta wa ga k ko u ga ya su mi de su k yo u wa to mo da chi to ko u e n ni i ki ma su", "Hiragana", 2}, //2 sentence, pure hiragana
                {"こ こ  は  ア メ リ カ で す", "ko ko wa a me ri ka de su", "Mixed", 1}, //1 sentence, mixed
                {"に ほ ん ご が は な せ ま せ ん", "ni ho n go ga ha na se ma se n", "Hiragana", 1} //1 sentence, pure hiragana
        };


        for (Object[] paragraph : initialParagraphs) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_PARAGRAPH, (String) paragraph[0]);
            values.put(COLUMN_ROMAJI, (String) paragraph[1]);
            values.put(COLUMN_CATEGORY, (String) paragraph[2]);
            values.put(COLUMN_SENTENCES, (Integer) paragraph[3]);
            db.insert(TABLE_PARAGRAPHS, null, values);
        }
    }
    public void logAllData() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_PARAGRAPHS, null);
        if (cursor.moveToFirst()) {
            do {
                String paragraph = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PARAGRAPH));
                String romaji = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROMAJI));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
                int sentences = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SENTENCES));
                Log.d("Database", "Paragraph: " + paragraph + ", Romaji: " + romaji +
                        ", Category: " + category + ", Sentences: " + sentences);
            } while (cursor.moveToNext());
        }
        cursor.close();
    }

}


package com.example.JapLearn;


public class LessonItemDataClass {
    private String dataRomaji;
    private String dataDesc;
    private String dataExampleEn;
    private String dataExampleJp;
    private String dataPronun;
    private String japaneseChar;
    private String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getDataRomaji() {
        return dataRomaji;
    }

    public String getDataDesc() {
        return dataDesc;
    }

    public String getDataExampleEn() {
        return dataExampleEn;
    }
    public String getDataExampleJp() {
        return dataExampleJp;
    }
    public String getDataPronun() {
        return dataPronun;
    }

    public String getJapaneseChar() {
        return japaneseChar;
    }

    public LessonItemDataClass(String dataRomaji, String dataDesc, String dataExampleEn, String dataExampleJp, String dataPronun, String japaneseChar) {
        this.dataRomaji = dataRomaji;
        this.dataDesc = dataDesc;
        this.dataExampleEn = dataExampleEn;
        this.dataExampleJp = dataExampleJp;
        this.dataPronun = dataPronun;
        this.japaneseChar = japaneseChar;
    }

    public LessonItemDataClass() {
    }
}

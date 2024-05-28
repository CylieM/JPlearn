package com.example.jlearnn;


public class DataClass {
    private String dataRomaji;
    private String dataDesc;
    private String dataExample;
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

    public String getDataExample() {
        return dataExample;
    }

    public String getJapaneseChar() {
        return japaneseChar;
    }

    public DataClass(String dataRomaji, String dataDesc, String dataExample, String japaneseChar) {
        this.dataRomaji = dataRomaji;
        this.dataDesc = dataDesc;
        this.dataExample = dataExample;
        this.japaneseChar = japaneseChar;
    }

    public DataClass() {
    }
}

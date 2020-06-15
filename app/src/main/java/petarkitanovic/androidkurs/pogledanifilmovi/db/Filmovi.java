package petarkitanovic.androidkurs.pogledanifilmovi.db;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Filmovi.TABLE_NAME_USERS)
public class Filmovi {

    public static final String TABLE_NAME_USERS = "filmovi";

    public static final String FIELD_NAME_ID = "id";
    public static final String FIELD_NAME_NAZIV = "naziv";
    public static final String FIELD_NAME_GODINA = "godina";
    public static final String FIELD_NAME_VREME = "vreme";
    public static final String FIELD_NAME_IMAGE   = "image";
    public static final String FIELD_NAME_IMDBID   = "imdb_id";
    public static final String FIELD_NAME_ZANR   = "zanr";
    public static final String FIELD_NAME_JEZIK   = "jezik";
    public static final String FIELD_NAME_AWARDS   = "awards";
    public static final String FIELD_NAME_PLOT   = "plot";
    public static final String FIELD_NAME_RATING   = "rating";
    public static final String FIELD_NAME_HOUR   = "hour";
    public static final String FIELD_NAME_MIN   = "min";



    @DatabaseField(columnName = FIELD_NAME_ID, generatedId = true)
    private int mId;

    @DatabaseField(columnName = FIELD_NAME_NAZIV)
    private String mNaziv;

    @DatabaseField(columnName = FIELD_NAME_GODINA)
    private String mGodina;

    @DatabaseField(columnName = FIELD_NAME_VREME)
    private String mVreme;

    @DatabaseField(columnName = FIELD_NAME_IMAGE)
    private String mImage;

    @DatabaseField(columnName = FIELD_NAME_IMDBID)
    private String mImdbId;

    @DatabaseField(columnName = FIELD_NAME_ZANR)
    private String mZanr;

    @DatabaseField(columnName = FIELD_NAME_JEZIK)
    private String mJezik;

    @DatabaseField(columnName = FIELD_NAME_AWARDS)
    private String mAwards;

    @DatabaseField(columnName = FIELD_NAME_PLOT)
    private String mPlot;

    @DatabaseField(columnName = FIELD_NAME_RATING)
    private Float mRating;

    @DatabaseField(columnName = FIELD_NAME_HOUR)
    private int mHour;

    @DatabaseField(columnName = FIELD_NAME_MIN)
    private int mMin;

    public Filmovi() {
    }

    public int getmHour() {
        return mHour;
    }

    public void setmHour(int mHour) {
        this.mHour = mHour;
    }

    public int getmMin() {
        return mMin;
    }

    public void setmMin(int mMin) {
        this.mMin = mMin;
    }

    public float getmRating() {
        return mRating;
    }

    public void setmRating(Float mRating) {
        this.mRating = mRating;
    }

    public int getmId() {
        return mId;
    }

    public void setmId(int mId) {
        this.mId = mId;
    }

    public String getmZanr() {
        return mZanr;
    }

    public void setmZanr(String mZanr) {
        this.mZanr = mZanr;
    }

    public String getmJezik() {
        return mJezik;
    }

    public void setmJezik(String mJezik) {
        this.mJezik = mJezik;
    }

    public String getmAwards() {
        return mAwards;
    }

    public void setmAwards(String mAwards) {
        this.mAwards = mAwards;
    }

    public String getmPlot() {
        return mPlot;
    }

    public void setmPlot(String mPlot) {
        this.mPlot = mPlot;
    }

    public String getmNaziv() {
        return mNaziv;
    }

    public void setmNaziv(String mNaziv) {
        this.mNaziv = mNaziv;
    }

    public String getmGodina() {
        return mGodina;
    }

    public void setmGodina(String mGodina) {
        this.mGodina = mGodina;
    }


    public String getmVreme() {
        return mVreme;
    }

    public void setmVreme(String mVreme) {
        this.mVreme = mVreme;
    }

    public String getmImage() {
        return mImage;
    }

    public void setmImage(String mImage) {
        this.mImage = mImage;
    }

    public String getmImdbId() {
        return mImdbId;
    }

    public void setmImdbId(String mImdbId) {
        this.mImdbId = mImdbId;
    }
}


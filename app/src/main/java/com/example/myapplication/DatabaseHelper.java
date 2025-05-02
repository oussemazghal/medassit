package com.example.myapplication;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "medspeak.db";
    private static final int DATABASE_VERSION = 4;
    public static final String MED_COL_IMAGE_PATH = "image_path";

    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";

    public static final String TABLE_PROFILE = "user_profile";
    public static final String PROFILE_COLUMN_ID = "id";
    public static final String PROFILE_COLUMN_USERNAME = "username";
    public static final String PROFILE_COLUMN_AGE = "age";
    public static final String PROFILE_COLUMN_GENDER = "gender";
    public static final String PROFILE_COLUMN_BLOOD_TYPE = "blood_type";
    public static final String PROFILE_COLUMN_WEIGHT = "weight";
    public static final String PROFILE_COLUMN_HEIGHT = "height";
    public static final String PROFILE_COLUMN_ALLERGIES = "allergies";
    public static final String PROFILE_COLUMN_PHONE = "phone";
    public static final String PROFILE_COLUMN_ADDRESS = "address";
    public static final String PROFILE_COLUMN_IMAGE_URI = "image_uri";

    public static final String TABLE_MEDICATIONS = "medications";
    public static final String MED_COL_ID = "id";
    public static final String MED_COL_NAME = "name";
    public static final String MED_COL_TYPE = "type";
    public static final String MED_COL_DOSAGE = "dosage";
    public static final String MED_COL_FREQUENCY = "frequency";
    public static final String MED_COL_TIME = "time";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_EMAIL + " TEXT, " +
                COLUMN_USERNAME + " TEXT UNIQUE, " +
                COLUMN_PASSWORD + " TEXT)";

        String CREATE_PROFILE_TABLE = "CREATE TABLE " + TABLE_PROFILE + " (" +
                PROFILE_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PROFILE_COLUMN_USERNAME + " TEXT UNIQUE, " +
                PROFILE_COLUMN_AGE + " INTEGER, " +
                PROFILE_COLUMN_GENDER + " TEXT, " +
                PROFILE_COLUMN_BLOOD_TYPE + " TEXT, " +
                PROFILE_COLUMN_WEIGHT + " REAL, " +
                PROFILE_COLUMN_HEIGHT + " INTEGER, " +
                PROFILE_COLUMN_ALLERGIES + " TEXT, " +
                PROFILE_COLUMN_PHONE + " TEXT, " +
                PROFILE_COLUMN_ADDRESS + " TEXT, " +
                PROFILE_COLUMN_IMAGE_URI + " TEXT)";

        String CREATE_MEDICATIONS_TABLE = "CREATE TABLE " + TABLE_MEDICATIONS + " (" +
                MED_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MED_COL_NAME + " TEXT, " +
                MED_COL_TYPE + " TEXT, " +
                MED_COL_DOSAGE + " TEXT, " +
                MED_COL_FREQUENCY + " TEXT, " +
                MED_COL_TIME + " TEXT, " +    // time = date de fin
                "start_date TEXT, " +          // 👈 nouvelle colonne !
                MED_COL_IMAGE_PATH + " TEXT)";
        String CREATE_EMERGENCY_TABLE = "CREATE TABLE emergency_contacts (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "phone TEXT, " +
                "image_uri TEXT)";
        String createAppointmentsTable = "CREATE TABLE appointments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +
                "date TEXT, " +
                "location TEXT, " +
                "category TEXT)";
        String createPrescriptionsTable = "CREATE TABLE prescriptions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +
                "source TEXT, " +
                "image_uri TEXT)";
        String createScheduleEventsTable = "CREATE TABLE schedule_events (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "date TEXT, " +
                "event_text TEXT)";
        db.execSQL(createScheduleEventsTable);
        db.execSQL(createPrescriptionsTable);

        db.execSQL(createAppointmentsTable);
        db.execSQL(CREATE_EMERGENCY_TABLE);
        db.execSQL(CREATE_USERS_TABLE);
        db.execSQL(CREATE_PROFILE_TABLE);
        db.execSQL(CREATE_MEDICATIONS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle migrations properly
        if (oldVersion < 2) {
            // Migration from v1 to v2
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_MEDICATIONS);
            db.execSQL("CREATE TABLE " + TABLE_MEDICATIONS + " (" +
                    MED_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    MED_COL_NAME + " TEXT, " +
                    MED_COL_TYPE + " TEXT, " +
                    MED_COL_DOSAGE + " TEXT, " +
                    MED_COL_FREQUENCY + " TEXT, " +
                    MED_COL_TIME + " TEXT, " +
                    MED_COL_IMAGE_PATH + " TEXT)");
        }

        if (oldVersion < 3) {
            // Migration from v2 to v3
            db.execSQL("CREATE TABLE IF NOT EXISTS emergency_contacts (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "name TEXT, " +
                    "phone TEXT, " +
                    "image_uri TEXT)");
        }

        if (oldVersion < 4) {
            // Migration from v3 to v4
            db.execSQL("CREATE TABLE IF NOT EXISTS appointments (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "title TEXT, " +
                    "date TEXT, " +
                    "location TEXT, " +
                    "category TEXT)");
        }
    }

    private void createAllTables(SQLiteDatabase db) {
        // Create all tables fresh
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_EMAIL + " TEXT, " +
                COLUMN_USERNAME + " TEXT UNIQUE, " +
                COLUMN_PASSWORD + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_PROFILE + " (" +
                PROFILE_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PROFILE_COLUMN_USERNAME + " TEXT UNIQUE, " +
                PROFILE_COLUMN_AGE + " INTEGER, " +
                PROFILE_COLUMN_GENDER + " TEXT, " +
                PROFILE_COLUMN_BLOOD_TYPE + " TEXT, " +
                PROFILE_COLUMN_WEIGHT + " REAL, " +
                PROFILE_COLUMN_HEIGHT + " INTEGER, " +
                PROFILE_COLUMN_ALLERGIES + " TEXT, " +
                PROFILE_COLUMN_PHONE + " TEXT, " +
                PROFILE_COLUMN_ADDRESS + " TEXT, " +
                PROFILE_COLUMN_IMAGE_URI + " TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_MEDICATIONS + " (" +
                MED_COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MED_COL_NAME + " TEXT, " +
                MED_COL_TYPE + " TEXT, " +
                MED_COL_DOSAGE + " TEXT, " +
                MED_COL_FREQUENCY + " TEXT, " +
                MED_COL_TIME + " TEXT, " +
                MED_COL_IMAGE_PATH + " TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS emergency_contacts (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "phone TEXT, " +
                "image_uri TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS appointments (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +
                "date TEXT, " +
                "location TEXT, " +
                "category TEXT)");
    }

    public boolean insertUser(String name, String email, String username, String password) {
        if (isUsernameExists(username)) return false;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, values);
        db.close();
        return result != -1;
    }

    public boolean isUsernameExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID}, COLUMN_USERNAME + "=?", new String[]{username}, null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public boolean checkUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_ID}, COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?", new String[]{username, password}, null, null, null);
        boolean isValid = cursor.moveToFirst();
        cursor.close();
        return isValid;
    }

    public boolean saveOrUpdateProfile(String username, int age, String gender, String bloodType,
                                       float weight, int height, String allergies,
                                       String phone, String address, String imageUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(PROFILE_COLUMN_USERNAME, username);
        values.put(PROFILE_COLUMN_AGE, age);
        values.put(PROFILE_COLUMN_GENDER, gender);
        values.put(PROFILE_COLUMN_BLOOD_TYPE, bloodType);
        values.put(PROFILE_COLUMN_WEIGHT, weight);
        values.put(PROFILE_COLUMN_HEIGHT, height);
        values.put(PROFILE_COLUMN_ALLERGIES, allergies);
        values.put(PROFILE_COLUMN_PHONE, phone);
        values.put(PROFILE_COLUMN_ADDRESS, address);
        values.put(PROFILE_COLUMN_IMAGE_URI, imageUri);

        Cursor cursor = db.query(TABLE_PROFILE, new String[]{PROFILE_COLUMN_ID}, PROFILE_COLUMN_USERNAME + "=?", new String[]{username}, null, null, null);
        long result;
        if (cursor.moveToFirst()) {
            result = db.update(TABLE_PROFILE, values, PROFILE_COLUMN_USERNAME + "=?", new String[]{username});
        } else {
            result = db.insert(TABLE_PROFILE, null, values);
        }
        cursor.close();
        db.close();
        return result != -1;
    }

    public Cursor getProfileByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_PROFILE, null, PROFILE_COLUMN_USERNAME + "=?", new String[]{username}, null, null, null);
    }

    // 🔵 Nouvelle méthode
    public boolean insertMedication(String name, String type, String dosage, String frequency, String startDate, String endDate, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MED_COL_NAME, name);
        values.put(MED_COL_TYPE, type);
        values.put(MED_COL_DOSAGE, dosage);
        values.put(MED_COL_FREQUENCY, frequency);
        values.put("start_date", startDate); // 👈 nouvelle donnée
        values.put(MED_COL_TIME, endDate);    // ancien time = date de fin
        values.put(MED_COL_IMAGE_PATH, imagePath);

        long result = db.insert(TABLE_MEDICATIONS, null, values);
        db.close();
        return result != -1;
    }



    public boolean updateMedication(int id, String name, String type, String dosage, String frequency, String time, String imagePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(MED_COL_NAME, name);
        values.put(MED_COL_TYPE, type);
        values.put(MED_COL_DOSAGE, dosage);
        values.put(MED_COL_FREQUENCY, frequency);
        values.put(MED_COL_TIME, time);
        values.put(MED_COL_IMAGE_PATH, imagePath);
        int rows = db.update(TABLE_MEDICATIONS, values, MED_COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rows > 0;
    }

    public boolean deleteMedication(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_MEDICATIONS, MED_COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return rows > 0;
    }

    public Cursor getAllMedications() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_MEDICATIONS, null, null, null, null, null, null);
    }

    public Cursor getMedicationById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query(TABLE_MEDICATIONS, null, MED_COL_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);
    }
    public boolean deleteMedicationById(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_MEDICATIONS, MED_COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return result > 0;
    }
    public boolean insertEmergencyContact(String name, String phone, String imageUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("phone", phone);
        values.put("image_uri", imageUri);
        long result = db.insert("emergency_contacts", null, values);
        db.close();
        return result != -1;
    }

    public Cursor getAllEmergencyContacts() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM emergency_contacts", null);
    }
    public boolean updateEmergencyContact(int id, String name, String phone, String imageUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("phone", phone);
        values.put("image_uri", imageUri);
        int rows = db.update("emergency_contacts", values, "id = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public boolean deleteEmergencyContact(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete("emergency_contacts", "id = ?", new String[]{String.valueOf(id)});
        return rows > 0;
    }

    public boolean insertAppointment(String title, String date, String location, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("date", date);
        values.put("location", location);
        values.put("category", category);
        long result = db.insert("appointments", null, values);
        return result != -1;
    }
    public Cursor getAllAppointments() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM appointments", null);
    }
    public boolean updateAppointment(int id, String title, String date, String location, String category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("date", date);
        values.put("location", location);
        values.put("category", category);
        int result = db.update("appointments", values, "id=?", new String[]{String.valueOf(id)});
        return result > 0;
    }
    public boolean deleteAppointment(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete("appointments", "id=?", new String[]{String.valueOf(id)});
        return result > 0;
    }


    // 🔵 Insérer une prescription
    public boolean insertPrescription(String title, String source, String imageUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("source", source);
        values.put("image_uri", imageUri);
        long result = db.insert("prescriptions", null, values);
        db.close();
        return result != -1;
    }

    // 🔵 Récupérer toutes les prescriptions
    public Cursor getAllPrescriptions() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM prescriptions", null);
    }

    // 🔵 Supprimer une prescription
    public boolean deletePrescription(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete("prescriptions", "id = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows > 0;
    }

    // 🔵 Modifier une prescription (optionnel)
    public boolean updatePrescription(int id, String title, String source, String imageUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("source", source);
        values.put("image_uri", imageUri);
        int rows = db.update("prescriptions", values, "id = ?", new String[]{String.valueOf(id)});
        db.close();
        return rows > 0;
    }
    public boolean insertScheduleEvent(String date, String eventText) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("date", date);
        values.put("event_text", eventText);
        long result = db.insert("schedule_events", null, values);
        db.close();
        return result != -1;
    }

    // 🔵 Récupérer tous les événements d'une date précise
    public Cursor getScheduleEventsByDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query("schedule_events", null, "date=?", new String[]{date}, null, null, null);
    }
    public Cursor getAppointmentsByDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.query("appointments", null, "date=?", new String[]{date}, null, null, null);
    }
    // 🔵 Récupérer tous les médicaments prévus pour une date
    public Cursor getMedicationsByDate(String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        // 🛠 Trouver les médicaments où (start_date <= date <= end_date)
        return db.rawQuery(
                "SELECT * FROM medications WHERE start_date <= ? AND time >= ?",
                new String[]{date, date}
        );
    }




}


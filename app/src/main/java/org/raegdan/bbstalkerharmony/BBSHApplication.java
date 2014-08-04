package org.raegdan.bbstalkerharmony;

import android.app.Application;
import android.content.Context;

public class BBSHApplication extends Application {

    // Global database variable and methods for it
    public BlindbagDB database;
    public Boolean dbLoaded;

    public BBSHApplication() {
        super();

        database = new BlindbagDB();
        dbLoaded = false;
    }

    public Boolean loadDB(Context context) {
        return database.loadDB(context);
    }

    public BlindbagDB getDB(Context context) throws CloneNotSupportedException {
        return database.clone(context);
    }
}

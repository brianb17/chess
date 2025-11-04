package service;

import dataaccess.DataAccess;

public class ClearService {

    private final DataAccess dataAccess;

    public ClearService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void clearApp() {
        dataAccess.clear();
    }
}

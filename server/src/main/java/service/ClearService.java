package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;

public class ClearService {

    private final DataAccess dataAccess;

    public ClearService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void clearApp() throws DataAccessException {
        try {
            dataAccess.clear();
        } catch (Exception e) {
            throw new DataAccessException("Unexpected error while clearing database: " + e.getMessage(), e);
        }
    }
}

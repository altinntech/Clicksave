package com.altinntech.clicksave.interfaces;

import java.sql.SQLException;

public interface Observer {
    void update() throws SQLException;
}

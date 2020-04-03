package com.hypersocket.quartz;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HypersocketPostgresSQLDelegate extends HypersocketStdJDBCDelegate {

    static Logger log = LoggerFactory.getLogger(HypersocketPostgresSQLDelegate.class);

    /**
     * Below code is copied as it is from org.quartz.impl.jdbcjobstore.PostgreSQLDelegate
    **/

    @Override
    protected Object getObjectFromBlob(ResultSet rs, String colName)
            throws ClassNotFoundException, IOException, SQLException {
        InputStream binaryInput = null;
        byte[] bytes = rs.getBytes(colName);

        Object obj = null;

        if(bytes != null && bytes.length != 0) {
            binaryInput = new ByteArrayInputStream(bytes);

            ObjectInputStream in = new ObjectInputStream(binaryInput);
            try {
                obj = in.readObject();
            } finally {
                in.close();
            }

        }

        return obj;
    }

    @Override
    protected Object getJobDataFromBlob(ResultSet rs, String colName)
            throws ClassNotFoundException, IOException, SQLException {
        if (canUseProperties()) {
            InputStream binaryInput = null;
            byte[] bytes = rs.getBytes(colName);
            if(bytes == null || bytes.length == 0) {
                return null;
            }
            binaryInput = new ByteArrayInputStream(bytes);
            return binaryInput;
        }
        return getObjectFromBlob(rs, colName);
    }


}

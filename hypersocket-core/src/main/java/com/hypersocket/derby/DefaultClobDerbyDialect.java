package com.hypersocket.derby;

import java.sql.Types;

import org.hibernate.dialect.DerbyTenSevenDialect;

public class DefaultClobDerbyDialect extends DerbyTenSevenDialect {
 
   public DefaultClobDerbyDialect() {
       registerColumnType(Types.CLOB, "clob");
   }
 
}

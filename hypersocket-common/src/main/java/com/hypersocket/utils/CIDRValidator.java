/**
 * Copyright 2003-2020 JADAPTIVE Limited. All Rights Reserved.
 *
 * For product documentation visit https://www.jadaptive.com/
 *
 * This file is part of Hypersocket JSON Client.
 *
 * Hypersocket JSON Client is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Hypersocket JSON Client is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Hypersocket JSON Client.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.hypersocket.utils;

/**
 * From http://www.mkyong.com/regular-expressions/how-to-validate-ip-address-with-regular-expression/
 */
import java.util.regex.Matcher;
import java.util.regex.Pattern;
 
public class CIDRValidator {
 
    private Pattern pattern;
    private Matcher matcher;
 
    private static CIDRValidator instance = new CIDRValidator();
    
    public static CIDRValidator getInstance() {
    	return instance;
    }
    
    private static final String CIDR_PATTERN = 
		"^([0-9]{1,3}\\.){3}[0-9]{1,3}(\\/([0-9]|[1-2][0-9]|3[0-2]))?$";
 
    
    private CIDRValidator(){
	  pattern = Pattern.compile(CIDR_PATTERN);
    }
 
   /**
    * Validate ip address with regular expression
    * @param ip ip address for validation
    * @return true valid ip address, false invalid ip address
    */
    public boolean validate(final String ip){		  
	  matcher = pattern.matcher(ip);
	  return matcher.matches();	    	    
    }
}

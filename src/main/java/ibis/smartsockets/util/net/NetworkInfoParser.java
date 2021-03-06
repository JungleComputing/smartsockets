/*
 * Copyright 2010 Vrije Universiteit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ibis.smartsockets.util.net;

import ibis.smartsockets.util.InetAddressCache;

import java.util.List;
import java.util.regex.Pattern;

public abstract class NetworkInfoParser {

	// Note that normal MAC addresses are 48 bits (6 bytes), but on infiniband they seem to be 20 bytes. 
    protected final static Pattern macPattern = Pattern
	    .compile("([0-9a-fA-F]{2}[\\-:]){5,19}[0-9a-fA-F]{2}");

    protected final static Pattern ipv4Pattern = Pattern
	    .compile("\\A(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}\\z");

    protected final static Pattern ipv6Pattern = Pattern
	    .compile("\\A(?:[0-9a-fA-F]{1,4}:){7}[0-9a-fA-F]{1,4}\\z");

    protected final static Pattern ipv6PatternHexCompressed = Pattern
	    .compile("\\A((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)\\z");

    protected final static Pattern ipv6Pattern6Hex4Dec = Pattern
	    .compile("\\A((?:[0-9A-Fa-f]{1,4}:){6,6})(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}\\z");

    protected final static Pattern ipv6Pattern6Hex4DecCompressed = Pattern
	    .compile("\\A((?:[0-9A-Fa-f]{1,4}(?::[0-9A-Fa-f]{1,4})*)?)::((?:[0-9A-Fa-f]{1,4}:)*)(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)(\\.(25[0-5]|2[0-4]\\d|[0-1]?\\d?\\d)){3}\\z");

    protected final static boolean isMacAddress(String mac) {
	return macPattern.matcher(mac).matches();
    }

    public static boolean isIPv4Address(String ip) {
	return ipv4Pattern.matcher(ip).matches();
    }

    public static boolean isIPv6Address(String ip) {
	return ipv6Pattern.matcher(ip).matches()
		|| ipv6Pattern6Hex4Dec.matcher(ip).matches()
		|| ipv6Pattern6Hex4DecCompressed.matcher(ip).matches()
		|| ipv6PatternHexCompressed.matcher(ip).matches();
    }

	protected static final String getField(String line, String header) {
		int index = line.indexOf(header);
	
		if (index >= 0) {
			String tmp = line.substring(index + header.length()).trim();
	
			index = tmp.indexOf(' ');
	
			if (index >= 0) {
				tmp = tmp.substring(0, index).trim();
			}

			return tmp;
		} 

		return null;
	}
	
    protected static final String getIPv4Field(String line, String header) {
		int index = line.indexOf(header);

		if (index >= 0) {
			String tmp = line.substring(index + header.length()).trim();

			index = tmp.indexOf(' ');

			if (index >= 0) {
				tmp = tmp.substring(0, index).trim();
			}

			if (isIPv4Address(tmp)) {
				return tmp;
			}
		}

		return null;
    }

    protected static final String getIPv6Field(String line, String header) {
	int index = line.indexOf(header);

	if (index >= 0) {
	    String tmp = line.substring(index + header.length()).trim();

	    index = tmp.indexOf(' ');

	    if (index > 0) {
		tmp = tmp.substring(0, index).trim();
	    }

	    index = tmp.indexOf('/');

	    if (index > 0) {
		String tmp1 = tmp.substring(0, index).trim();

		if (isIPv6Address(tmp1)) {
		    return tmp1;
		}
	    } else {
		if (isIPv6Address(tmp)) {
		    return tmp;
		}
	    }
	}

	return null;
    }

    protected static final byte[] ipStringToBytes(String ip) {
	try {
	    return InetAddressCache.getByName(ip).getAddress();
	} catch (Exception e) {
	    // print ??
	    return null;
	}
    }

    public final String osName;

    protected NetworkInfoParser(String osName) {
	this.osName = osName;
    }

    abstract int numberOfCommands();

    abstract String[] getCommand(int number);

    abstract boolean parse(byte[] output, List<NetworkInfo> info);
}

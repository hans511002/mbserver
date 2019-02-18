/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.sobey.mbserver.util;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;

import javax.security.auth.kerberos.KerberosPrincipal;
import javax.security.auth.kerberos.KerberosTicket;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sun.net.dns.ResolverConfiguration;
import sun.net.util.IPAddressUtil;

import com.google.common.annotations.VisibleForTesting;

public class SecurityUtil {
	public static final Log LOG = LogFactory.getLog(SecurityUtil.class);
	public static final String HOSTNAME_PATTERN = "_HOST";
	public static final String FAILED_TO_GET_UGI_MSG_HEADER = "Failed to obtain user group information:";

	// controls whether buildTokenService will use an ip or host/ip as given
	// by the user
	@VisibleForTesting
	static boolean useIpForTokenService;
	@VisibleForTesting
	static HostResolver hostResolver;

	/**
	 * For use only by tests and initialization
	 */
	@VisibleForTesting
	public static void setTokenServiceUseIp(boolean flag) {
		useIpForTokenService = flag;
		hostResolver = !useIpForTokenService ? new QualifiedHostResolver() : new StandardHostResolver();
	}

	/**
	 * TGS must have the server principal of the form "krbtgt/FOO@FOO".
	 * 
	 * @param principal
	 * @return true or false
	 */
	static boolean isTGSPrincipal(KerberosPrincipal principal) {
		if (principal == null)
			return false;
		if (principal.getName().equals("krbtgt/" + principal.getRealm() + "@" + principal.getRealm())) {
			return true;
		}
		return false;
	}

	/**
	 * Check whether the server principal is the TGS's principal
	 * 
	 * @param ticket
	 *            the original TGT (the ticket that is obtained when a kinit is done)
	 * @return true or false
	 */
	protected static boolean isOriginalTGT(KerberosTicket ticket) {
		return isTGSPrincipal(ticket.getServer());
	}

	/**
	 * Resolves a host subject to the security requirements determined by hadoop.security.token.service.use_ip.
	 * 
	 * @param hostname
	 *            host or ip to resolve
	 * @return a resolved host
	 * @throws UnknownHostException
	 *             if the host doesn't exist
	 */
	public static InetAddress getByName(String hostname) throws UnknownHostException {
		return hostResolver.getByName(hostname);
	}

	interface HostResolver {
		InetAddress getByName(String host) throws UnknownHostException;
	}

	/**
	 * Uses standard java host resolution
	 */
	static class StandardHostResolver implements HostResolver {

		public InetAddress getByName(String host) throws UnknownHostException {
			return InetAddress.getByName(host);
		}
	}

	/**
	 * This an alternate resolver with important properties that the standard java resolver lacks: 1) The hostname is
	 * fully qualified. This avoids security issues if not all hosts in the cluster do not share the same search
	 * domains. It also prevents other hosts from performing unnecessary dns searches. In contrast, InetAddress simply
	 * returns the host as given. 2) The InetAddress is instantiated with an exact host and IP to prevent further
	 * unnecessary lookups. InetAddress may perform an unnecessary reverse lookup for an IP. 3) A call to getHostName()
	 * will always return the qualified hostname, or more importantly, the IP if instantiated with an IP. This avoids
	 * unnecessary dns timeouts if the host is not resolvable. 4) Point 3 also ensures that if the host is re-resolved,
	 * ex. during a connection re-attempt, that a reverse lookup to host and forward lookup to IP is not performed since
	 * the reverse/forward mappings may not always return the same IP. If the client initiated a connection with an IP,
	 * then that IP is all that should ever be contacted.
	 * 
	 * NOTE: this resolver is only used if: hadoop.security.token.service.use_ip=false
	 */
	protected static class QualifiedHostResolver implements HostResolver {
		@SuppressWarnings("unchecked")
		private List<String> searchDomains = ResolverConfiguration.open().searchlist();

		/**
		 * Create an InetAddress with a fully qualified hostname of the given hostname. InetAddress does not qualify an
		 * incomplete hostname that is resolved via the domain search list. {@link InetAddress#getCanonicalHostName()}
		 * will fully qualify the hostname, but it always return the A record whereas the given hostname may be a CNAME.
		 * 
		 * @param host
		 *            a hostname or ip address
		 * @return InetAddress with the fully qualified hostname or ip
		 * @throws UnknownHostException
		 *             if host does not exist
		 */

		public InetAddress getByName(String host) throws UnknownHostException {
			InetAddress addr = null;

			if (IPAddressUtil.isIPv4LiteralAddress(host)) {
				// use ipv4 address as-is
				byte[] ip = IPAddressUtil.textToNumericFormatV4(host);
				addr = InetAddress.getByAddress(host, ip);
			} else if (IPAddressUtil.isIPv6LiteralAddress(host)) {
				// use ipv6 address as-is
				byte[] ip = IPAddressUtil.textToNumericFormatV6(host);
				addr = InetAddress.getByAddress(host, ip);
			} else if (host.endsWith(".")) {
				// a rooted host ends with a dot, ex. "host."
				// rooted hosts never use the search path, so only try an exact lookup
				addr = getByExactName(host);
			} else if (host.contains(".")) {
				// the host contains a dot (domain), ex. "host.domain"
				// try an exact host lookup, then fallback to search list
				addr = getByExactName(host);
				if (addr == null) {
					addr = getByNameWithSearch(host);
				}
			} else {
				// it's a simple host with no dots, ex. "host"
				// try the search list, then fallback to exact host
				InetAddress loopback = InetAddress.getByName(null);
				if (host.equalsIgnoreCase(loopback.getHostName())) {
					addr = InetAddress.getByAddress(host, loopback.getAddress());
				} else {
					addr = getByNameWithSearch(host);
					if (addr == null) {
						addr = getByExactName(host);
					}
				}
			}
			// unresolvable!
			if (addr == null) {
				throw new UnknownHostException(host);
			}
			return addr;
		}

		InetAddress getByExactName(String host) {
			InetAddress addr = null;
			// InetAddress will use the search list unless the host is rooted
			// with a trailing dot. The trailing dot will disable any use of the
			// search path in a lower level resolver. See RFC 1535.
			String fqHost = host;
			if (!fqHost.endsWith("."))
				fqHost += ".";
			try {
				addr = getInetAddressByName(fqHost);
				// can't leave the hostname as rooted or other parts of the system
				// malfunction, ex. kerberos principals are lacking proper host
				// equivalence for rooted/non-rooted hostnames
				addr = InetAddress.getByAddress(host, addr.getAddress());
			} catch (UnknownHostException e) {
				// ignore, caller will throw if necessary
			}
			return addr;
		}

		InetAddress getByNameWithSearch(String host) {
			InetAddress addr = null;
			if (host.endsWith(".")) { // already qualified?
				addr = getByExactName(host);
			} else {
				for (String domain : searchDomains) {
					String dot = !domain.startsWith(".") ? "." : "";
					addr = getByExactName(host + dot + domain);
					if (addr != null)
						break;
				}
			}
			return addr;
		}

		// implemented as a separate method to facilitate unit testing
		InetAddress getInetAddressByName(String host) throws UnknownHostException {
			return InetAddress.getByName(host);
		}

		void setSearchDomains(String... domains) {
			searchDomains = Arrays.asList(domains);
		}
	}

	public static boolean isPrivilegedPort(final int port) {
		return port < 1024;
	}
}

/**
 * Copyright (c) 2012, Ben Fortuna
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  o Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 *  o Neither the name of Ben Fortuna nor the names of any other contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.fortuna.ical4j.connector.dav;

/**
 * Implementations resolve host path elements.
 * 
 * @author fortuna
 * 
 *         Created on: 02/04/2009
 * 
 *         $Id$
 */
public abstract class PathResolver {

    /**
	 * 
	 */
    public static final PathResolver CHANDLER = new ChandlerPathResolver();

    /**
	 * 
	 */
    public static final PathResolver CGP = new CgpPathResolver();

    /**
	 * 
	 */
    public static final PathResolver KMS = new KmsPathResolver();

    /**
	 * 
	 */
    public static final PathResolver ZIMBRA = new ZimbraPathResolver();

    /**
	 * 
	 */
    public static final PathResolver ICAL_SERVER = new ICalServerPathResolver();

    /**
	 * 
	 */
    public static final PathResolver CALENDAR_SERVER = new CalendarServerPathResolver();
    
    public static final PathResolver GCAL = new GCalPathResolver();

    public static final PathResolver SOGO = new SOGoPathResolver();
    
    public static final PathResolver DAVICAL = new DAViCalPathResolver();
    
    public static final PathResolver BEDEWORK = new BedeworkPathResolver();

    public static final PathResolver ORACLE_CS = new OracleCalendarServerPathResolver();
    
    public static final PathResolver GENERIC = new GenericPathResolver();


    /**
     * Resolves the path component for a user's calendar store URL.
     * @param username a username
     * @return the user path for a server implementation
     */
    public abstract String getUserPath(String username);

    /**
     * Resolves the path component for a principal URL.
     * @param username a username
     * @return the principal path for a server implementation
     */
    public abstract String getPrincipalPath(String username);

    /**
     * A {@link PathResolver} implementation for connecting to Chandler Server instances.
     * 
     * @author fortuna
     * 
     *         Created on: 02/04/2009
     * 
     *         $Id$
     */
    private static class ChandlerPathResolver extends PathResolver {

        @Override
        public String getPrincipalPath(String username) {
            return "/dav/users/" + username;
        }

        @Override
        public String getUserPath(String username) {
            return "/dav/" + username + "/";
        }
    }

    /**
     * A {@link PathResolver} implementation for connecting to CommuniGate Pro instances.
     * 
     * @author fortuna
     * 
     *         Created on: 02/04/2009
     * 
     *         $Id$
     */
    private static class CgpPathResolver extends PathResolver {

        @Override
        public String getPrincipalPath(String username) {
            return "/CalDAV/";
        }

        @Override
        public String getUserPath(String arg0) {
            return "/CalDAV/";
        }
    }

    /**
     * A {@link PathResolver} implementation for connecting to Kerio MailServer instances.
     * 
     * @author fortuna
     * 
     *         Created on: 02/04/2009
     * 
     *         $Id$
     */
    private static class KmsPathResolver extends PathResolver {

        @Override
        public String getPrincipalPath(String username) {
            return "/caldav/";
        }

        @Override
        public String getUserPath(String arg0) {
            // TODO Auto-generated method stub
            return null;
        }
    }

    /**
     * A {@link PathResolver} implementation for connecting to Zimbra instances.
     * 
     * @author fortuna
     * 
     *         Created on: 02/04/2009
     * 
     *         $Id$
     */
    private static class ZimbraPathResolver extends PathResolver {

        @Override
        public String getPrincipalPath(String username) {
            return "/principals/users/" + username + "/";
        }

        @Override
        public String getUserPath(String username) {
            return "/dav/" + username + "/";
        }
    }

    /**
     * A {@link PathResolver} implementation for connecting to iCal Server (Mac OS X Server) instances.
     * 
     * @author Pascal Robert
     * 
     *         Created on: 05/04/2009
     */
    private static class ICalServerPathResolver extends PathResolver {

        @Override
        public String getPrincipalPath(String username) {
            return "/principals/users/" + username + "/";
        }

        @Override
        public String getUserPath(String username) {
            return "/dav/" + username + "/";
        }
    }

    /**
     * A {@link PathResolver} implementation for connecting to Calendar Server (open source version of iCal Server)
     * instances.
     * 
     * @author Pascal Robert
     * 
     *         Created on: 05/04/2009
     */
    private static class CalendarServerPathResolver extends PathResolver {

        @Override
        public String getPrincipalPath(String username) {
          return "/dav/" + username + "/";
        }

        @Override
        public String getUserPath(String username) {
            return "/dav/" + username + "/";
        }
    }

    private static class GCalPathResolver extends PathResolver {

        @Override
        public String getPrincipalPath(String username) {
          return "/calendar/dav/" + username + "/user/";
        }

        @Override
        public String getUserPath(String username) {
            return "/calendar/dav/" + username + "/events/";
        }
    }
    
    private static class SOGoPathResolver extends PathResolver {

      @Override
      public String getPrincipalPath(String username) {
        return "/SOGo/dav/" + username + "/";
      }

      @Override
      public String getUserPath(String username) {
        return "/SOGo/dav/" + username + "/";
      }
    }
    
    private static class DAViCalPathResolver extends PathResolver {

      @Override
      public String getPrincipalPath(String username) {
        return "/caldav.php/" + username + "/";
      }

      @Override
      public String getUserPath(String username) {
        return "/caldav.php/" + username + "/";
      }
    }
    
    private static class BedeworkPathResolver extends PathResolver {

      @Override
      public String getPrincipalPath(String username) {
        return "/ucaldav/principals/users/" + username + "/";
      }

      @Override
      public String getUserPath(String username) {
        return "/ucaldav/users/" + username + "/";
      }
    }
    
    private static class OracleCalendarServerPathResolver extends PathResolver {

      @Override
      public String getPrincipalPath(String username) {
        return "/dav/principals/" + username + "/";
      }

      @Override
      public String getUserPath(String username) {
        return "/dav/home/" + username + "/";
      }
    }
    
    public static class GenericPathResolver extends PathResolver {

        private String principalPath;
        private String userPath;
        
        public String principalPath() {
            return principalPath;
        }
        
        public void setPrincipalPath(String principalPath) {
            this.principalPath = principalPath;
        }
        
        @Override
        public String getPrincipalPath(String username) {
          return principalPath + "/" + username + "/";
        }

        public String userPath() {
            return userPath;
        }
        
        public void setUserPath(String userPath) {
            this.userPath = userPath;
        }
        
        @Override
        public String getUserPath(String username) {
            return userPath + "/" + username;
        }
    }
}

package de.domschmidt.koku.controller.customer;

import net.fortuna.ical4j.connector.dav.PathResolver;

public class NextcloudPathResolver extends PathResolver {

    private final String contextPath;

    public NextcloudPathResolver(final String contextPath) {
        this.contextPath = contextPath;
    }
    public NextcloudPathResolver() {
        this.contextPath = "";
    }

    @Override
    public String getPrincipalPath(String username) {
        return contextPath + "/remote.php/dav/addressbooks/users/" + username + "/contacts/";
    }

    @Override
    public String getUserPath(String username) {
        return getPrincipalPath(username);
    }
}

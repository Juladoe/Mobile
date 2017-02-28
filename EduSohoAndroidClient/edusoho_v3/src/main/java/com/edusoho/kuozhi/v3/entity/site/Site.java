package com.edusoho.kuozhi.v3.entity.site;

import java.io.Serializable;

/**
 * Created by Zhang on 2016/11/29.
 */

public class Site implements Serializable {
    private String edition;
    private String level;
    private String siteName;
    private String siteUrl;
    private String licenseDomains;
    private String licenseIps;

    public String getEdition() {
        return edition;
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getSiteName() {
        return siteName;
    }

    public void setSiteName(String siteName) {
        this.siteName = siteName;
    }

    public String getSiteUrl() {
        return siteUrl;
    }

    public void setSiteUrl(String siteUrl) {
        this.siteUrl = siteUrl;
    }

    public String getLicenseDomains() {
        return licenseDomains;
    }

    public void setLicenseDomains(String licenseDomains) {
        this.licenseDomains = licenseDomains;
    }

    public String getLicenseIps() {
        return licenseIps;
    }

    public void setLicenseIps(String licenseIps) {
        this.licenseIps = licenseIps;
    }
}

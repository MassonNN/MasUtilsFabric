package ru.massonnn.masutils.client.features.updater;

public class VersionInfo {
    private final String versionName;
    private final String downloadUrl;
    private final String changelog;
    private final ModVersion modVersion;

    public VersionInfo(String versionName, String downloadUrl, String changelog) {
        this.versionName = versionName;
        this.downloadUrl = downloadUrl;
        this.changelog = changelog;
        this.modVersion = new ModVersion(versionName);
    }

    public String getVersionName() {
        return versionName;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public String getChangelog() {
        return changelog;
    }

    public ModVersion getModVersion() {
        return modVersion;
    }

    @Override
    public String toString() {
        return "VersionInfo{" +
                "name='" + versionName + '\'' +
                ", url='" + downloadUrl + '\'' +
                '}';
    }
}

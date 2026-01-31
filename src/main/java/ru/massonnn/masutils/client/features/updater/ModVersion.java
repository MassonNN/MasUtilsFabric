package ru.massonnn.masutils.client.features.updater;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModVersion implements Comparable<ModVersion> {
    private static final Pattern PATTERN = Pattern.compile("(\\d+\\.\\d+)(?:-(alpha|beta))?\\.?(\\d+)?(?:\\+(.+))?");

    private final double baseVersion; // 1.1
    private final UpdateChannel channel;
    private final int build; // 5
    private String minecraftVersion;

    public ModVersion(String versionStr) {
        Matcher matcher = PATTERN.matcher(versionStr.toLowerCase());
        if (matcher.find()) {
            this.baseVersion = Double.parseDouble(matcher.group(1));
            String channelStr = matcher.group(2);

            if (channelStr == null) this.channel = UpdateChannel.MAIN;
            else if (channelStr.equals("alpha")) this.channel = UpdateChannel.ALPHA;
            else if (channelStr.equals("beta")) this.channel = UpdateChannel.BETA;
            else this.channel = UpdateChannel.MAIN;

            String buildStr = matcher.group(3);
            this.build = (buildStr != null) ? Integer.parseInt(buildStr) : 0;
            this.minecraftVersion = matcher.group(4);
        } else {
            this.baseVersion = 0;
            this.channel = UpdateChannel.MAIN;
            this.build = 0;
        }
    }

    public UpdateChannel getChannel() { return channel; }

    @Override
    public int compareTo(ModVersion other) {
        if (this.baseVersion != other.baseVersion)
            return Double.compare(this.baseVersion, other.baseVersion);

        if (this.channel != other.channel)
            return Integer.compare(this.channel.ordinal(), other.channel.ordinal());

        return Integer.compare(this.build, other.build);
    }
}
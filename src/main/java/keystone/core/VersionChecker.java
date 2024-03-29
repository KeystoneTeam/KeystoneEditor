package keystone.core;

import keystone.api.Keystone;
import keystone.core.utils.IniFile;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Pair;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class VersionChecker
{
    public static void doVersionCheck()
    {
        try
        {
            ModMetadata metadata = FabricLoader.getInstance().getModContainer(KeystoneMod.MODID).get().getMetadata();
            Version keystoneVersion = Version.parse(metadata.getVersion().getFriendlyString().split("-")[0]);
            String minecraftVersion = SharedConstants.getGameVersion().getReleaseTarget();
            String homepage = metadata.getContact().get("homepage").get();

            IniFile updatesFile = IniFile.read(new URL("https://raw.githubusercontent.com/KeystoneTeam/KeystoneEditor/master/updates.ini"));
            IniFile.Section latestSection = updatesFile.getSection("Latest");
            IniFile.Section recommendedSection = updatesFile.getSection("Recommended");
            IniFile.Section changelogSection = updatesFile.getSection("Changelog");

            String latestStr = latestSection == null ? null : latestSection.getEntry(minecraftVersion);
            String recommendedStr = recommendedSection == null ? null : latestSection.getEntry(minecraftVersion);

            Version latest = latestStr != null ? Version.parse(latestStr) : null;
            Version recommended = recommendedStr != null ? Version.parse(recommendedStr) : null;

            Version target = recommended != null ? recommended : latest;

            if (target != null && keystoneVersion.compareTo(target) < 0)
            {
                List<MutableText> lines = new ArrayList<>();

                // Out of date message
                lines.add(Text.translatable("keystone.version_check.outdated").styled(style -> style.withColor(Formatting.GOLD)));
                lines.add(Text.translatable("keystone.version_check.currentVersion",
                                Text.literal(keystoneVersion.getFriendlyString()).styled(style -> style.withColor(Formatting.AQUA)),
                                Text.literal(target.getFriendlyString()).styled(style -> style.withColor(Formatting.AQUA))).styled(style -> style.withColor(Formatting.GOLD)));

                // Changelog
                if (changelogSection != null)
                {
                    List<Pair<Version, String>> changelog = new ArrayList<>();
                    changelogSection.forEachEntry((key, value) ->
                    {
                        try
                        {
                            Version version = Version.parse(key);
                            if (keystoneVersion.compareTo(version) < 0) changelog.add(new Pair<>(version, value));
                        } catch (VersionParsingException e)
                        {
                            Keystone.LOGGER.error("Could not parse changelog entry version key!");
                            e.printStackTrace();
                        }
                    });
                    changelog.sort(Comparator.comparing(Pair::getLeft));
                    if (changelog.size() > 0)
                    {
                        lines.add(Text.literal(""));
                        lines.add(Text.translatable("keystone.version_check.changelog").styled(style -> style.withColor(Formatting.GOLD)));
                        changelog.forEach(pair ->
                                lines.add(Text.literal("    " + pair.getLeft().getFriendlyString() + ": ").styled(style -> style.withColor(Formatting.AQUA)).append(
                                        Text.literal(pair.getRight()).styled(style -> style.withColor(Formatting.GREEN))
                                )));
                    }
                }

                // Update link
                lines.add(Text.literal(""));
                lines.add(Text.translatable("keystone.version_check.releasesLink").styled(style -> style.withColor(Formatting.GOLD)).append(Text.literal(" "))
                        .append(Text.translatable("keystone.version_check.releasesLink.hyperlink").setStyle
                        (
                                Style.EMPTY
                                        .withColor(Formatting.AQUA)
                                        .withUnderline(true)
                                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, homepage))
                        )));

                for (MutableText line : lines) MinecraftClient.getInstance().player.sendMessage(line, false);
                Keystone.disableKeystone();
            }
        }
        catch (IOException | VersionParsingException e)
        {
            if (e instanceof SocketTimeoutException)
            {
                Keystone.LOGGER.error("Version check timed out!");
            }
            else
            {
                Keystone.LOGGER.error("Failed to perform version check!");
                e.printStackTrace();
            }

            MinecraftClient.getInstance().player.sendMessage(Text.literal("Failed to perform version check!").styled(style -> style.withColor(Formatting.RED)), false);
        }
    }
}

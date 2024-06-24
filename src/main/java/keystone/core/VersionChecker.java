package keystone.core;

import keystone.api.Keystone;
import keystone.core.utils.IniFile;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.Version;
import net.fabricmc.loader.api.VersionParsingException;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
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
    private static Version version = null;

    public static Version getKeystoneVersion()
    {
        if (version == null)
        {
            try
            {
                ModMetadata metadata = FabricLoader.getInstance().getModContainer(KeystoneMod.MODID).get().getMetadata();
                version = Version.parse(metadata.getVersion().getFriendlyString().split("-")[0]);
            }
            catch (VersionParsingException e)
            {
                Keystone.LOGGER.error("Failed to retrieve keystone version!");
                e.printStackTrace();
            }
        }
        return version;
    }

    public static void doVersionCheck()
    {
        Thread thread = new Thread(VersionChecker::threadedVersionCheck, "Keystone Version Check");
        thread.start();
    }
    private static void threadedVersionCheck()
    {
        try
        {
            ModMetadata metadata = FabricLoader.getInstance().getModContainer(KeystoneMod.MODID).get().getMetadata();
            Version keystoneVersion = getKeystoneVersion();
            String minecraftVersion = SharedConstants.getGameVersion().getName();
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
                
                for (MutableText line : lines) printResult(line);
                Keystone.disableKeystone();
            }
        }
        catch (IOException | VersionParsingException e)
        {
            if (e instanceof SocketTimeoutException) printResult(Text.translatable("keystone.version_check.error.timeout").styled(style -> style.withColor(Formatting.RED)));
            else printResult(Text.translatable("keystone.version_check.error.generic").styled(style -> style.withColor(Formatting.RED)));
        }
    }
    
    private static void printResult(Text result)
    {
        Keystone.LOGGER.info(result.getString());
        PlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) player.sendMessage(result);
    }
}

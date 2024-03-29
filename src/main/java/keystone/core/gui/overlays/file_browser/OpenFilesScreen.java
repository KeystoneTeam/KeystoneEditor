package keystone.core.gui.overlays.file_browser;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.io.File;
import java.util.Set;
import java.util.function.Consumer;

public class OpenFilesScreen extends FileBrowserScreen
{
    protected static Text OPEN_LABEL = Text.translatable("keystone.file_browser.open");

    protected OpenFilesScreen(Text prompt, Set<String> fileExtensions, File root, boolean recursive, Consumer<File[]> callback)
    {
        super(prompt, fileExtensions, root, recursive, callback);
    }
    public static void openFiles(Text prompt, Set<String> fileExtensions, File root, boolean recursive, Consumer<File[]> callback)
    {
        MinecraftClient.getInstance().setScreen(new OpenFilesScreen(prompt, fileExtensions, root, recursive, callback));
    }

    @Override
    protected Text getDoneButtonLabel()
    {
        return OPEN_LABEL;
    }
}

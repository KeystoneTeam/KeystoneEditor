package keystone.core.gui.overlays.file_browser;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.io.File;
import java.nio.file.Path;
import java.util.Set;
import java.util.function.Consumer;

public class OpenFilesScreen extends FileBrowserScreen
{
    protected static Text OPEN_LABEL = Text.translatable("keystone.file_browser.open");

    protected OpenFilesScreen(Text prompt, Set<String> fileExtensions, Path root, boolean recursive, boolean allowMultiple, Consumer<File[]> callback)
    {
        super(prompt, fileExtensions, root, recursive, allowMultiple, callback);
    }
    
    public static void openFile(Text prompt, Set<String> fileExtensions, Path root, boolean recursive, Consumer<File> callback, Runnable cancelled)
    {
        MinecraftClient.getInstance().setScreen(new OpenFilesScreen(prompt, fileExtensions, root, recursive, false, files ->
        {
            if (files.length > 0) callback.accept(files[0]);
            else cancelled.run();
        }));
    }
    public static void openFiles(Text prompt, Set<String> fileExtensions, Path root, boolean recursive, Consumer<File[]> callback)
    {
        MinecraftClient.getInstance().setScreen(new OpenFilesScreen(prompt, fileExtensions, root, recursive, true, callback));
    }

    @Override
    protected Text getDoneButtonLabel()
    {
        return OPEN_LABEL;
    }
}

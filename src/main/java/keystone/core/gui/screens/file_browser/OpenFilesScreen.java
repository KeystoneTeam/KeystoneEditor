package keystone.core.gui.screens.file_browser;

import net.minecraft.client.Minecraft;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import java.io.File;
import java.util.Set;
import java.util.function.Consumer;

public class OpenFilesScreen extends FileBrowserScreen
{
    protected static TranslationTextComponent OPEN_LABEL = new TranslationTextComponent("keystone.file_browser.open");

    protected OpenFilesScreen(ITextComponent prompt, Set<String> fileExtensions, File root, boolean recursive, Consumer<File[]> callback)
    {
        super(prompt, fileExtensions, root, recursive, callback);
    }
    public static void openFiles(ITextComponent prompt, Set<String> fileExtensions, File root, boolean recursive, Consumer<File[]> callback)
    {
        Minecraft.getInstance().setScreen(new OpenFilesScreen(prompt, fileExtensions, root, recursive, callback));
    }

    @Override
    protected ITextComponent getDoneButtonLabel()
    {
        return OPEN_LABEL;
    }
}

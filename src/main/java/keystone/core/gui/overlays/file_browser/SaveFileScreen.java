package keystone.core.gui.overlays.file_browser;

import keystone.core.gui.widgets.inputs.ParsableTextWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class SaveFileScreen extends FileBrowserScreen
{
    protected static Text SAVE_FILE_LABEL = Text.translatable("keystone.file_browser.saveFile");
    protected static Text FILE_NAME_LABEL = Text.translatable("keystone.file_browser.fileName");
    protected static Text SAVE_LABEL = Text.translatable("keystone.file_browser.save");

    private final String extension;
    private final Consumer<File> callback;
    private ParsableTextWidget<String> fileName;

    protected SaveFileScreen(Text prompt, String extension, Set<String> fileExtensions, Path root, boolean recursive, Consumer<File> callback)
    {
        super(prompt, fileExtensions, root, recursive, false, (files) -> {});
        this.extension = extension;
        this.callback = callback;
    }
    public static void saveFile(String fileExtension, Path root, boolean recursive, Consumer<File> callback)
    {
        Set<String> extensionSet = new HashSet<>();
        extensionSet.add(fileExtension);
        MinecraftClient.getInstance().setScreen(new SaveFileScreen(SAVE_FILE_LABEL, fileExtension, extensionSet, root, recursive, callback));
    }

    @Override
    protected int getBottomMargin()
    {
        return super.getBottomMargin() + PADDING + ParsableTextWidget.getFinalHeight();
    }

    @Override
    protected int getPanelHeightExtension()
    {
        return super.getPanelHeightExtension() + PADDING + ParsableTextWidget.getFinalHeight();
    }

    @Override
    protected Text getDoneButtonLabel()
    {
        return SAVE_LABEL;
    }
    @Override
    protected Text getPromptLabel()
    {
        return selectedFiles.size() > 0 ? selectedFiles.get(0).path : currentPath;
    }
    @Override
    protected boolean allowMultipleFiles() { return false; }
    @Override
    protected void runCallback(File[] files)
    {
        if (files.length > 0) callback.accept(files[0]);
        else
        {
            String name = fileName.getText().trim();
            if (name.equals("")) name = "New File";
            File file = currentFileStructure.resolveFile(name + "." + extension);
            callback.accept(file);
        }
    }

    @Override
    protected void init()
    {
        super.init();
        fileName = new ParsableTextWidget<>(FILE_NAME_LABEL, panelX, panelY + filePanelHeight + MARGINS, panelWidth, "")
        {
            @Override
            protected String parse(String str)
            {
                return str;
            }
        };
        addDrawableChild(fileName);
    }
}

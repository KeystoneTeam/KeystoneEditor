package keystone.core.gui.screens.file_browser;

import keystone.core.gui.widgets.inputs.ParsableTextWidget;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class SaveFileScreen extends FileBrowserScreen
{
    protected static TranslatableText SAVE_FILE_LABEL = new TranslatableText("keystone.file_browser.saveFile");
    protected static TranslatableText FILE_NAME_LABEL = new TranslatableText("keystone.file_browser.fileName");
    protected static TranslatableText SAVE_LABEL = new TranslatableText("keystone.file_browser.save");

    private String extension;
    private ParsableTextWidget<String> fileName;
    private Consumer<File> callback;

    protected SaveFileScreen(Text prompt, String extension, Set<String> fileExtensions, File root, boolean recursive, Consumer<File> callback)
    {
        super(prompt, fileExtensions, root, recursive, (files) -> {});
        this.extension = extension;
        this.callback = callback;
    }
    public static void saveFile(String fileExtension, File root, boolean recursive, Consumer<File> callback)
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
        fileName = new ParsableTextWidget<String>(FILE_NAME_LABEL, panelX, panelY + filePanelHeight + MARGINS, panelWidth, "")
        {
            @Override
            protected String parse(String str) throws Exception
            {
                return str;
            }
        };
        addDrawableChild(fileName);
    }
}

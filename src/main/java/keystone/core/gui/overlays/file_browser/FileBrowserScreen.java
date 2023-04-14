package keystone.core.gui.overlays.file_browser;

import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import keystone.core.gui.widgets.buttons.SimpleButton;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public abstract class FileBrowserScreen extends Screen
{
    protected static final int MARGINS = 2;
    protected static final int PADDING = 5;
    protected static final int BUTTON_HEIGHT = 20;
    protected static Text CANCEL_LABEL = Text.translatable("keystone.cancel");

    //region Data Types
    protected static class FileStructure
    {
        public Path directory;
        public FileStructure parent;
        public List<FileStructure> directories;
        public List<File> files;
        public String path;

        public FileStructure(FileStructure parent, Path directory, boolean recursive, Set<String> fileExtensions)
        {
            this.path = (parent != null) ? parent.path + "/" + directory.toFile().getName() : directory.toFile().getName();

            this.directory = directory;
            this.parent = parent;
            File[] contents = directory.toFile().listFiles();
            Arrays.sort(contents, File::compareTo);

            this.directories = new ArrayList<>();
            this.files = new ArrayList<>();
            for (File file : contents)
            {
                if (file.isDirectory() && recursive) directories.add(new FileStructure(this, file.toPath(), true, fileExtensions));
                else if (file.isFile() && fileExtensions.contains(FilenameUtils.getExtension(file.getName()).toLowerCase())) files.add(file);
            }
        }

        public Path getThisDirectory() { return this.directory; }
        public File resolveFile(String fileName)
        {
            return directory.resolve(fileName).toFile();
        }
    }
    protected class IndexedButton extends SimpleButton
    {
        protected int index;

        public IndexedButton(int index, int width, Text label, ButtonWidget.PressAction pressable)
        {
            super(MARGINS, MARGINS + index * (BUTTON_HEIGHT + PADDING), width, BUTTON_HEIGHT, label, pressable);
            this.index = index;
            setBorderColor(0xFF202020);
        }

        public void setScrollIndex(int minIndex, int maxIndex)
        {
            if (index < minIndex || index > maxIndex)
            {
                this.active = false;
                this.visible = false;
            }
            else
            {
                this.x = panelX + MARGINS;
                this.y = panelY + MARGINS + (index - minIndex) * (BUTTON_HEIGHT + PADDING);
                this.active = true;
                this.visible = true;
            }
        }
    }
    protected class FileButton extends IndexedButton
    {
        public File file;
        public boolean selected;
        public Text path;

        public FileButton(FileBrowserScreen screen, int index, int width, File file, FileStructure parent, boolean selected)
        {
            super(index, width, Text.literal(file.getName()), (button) -> screen.selectFile((FileButton) button));

            this.file = file;
            this.selected = selected;
            this.path = Text.literal(parent.path + "/" + file.getName());
        }

        @Override
        protected int getYImage(boolean hovered)
        {
            hovered = hovered || selected;
            return super.getYImage(hovered);
        }
    }
    protected class DirectoryButton extends IndexedButton
    {
        public FileStructure fileStructure;

        public DirectoryButton(FileBrowserScreen screen, int index, int width, FileStructure fileStructure, boolean parent)
        {
            super(index, width, Text.literal(parent ? "../" : fileStructure.getThisDirectory().toFile().getName() + "/"), (button) -> screen.moveToDirectory((DirectoryButton)button));
            this.fileStructure = fileStructure;
        }
    }
    //endregion

    protected Text currentPath;
    protected FileStructure currentFileStructure;
    protected Consumer<File[]> callback;
    protected boolean allowMultipleFiles;

    protected int panelX;
    protected int panelY;
    protected int panelWidth;
    protected int panelHeight;
    protected int filePanelHeight;
    protected int scroll;
    protected int maxFilesOnScreen;
    protected List<IndexedButton> allButtons;
    protected List<FileButton> selectedFiles;

    protected FileBrowserScreen(Text prompt, Set<String> fileExtensions, Path root, boolean recursive, boolean allowMultipleFiles, Consumer<File[]> callback)
    {
        super(prompt);

        this.currentFileStructure = new FileStructure(null, root, recursive, fileExtensions);
        this.callback = callback;
        this.allowMultipleFiles = allowMultipleFiles;
        
        this.allButtons = new ArrayList<>();
        this.selectedFiles = new ArrayList<>();
        this.scroll = 0;
    }

    protected boolean allowMultipleFiles() { return allowMultipleFiles; }
    protected int getBottomMargin() { return 100 + 20; }
    protected int getPanelHeightExtension() { return 20 + MARGINS; }
    protected abstract Text getDoneButtonLabel();
    protected Text getPromptLabel() { return title; }
    protected void runCallback(File[] files) { callback.accept(files); }

    @Override
    protected void init()
    {
        this.panelWidth = client.getWindow().getScaledWidth() / 2;
        int idealPanelHeight = client.getWindow().getScaledHeight() - PADDING - getBottomMargin();
        this.maxFilesOnScreen = idealPanelHeight / (BUTTON_HEIGHT + PADDING);
        this.filePanelHeight = this.maxFilesOnScreen * (BUTTON_HEIGHT + PADDING) - PADDING + 2 * MARGINS;
        this.panelHeight = this.filePanelHeight + getPanelHeightExtension();
        this.panelX = (client.getWindow().getScaledWidth() - this.panelWidth) / 2;
        this.panelY = 50;
        int buttonWidth = this.panelWidth - 2 * MARGINS;

        this.currentPath = Text.literal(this.currentFileStructure.path);

        this.allButtons.clear();
        int i = 0;
        if (this.currentFileStructure.parent != null) allButtons.add(new DirectoryButton(this, i++, buttonWidth, this.currentFileStructure.parent, true));
        for (FileStructure dir : currentFileStructure.directories) allButtons.add(new DirectoryButton(this, i++, buttonWidth, dir, false));
        for (File file : currentFileStructure.files) allButtons.add(new FileButton(this, i++, buttonWidth, file, currentFileStructure, selectedFiles.contains(file)));

        int panelCenterX = panelX + (panelWidth / 2);
        ButtonWidget open = new ButtonNoHotkey(this.panelX, this.panelY + this.panelHeight - 20, panelCenterX - panelX - PADDING / 2, 20, getDoneButtonLabel(), (button) ->
        {
            File[] files = new File[selectedFiles.size()];
            for (int j = 0; j < files.length; j++) files[j] = selectedFiles.get(j).file;
            runCallback(files);
            close();
        });
        ButtonWidget cancel = new ButtonNoHotkey(this.panelX + open.getWidth() + PADDING, this.panelY + this.panelHeight - 20, panelCenterX - panelX - PADDING / 2, 20, CANCEL_LABEL, (button) ->
        {
            this.callback.accept(new File[0]);
            close();
        });

        for (IndexedButton button : allButtons) addDrawableChild(button);
        addDrawableChild(cancel);
        addDrawableChild(open);

        updateScroll();
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
        drawCenteredText(stack, textRenderer, getPromptLabel(), panelX + panelWidth / 2, panelY - 14, 0xFFFFFFFF);

        fill(stack, panelX, panelY, panelX + panelWidth, panelY + filePanelHeight, 0x80000000);
        if (maxFilesOnScreen < allButtons.size())
        {
            int scrollbarY = panelY + (int)(scroll * (filePanelHeight / (float)allButtons.size()));
            int scrollbarHeight = (int)(maxFilesOnScreen * (filePanelHeight / (float)allButtons.size()));
            fill(stack, panelX + panelWidth, scrollbarY, panelX + panelWidth + 2, scrollbarY + scrollbarHeight, 0xFF808080);
        }
        super.render(stack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta)
    {
        if (maxFilesOnScreen < allButtons.size())
        {
            scroll += (delta < 0) ? 1 : -1;
            int maxScrollSteps = allButtons.size() - maxFilesOnScreen;
            scroll = Math.min(maxScrollSteps, Math.max(0, scroll));
            updateScroll();
            return true;
        }
        else return false;
    }

    protected void selectFile(FileButton fileButton)
    {
        fileButton.selected = !fileButton.selected;
        if (fileButton.selected)
        {
            fileButton.setFormatting(0xFF00FF00);
            if (!allowMultipleFiles())
            {
                for (FileButton selected : selectedFiles)
                {
                    selected.selected = false;
                    selected.setFormatting(0xFFFFFF00);
                }
                selectedFiles.clear();
            }
            this.selectedFiles.add(fileButton);
        }
        else
        {
            fileButton.setFormatting(0xFFFFFF00);
            this.selectedFiles.remove(fileButton);
        }
    }
    protected void moveToDirectory(DirectoryButton directory)
    {
        this.scroll = 0;
        this.currentFileStructure = directory.fileStructure;
        init(client, width, height);
    }
    protected void updateScroll()
    {
        if (maxFilesOnScreen < children().size())
        {
            int maxScrollSteps = allButtons.size() - maxFilesOnScreen;
            scroll = Math.min(maxScrollSteps, Math.max(0, scroll));
        }
        else scroll = 0;

        int minIndex = scroll;
        int maxIndex = scroll + maxFilesOnScreen - 1;
        for (IndexedButton button : allButtons) button.setScrollIndex(minIndex, maxIndex);
    }
}

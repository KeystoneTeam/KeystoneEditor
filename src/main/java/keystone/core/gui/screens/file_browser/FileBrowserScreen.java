package keystone.core.gui.screens.file_browser;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.core.gui.widgets.buttons.ButtonNoHotkey;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
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
    protected static TranslationTextComponent CANCEL_LABEL = new TranslationTextComponent("keystone.cancel");

    //region Data Types
    protected class FileStructure
    {
        public File directory;
        public FileStructure parent;
        public List<FileStructure> directories;
        public List<File> files;
        public String path;

        public FileStructure(FileStructure parent, File directory, boolean recursive, Set<String> fileExtensions)
        {
            this.path = (parent != null) ? parent.path + "/" + directory.getName() : directory.getName();

            this.directory = directory;
            this.parent = parent;
            File[] contents = directory.listFiles();
            Arrays.sort(contents, File::compareTo);

            this.directories = new ArrayList<>();
            this.files = new ArrayList<>();
            for (File file : contents)
            {
                if (file.isDirectory() && recursive) directories.add(new FileStructure(this, file, true, fileExtensions));
                else if (file.isFile() && fileExtensions.contains(FilenameUtils.getExtension(file.getName()).toLowerCase())) files.add(file);
            }
        }

        public File getThisDirectory() { return this.directory; }
        public File resolveFile(String fileName)
        {
            return directory.toPath().resolve(fileName).toFile();
        }
    }
    protected class IndexedButton extends ButtonNoHotkey
    {
        protected int index;

        public IndexedButton(int index, int width, ITextComponent label, Button.IPressable pressable)
        {
            super(MARGINS, MARGINS + index * (BUTTON_HEIGHT + PADDING), width, BUTTON_HEIGHT, label, pressable);
            this.index = index;
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
        public ITextComponent path;

        public FileButton(FileBrowserScreen screen, int index, int width, File file, FileStructure parent, boolean selected)
        {
            super(index, width, new StringTextComponent(file.getName()), (button) -> screen.selectFile((FileButton) button));

            this.file = file;
            this.selected = selected;
            this.path = new StringTextComponent(parent.path + "/" + file.getName());
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
            super(index, width, new StringTextComponent(parent ? "../" : fileStructure.getThisDirectory().getName() + "/"), (button) -> screen.moveToDirectory((DirectoryButton)button));
            this.fileStructure = fileStructure;
        }
    }
    //endregion

    protected ITextComponent currentPath;
    protected FileStructure currentFileStructure;
    protected Consumer<File[]> callback;

    protected int panelX;
    protected int panelY;
    protected int panelWidth;
    protected int panelHeight;
    protected int filePanelHeight;
    protected int scroll;
    protected int maxFilesOnScreen;
    protected List<IndexedButton> allButtons;
    protected List<FileButton> selectedFiles;

    protected FileBrowserScreen(ITextComponent prompt, Set<String> fileExtensions, File root, boolean recursive, Consumer<File[]> callback)
    {
        super(prompt);

        this.currentFileStructure = new FileStructure(null, root, recursive, fileExtensions);
        this.callback = callback;
        this.allButtons = new ArrayList<>();
        this.selectedFiles = new ArrayList<>();
        this.scroll = 0;
    }

    protected boolean allowMultipleFiles() { return true; }
    protected int getBottomMargin() { return 100 + 20; }
    protected int getPanelHeightExtension() { return 20 + MARGINS; }
    protected abstract ITextComponent getDoneButtonLabel();
    protected ITextComponent getPromptLabel() { return title; }
    protected void runCallback(File[] files) { callback.accept(files); }

    @Override
    protected void init()
    {
        this.panelWidth = minecraft.getWindow().getGuiScaledWidth() / 2;
        int idealPanelHeight = minecraft.getWindow().getGuiScaledHeight() - PADDING - getBottomMargin();
        this.maxFilesOnScreen = idealPanelHeight / (BUTTON_HEIGHT + PADDING);
        this.filePanelHeight = this.maxFilesOnScreen * (BUTTON_HEIGHT + PADDING) - PADDING + 2 * MARGINS;
        this.panelHeight = this.filePanelHeight + getPanelHeightExtension();
        this.panelX = (minecraft.getWindow().getGuiScaledWidth() - this.panelWidth) / 2;
        this.panelY = 50;
        int buttonWidth = this.panelWidth - 2 * MARGINS;

        this.currentPath = new StringTextComponent(this.currentFileStructure.path);

        this.allButtons.clear();
        int i = 0;
        if (this.currentFileStructure.parent != null) allButtons.add(new DirectoryButton(this, i++, buttonWidth, this.currentFileStructure.parent, true));
        for (FileStructure dir : currentFileStructure.directories) allButtons.add(new DirectoryButton(this, i++, buttonWidth, dir, false));
        for (File file : currentFileStructure.files) allButtons.add(new FileButton(this, i++, buttonWidth, file, currentFileStructure, selectedFiles.contains(file)));

        int panelCenterX = panelX + (panelWidth / 2);
        Button cancel = new ButtonNoHotkey(this.panelX, this.panelY + this.panelHeight - 20, panelCenterX - panelX - PADDING / 2, 20, CANCEL_LABEL, (button) ->
        {
            this.callback.accept(new File[0]);
            onClose();
        });
        Button open = new ButtonNoHotkey(this.panelX + cancel.getWidth() + PADDING, this.panelY + this.panelHeight - 20, panelCenterX - panelX - PADDING / 2, 20, getDoneButtonLabel(), (button) ->
        {
            File[] files = new File[selectedFiles.size()];
            for (int j = 0; j < files.length; j++) files[j] = selectedFiles.get(j).file;
            runCallback(files);
            onClose();
        });

        for (IndexedButton button : allButtons) addButton(button);
        addButton(cancel);
        addButton(open);

        updateScroll();
    }

    @Override
    public void render(MatrixStack stack, int mouseX, int mouseY, float partialTicks)
    {
        drawCenteredString(stack, font, getPromptLabel(), panelX + panelWidth / 2, panelY - 14, 0xFFFFFFFF);

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
            if (!allowMultipleFiles())
            {
                for (FileButton selected : selectedFiles) selected.selected = false;
                selectedFiles.clear();
            }
            this.selectedFiles.add(fileButton);
        }
        else this.selectedFiles.remove(fileButton);
    }
    protected void moveToDirectory(DirectoryButton directory)
    {
        this.scroll = 0;
        this.currentFileStructure = directory.fileStructure;
        init(minecraft, width, height);
    }
    protected void updateScroll()
    {
        if (maxFilesOnScreen < buttons.size())
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

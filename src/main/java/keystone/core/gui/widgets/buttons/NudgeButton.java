package keystone.core.gui.widgets.buttons;

import keystone.api.Keystone;
import keystone.core.KeystoneGlobalState;
import keystone.core.client.Player;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.history.IHistoryEntry;
import keystone.core.modules.history.entries.ImportBoxesHistoryEntry;
import keystone.core.modules.history.entries.SelectionHistoryEntry;
import keystone.core.modules.schematic_import.ImportModule;
import keystone.core.modules.selection.SelectionModule;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction;
import org.lwjgl.glfw.GLFW;

public class NudgeButton extends SimpleButton
{
    public interface NudgeConsumer { void nudge(Direction direction, int amount); }
    public interface NudgeHistorySupplier { IHistoryEntry get(); }

    public static final Text NUDGE = Text.translatable("keystone.nudge");
    public static final NudgeHistorySupplier SELECTION_HISTORY_SUPPLIER = () ->
    {
        SelectionModule selectionModule = Keystone.getModule(SelectionModule.class);
        return new SelectionHistoryEntry(selectionModule.getSelectionBoundingBoxes(), true);
    };
    public static final NudgeHistorySupplier IMPORT_HISTORY_SUPPLIER = () ->
    {
        ImportModule importModule = Keystone.getModule(ImportModule.class);
        return new ImportBoxesHistoryEntry(importModule.getImportBoxes());
    };

    private static final int HOLD_DELAY = 10;
    private static final int HOLD_TICKS_BETWEEN = 5;

    private final GameOptions gameSettings;
    private final NudgeConsumer nudge;
    private final NudgeHistorySupplier historySupplier;
    private int nudgeButton;
    private boolean nudgeButtonDown;

    private boolean forwardPressed;
    private boolean backPressed;
    private boolean leftPressed;
    private boolean rightPressed;
    private boolean upPressed;
    private boolean downPressed;

    private int forwardTime;
    private int backTime;
    private int leftTime;
    private int rightTime;
    private int upTime;
    private int downTime;

    public NudgeButton(int x, int y, int width, int height, NudgeConsumer nudge, NudgeHistorySupplier historySupplier)
    {
        super(x, y, width, height, NUDGE, null);
        this.gameSettings = MinecraftClient.getInstance().options;
        this.nudge = nudge;
        this.historySupplier = historySupplier;
    }

    protected int getNudgeStep(Direction direction, int button)
    {
        if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) return 1;
        else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) return 16;
        return 0;
    }

    @Override
    public void renderButton(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
    {
        super.renderButton(matrixStack, mouseX, mouseY, partialTicks);
    }

    public void tick()
    {
        if (!nudgeButtonDown) return;

        boolean blockKeys = true;
        Direction forward = Direction.fromRotation(Player.getYaw());
        int forwardIndex = forward.getHorizontal();
        Direction right = Direction.fromHorizontal(forwardIndex + 1);
        Direction back = Direction.fromHorizontal(forwardIndex + 2);
        Direction left = Direction.fromHorizontal(forwardIndex + 3);

        if (forwardPressed)
        {
            forwardTime++;
            if (forwardTime == 1) nudge.nudge(forward, getNudgeStep(forward, nudgeButton));
            else if (forwardTime >= HOLD_DELAY && forwardTime % HOLD_TICKS_BETWEEN == 0) nudge.nudge(forward, getNudgeStep(forward, nudgeButton));
        }
        else
        {
            forwardTime = 0;
            if (backPressed)
            {
                backTime++;
                if (backTime == 1) nudge.nudge(back, getNudgeStep(back, nudgeButton));
                else if (backTime >= HOLD_DELAY && backTime % HOLD_TICKS_BETWEEN == 0) nudge.nudge(back, getNudgeStep(back, nudgeButton));
            }
            else
            {
                backTime = 0;
                if (leftPressed)
                {
                    leftTime++;
                    if (leftTime == 1) nudge.nudge(left, getNudgeStep(left, nudgeButton));
                    else if (leftTime >= HOLD_DELAY && leftTime % HOLD_TICKS_BETWEEN == 0) nudge.nudge(left, getNudgeStep(left, nudgeButton));
                }
                else
                {
                    leftTime = 0;
                    if (rightPressed)
                    {
                        rightTime++;
                        if (rightTime == 1) nudge.nudge(right, getNudgeStep(right, nudgeButton));
                        else if (rightTime >= HOLD_DELAY && rightTime % HOLD_TICKS_BETWEEN == 0) nudge.nudge(right, getNudgeStep(right, nudgeButton));
                    }
                    else
                    {
                        rightTime = 0;
                        if (upPressed)
                        {
                            upTime++;
                            if (upTime == 1) nudge.nudge(Direction.UP, getNudgeStep(Direction.UP, nudgeButton));
                            else if (upTime >= HOLD_DELAY && upTime % HOLD_TICKS_BETWEEN == 0) nudge.nudge(Direction.UP, getNudgeStep(Direction.UP, nudgeButton));
                        }
                        else
                        {
                            upTime = 0;
                            if (downPressed)
                            {
                                downTime++;
                                if (downTime == 1) nudge.nudge(Direction.DOWN, getNudgeStep(Direction.DOWN, nudgeButton));
                                else if (downTime >= HOLD_DELAY && downTime % HOLD_TICKS_BETWEEN == 0) nudge.nudge(Direction.DOWN, getNudgeStep(Direction.DOWN, nudgeButton));
                            }
                            else
                            {
                                downTime = 0;
                                blockKeys = false;
                            }
                        }
                    }
                }
            }
        }

        if (blockKeys) KeystoneGlobalState.GuiConsumingKeys = true;
    }

    //region Input
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button)
    {
        if (this.active && this.visible)
        {
            boolean flag = this.clicked(mouseX, mouseY);
            if (flag)
            {
                nudgeButtonDown = button == GLFW.GLFW_MOUSE_BUTTON_LEFT || button == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
                if (nudgeButtonDown)
                {
                    IHistoryEntry historyEntry = historySupplier.get();
                    if (historyEntry != null)
                    {
                        HistoryModule historyModule = Keystone.getModule(HistoryModule.class);
                        historyModule.tryBeginHistoryEntry();
                        historyModule.pushToEntry(historyEntry);
                        historyModule.tryEndHistoryEntry();
                    }

                    nudgeButton = button;
                    return true;
                }
                else return false;
            }
        }

        return false;
    }
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button)
    {
        if (nudgeButtonDown)
        {
            nudgeButton = -1;
            nudgeButtonDown = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (keyCode == gameSettings.forwardKey.getDefaultKey().getCode())
        {
            forwardPressed = true;
            forwardTime = 0;
            return true;
        }
        if (keyCode == gameSettings.backKey.getDefaultKey().getCode())
        {
            backPressed = true;
            backTime = 0;
            return true;
        }
        if (keyCode == gameSettings.leftKey.getDefaultKey().getCode())
        {
            leftPressed = true;
            leftTime = 0;
            return true;
        }
        if (keyCode == gameSettings.rightKey.getDefaultKey().getCode())
        {
            rightPressed = true;
            rightTime = 0;
            return true;
        }
        if (keyCode == gameSettings.jumpKey.getDefaultKey().getCode())
        {
            upPressed = true;
            upTime = 0;
            return true;
        }
        if (keyCode == gameSettings.sneakKey.getDefaultKey().getCode())
        {
            downPressed = true;
            downTime = 0;
            return true;
        }

        return false;
    }
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers)
    {
        if (keyCode == gameSettings.forwardKey.getDefaultKey().getCode())
        {
            forwardPressed = false;
            forwardTime = 0;
            return true;
        }
        if (keyCode == gameSettings.backKey.getDefaultKey().getCode())
        {
            backPressed = false;
            backTime = 0;
            return true;
        }
        if (keyCode == gameSettings.leftKey.getDefaultKey().getCode())
        {
            leftPressed = false;
            leftTime = 0;
            return true;
        }
        if (keyCode == gameSettings.rightKey.getDefaultKey().getCode())
        {
            rightPressed = false;
            rightTime = 0;
            return true;
        }
        if (keyCode == gameSettings.jumpKey.getDefaultKey().getCode())
        {
            upPressed = false;
            upTime = 0;
            return true;
        }
        if (keyCode == gameSettings.sneakKey.getDefaultKey().getCode())
        {
            downPressed = false;
            downTime = 0;
            return true;
        }

        return false;
    }
    //endregion
}

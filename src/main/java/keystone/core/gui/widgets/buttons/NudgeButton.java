package keystone.core.gui.widgets.buttons;

import com.mojang.blaze3d.matrix.MatrixStack;
import keystone.api.Keystone;
import keystone.core.KeystoneGlobalState;
import keystone.core.modules.history.HistoryModule;
import keystone.core.modules.history.entries.SelectionHistoryEntry;
import keystone.core.modules.selection.SelectionModule;
import keystone.core.renderer.client.Player;
import net.minecraft.client.GameSettings;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Direction;
import net.minecraft.util.text.TranslationTextComponent;
import org.lwjgl.glfw.GLFW;

import java.util.function.BiConsumer;

public class NudgeButton extends SimpleButton
{
    public interface NudgeConsumer { void nudge(Direction direction, int amount); }

    public static final TranslationTextComponent NUDGE = new TranslationTextComponent("keystone.nudge");
    private static final int HOLD_DELAY = 10;
    private static final int HOLD_TICKS_BETWEEN = 5;

    private final GameSettings gameSettings;
    private final NudgeConsumer nudge;
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

    public NudgeButton(int x, int y, int width, int height, NudgeConsumer nudge)
    {
        super(x, y, width, height, NUDGE, null);
        this.gameSettings = Minecraft.getInstance().options;
        this.nudge = nudge;
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
        Direction forward = Direction.fromYRot(Player.getYaw());
        int forwardIndex = forward.get2DDataValue();
        Direction right = Direction.from2DDataValue(forwardIndex + 1);
        Direction back = Direction.from2DDataValue(forwardIndex + 2);
        Direction left = Direction.from2DDataValue(forwardIndex + 3);

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

        if (blockKeys) KeystoneGlobalState.BlockingKeys = true;
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
                    SelectionModule selectionModule = Keystone.getModule(SelectionModule.class);
                    HistoryModule historyModule = Keystone.getModule(HistoryModule.class);
                    historyModule.beginHistoryEntry();
                    historyModule.pushToEntry(new SelectionHistoryEntry(selectionModule.getSelectionBoundingBoxes(), true));
                    historyModule.endHistoryEntry();

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
        if (keyCode == gameSettings.keyUp.getKey().getValue())
        {
            forwardPressed = true;
            forwardTime = 0;
            return true;
        }
        if (keyCode == gameSettings.keyDown.getKey().getValue())
        {
            backPressed = true;
            backTime = 0;
            return true;
        }
        if (keyCode == gameSettings.keyLeft.getKey().getValue())
        {
            leftPressed = true;
            leftTime = 0;
            return true;
        }
        if (keyCode == gameSettings.keyRight.getKey().getValue())
        {
            rightPressed = true;
            rightTime = 0;
            return true;
        }
        if (keyCode == gameSettings.keyJump.getKey().getValue())
        {
            upPressed = true;
            upTime = 0;
            return true;
        }
        if (keyCode == gameSettings.keyShift.getKey().getValue())
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
        if (keyCode == gameSettings.keyUp.getKey().getValue())
        {
            forwardPressed = false;
            forwardTime = 0;
            return true;
        }
        if (keyCode == gameSettings.keyDown.getKey().getValue())
        {
            backPressed = false;
            backTime = 0;
            return true;
        }
        if (keyCode == gameSettings.keyLeft.getKey().getValue())
        {
            leftPressed = false;
            leftTime = 0;
            return true;
        }
        if (keyCode == gameSettings.keyRight.getKey().getValue())
        {
            rightPressed = false;
            rightTime = 0;
            return true;
        }
        if (keyCode == gameSettings.keyJump.getKey().getValue())
        {
            upPressed = false;
            upTime = 0;
            return true;
        }
        if (keyCode == gameSettings.keyShift.getKey().getValue())
        {
            downPressed = false;
            downTime = 0;
            return true;
        }

        return false;
    }
    //endregion
}

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
    private static final int HOLD_DELAY = 10;
    private static final int HOLD_TICKS_BETWEEN = 5;
    private static final TranslationTextComponent NUDGE = new TranslationTextComponent("keystone.nudge");

    private final GameSettings gameSettings;
    private final BiConsumer<Direction, Integer> nudge;
    private int nudgeStep;

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

    public NudgeButton(int x, int y, int width, int height, BiConsumer<Direction, Integer> nudge)
    {
        super(x, y, width, height, NUDGE, null);
        this.gameSettings = Minecraft.getInstance().gameSettings;
        this.nudge = nudge;
    }

    protected int getNudgeStep(int button)
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
        if (nudgeStep <= 0) return;

        boolean blockKeys = true;
        double yaw = Player.getYaw();
        if (yaw > 180) yaw -= 360;
        // North
        Direction forward = Direction.NORTH;
        Direction back = Direction.SOUTH;
        Direction left = Direction.WEST;
        Direction right = Direction.EAST;
        // South
        if (yaw >= -45 && yaw <= 45)
        {
            forward = Direction.SOUTH;
            back = Direction.NORTH;
            left = Direction.EAST;
            right = Direction.WEST;
        }
        // East
        else if (yaw >= -135 && yaw <= -45)
        {
            forward = Direction.EAST;
            back = Direction.WEST;
            left = Direction.NORTH;
            right = Direction.SOUTH;
        }
        else if (yaw >= 45 && yaw <= 135)
        {
            forward = Direction.WEST;
            back = Direction.EAST;
            left = Direction.SOUTH;
            right = Direction.NORTH;
        }

        if (forwardPressed)
        {
            forwardTime++;
            if (forwardTime == 1) nudge.accept(forward, nudgeStep);
            else if (forwardTime >= HOLD_DELAY && forwardTime % HOLD_TICKS_BETWEEN == 0) nudge.accept(forward, nudgeStep);
        }
        else
        {
            forwardTime = 0;
            if (backPressed)
            {
                backTime++;
                if (backTime == 1) nudge.accept(back, nudgeStep);
                else if (backTime >= HOLD_DELAY && backTime % HOLD_TICKS_BETWEEN == 0) nudge.accept(back, nudgeStep);
            }
            else
            {
                backTime = 0;
                if (leftPressed)
                {
                    leftTime++;
                    if (leftTime == 1) nudge.accept(left, nudgeStep);
                    else if (leftTime >= HOLD_DELAY && leftTime % HOLD_TICKS_BETWEEN == 0) nudge.accept(left, nudgeStep);
                }
                else
                {
                    leftTime = 0;
                    if (rightPressed)
                    {
                        rightTime++;
                        if (rightTime == 1) nudge.accept(right, nudgeStep);
                        else if (rightTime >= HOLD_DELAY && rightTime % HOLD_TICKS_BETWEEN == 0) nudge.accept(right, nudgeStep);
                    }
                    else
                    {
                        rightTime = 0;
                        if (upPressed)
                        {
                            upTime++;
                            if (upTime == 1) nudge.accept(Direction.UP, nudgeStep);
                            else if (upTime >= HOLD_DELAY && upTime % HOLD_TICKS_BETWEEN == 0) nudge.accept(Direction.UP, nudgeStep);
                        }
                        else
                        {
                            upTime = 0;
                            if (downPressed)
                            {
                                downTime++;
                                if (downTime == 1) nudge.accept(Direction.DOWN, nudgeStep);
                                else if (downTime >= HOLD_DELAY && downTime % HOLD_TICKS_BETWEEN == 0) nudge.accept(Direction.DOWN, nudgeStep);
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
                nudgeStep = getNudgeStep(button);
                if (nudgeStep > 0)
                {
                    SelectionModule selectionModule = Keystone.getModule(SelectionModule.class);
                    Keystone.getModule(HistoryModule.class).pushToHistory(new SelectionHistoryEntry(selectionModule.getSelectionBoundingBoxes(), true));
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
        if (nudgeStep > 0)
        {
            nudgeStep = 0;
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (keyCode == gameSettings.keyBindForward.getKey().getKeyCode())
        {
            forwardPressed = true;
            forwardTime = 0;
            return true;
        }
        if (keyCode == gameSettings.keyBindBack.getKey().getKeyCode())
        {
            backPressed = true;
            backTime = 0;
            return true;
        }
        if (keyCode == gameSettings.keyBindLeft.getKey().getKeyCode())
        {
            leftPressed = true;
            leftTime = 0;
            return true;
        }
        if (keyCode == gameSettings.keyBindRight.getKey().getKeyCode())
        {
            rightPressed = true;
            rightTime = 0;
            return true;
        }
        if (keyCode == gameSettings.keyBindJump.getKey().getKeyCode())
        {
            upPressed = true;
            upTime = 0;
            return true;
        }
        if (keyCode == gameSettings.keyBindSneak.getKey().getKeyCode())
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
        if (keyCode == gameSettings.keyBindForward.getKey().getKeyCode())
        {
            forwardPressed = false;
            forwardTime = 0;
            return true;
        }
        if (keyCode == gameSettings.keyBindBack.getKey().getKeyCode())
        {
            backPressed = false;
            backTime = 0;
            return true;
        }
        if (keyCode == gameSettings.keyBindLeft.getKey().getKeyCode())
        {
            leftPressed = false;
            leftTime = 0;
            return true;
        }
        if (keyCode == gameSettings.keyBindRight.getKey().getKeyCode())
        {
            rightPressed = false;
            rightTime = 0;
            return true;
        }
        if (keyCode == gameSettings.keyBindJump.getKey().getKeyCode())
        {
            upPressed = false;
            upTime = 0;
            return true;
        }
        if (keyCode == gameSettings.keyBindSneak.getKey().getKeyCode())
        {
            downPressed = false;
            downTime = 0;
            return true;
        }

        return false;
    }
    //endregion
}

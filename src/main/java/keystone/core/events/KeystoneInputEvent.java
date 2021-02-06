package keystone.core.events;

import net.minecraftforge.eventbus.api.Event;

public class KeystoneInputEvent extends Event
{
    public static class MouseMoveEvent extends KeystoneInputEvent
    {
        public final double mouseX;
        public final double mouseY;

        public MouseMoveEvent(double mouseX, double mouseY)
        {
            this.mouseX = mouseX;
            this.mouseY = mouseY;
        }
    }
    public static class MouseClickEvent extends KeystoneInputEvent
    {
        public final int button;
        public final int modifiers;
        public final double mouseX;
        public final double mouseY;

        public MouseClickEvent(int button, int modifiers, double mouseX, double mouseY)
        {
            this.button = button;
            this.modifiers = modifiers;
            this.mouseX = mouseX;
            this.mouseY = mouseY;
        }
    }
    public static class MouseDragStartEvent extends KeystoneInputEvent
    {
        public final int button;
        public final double mouseX;
        public final double mouseY;

        public MouseDragStartEvent(int button, double mouseX, double mouseY)
        {
            this.button = button;
            this.mouseX = mouseX;
            this.mouseY = mouseY;
        }
    }
    public static class MouseDragEvent extends KeystoneInputEvent
    {
        public final int button;
        public final double mouseX;
        public final double mouseY;
        public final double dragX;
        public final double dragY;

        public MouseDragEvent(int button, double mouseX, double mouseY, double dragX, double dragY)
        {
            this.button = button;
            this.mouseX = mouseX;
            this.mouseY = mouseY;
            this.dragX = dragX;
            this.dragY = dragY;
        }
    }
    public static class MouseDragEndEvent extends KeystoneInputEvent
    {
        public final int button;
        public final double mouseX;
        public final double mouseY;

        public MouseDragEndEvent(int button, double mouseX, double mouseY)
        {
            this.button = button;
            this.mouseX = mouseX;
            this.mouseY = mouseY;
        }
    }
}

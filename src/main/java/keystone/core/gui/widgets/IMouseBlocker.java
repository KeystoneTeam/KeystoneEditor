package keystone.core.gui.widgets;

public interface IMouseBlocker
{
    /**
     * @return Returns true if this GUI component is blocking the mouse,
     * or false otherwise. Blocking the mouse tells Keystone that the
     * cursor is being used by the GUI, and that the mouse should not
     * interact with the world.
     * @param mouseX The x-coordinate of the mouse cursor, in GUI space
     * @param mouseY The y-coordinate of the mouse cursor, in GUI space
     */
    boolean isMouseBlocked(double mouseX, double mouseY);
}

package keystone.core.renderer;

import java.awt.*;
import java.util.Objects;

public class Color4f
{
    public static final Color4f white     = new Color4f(new Color(255, 255, 255));
    public static final Color4f lightGray = new Color4f(new Color(192, 192, 192));
    public static final Color4f gray      = new Color4f(new Color(128, 128, 128));
    public static final Color4f darkGray  = new Color4f(new Color(64, 64, 64));
    public static final Color4f black     = new Color4f(new Color(0, 0, 0));
    public static final Color4f red       = new Color4f(new Color(255, 0, 0));
    public static final Color4f pink      = new Color4f(new Color(255, 175, 175));
    public static final Color4f orange    = new Color4f(new Color(255, 200, 0));
    public static final Color4f yellow    = new Color4f(new Color(255, 255, 0));
    public static final Color4f green     = new Color4f(new Color(0, 255, 0));
    public static final Color4f magenta   = new Color4f(new Color(255, 0, 255));
    public static final Color4f cyan      = new Color4f(new Color(0, 255, 255));
    public static final Color4f blue      = new Color4f(new Color(0, 0, 255));

    public final float r;
    public final float g;
    public final float b;
    public final float a;

    public Color4f(float r, float g, float b, float a)
    {
        this.r = r;
        this.g = g;
        this.b = b;
        this.a = a;
    }
    public Color4f(Color4f color)
    {
        this.r = color.r;
        this.g = color.g;
        this.b = color.b;
        this.a = color.a;
    }
    public Color4f(Color javaColor)
    {
        this.r = javaColor.getRed() / 255.0f;
        this.g = javaColor.getGreen() / 255.0f;
        this.b = javaColor.getBlue() / 255.0f;
        this.a = javaColor.getAlpha() / 255.0f;
    }

    public Color4f withAlpha(float alpha)
    {
        return new Color4f(this.r, this.g, this.b, alpha);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Color4f color4f = (Color4f) o;
        return Float.compare(color4f.r, r) == 0 && Float.compare(color4f.g, g) == 0 && Float.compare(color4f.b, b) == 0 && Float.compare(color4f.a, a) == 0;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(r, g, b, a);
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder("Color4f{");
        sb.append("r=").append(r);
        sb.append(", g=").append(g);
        sb.append(", b=").append(b);
        sb.append(", a=").append(a);
        sb.append('}');
        return sb.toString();
    }
}

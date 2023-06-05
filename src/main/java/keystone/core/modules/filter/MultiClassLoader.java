package keystone.core.modules.filter;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;

public class MultiClassLoader extends ClassLoader
{
    private ClassLoader[] loaders;

    public MultiClassLoader(ClassLoader... loaders)
    {
        this.loaders = loaders;
    }

    public static MultiClassLoader from(ClassLoader first, ClassLoader... others)
    {
        MultiClassLoader loader = new MultiClassLoader(first);

        othersLoop:
        for (ClassLoader other : others)
        {
            for (ClassLoader existing : loader.loaders) if (existing.equals(other)) continue othersLoop;
            loader.appendLoader(other);
        }

        return loader;
    }

    public MultiClassLoader prependLoader(ClassLoader loader)
    {
        ClassLoader[] newLoaders = new ClassLoader[loaders.length + 1];
        System.arraycopy(this.loaders, 0, newLoaders, 1, this.loaders.length);
        newLoaders[0] = loader;
        this.loaders = newLoaders;
        return this;
    }
    public MultiClassLoader appendLoader(ClassLoader loader)
    {
        ClassLoader[] newLoaders = new ClassLoader[loaders.length + 1];
        System.arraycopy(this.loaders, 0, newLoaders, 0, this.loaders.length);
        newLoaders[this.loaders.length] = loader;
        this.loaders = newLoaders;
        return this;
    }

    @Override
    protected Class<?> findClass (String name) throws ClassNotFoundException
    {
        String path = name.replace('.', '/') + ".class";
        URL url = findResource(path);

        if (url == null) throw new ClassNotFoundException(name);

        ByteBuffer byteCode;
        try { byteCode = loadResource(url); }
        catch (IOException e) { throw new ClassNotFoundException(name, e); }

        return defineClass(name, byteCode, null);
    }

    private ByteBuffer loadResource (URL url) throws IOException
    {
        try (InputStream stream = url.openStream())
        {
            int initialBufferCapacity = Math.min(0x40000, stream.available() + 1);
            if (initialBufferCapacity <= 2) initialBufferCapacity = 0x10000;
            else initialBufferCapacity = Math.max(initialBufferCapacity, 0x200);

            ByteBuffer buf = ByteBuffer.allocate(initialBufferCapacity);
            while (true)
            {
                if (!buf.hasRemaining())
                {
                    ByteBuffer newBuf = ByteBuffer.allocate(2 * buf.capacity());
                    buf.flip();
                    newBuf.put(buf);
                    buf = newBuf;
                }

                int len = stream.read(buf.array(), buf.position(), buf.remaining());
                if (len <= 0) break;
                buf.position(buf.position() + len);
            }

            buf.flip();
            return buf;
        }
    }

    protected URL findResource (String name)
    {
        for (ClassLoader delegate : loaders)
        {
            URL resource = delegate.getResource(name);
            if (resource != null) return resource;
        }
        return null;
    }
}

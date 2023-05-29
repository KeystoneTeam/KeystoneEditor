package keystone.core.modules.filter;

import org.codehaus.janino.Descriptor;
import org.codehaus.janino.IClass;
import org.codehaus.janino.IClassLoader;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FilterIClassLoader extends IClassLoader
{
    //region Static
    private static final Constructor<?> ICLASS_CONSTRUCTOR;
    static
    {
        try
        {
            Class<?> reflectionIClass = Class.forName("org.codehaus.janino.ReflectionIClass");
            ICLASS_CONSTRUCTOR = reflectionIClass.getDeclaredConstructor(Class.class, IClassLoader.class);
            ICLASS_CONSTRUCTOR.setAccessible(true);
        }
        catch (Throwable e)
        {
            throw new RuntimeException(e);
        }
    
    }
    //endregion
    
    private List<ClassLoader> loaders;
    
    public FilterIClassLoader(ClassLoader... loaders)
    {
        super(null);
        this.loaders = new ArrayList<>(loaders.length);
        Collections.addAll(this.loaders, loaders);
    }
    
    public FilterIClassLoader appendLoader(ClassLoader loader)
    {
        this.loaders.add(loader);
        return this;
    }
    public FilterIClassLoader prependLoader(ClassLoader loader)
    {
        this.loaders.add(0, loader);
        return this;
    }
    public FilterIClassLoader build()
    {
        this.loaders = Collections.unmodifiableList(this.loaders);
        super.postConstruct();
        return this;
    }
    
    @Override
    protected IClass findIClass(String descriptor) throws ClassNotFoundException
    {
        return findIClassInternal(descriptor);
//        try { return findIClassInternal(descriptor); }
//        catch (ClassNotFoundException ignored) { return findIClassInternal("Ljava/lang/" + descriptor.substring(1, descriptor.length() - 1) + ";"); }
    }
    
    private IClass findIClassInternal(String descriptor) throws ClassNotFoundException
    {
        Class<?> clazz = null;
        ClassNotFoundException lastException = null;
    
        // For each loader
        for (ClassLoader loader : loaders)
        {
            // Try to find the class with this loader
            try { clazz = loader.loadClass(Descriptor.toClassName(descriptor)); }
            catch (ClassNotFoundException e) { lastException = e; }
        
            // If the class was found, break the loop
            if (clazz != null) break;
        }
    
        // Exception Throwing
        if (lastException == null) lastException = new ClassNotFoundException("Unable to find IClass for '" + descriptor + "'!");
        if (clazz == null) throw lastException;
    
        // Instantiate the IClass
        IClass iClass;
        try { iClass = (IClass) ICLASS_CONSTRUCTOR.newInstance(clazz, this); }
        catch (Throwable e) { throw new ClassNotFoundException("Failed to reflectively instantiate ReflectionIClass for '" + clazz.getName() + "'!", e); }
    
        // Define and return the IClass
        this.defineIClass(iClass);
        return iClass;
    }
}

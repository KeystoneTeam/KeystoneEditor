package keystone.core.utils;

import keystone.api.Keystone;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class Result<T>
{
    private final T content;
    private final Throwable exception;
    private final String errorMessage;
    private final boolean successful;

    private Result(T content, Throwable exception, String errorMessage, boolean successful)
    {
        this.content = content;
        this.exception = exception;
        this.errorMessage = errorMessage;
        this.successful = successful;
    }

    public static <T> Result<T> success(T content) { return new Result<>(content, null, "", true); }
    public static <T> Result<T> failed(String error) { return failed(error, null); }
    public static <T> Result<T> failed(String error, Throwable exception) { return new Result<T>(null, exception, error + (exception != null ? ": " + exception.getLocalizedMessage() : "!"), false); }
    public static <T> Result<T> failed(Result<?> from) { return new Result<T>(null, from.exception, from.errorMessage, false); }

    public T get() { return content; }
    public Throwable exception() { return exception; }

    public boolean isSuccessful() { return successful; }
    public boolean isFailed() { return !successful; }
    public void logFailure()
    {
        Keystone.LOGGER.error(errorMessage);
        MinecraftClient client = MinecraftClient.getInstance();
        if (client != null && client.player != null) client.player.sendMessage(Text.literal(errorMessage).styled(style -> style.withColor(Formatting.RED)));
        if (exception != null) exception.printStackTrace();
    }
}

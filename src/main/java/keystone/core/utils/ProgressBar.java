package keystone.core.utils;

import keystone.core.gui.screens.ProgressBarOverlay;

public class ProgressBar
{
    private static int iterations;
    private static int completedIterations;
    private static int steps;
    private static int completedSteps;
    private static Runnable cancelledCallback;

    public static void start(String title, int iterations) { start(title, iterations, null); }
    public static void start(String title, int iterations, Runnable cancelledCallback)
    {
        ProgressBar.iterations = iterations;
        steps = 1;
        completedIterations = 0;
        completedSteps = 0;
        ProgressBar.cancelledCallback = cancelledCallback;

        ProgressBarOverlay.open(title);
    }
    public static void beginIteration(int steps)
    {
        ProgressBar.steps = steps;
        ProgressBar.completedSteps = 0;
    }
    public static void completeStep()
    {
        ProgressBar.completedSteps++;
    }
    public static void completeIteration()
    {
        ProgressBar.completedIterations++;
        ProgressBar.completedSteps = 0;
    }
    public static void finish()
    {
        iterations = 1;
        steps = 1;
        completedIterations = 0;
        completedSteps = 0;

        ProgressBarOverlay.close();
    }
    public static void cancel()
    {
        if (cancelledCallback != null) cancelledCallback.run();
        finish();
    }

    public static float getCurrentProgress() { return Math.min(completedSteps / (float)steps, 1.0f); }
    public static int getIterations() { return iterations; }
    public static int getCompletedIterations() { return completedIterations; }
    public static int getSteps() { return steps; }
    public static int getCompletedSteps() { return completedSteps; }
    public static boolean isCancellable() { return cancelledCallback != null; }
}

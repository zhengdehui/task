package cn.dehui.task.browser.search;

public class WithResultRunnable<T> implements Runnable {
    public T    result;

    private Callback<T> callback;

    public WithResultRunnable(Callback<T> callback) {
        this.callback = callback;
    }

    @Override
    public void run() {
        result = callback.execute();
    }
}
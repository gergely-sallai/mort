package gergelysallai.mort.android.connection;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;

class MainThreadExecutor implements Executor {

    private final Handler handler;
    private final Thread mainThread;

    public MainThreadExecutor() {
        final Looper looper = Looper.getMainLooper();
        this.mainThread = looper.getThread();
        this.handler = new Handler(looper);
    }

    @Override
    public void execute(@NonNull Runnable command) {
        if (mainThread == Thread.currentThread()) {
            command.run();
        } else {
            handler.post(command);
        }
    }

}


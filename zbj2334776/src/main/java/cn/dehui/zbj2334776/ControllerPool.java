package cn.dehui.zbj2334776;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.memetix.mst.language.Language;

public class ControllerPool implements Runnable {
    private int              activeCount = 5;

    private File[]           txtFiles;

    private Language         from;

    private Language         to;

    private int              cursor      = 0;

    private Set<Controller>  controllerSet;

    private Callback<String> endPerFileCallback;

    private Callback<String> endAllFileCallback;

    private Callback<String> startPerFileCallback;

    ControllerPool(File[] txtFiles, Language from, Language to, int activeCount, Callback<String> startPerFileCallback,
            Callback<String> endPerFileCallback, Callback<String> endAllFileCallback) {
        this.txtFiles = txtFiles;
        this.activeCount = activeCount;
        this.from = from;
        this.to = to;
        this.startPerFileCallback = startPerFileCallback;
        this.endPerFileCallback = endPerFileCallback;
        this.endAllFileCallback = endAllFileCallback;

        controllerSet = new HashSet<Controller>(activeCount);
    }

    public synchronized void notifyEnd(Controller oldController) {
        controllerSet.remove(oldController);
        endPerFileCallback.execute(oldController.toString());
        try {
            if (cursor < txtFiles.length) {
                Controller controller = new Controller(txtFiles[cursor], from, to, startPerFileCallback,
                        new Callback<Controller>() {
                            @Override
                            public void execute(Controller c) {
                                notifyEnd(c);
                            }
                        });

                controllerSet.add(controller);

                new Thread(controller).start();

                cursor++;
            } else {
                if (controllerSet.isEmpty()) {
                    endAllFileCallback.execute();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        for (cursor = 0; cursor < activeCount && cursor < txtFiles.length; cursor++) {
            try {
                Controller controller = new Controller(txtFiles[cursor], from, to, startPerFileCallback,
                        new Callback<Controller>() {
                            @Override
                            public void execute(Controller c) {
                                notifyEnd(c);
                            }
                        });
                controllerSet.add(controller);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        startThreads();
    }

    public void startThreads() {
        for (Controller controller : controllerSet) {
            new Thread(controller).start();
        }
    }

    public void parseThreads() {
        for (Controller controller : controllerSet) {
            controller.pause();
        }
    }
}

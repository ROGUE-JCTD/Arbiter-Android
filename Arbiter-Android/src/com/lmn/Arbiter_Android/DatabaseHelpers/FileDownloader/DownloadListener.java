package com.lmn.Arbiter_Android.DatabaseHelpers.FileDownloader;

import com.lmn.Arbiter_Android.BaseClasses.Tileset;

import java.util.ArrayList;

interface IDownloadListener{
    void execute();
}

public class DownloadListener implements IDownloadListener {

    private ArrayList<Runnable> runnables;

    private static DownloadListener Listener = null;
    public static DownloadListener getListener(){
        if (Listener == null){
            Listener = new DownloadListener();
            Listener.runnables = new ArrayList<Runnable>();
        }

        return Listener;
    }

    public void addToListenerList(Runnable runnable){
        // Hack for now
        if (runnables.size() <= 0)
            runnables.add(runnable);
    }

    public void execute(){
        for (int i = 0; i < runnables.size(); i++){
            runnables.get(i).run();
        }
    }
}

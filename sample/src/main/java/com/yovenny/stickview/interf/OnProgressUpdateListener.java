package com.yovenny.stickview.interf;

public interface OnProgressUpdateListener {
    void onProgressUpdate(long downloadedBytes, long fileLength);

    void onComplete();

    void onError(Exception e);
}

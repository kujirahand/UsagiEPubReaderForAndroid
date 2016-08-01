package com.kujirahand.KEPUB.async;

/**
 * Created by kujira on 2016/04/18.
 */
public class SyncTaskInt3 {
    protected Integer doInBackground(Integer... params) {
        return null;
    }
    protected void onPostExecute(Integer result) {
    }
    public void execute() {
        int result = doInBackground();
        onPostExecute(result);
    }
}

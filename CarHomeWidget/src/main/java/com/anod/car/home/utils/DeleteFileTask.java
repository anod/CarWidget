package com.anod.car.home.utils;

import android.os.AsyncTask;

import java.io.File;

/**
 * @author alex
 * @date 12/30/13
 */
public class DeleteFileTask extends AsyncTask<File, Void, Boolean> {
	private int mType;
	private DeleteFileTaskListener mListener;

	public interface DeleteFileTaskListener {
		void onDeleteFileFinish(boolean success);
	}

	public DeleteFileTask(DeleteFileTaskListener listener) {
		mListener = listener;
	}


	@Override
	protected void onPreExecute() {
	}

	protected Boolean doInBackground(File... files) {
		return files[0].delete();
	}

	protected void onPostExecute(Boolean result) {
		mListener.onDeleteFileFinish(result);
	}
}
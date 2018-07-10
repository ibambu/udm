package com.ibamb.udm.task;

import android.app.Activity;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ibamb.udm.R;
import com.ibamb.udm.component.AESCrypt;
import com.ibamb.udm.component.FileDirManager;
import com.ibamb.udm.log.UdmLog;
import com.ibamb.udm.module.constants.Constants;
import com.ibamb.udm.module.security.DefualtECryptValue;
import com.ibamb.udm.module.security.ICryptStrategy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class ImportTypeFileAsyncTask extends AsyncTask<String, String, String> {
    private Activity activity;
    private View currentView;

    public ImportTypeFileAsyncTask(Activity activity ,View currentView) {
        this.activity = activity;
        this.currentView = currentView;
    }

    @Override
    protected String doInBackground(String... strings) {
        BufferedReader bufferedReader = null;
        FileOutputStream outputStream = null;
        if (strings != null && strings.length > 0) {
            File typeFile = new File(strings[0]);
            try {
                /**
                 * 读取文件
                 */
                FileInputStream fileInputStream = new FileInputStream(typeFile);
                bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream, Constants.DEFAULT_CHARSET));
                String readLine = null;
                /**
                 * 加密文件
                 */

                StringBuilder stringBuffer = new StringBuilder();
                publishProgress("Loading file :"+typeFile.getName());
                boolean isFileError = false;
                while ((readLine = bufferedReader.readLine()) != null) {
                    String[] dataArray = readLine.split(Constants.FILE_PARAM_MAPPING_COLUMN_SPLIT);
                    if(dataArray.length<12){
                        onProgressUpdate("Error data :"+readLine);
                        isFileError = true;
                        break;
                    }
                    stringBuffer.append(readLine).append("&&");
                }
                ICryptStrategy aes = new AESCrypt();
                String content = aes.encode(stringBuffer.toString(), DefualtECryptValue.KEY);
                if(!isFileError){
                    publishProgress("Data encode completed.");
                    /**
                     * 保存文件
                     */
                    FileDirManager fileDirManager = new FileDirManager(activity);
                    File distFile = fileDirManager.getFileByName(Constants.FILE_PARAM_MAPPING);
                    if (distFile != null) {
                        distFile.delete();
                    }
                    outputStream = activity.openFileOutput(Constants.FILE_PARAM_MAPPING, Activity.MODE_APPEND);
                    outputStream.write(content.getBytes());//写入新文件
                    publishProgress("Import completed.");
                }

            } catch (Exception e) {
                e.printStackTrace();
                UdmLog.e(this.getClass().getName(),e.getMessage());
            } finally {
                try {
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                    if (outputStream != null) {
                        outputStream.close();
                    }
                } catch (IOException e) {
                    UdmLog.e(this.getClass().getName(),e.getMessage());
                }
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(String string) {
        ProgressBar bar = currentView.findViewById(R.id.import_progress_bar);
        bar.setProgress(3);
        super.onPostExecute(string);
    }

    @Override
    protected void onProgressUpdate(String... values) {

        TextView notice = currentView.findViewById(R.id.import_progress_notice);
        notice.setText(values[0]);
        super.onProgressUpdate(values);
    }
}
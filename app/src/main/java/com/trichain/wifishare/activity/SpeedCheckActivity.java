package com.trichain.wifishare.activity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.github.anastr.speedviewlib.Speedometer;
import com.trichain.wifishare.R;
import com.trichain.wifishare.databinding.ActivitySpeedCheckBinding;
import com.trichain.wifishare.util.AppExecutors;
import com.trichain.wifishare.util.CheckConnectivity;

import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;
import fr.bmartel.speedtest.utils.SpeedTestUtils;

public class SpeedCheckActivity extends AppCompatActivity {

    private ActivitySpeedCheckBinding b;
    private static final String TAG = "SpeedCheckActivity";
    private SpeedTestSocket speedTestSocket;
    private SpeedTestSocket speedTestSocket2;
    private AppExecutors appExecutors;
    private boolean isDownloadTest = true;
    float upload = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, R.layout.activity_speed_check);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Speed Check");

        appExecutors = AppExecutors.getInstance();

        speedTestSocket = new SpeedTestSocket();
        speedTestSocket2 = new SpeedTestSocket();

        b.tvWiFiNameSpeed.setText(CheckConnectivity.getWiFiName(this).replace("\"", ""));
        b.tvDownloadSpeed.setText(getString(R.string.string_string, "Download: ", "0Kbps"));
        b.tvUpSpeed.setText(getString(R.string.string_string, "Upload:   ", "0Kbps"));
        b.speedView.setSpeedometerMode(Speedometer.Mode.NORMAL);
        b.speedView.setWithTremble(false);
        b.speedViewUpload.setSpeedometerMode(Speedometer.Mode.NORMAL);
        b.speedViewUpload.setWithTremble(false);

        // add a listener to wait for speedtest completion and progress
        speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {

            @Override
            public void onCompletion(SpeedTestReport report) {
                Log.e(TAG, "onCompletion: [Complete] ");
                // called when download/upload is complete
                System.out.println("[COMPLETED] rate in octet/s : " + report.getTransferRateOctet());
                System.out.println("[COMPLETED] rate in bit/s   : " + report.getTransferRateBit());
                appExecutors.mainThread().execute(() -> {

                    //b.speedView.speedTo(0, 1500);
                    b.tvDownloadSpeed.setText(getString(R.string.string_string, "Download: ", Float.parseFloat(String.valueOf(report.getTransferRateBit().toBigInteger())) / 1000 + "Kbps"));
                    isDownloadTest = false;

                    //testUploadSpeed();

                });

            }

            @Override
            public void onError(SpeedTestError speedTestError, String errorMessage) {
                // called when a download/upload error occur
                Log.e(TAG, "onError: Network Error occurred -> " + errorMessage);

                appExecutors.mainThread().execute(() -> {
                    b.speedView.speedTo(0, 1500);
                });
            }

            @Override
            public void onProgress(float percent, SpeedTestReport report) {
                Log.e(TAG, "onProgress: [ Progress%] " + percent);
                Log.e(TAG, "onProgress: [ Progress] " + Float.parseFloat(String.valueOf(report.getTransferRateBit().toBigInteger())) / 1000000);
                // called to notify download/upload progress
                System.out.println("[PROGRESS]  progress : " + percent + "%");
                appExecutors.mainThread().execute(() -> {
                    b.speedView.speedTo(Float.parseFloat(String.valueOf(report.getTransferRateBit().toBigInteger())) / 1000000, 1000);
                    b.tvDownloadSpeed.setText(getString(R.string.string_string, "Download: ", Float.parseFloat(String.valueOf(report.getTransferRateBit().toBigInteger())) / 1000 + "Kbps"));


                });
                System.out.println("[PROGRESS] rate in octet/s : " + report.getTransferRateOctet());
                System.out.println("[PROGRESS] rate in bit/s   : " + report.getTransferRateBit());
            }
        });
        // add a listener to wait for speedtest completion and progress
        speedTestSocket2.addSpeedTestListener(new ISpeedTestListener() {

            @Override
            public void onCompletion(SpeedTestReport report) {
                Log.e(TAG, "onCompletion: [Complete] ");
                // called when download/upload is complete
                System.out.println("[COMPLETED] rate in octet/s : " + report.getTransferRateOctet());
                System.out.println("[COMPLETED] rate in bit/s   : " + report.getTransferRateBit());
                appExecutors.mainThread().execute(() -> {
                   // b.speedViewUpload.speedTo(0, 1500);
                    b.tvUpSpeed.setText(getString(R.string.string_string, "Upload:   ", Float.parseFloat(String.valueOf(report.getTransferRateBit().toBigInteger())) / 1000 + "Kbps"));
                });
              }


            @Override
            public void onError(SpeedTestError speedTestError, String errorMessage) {
                // called when a download/upload error occur
                Log.e(TAG, "onError: Network Error occurred -> " + errorMessage);

                appExecutors.mainThread().execute(() -> {
                    b.speedViewUpload.speedTo(0, 1500);
                });
            }

            @Override
            public void onProgress(float percent, SpeedTestReport report) {
                Log.e(TAG, "onProgress: [Upload Progress%] " + percent);
                Log.e(TAG, "onProgress: [Upload Progress] " + Float.parseFloat(String.valueOf(report.getTransferRateBit().toBigInteger())) / 1000000);
                // called to notify download/upload progress
                System.out.println("[PROGRESS] Upload progress : " + percent + "%");
                if (upload != 0 && Float.parseFloat(String.valueOf(report.getTransferRateBit().toBigInteger())) / 1000000 < 50) {
                        appExecutors.mainThread().execute(() -> {
                            Log.e(TAG, "onProgress: upload");
                            b.speedViewUpload.speedTo(Float.parseFloat(String.valueOf(report.getTransferRateBit().toBigInteger())) / 1000000, 1000);
                            b.tvUpSpeed.setText(getString(R.string.string_string, "Upload:   ", Float.parseFloat(String.valueOf(report.getTransferRateBit().toBigInteger())) / 1000 + "Kbps"));


                        });

                }
                upload = Float.parseFloat(String.valueOf(report.getTransferRateBit().toBigInteger())) / 1000000;
                System.out.println("[PROGRESS] rate in octet/s : " + report.getTransferRateOctet());
                System.out.println("[PROGRESS] rate in bit/s   : " + report.getTransferRateBit());
            }
        });

        startSpeedTest();

    }

    private void testDownloadSpeed() {
        Log.e(TAG, "testDownloadSpeed: Attempting to start download ");
        appExecutors.networkIO().execute(() -> {
            speedTestSocket.startDownload("https://scaleway.testdebit.info/5M/5M.zip");
        });
    }

    private void testUploadSpeed() {
        Log.e(TAG, "testUploadSpeed: Attempting to start upload ");

        appExecutors.networkIO().execute(() -> {
            String fileName = SpeedTestUtils.generateFileName() + ".txt";
            speedTestSocket2.startFixedUpload("ftp://speedtest.tele2.net/upload/" + fileName, 10000000, 18000);
            //speedTestSocket.startUpload("http://ipv4.ikoula.testdebit.info/", 1000000);
        });
    }

    private float b2Kb(float bits) {
        return Math.round(Math.ceil(bits / 1000));
    }

    private String Kb2Mb(Float raw) {
        float mbps = Math.round(Math.ceil(raw / 1000));
        return String.valueOf(mbps);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_speed_test, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        if (item.getItemId() == R.id.action_refresh) {
            startSpeedTest();
        }
        return super.onOptionsItemSelected(item);
    }


    private void startSpeedTest() {

        b.tvDownloadSpeed.setText(getString(R.string.string_string, "Download: ", "0Kbps"));
        b.tvUpSpeed.setText(getString(R.string.string_string, "Upload:   ", "0Kbps"));
        isDownloadTest = true;
        testUploadSpeed();
        testDownloadSpeed();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                testDownloadSpeed();
            }
        }, 1500);
//        testDownloadSpeed();

    }

}
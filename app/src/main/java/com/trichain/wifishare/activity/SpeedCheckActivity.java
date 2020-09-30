package com.trichain.wifishare.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.trichain.wifishare.R;
import com.trichain.wifishare.databinding.ActivitySpeedCheckBinding;
import com.trichain.wifishare.util.AppExecutors;
import com.trichain.wifishare.util.CheckConnectivity;

import fr.bmartel.speedtest.SpeedTestReport;
import fr.bmartel.speedtest.SpeedTestSocket;
import fr.bmartel.speedtest.inter.ISpeedTestListener;
import fr.bmartel.speedtest.model.SpeedTestError;

public class SpeedCheckActivity extends AppCompatActivity {

    private ActivitySpeedCheckBinding b;
    private static final String TAG = "SpeedCheckActivity";
    private SpeedTestSocket speedTestSocket;
    private AppExecutors appExecutors;
    private boolean isDownloadTest = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        b = DataBindingUtil.setContentView(this, R.layout.activity_speed_check);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        appExecutors = AppExecutors.getInstance();

        speedTestSocket = new SpeedTestSocket();

        b.tvWiFiNameSpeed.setText(CheckConnectivity.getWiFiName(this));
        b.tvDownloadSpeed.setText(getString(R.string.string_string, "Download: ", "0Kbps"));
        b.tvUpSpeed.setText(getString(R.string.string_string, "Upload:   ", "0Kbps"));

        // add a listener to wait for speedtest completion and progress
        speedTestSocket.addSpeedTestListener(new ISpeedTestListener() {

            @Override
            public void onCompletion(SpeedTestReport report) {
                Log.e(TAG, "onCompletion: [Complete] ");
                // called when download/upload is complete
                System.out.println("[COMPLETED] rate in octet/s : " + report.getTransferRateOctet());
                System.out.println("[COMPLETED] rate in bit/s   : " + report.getTransferRateBit());
                appExecutors.mainThread().execute(() -> {
                    b.speedView.speedTo(0, 100);

                    if (isDownloadTest) {
                        b.tvDownloadSpeed.setText(getString(R.string.string_string, "Download: ", Float.parseFloat(String.valueOf(report.getTransferRateBit().toBigInteger())) / 1000 + "Kbps"));
                        isDownloadTest = false;
                        testUploadSpeed();
                    } else {
                        b.tvUpSpeed.setText(getString(R.string.string_string, "Upload:   ", Float.parseFloat(String.valueOf(report.getTransferRateBit().toBigInteger())) / 1000 + "Kbps"));
                    }

                });

            }

            @Override
            public void onError(SpeedTestError speedTestError, String errorMessage) {
                // called when a download/upload error occur
                Log.e(TAG, "onError: Network Error occurred -> " + errorMessage);

                appExecutors.mainThread().execute(() -> {
                    b.speedView.speedTo(0, 100);
                });
            }

            @Override
            public void onProgress(float percent, SpeedTestReport report) {
                Log.e(TAG, "onProgress: [Progress] " + percent);
                // called to notify download/upload progress
                System.out.println("[PROGRESS] progress : " + percent + "%");
                appExecutors.mainThread().execute(() -> {
                    b.speedView.speedTo(Float.parseFloat(String.valueOf(report.getTransferRateBit().toBigInteger())) / 1000, 700);
                    if (isDownloadTest) {
                        b.tvDownloadSpeed.setText(getString(R.string.string_string, "Download: ", Float.parseFloat(String.valueOf(report.getTransferRateBit().toBigInteger())) / 1000 + "Kbps"));
                    } else {
                        b.tvUpSpeed.setText(getString(R.string.string_string, "Upload:   ", Float.parseFloat(String.valueOf(report.getTransferRateBit().toBigInteger())) / 1000 + "Kbps"));
                    }

                });
                System.out.println("[PROGRESS] rate in octet/s : " + report.getTransferRateOctet());
                System.out.println("[PROGRESS] rate in bit/s   : " + report.getTransferRateBit());
            }
        });

        startSpeedTest();

    }

    private void testDownloadSpeed() {
        Log.e(TAG, "testDownloadSpeed: Attempting to start download ");
        appExecutors.networkIO().execute(() -> {
            speedTestSocket.startDownload("https://scaleway.testdebit.info/1M/1M.zip");
        });
    }

    private void testUploadSpeed() {
        Log.e(TAG, "testUploadSpeed: Attempting to start upload ");

        appExecutors.networkIO().execute(() -> {
            speedTestSocket.startUpload("http://ipv4.ikoula.testdebit.info/", 1000000);
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
        if (item.getItemId() == R.id.action_refresh){
            startSpeedTest();
        }
        return super.onOptionsItemSelected(item);
    }


    private void startSpeedTest() {

        b.tvDownloadSpeed.setText(getString(R.string.string_string, "Download: ", "0Kbps"));
        b.tvUpSpeed.setText(getString(R.string.string_string, "Upload:   ", "0Kbps"));

        testDownloadSpeed();

    }

}
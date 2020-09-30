package com.trichain.wifishare.listeners;

import java.util.List;

public interface ScanResultsInterface {
    void onScanResultsAvailable(List<ScanResultsInterface> scanResultInterfaces);
}

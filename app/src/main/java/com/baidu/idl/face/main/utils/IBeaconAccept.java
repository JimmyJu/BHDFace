package com.baidu.idl.face.main.utils;

public class IBeaconAccept {

    public IBeaconInfo getIBeaconInfo(byte[] data, int rssi) {

        IBeaconInfo iBeaconInfo = new IBeaconInfo();
        if (data == null) {
            return iBeaconInfo;
        }
        int startByte = 2;
        boolean patternFound = true;
        // 寻找ibeacon
        while (startByte <= 5) {
            if (((int) data[startByte + 2] & 0xff) == 0x02 && // Identifies
                    // an
                    // iBeacon
                    ((int) data[startByte + 3] & 0xff) == 0x15) { // Identifies
                // correct
                // data
                // length
                patternFound = true;
                break;
            }
            startByte++;
        }
        // 假设找到了的话
        if (patternFound) {
            // 转换为16进制
            byte[] uuidBytes = new byte[16];
            System.arraycopy(data, startByte + 4, uuidBytes, 0, 16);
            String hexString = ByteUtils.bytesToHex(uuidBytes);

            // ibeacon的UUID值
            String uuid = hexString.substring(0, 8) + "-"
                    + hexString.substring(8, 12) + "-"
                    + hexString.substring(12, 16) + "-"
                    + hexString.substring(16, 20) + "-"
                    + hexString.substring(20, 32);

            // ibeacon的Major值
            int major = (data[startByte + 20] & 0xff) * 0x100
                    + (data[startByte + 21] & 0xff);

            // ibeacon的Minor值
            int minor = (data[startByte + 22] & 0xff) * 0x100
                    + (data[startByte + 23] & 0xff);

            int txPower = (data[startByte + 24]);
            iBeaconInfo.uuid = uuid;
            iBeaconInfo.major = major;
            iBeaconInfo.minor = minor;
            iBeaconInfo.txPower = txPower;
            iBeaconInfo.rssi = rssi;
            iBeaconInfo.accuracy = calculateAccuracy(txPower, rssi);
        }
        return iBeaconInfo;
    }

    public double calculateAccuracy(int txPower, double rssi) {
        if (rssi == 0) {
            return -1.0;
        }
        double ratio = rssi * 1.0 / txPower;
        if (ratio < 1.0) {
            return Math.pow(ratio, 10);
        } else {
            double accuracy = (0.89976) * Math.pow(ratio, 7.7095) + 0.111;
            return accuracy;
        }
    }

    public static class IBeaconInfo {

        public String uuid;
        public int major;
        public int txPower;
        public int minor;
        public int rssi;
        public double accuracy;

        @Override
        public String toString() {
            return "IBeaconInfo{" +
                    "uuid='" + uuid + '\'' +
                    ", major=" + major +
                    ", txPower=" + txPower +
                    ", minor=" + minor +
                    ", rssi=" + rssi +
                    ", accuracy=" + accuracy +
                    '}';
        }
    }
}

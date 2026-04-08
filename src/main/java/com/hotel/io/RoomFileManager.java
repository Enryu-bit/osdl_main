package com.hotel.io;

import java.io.*;


public class RoomFileManager {

    private static final int RECORD_SIZE  = 128;
    private static final int ROOM_ID_LEN  = 20; // chars (40 bytes)
    private static final int STATUS_LEN   = 20; // chars (40 bytes)

    private final String filePath;

    public RoomFileManager(String filePath) {
        this.filePath = filePath;
        ensureDirectoryExists(filePath);
    }

  
    public void writeRecord(int index, String roomId, String status, double price)
            throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "rw")) {
            long position = (long) index * RECORD_SIZE;
            raf.seek(position);                              // JUMP to byte position

            raf.writeChars(pad(roomId, ROOM_ID_LEN));       // 40 bytes
            raf.writeChars(pad(status,  STATUS_LEN));        // 40 bytes
            raf.writeDouble(price);                          //  8 bytes
            raf.write(new byte[40]);                         // 40 bytes padding
        }
    }


    public String[] readRecord(int index) throws IOException {
        File f = new File(filePath);
        if (!f.exists()) return null;

        try (RandomAccessFile raf = new RandomAccessFile(filePath, "r")) {
            long position = (long) index * RECORD_SIZE;
            if (position >= raf.length()) return null;

            raf.seek(position);
            String roomId = readChars(raf, ROOM_ID_LEN).trim();
            String status = readChars(raf, STATUS_LEN).trim();
            double price  = raf.readDouble();

            return new String[]{roomId, status, String.valueOf(price)};
        }
    }

   
    public void updateStatus(int index, String newStatus) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(filePath, "rw")) {
            // Skip past roomId field (20 chars × 2 bytes each = 40 bytes)
            long statusOffset = (long) index * RECORD_SIZE + (ROOM_ID_LEN * 2L);
            raf.seek(statusOffset);
            raf.writeChars(pad(newStatus, STATUS_LEN));      // overwrite only status
        }
    }

    /** Total number of records currently in the file */
    public int getTotalRecords() throws IOException {
        File f = new File(filePath);
        if (!f.exists()) return 0;
        return (int) (f.length() / RECORD_SIZE);
    }

    // -------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------

    /** Pad or truncate string to exactly [length] characters */
    private String pad(String s, int length) {
        if (s.length() >= length) return s.substring(0, length);
        return s + " ".repeat(length - s.length());
    }

    /** Read [count] chars from RandomAccessFile (each char = 2 bytes) */
    private String readChars(RandomAccessFile raf, int count) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < count; i++) {
            sb.append(raf.readChar()); // readChar reads 2 bytes → 1 char
        }
        return sb.toString();
    }

    private void ensureDirectoryExists(String path) {
        File parent = new File(path).getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();
    }

    public String getFilePath() { return filePath; }
}

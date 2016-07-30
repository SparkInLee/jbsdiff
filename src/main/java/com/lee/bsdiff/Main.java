package com.lee.bsdiff;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Main {
    private static final String TAG = "BSDIFF";

    public static void main(String[] args) {
        if (args.length < 5) {
            usage();
            return;
        }
        System.out.println(new File("1").getAbsolutePath());
        if (args[0].equals("bsdiff")) {
            String oldFileName = null;
            String newFileName = null;
            String patchFileName = null;
            for (int i = 1; i < args.length; ++i) {
                if (args[i].equals("-o")) {
                    oldFileName = args[++i];
                } else if (args[i].equals("-n")) {
                    newFileName = args[++i];
                } else if (args[i].equals("-p")) {
                    patchFileName = args[++i];
                }
            }
            if (null == oldFileName || null == newFileName) {
                usage();
            } else {
                long startTime = System.currentTimeMillis();
                BsDiff bsdiff = new BsDiff(oldFileName, newFileName);
                patchFileName = bsdiff.bsdiff(patchFileName);
                long endTime = System.currentTimeMillis();
                Logger.i(TAG, "bsdiff successfully {time = " + (endTime - startTime) / 1000 + "s, file = "
                        + patchFileName + "}");
            }
        } else if (args[0].equals("bspatch")) {
            String oldFileName = null;
            String newFileName = null;
            String patchFileName = null;
            for (int i = 1; i < args.length; ++i) {
                if (args[i].equals("-o")) {
                    oldFileName = args[++i];
                } else if (args[i].equals("-n")) {
                    newFileName = args[++i];
                } else if (args[i].equals("-p")) {
                    patchFileName = args[++i];
                }
            }
            if (null == oldFileName || null == patchFileName) {
                usage();
            } else {
                long startTime = System.currentTimeMillis();
                BsPatch bsPatch = new BsPatch(oldFileName, patchFileName);
                patchFileName = bsPatch.bsPatch(newFileName);
                long endTime = System.currentTimeMillis();
                Logger.i(TAG, "bspatch successfully {time = " + (endTime - startTime) / 1000 + "s, file = "
                        + patchFileName + "}");
            }
        } else if (args[0].equals("check")) {
            String newFileName = null;
            String patchFileName = null;
            for (int i = 1; i < args.length; ++i) {
                if (args[i].equals("-nf")) {
                    newFileName = args[++i];
                } else if (args[i].equals("-pf")) {
                    patchFileName = args[++i];
                }
            }
            if (null == newFileName || null == patchFileName) {
                usage();
            } else {
                check(newFileName, patchFileName);
            }
        } else {
            usage();
        }
    }

    public static boolean check(String file1, String file2) {
        FileInputStream in = null;
        try {
            in = new FileInputStream(file1);
            byte[] newData = new byte[in.available()];
            in.read(newData);
            in.close();

            in = new FileInputStream(file2);
            byte[] patchData = new byte[in.available()];
            in.read(patchData);
            in.close();
            in = null;

            if (newData.length == patchData.length) {
                for (int i = 0; i < newData.length; ++i) {
                    if (newData[i] != patchData[i]) {
                        Logger.i(TAG, "check failed, failed index : " + i);
                        return false;
                    }
                }
                Logger.i(TAG, "check successfully.");
                return true;
            } else {
                Logger.i(TAG, "check failed, length not equal.");
                return false;
            }
        } catch (Throwable e) {
            Logger.e(TAG, e);
            throw new RuntimeException(e);
        } finally {
            try {
                if (null != in) {
                    in.close();
                }
            } catch (IOException e) {
                Logger.w(TAG, e);
            }
        }
    }

    private static final String USAGE = "Usage:\n" + "\tbsdiff -o oldFile -n newFile [-p patchFile]\n"
            + "\tbspatch -o oldFile -p patchFile [-n newFile]\n" + "\tcheck -nf newFile -pf patchFile\n";

    private static final void usage() {
        System.out.println(USAGE);
    }
}

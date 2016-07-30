package com.lee.bsdiff;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.tools.bzip2.CBZip2InputStream;

public class BsPatch {

	private static final String TAG = "BsPatch";

	private File oldFile = null;

	private File patchFile = null;

	public BsPatch(String oldFileName, String patchFileName) {
		if (null == oldFileName || null == patchFileName) {
			throw new IllegalArgumentException("old file name or patch file name is null.");
		}
		oldFile = new File(oldFileName);
		if (!oldFile.exists() || !oldFile.isFile()) {
			throw new IllegalArgumentException("old file not exists.");
		}

		patchFile = new File(patchFileName);
		if (!patchFile.exists() || !patchFile.isFile()) {
			throw new IllegalArgumentException("patch file not exists.");
		}
	}

	@SuppressWarnings("resource")
	public String bsPatch(String newFileName) {
		File newFile = null;
		if (null == newFileName || newFileName.length() == 0) {
			int i = 1;
			do {
				newFileName = "new" + i + "_" + oldFile.getName();
				newFile = new File(newFileName);
				++i;
				if (i == Integer.MAX_VALUE) {
					throw new IllegalStateException("can not create new file.");
				}
			} while (newFile.exists());
		} else {
			newFile = new File(newFileName);
		}

		InputStream in = null;
		OutputStream out = null;
		try {
			in = new FileInputStream(oldFile);
			byte[] oldData = new byte[in.available()];
			in.read(oldData);
			in.close();
			
			in = new CBZip2InputStream(new FileInputStream(patchFile));
			File temp = new File("patch_temp" + System.currentTimeMillis());
			out = new FileOutputStream(temp);
			byte[] buf = new byte[1024];
			int len = -1;
			while ((len = in.read(buf, 0, buf.length)) != -1) {
				out.write(buf, 0, len);
			}
			in.close();
			out.close();

			in = new FileInputStream(temp);
			byte[] patchData = new byte[in.available()];
			in.read(patchData);
			in.close();
			temp.delete();

			out = new FileOutputStream(newFile);

			byte[] header = "ENDSLEY/BSDIFF43".getBytes();
			int pos = 0;
			int oldPos = 0;
			for (; pos < header.length; ++pos) {
				if (header[pos] != patchData[pos]) {
					throw new IllegalStateException("patch file is corrupted.");
				}
			}

			int newFileSize = readSize(patchData, pos);
			pos += 4;

			byte[] data = null;
			while (pos < patchData.length) {
				int diffLen = readSize(patchData, pos);
				pos += 4;
				int extraLen = readSize(patchData, pos);
				pos += 4;
				int offset = readSize(patchData, pos);
				pos += 4;

				data = new byte[diffLen];
				for (int i = 0; i < diffLen; ++i) {
					data[i] = (byte) toNormal(toPositive(oldData[oldPos++]) - patchData[pos++]);
				}
				out.write(data);

				data = new byte[extraLen];
				for (int i = 0; i < extraLen; ++i) {
					data[i] = patchData[pos++];
				}
				out.write(data);

				oldPos += offset;
			}

			if (newFile.length() != newFileSize) {
				throw new IllegalStateException("size of new file not right.");
			}

			out.close();
			out = null;
		} catch (Throwable e) {
			Logger.e(TAG, e);
			throw new RuntimeException(e);
		} finally {
			try {
				if (null != in) {
					in.close();
				}
				if (null != out) {
					out.close();
				}
			} catch (IOException e) {
				Logger.w(TAG, e);
			}
		}

		return newFileName;
	}

	private int readSize(byte[] data, int pos) {
		int size;

		size = data[pos + 3] & 0x7F;
		size = size * 256;
		size += data[pos + 2];
		size = size * 256;
		size += data[pos + 1];
		size = size * 256;
		size += data[pos + 0];

		if ((data[pos + 3] & 0x80) != 0) {
			size = -size;
		}

		return size;
	}

	private int toPositive(byte b) {
		return b + 128;
	}

	private int toNormal(int b) {
		return b - 128;
	}
}

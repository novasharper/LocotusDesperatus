package daedalus.util;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Util {
	private static File workdir = null;

	public enum OS {
		linux, solaris, windows, mac, other;

		public boolean x64 = false;
	}

	public static OS getOS() {
		String osType = System.getProperty("os.name").toLowerCase();
		String osArch = System.getProperty("os.arch");

		OS type;

		if (osType.contains("win"))
			type = OS.windows;
		else if (osType.contains("mac"))
			type = OS.mac;
		else if (osType.contains("solaris"))
			type = OS.solaris;
		else if (osType.contains("sunos"))
			type = OS.solaris;
		else if (osType.contains("linux"))
			type = OS.linux;
		else if (osType.contains("unix"))
			type = OS.linux;
		else
			type = OS.other;

		if (osArch.contains("64"))
			type.x64 = true;

		return type;
	}

	public static File getWorkingDir() {
		if (workdir == null)
			workdir = getWorkingDir("daedalus");
		return workdir;
	}

	public static File getWorkingDir(String appname) {
		String userHome = System.getProperty("user.home", ".");
		File workingDir;
		switch (getOS().ordinal()) {
		case 0:
		case 1:
			workingDir = new File(userHome, '.' + appname + File.separatorChar);
			break;
		case 2:
			String appData = System.getenv("APPDATA");
			if (appData != null)
				workingDir = new File(appData, "." + appname
						+ File.separatorChar);
			else
				workingDir = new File(userHome, "." + appname
						+ File.separatorChar);
			break;
		case 3:
			workingDir = new File(userHome, "Library/Application Support/"
					+ appname + File.separatorChar);
			break;
		default:
			workingDir = new File(userHome, appname + File.separatorChar);
			break;
		}
		return workingDir;
	}
	
	public static String getExtension(String fname) {
		int mid = fname.lastIndexOf('.');
		String ext = "";
		if(mid > 0 && mid < fname.length() - 1) ext = fname.substring(mid + 1);
		return ext;
	}
	
	public static double angleDifference(double a, double b) {
		return Math.abs((a + Math.PI - b) % (Math.PI * 2) - Math.PI);
	}
	
	public static void extractZipNative(File zipFile) {
		try {
			String dest = zipFile.getParent() + File.separatorChar;
			System.out.println(dest);
			byte[] buf = new byte[1024];
			ZipInputStream in = null;
			ZipEntry zipentry;
			in = new ZipInputStream(new FileInputStream(zipFile));

			zipentry = in.getNextEntry();
			while (zipentry != null) {
				// for each entry to be extracted
				String entryName = dest + zipentry.getName();
				entryName = entryName.replace('/', File.separatorChar);
				entryName = entryName.replace('\\', File.separatorChar);
				int n;
				FileOutputStream out;
				File newFile = new File(entryName);
				if (zipentry.isDirectory()) {
					if (!newFile.mkdirs()) {
						break;
					}
					zipentry = in.getNextEntry();
					continue;
				}

				out = new FileOutputStream(entryName);

				while ((n = in.read(buf, 0, 1024)) > -1) {
					out.write(buf, 0, n);
				}

				out.close();
				in.closeEntry();
				zipentry = in.getNextEntry();

			}// while

			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

package de.mephisto.vpin.commons.utils;

import java.io.File;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Version;
import com.sun.jna.platform.win32.VerRsrc.VS_FIXEDFILEINFO;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * An utilities to extract the product version from an exe 
 * https://stackoverflow.com/questions/6918022/get-version-info-for-exe
 */
public class FileVersion {

  public static String fetch(File exe)  {
    if (!exe.exists()) {
      return null;
    }

    IntByReference dwDummy = new IntByReference();
    dwDummy.setValue(0);
    int versionlength = Version.INSTANCE.GetFileVersionInfoSize(exe.getAbsolutePath(), dwDummy);

    byte[] bufferarray = new byte[versionlength];
    Pointer lpData = new Memory(bufferarray.length);
    PointerByReference lplpBuffer = new PointerByReference();
    IntByReference puLen = new IntByReference();

    @SuppressWarnings("unused")
		boolean fileInfoResult = Version.INSTANCE.GetFileVersionInfo(exe.getAbsolutePath(), 0, versionlength, lpData);
    @SuppressWarnings("unused")
    boolean verQueryVal = Version.INSTANCE.VerQueryValue(lpData, "\\", lplpBuffer, puLen);

    VS_FIXEDFILEINFO lplpBufStructure = new VS_FIXEDFILEINFO(lplpBuffer.getValue());
    lplpBufStructure.read();

    int v1 = (lplpBufStructure.dwFileVersionMS).intValue() >> 16;
    int v2 = (lplpBufStructure.dwFileVersionMS).intValue() & 0xffff;
    int v3 = (lplpBufStructure.dwFileVersionLS).intValue() >> 16;
    int v4 = (lplpBufStructure.dwFileVersionLS).intValue() & 0xffff;

    return String.valueOf(v1) + "." +
           String.valueOf(v2) + "." +
           String.valueOf(v3) + "." +
           String.valueOf(v4);
  }

  public static void main(String[] args)  {
    File file = new File("C:\\B2SServer\\B2SBackglassServerEXE.exe");
    System.out.println("version B2S = " + FileVersion.fetch(file));

    file = new File("C:\\Visual Pinball\\VPinballX64.exe");
    System.out.println("version PBX = " + FileVersion.fetch(file));
  }
}
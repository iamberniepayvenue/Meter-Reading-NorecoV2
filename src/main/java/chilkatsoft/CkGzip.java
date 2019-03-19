/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 3.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package com.chilkatsoft;

public class CkGzip {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected CkGzip(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(CkGzip obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        chilkatJNI.delete_CkGzip(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public CkGzip() {
    this(chilkatJNI.new_CkGzip(), true);
  }

  public void LastErrorXml(CkString str) {
    chilkatJNI.CkGzip_LastErrorXml(swigCPtr, this, CkString.getCPtr(str), str);
  }

  public void LastErrorHtml(CkString str) {
    chilkatJNI.CkGzip_LastErrorHtml(swigCPtr, this, CkString.getCPtr(str), str);
  }

  public void LastErrorText(CkString str) {
    chilkatJNI.CkGzip_LastErrorText(swigCPtr, this, CkString.getCPtr(str), str);
  }

  public void put_EventCallbackObject(CkBaseProgress progress) {
    chilkatJNI.CkGzip_put_EventCallbackObject(swigCPtr, this, CkBaseProgress.getCPtr(progress), progress);
  }

  public boolean get_AbortCurrent() {
    return chilkatJNI.CkGzip_get_AbortCurrent(swigCPtr, this);
  }

  public void put_AbortCurrent(boolean newVal) {
    chilkatJNI.CkGzip_put_AbortCurrent(swigCPtr, this, newVal);
  }

  public void get_Comment(CkString str) {
    chilkatJNI.CkGzip_get_Comment(swigCPtr, this, CkString.getCPtr(str), str);
  }

  public String comment() {
    return chilkatJNI.CkGzip_comment(swigCPtr, this);
  }

  public void put_Comment(String newVal) {
    chilkatJNI.CkGzip_put_Comment(swigCPtr, this, newVal);
  }

  public int get_CompressionLevel() {
    return chilkatJNI.CkGzip_get_CompressionLevel(swigCPtr, this);
  }

  public void put_CompressionLevel(int newVal) {
    chilkatJNI.CkGzip_put_CompressionLevel(swigCPtr, this, newVal);
  }

  public void get_DebugLogFilePath(CkString str) {
    chilkatJNI.CkGzip_get_DebugLogFilePath(swigCPtr, this, CkString.getCPtr(str), str);
  }

  public String debugLogFilePath() {
    return chilkatJNI.CkGzip_debugLogFilePath(swigCPtr, this);
  }

  public void put_DebugLogFilePath(String newVal) {
    chilkatJNI.CkGzip_put_DebugLogFilePath(swigCPtr, this, newVal);
  }

  public void get_ExtraData(CkByteData outBytes) {
    chilkatJNI.CkGzip_get_ExtraData(swigCPtr, this, CkByteData.getCPtr(outBytes), outBytes);
  }

  public void put_ExtraData(CkByteData inBytes) {
    chilkatJNI.CkGzip_put_ExtraData(swigCPtr, this, CkByteData.getCPtr(inBytes), inBytes);
  }

  public void get_Filename(CkString str) {
    chilkatJNI.CkGzip_get_Filename(swigCPtr, this, CkString.getCPtr(str), str);
  }

  public String filename() {
    return chilkatJNI.CkGzip_filename(swigCPtr, this);
  }

  public void put_Filename(String newVal) {
    chilkatJNI.CkGzip_put_Filename(swigCPtr, this, newVal);
  }

  public int get_HeartbeatMs() {
    return chilkatJNI.CkGzip_get_HeartbeatMs(swigCPtr, this);
  }

  public void put_HeartbeatMs(int newVal) {
    chilkatJNI.CkGzip_put_HeartbeatMs(swigCPtr, this, newVal);
  }

  public void get_LastErrorHtml(CkString str) {
    chilkatJNI.CkGzip_get_LastErrorHtml(swigCPtr, this, CkString.getCPtr(str), str);
  }

  public String lastErrorHtml() {
    return chilkatJNI.CkGzip_lastErrorHtml(swigCPtr, this);
  }

  public void get_LastErrorText(CkString str) {
    chilkatJNI.CkGzip_get_LastErrorText(swigCPtr, this, CkString.getCPtr(str), str);
  }

  public String lastErrorText() {
    return chilkatJNI.CkGzip_lastErrorText(swigCPtr, this);
  }

  public void get_LastErrorXml(CkString str) {
    chilkatJNI.CkGzip_get_LastErrorXml(swigCPtr, this, CkString.getCPtr(str), str);
  }

  public String lastErrorXml() {
    return chilkatJNI.CkGzip_lastErrorXml(swigCPtr, this);
  }

  public boolean get_LastMethodSuccess() {
    return chilkatJNI.CkGzip_get_LastMethodSuccess(swigCPtr, this);
  }

  public void put_LastMethodSuccess(boolean newVal) {
    chilkatJNI.CkGzip_put_LastMethodSuccess(swigCPtr, this, newVal);
  }

  public void get_LastMod(SYSTEMTIME outSysTime) {
    chilkatJNI.CkGzip_get_LastMod(swigCPtr, this, SYSTEMTIME.getCPtr(outSysTime), outSysTime);
  }

  public void put_LastMod(SYSTEMTIME sysTime) {
    chilkatJNI.CkGzip_put_LastMod(swigCPtr, this, SYSTEMTIME.getCPtr(sysTime), sysTime);
  }

  public void get_LastModStr(CkString str) {
    chilkatJNI.CkGzip_get_LastModStr(swigCPtr, this, CkString.getCPtr(str), str);
  }

  public String lastModStr() {
    return chilkatJNI.CkGzip_lastModStr(swigCPtr, this);
  }

  public void put_LastModStr(String newVal) {
    chilkatJNI.CkGzip_put_LastModStr(swigCPtr, this, newVal);
  }

  public boolean get_UseCurrentDate() {
    return chilkatJNI.CkGzip_get_UseCurrentDate(swigCPtr, this);
  }

  public void put_UseCurrentDate(boolean newVal) {
    chilkatJNI.CkGzip_put_UseCurrentDate(swigCPtr, this, newVal);
  }

  public boolean get_VerboseLogging() {
    return chilkatJNI.CkGzip_get_VerboseLogging(swigCPtr, this);
  }

  public void put_VerboseLogging(boolean newVal) {
    chilkatJNI.CkGzip_put_VerboseLogging(swigCPtr, this, newVal);
  }

  public void get_Version(CkString str) {
    chilkatJNI.CkGzip_get_Version(swigCPtr, this, CkString.getCPtr(str), str);
  }

  public String version() {
    return chilkatJNI.CkGzip_version(swigCPtr, this);
  }

  public boolean CompressBd(CkBinData binDat) {
    return chilkatJNI.CkGzip_CompressBd(swigCPtr, this, CkBinData.getCPtr(binDat), binDat);
  }

  public CkTask CompressBdAsync(CkBinData binDat) {
    long cPtr = chilkatJNI.CkGzip_CompressBdAsync(swigCPtr, this, CkBinData.getCPtr(binDat), binDat);
    return (cPtr == 0) ? null : new CkTask(cPtr, true);
  }

  public boolean CompressFile(String inFilename, String destPath) {
    return chilkatJNI.CkGzip_CompressFile(swigCPtr, this, inFilename, destPath);
  }

  public CkTask CompressFileAsync(String inFilename, String destPath) {
    long cPtr = chilkatJNI.CkGzip_CompressFileAsync(swigCPtr, this, inFilename, destPath);
    return (cPtr == 0) ? null : new CkTask(cPtr, true);
  }

  public boolean CompressFile2(String inFilename, String embeddedFilename, String destPath) {
    return chilkatJNI.CkGzip_CompressFile2(swigCPtr, this, inFilename, embeddedFilename, destPath);
  }

  public CkTask CompressFile2Async(String inFilename, String embeddedFilename, String destPath) {
    long cPtr = chilkatJNI.CkGzip_CompressFile2Async(swigCPtr, this, inFilename, embeddedFilename, destPath);
    return (cPtr == 0) ? null : new CkTask(cPtr, true);
  }

  public boolean CompressFileToMem(String inFilename, CkByteData outData) {
    return chilkatJNI.CkGzip_CompressFileToMem(swigCPtr, this, inFilename, CkByteData.getCPtr(outData), outData);
  }

  public CkTask CompressFileToMemAsync(String inFilename) {
    long cPtr = chilkatJNI.CkGzip_CompressFileToMemAsync(swigCPtr, this, inFilename);
    return (cPtr == 0) ? null : new CkTask(cPtr, true);
  }

  public boolean CompressMemory(CkByteData inData, CkByteData outData) {
    return chilkatJNI.CkGzip_CompressMemory(swigCPtr, this, CkByteData.getCPtr(inData), inData, CkByteData.getCPtr(outData), outData);
  }

  public CkTask CompressMemoryAsync(CkByteData inData) {
    long cPtr = chilkatJNI.CkGzip_CompressMemoryAsync(swigCPtr, this, CkByteData.getCPtr(inData), inData);
    return (cPtr == 0) ? null : new CkTask(cPtr, true);
  }

  public boolean CompressMemToFile(CkByteData inData, String destPath) {
    return chilkatJNI.CkGzip_CompressMemToFile(swigCPtr, this, CkByteData.getCPtr(inData), inData, destPath);
  }

  public CkTask CompressMemToFileAsync(CkByteData inData, String destPath) {
    long cPtr = chilkatJNI.CkGzip_CompressMemToFileAsync(swigCPtr, this, CkByteData.getCPtr(inData), inData, destPath);
    return (cPtr == 0) ? null : new CkTask(cPtr, true);
  }

  public boolean CompressString(String inStr, String destCharset, CkByteData outBytes) {
    return chilkatJNI.CkGzip_CompressString(swigCPtr, this, inStr, destCharset, CkByteData.getCPtr(outBytes), outBytes);
  }

  public CkTask CompressStringAsync(String inStr, String destCharset) {
    long cPtr = chilkatJNI.CkGzip_CompressStringAsync(swigCPtr, this, inStr, destCharset);
    return (cPtr == 0) ? null : new CkTask(cPtr, true);
  }

  public boolean CompressStringENC(String inStr, String charset, String encoding, CkString outStr) {
    return chilkatJNI.CkGzip_CompressStringENC(swigCPtr, this, inStr, charset, encoding, CkString.getCPtr(outStr), outStr);
  }

  public String compressStringENC(String inStr, String charset, String encoding) {
    return chilkatJNI.CkGzip_compressStringENC(swigCPtr, this, inStr, charset, encoding);
  }

  public boolean CompressStringToFile(String inStr, String destCharset, String destPath) {
    return chilkatJNI.CkGzip_CompressStringToFile(swigCPtr, this, inStr, destCharset, destPath);
  }

  public CkTask CompressStringToFileAsync(String inStr, String destCharset, String destPath) {
    long cPtr = chilkatJNI.CkGzip_CompressStringToFileAsync(swigCPtr, this, inStr, destCharset, destPath);
    return (cPtr == 0) ? null : new CkTask(cPtr, true);
  }

  public boolean Decode(String encodedStr, String encoding, CkByteData outBytes) {
    return chilkatJNI.CkGzip_Decode(swigCPtr, this, encodedStr, encoding, CkByteData.getCPtr(outBytes), outBytes);
  }

  public boolean DeflateStringENC(String inString, String charsetName, String outputEncoding, CkString outStr) {
    return chilkatJNI.CkGzip_DeflateStringENC(swigCPtr, this, inString, charsetName, outputEncoding, CkString.getCPtr(outStr), outStr);
  }

  public String deflateStringENC(String inString, String charsetName, String outputEncoding) {
    return chilkatJNI.CkGzip_deflateStringENC(swigCPtr, this, inString, charsetName, outputEncoding);
  }

  public boolean Encode(CkByteData byteData, String encoding, CkString outStr) {
    return chilkatJNI.CkGzip_Encode(swigCPtr, this, CkByteData.getCPtr(byteData), byteData, encoding, CkString.getCPtr(outStr), outStr);
  }

  public String encode(CkByteData byteData, String encoding) {
    return chilkatJNI.CkGzip_encode(swigCPtr, this, CkByteData.getCPtr(byteData), byteData, encoding);
  }

  public boolean ExamineFile(String inGzFilename) {
    return chilkatJNI.CkGzip_ExamineFile(swigCPtr, this, inGzFilename);
  }

  public boolean ExamineMemory(CkByteData inGzData) {
    return chilkatJNI.CkGzip_ExamineMemory(swigCPtr, this, CkByteData.getCPtr(inGzData), inGzData);
  }

  public CkDateTime GetDt() {
    long cPtr = chilkatJNI.CkGzip_GetDt(swigCPtr, this);
    return (cPtr == 0) ? null : new CkDateTime(cPtr, true);
  }

  public boolean InflateStringENC(String inString, String convertFromCharset, String inputEncoding, CkString outStr) {
    return chilkatJNI.CkGzip_InflateStringENC(swigCPtr, this, inString, convertFromCharset, inputEncoding, CkString.getCPtr(outStr), outStr);
  }

  public String inflateStringENC(String inString, String convertFromCharset, String inputEncoding) {
    return chilkatJNI.CkGzip_inflateStringENC(swigCPtr, this, inString, convertFromCharset, inputEncoding);
  }

  public boolean IsUnlocked() {
    return chilkatJNI.CkGzip_IsUnlocked(swigCPtr, this);
  }

  public boolean ReadFile(String path, CkByteData outBytes) {
    return chilkatJNI.CkGzip_ReadFile(swigCPtr, this, path, CkByteData.getCPtr(outBytes), outBytes);
  }

  public boolean SaveLastError(String path) {
    return chilkatJNI.CkGzip_SaveLastError(swigCPtr, this, path);
  }

  public boolean SetDt(CkDateTime dt) {
    return chilkatJNI.CkGzip_SetDt(swigCPtr, this, CkDateTime.getCPtr(dt), dt);
  }

  public boolean UncompressBd(CkBinData binDat) {
    return chilkatJNI.CkGzip_UncompressBd(swigCPtr, this, CkBinData.getCPtr(binDat), binDat);
  }

  public CkTask UncompressBdAsync(CkBinData binDat) {
    long cPtr = chilkatJNI.CkGzip_UncompressBdAsync(swigCPtr, this, CkBinData.getCPtr(binDat), binDat);
    return (cPtr == 0) ? null : new CkTask(cPtr, true);
  }

  public boolean UncompressFile(String srcPath, String destPath) {
    return chilkatJNI.CkGzip_UncompressFile(swigCPtr, this, srcPath, destPath);
  }

  public CkTask UncompressFileAsync(String srcPath, String destPath) {
    long cPtr = chilkatJNI.CkGzip_UncompressFileAsync(swigCPtr, this, srcPath, destPath);
    return (cPtr == 0) ? null : new CkTask(cPtr, true);
  }

  public boolean UncompressFileToMem(String inFilename, CkByteData outData) {
    return chilkatJNI.CkGzip_UncompressFileToMem(swigCPtr, this, inFilename, CkByteData.getCPtr(outData), outData);
  }

  public CkTask UncompressFileToMemAsync(String inFilename) {
    long cPtr = chilkatJNI.CkGzip_UncompressFileToMemAsync(swigCPtr, this, inFilename);
    return (cPtr == 0) ? null : new CkTask(cPtr, true);
  }

  public boolean UncompressFileToString(String gzFilename, String charset, CkString outStr) {
    return chilkatJNI.CkGzip_UncompressFileToString(swigCPtr, this, gzFilename, charset, CkString.getCPtr(outStr), outStr);
  }

  public String uncompressFileToString(String gzFilename, String charset) {
    return chilkatJNI.CkGzip_uncompressFileToString(swigCPtr, this, gzFilename, charset);
  }

  public CkTask UncompressFileToStringAsync(String gzFilename, String charset) {
    long cPtr = chilkatJNI.CkGzip_UncompressFileToStringAsync(swigCPtr, this, gzFilename, charset);
    return (cPtr == 0) ? null : new CkTask(cPtr, true);
  }

  public boolean UncompressMemory(CkByteData inData, CkByteData outData) {
    return chilkatJNI.CkGzip_UncompressMemory(swigCPtr, this, CkByteData.getCPtr(inData), inData, CkByteData.getCPtr(outData), outData);
  }

  public CkTask UncompressMemoryAsync(CkByteData inData) {
    long cPtr = chilkatJNI.CkGzip_UncompressMemoryAsync(swigCPtr, this, CkByteData.getCPtr(inData), inData);
    return (cPtr == 0) ? null : new CkTask(cPtr, true);
  }

  public boolean UncompressMemToFile(CkByteData inData, String destPath) {
    return chilkatJNI.CkGzip_UncompressMemToFile(swigCPtr, this, CkByteData.getCPtr(inData), inData, destPath);
  }

  public CkTask UncompressMemToFileAsync(CkByteData inData, String destPath) {
    long cPtr = chilkatJNI.CkGzip_UncompressMemToFileAsync(swigCPtr, this, CkByteData.getCPtr(inData), inData, destPath);
    return (cPtr == 0) ? null : new CkTask(cPtr, true);
  }

  public boolean UncompressString(CkByteData inData, String inCharset, CkString outStr) {
    return chilkatJNI.CkGzip_UncompressString(swigCPtr, this, CkByteData.getCPtr(inData), inData, inCharset, CkString.getCPtr(outStr), outStr);
  }

  public String uncompressString(CkByteData inData, String inCharset) {
    return chilkatJNI.CkGzip_uncompressString(swigCPtr, this, CkByteData.getCPtr(inData), inData, inCharset);
  }

  public CkTask UncompressStringAsync(CkByteData inData, String inCharset) {
    long cPtr = chilkatJNI.CkGzip_UncompressStringAsync(swigCPtr, this, CkByteData.getCPtr(inData), inData, inCharset);
    return (cPtr == 0) ? null : new CkTask(cPtr, true);
  }

  public boolean UncompressStringENC(String inStr, String charset, String encoding, CkString outStr) {
    return chilkatJNI.CkGzip_UncompressStringENC(swigCPtr, this, inStr, charset, encoding, CkString.getCPtr(outStr), outStr);
  }

  public String uncompressStringENC(String inStr, String charset, String encoding) {
    return chilkatJNI.CkGzip_uncompressStringENC(swigCPtr, this, inStr, charset, encoding);
  }

  public boolean UnlockComponent(String unlockCode) {
    return chilkatJNI.CkGzip_UnlockComponent(swigCPtr, this, unlockCode);
  }

  public boolean UnTarGz(String tgzFilename, String destDir, boolean bNoAbsolute) {
    return chilkatJNI.CkGzip_UnTarGz(swigCPtr, this, tgzFilename, destDir, bNoAbsolute);
  }

  public CkTask UnTarGzAsync(String tgzFilename, String destDir, boolean bNoAbsolute) {
    long cPtr = chilkatJNI.CkGzip_UnTarGzAsync(swigCPtr, this, tgzFilename, destDir, bNoAbsolute);
    return (cPtr == 0) ? null : new CkTask(cPtr, true);
  }

  public boolean WriteFile(String path, CkByteData binaryData) {
    return chilkatJNI.CkGzip_WriteFile(swigCPtr, this, path, CkByteData.getCPtr(binaryData), binaryData);
  }

  public boolean XfdlToXml(String xfldData, CkString outStr) {
    return chilkatJNI.CkGzip_XfdlToXml(swigCPtr, this, xfldData, CkString.getCPtr(outStr), outStr);
  }

  public String xfdlToXml(String xfldData) {
    return chilkatJNI.CkGzip_xfdlToXml(swigCPtr, this, xfldData);
  }

}

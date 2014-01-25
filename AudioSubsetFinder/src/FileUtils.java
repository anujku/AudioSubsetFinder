import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.Map;

/**
 * The Class FileUtils.
 */
public class FileUtils {
  /**
   * Delete temporary files from the given map.
   * 
   * @param map the map
   */
  public static void deleteTempFiles(Map<String, String> map) {
    for (Map.Entry<String, String> entry : map.entrySet()) {
      new File(entry.getValue()).delete();
    }
  }

  /**
   * Copy contents from source file to destination file.
   * 
   * @param sourceFile the source file
   * @param destFile the dest file
   * @throws IOException Signals that an I/O exception has
   *           occurred.
   */
  public static void copyFile(File sourceFile, File destFile)
      throws IOException {
    if (!destFile.exists()) {
      destFile.createNewFile();
    }
    FileChannel source = null;
    FileChannel destination = null;
    try {
      source = new FileInputStream(sourceFile).getChannel();
      destination = new FileOutputStream(destFile).getChannel();
      destination.transferFrom(source, 0, source.size());
    } catch (Exception e) {} finally {
      if (source != null) {
        source.close();
      }
      if (destination != null) {
        destination.close();
      }
    }
  }

  /**
   * Method to store the Map of non-wave file and its
   * canonical form.
   * 
   * @param path the path
   * @return the output file path
   */
  public static String getOutputFilePath(String path) {
    StringBuilder outputFilePath = new StringBuilder();
    String[] splittedPath = path.split(Constants.SLASH);
    outputFilePath.append(Constants.OUTPUT_FILE_LOCATION);
    outputFilePath.append(Constants.SLASH);
    outputFilePath.append(splittedPath[splittedPath.length - 1]
        .split("\\.")[0]);
    outputFilePath.append(System.currentTimeMillis());
    outputFilePath.append(Constants.DOT_WAV);
    return outputFilePath.toString();
  }

  /**
   * Method to convert a set of .mp3 files to .wav.
   * 
   * @param filesToConvert the files to convert
   * @return : true if conversion occurred without any
   *         error, else false
   * @throws IOException Signals that an I/O exception has
   *           occurred.
   */
  public static void convertToWav(Map<String, String> filesToConvert)
      throws IOException {
    Process proc = null;
    String cmd = "";
    // If output location does not exist, create a directory
    File dir = new File(Constants.OUTPUT_FILE_LOCATION);
    if (!dir.exists()) dir.mkdir();
    for (Map.Entry<String, String> entry : filesToConvert.entrySet()) {
      cmd =
          Constants.LAME_LOCATION + Constants.SPACE
              + Constants.LAME_DECODE + Constants.SPACE
              + entry.getKey() + Constants.SPACE + entry.getValue();
      try {
        // Attach the process to execute the command
        proc = Runtime.getRuntime().exec(cmd);
        // Keep polling the errorStream of the spawned
        // process for any data:
        StreamReaderThread errorStreamReaderThread =
            new StreamReaderThread(proc.getErrorStream());
        // Keep polling the OutputStream of the spawned
        // process for any data:
        StreamReaderThread outputStreamReaderThread =
            new StreamReaderThread(proc.getInputStream());
        // Start the threads to poll the streams
        errorStreamReaderThread.start();
        outputStreamReaderThread.start();
        // Wait until the process has terminated
        proc.waitFor();
      } catch (Exception e) {}
    }
  }

  /**
   * Read file bytes.
   *
   * @param file the file
   * @return the byte[] of the file
   * @throws Exception the exception
   */
  public static byte[] readFileBytes(File file) throws Exception {
    RandomAccessFile randomAccessFile =
        new RandomAccessFile(file, "r");
    byte[] fileBytes = new byte[(int) randomAccessFile.length()];
    randomAccessFile.read(fileBytes);
    randomAccessFile.close();
    return fileBytes;
  }
}

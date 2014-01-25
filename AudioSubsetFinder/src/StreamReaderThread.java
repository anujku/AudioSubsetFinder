import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * The Class StreamReaderThread. This is used to poll the streams of
 * the subprocess
 */
public class StreamReaderThread extends Thread {

  /** The input stream. */
  private InputStream inputStream;

  /**
   * Instantiates a new stream reader thread.
   * 
   * @param inputStream: the stream
   */
  public StreamReaderThread(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  /**
   * Entry Point of the thread to run
   */
  public void run() {
    try {
      BufferedReader bufferedReader =
          new BufferedReader(new InputStreamReader(inputStream));

      // Keep reading output of any input stream so that stream stays
      // empty for other process and thus prevents deadlock situations

      while (bufferedReader.readLine() != null) {}
    } catch (Exception e) {}
  }
}

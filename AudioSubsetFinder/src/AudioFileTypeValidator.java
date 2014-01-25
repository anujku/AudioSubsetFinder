import java.util.Arrays;
import java.util.List;

/**
 * The Class AudioFileTypeValidator.
 */
public class AudioFileTypeValidator {
  /** The Constant RIFF_HEX_SIGNATURE. */
  private static final byte[] RIFF_HEX_SIGNATURE = new byte[] {
      (byte) 0x52, (byte) 0x49, (byte) 0x46, (byte) 0x46};
  /** The Constant WAV_HEX_SIGNATURE. */
  private static final byte[] WAV_HEX_SIGNATURE = new byte[] {
      (byte) 0x57, (byte) 0x41, (byte) 0x56, (byte) 0x45};
  /** The Constant OGG_HEX_SIGNATURE. */
  private static final byte[] OGG_HEX_SIGNATURE = new byte[] {
      (byte) 0x4F, (byte) 0x67, (byte) 0x67, (byte) 0x53};
  /** The Constant MP3_HEX_SIGNATURE_1. */
  private static final byte[] MP3_HEX_SIGNATURE_1 = new byte[] {
      (byte) 0x49, (byte) 0x44, (byte) 0x33};
  /** The Constant MP3_HEX_SIGNATURE_2_PREFIX. */
  private static final byte MP3_HEX_SIGNATURE_2_PREFIX = (byte) 0xff;
  /** The Constant MP3_VERSION_2_LAYER_3_PROTECTED. */
  private static final byte MP3_VERSION_2_LAYER_3_PROTECTED =
      (byte) 0xf2;
  /** The Constant MP3_VERSION_2_LAYER_3_UNPROTECTED. */
  private static final byte MP3_VERSION_2_LAYER_3_UNPROTECTED =
      (byte) 0xf3;
  /** The Constant MP3_VERSION_1_LAYER_3_PROTECTED. */
  private static final byte MP3_VERSION_1_LAYER_3_PROTECTED =
      (byte) 0xfa;
  /** The Constant MP3_VERSION_1_LAYER_3_UNPROTECTED. */
  private static final byte MP3_VERSION_1_LAYER_3_UNPROTECTED =
      (byte) 0xfb;
  /** The Constant MP3_HEX_SIGNATURE_2_SUFFIX. */
  private static final List<Byte> MP3_HEX_SIGNATURE_2_SUFFIX = Arrays
      .asList(MP3_VERSION_2_LAYER_3_PROTECTED,
          MP3_VERSION_2_LAYER_3_UNPROTECTED,
          MP3_VERSION_1_LAYER_3_PROTECTED,
          MP3_VERSION_1_LAYER_3_UNPROTECTED);

  /**
   * header's length must be HEADER_LENGTH
   * 
   * @param header the header
   * @return true, if is wav
   */
  public static boolean isWav(byte[] header) {
    for (int i = 0; i < header.length; i++) {
      if ((i < 4 && header[i] != RIFF_HEX_SIGNATURE[i])
          || (i >= 8 && header[i] != WAV_HEX_SIGNATURE[i - 8]))
        return false;
    }
    return true;
  }

  // header's length must be HEADER_LENGTH
  /**
   * Checks if is ogg.
   * 
   * @param header the header
   * @return true, if is ogg
   */
  public static boolean isOgg(byte[] header) {
    for (int i = 0; i < OGG_HEX_SIGNATURE.length; i++) {
      if (header[i] != OGG_HEX_SIGNATURE[i]) return false;
    }
    return true;
  }

  /**
   * header's length must be HEADER_LENGTH
   * 
   * @param header the header
   * @return true, if is mp3 signature1
   */
  public static boolean isMp3Signature1(byte[] header) {
    for (int i = 0; i < MP3_HEX_SIGNATURE_1.length; i++) {
      if (header[i] != MP3_HEX_SIGNATURE_1[i]) return false;
    }
    return true;
  }

  /**
   * header's length must be HEADER_LENGTH
   * 
   * @param header the header
   * @return true, if is mp3 signature2
   */
  public static boolean isMp3Signature2(byte[] header) {
    return (header[0] == MP3_HEX_SIGNATURE_2_PREFIX && MP3_HEX_SIGNATURE_2_SUFFIX
        .contains(header[1]));
  }
}

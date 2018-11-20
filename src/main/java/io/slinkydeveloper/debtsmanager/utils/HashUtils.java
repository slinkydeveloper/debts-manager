package io.slinkydeveloper.debtsmanager.utils;

import joptsimple.internal.Strings;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtils {
  private static MessageDigest shaDigest;

  static {
    try {
      shaDigest = MessageDigest.getInstance("SHA-256");
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException(e);
    }
  }

  private static String bytesToHex(byte[] hash) {
    StringBuffer hexString = new StringBuffer();
    for (int i = 0; i < hash.length; i++) {
      String hex = Integer.toHexString(0xff & hash[i]);
      if(hex.length() == 1) hexString.append('0');
      hexString.append(hex);
    }
    return hexString.toString();
  }

  public static String createHash(String ...args) {
    byte[] encodedhash = shaDigest.digest(Strings.join(args, "").getBytes(StandardCharsets.UTF_8));
    return bytesToHex(encodedhash);
  }

}

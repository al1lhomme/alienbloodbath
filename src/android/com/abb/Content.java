// Copyright 2008 and onwards Matthew Burkhart.
//
// This program is free software; you can redistribute it and/or modify it under
// the terms of the GNU General Public License as published by the Free Software
// Foundation; version 3 of the License.
//
// This program is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
// FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
// details.

package android.com.abb;

import android.content.res.Resources;
import android.net.Uri;
import android.util.Log;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;


/** Class to abstract the details of extracting and accessing files from the ABB
 * content zip file in the same was files on disk are accessed. */
public class Content {
  public static void Initialize(Resources resources) {
    try {
      InputStream content_input_stream =
          resources.openRawResource(R.raw.content_package);
      WriteStreamToFile(content_input_stream, kTempContentPath);
      content_input_stream.close();
    } catch (IOException ex) {
      Log.e("Content::Initialize", "Failed extracting content package.", ex);
    }
  }

  protected void finalize() {
    (new File(kTempFilePath)).delete();
    (new File(kTempContentPath)).delete();
  }

  public static boolean Exists(Uri uri) {
    // Handle "file://" scheme.
    if (uri.getScheme().equals("file")) {
      return (new File(uri.getPath())).exists();
    }
    // Handle "content://" scheme.
    else if (uri.getScheme().equals("content")) {
      String[] entry_list = List(uri);
      Arrays.sort(entry_list);
      return Arrays.binarySearch(entry_list, uri.getPath()) > 0;
    }
    // Bad uri scheme.
    else {
      Log.e("Content::ListFiles", "Bad URI scheme.");
      return false;
    }
  }

  public static String[] List(Uri uri) {
    // Handle "file://" scheme.
    if (uri.getScheme().equals("file")) {
      return (new File(uri.getPath())).list();
    }
    // Handle "content://" scheme.
    else if (uri.getScheme().equals("content")) {
      ZipFile content_file;
      try {
        content_file = new ZipFile(kTempContentPath);
      } catch (IOException ex) {
        Log.e("Content::List", "Unable to open package file.", ex);
        return null;
      }
      ArrayList<String> entry_list = new ArrayList<String>();
      for (Enumeration<? extends ZipEntry> entry_it = content_file.entries();
           entry_it.hasMoreElements();) {
        String entry_name = entry_it.nextElement().getName();
        entry_name = entry_name.replace("content_package", "");
        entry_list.add(entry_name);
      }
      return entry_list.toArray(new String[0]);
    }
    // Bad uri scheme.
    else {
      Log.e("Content::ListFiles", "Bad URI scheme.");
      return null;
    }
  }

  public static String GetTemporaryFilePath(Uri uri) {
    // Handle "file://" scheme.
    if (uri.getScheme().equals("file")) {
      return uri.getPath();
    }
    // Handle "content://" scheme. We must extract the file to a temporary path
    // in order to handle this request.
    else if (uri.getScheme().equals("content")) {
      ZipFile content_file;
      try {
        content_file = new ZipFile(kTempContentPath);
      } catch (IOException ex) {
        Log.e("Content::GetTemporaryFilePath", "Unable to open package file.", ex);
        return null;
      }
      String entry_name = "content_package" + uri.getPath();
      ZipEntry content_entry = content_file.getEntry(entry_name);
      if (content_entry == null) {
        Log.e("Content::GetTemporaryFilePath", "Unable to find entry: " + entry_name);
        return null;
      }
      try {
        InputStream content_stream = content_file.getInputStream(content_entry);
        WriteStreamToFile(content_stream, kTempFilePath);
      } catch (IOException ex) {
        Log.e("Content::GetTemporaryFilePath", "Unable to write out entry.", ex);
        return null;
      }
      return kTempFilePath;
    }
    // Bad uri scheme.
    else {
      Log.e("Content::ListFiles", "Bad URI scheme.");
      return null;
    }
  }

  private static void WriteStreamToFile(InputStream input_stream,
                                        String output_path) throws IOException {
    (new File(output_path)).createNewFile();
    BufferedOutputStream output_stream =
        new BufferedOutputStream(new FileOutputStream(output_path));
    byte[] buffer = new byte[1024];
    int bytes_read;
    while((bytes_read = input_stream.read(buffer)) >= 0)
      output_stream.write(buffer, 0, bytes_read);
    output_stream.close();
  }

  private static final String kTempFilePath = "/data/data/android.com.abb/abbfile.tmp";
  private static final String kTempContentPath = "/data/data/android.com.abb/abbpackage.tmp";
}
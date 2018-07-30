/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package bestan.common.db.util;

import com.google.common.collect.Lists;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;
import java.util.TreeMap;
import java.util.UUID;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused,unchecked")
public class Utils {
    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);
    public static final String DEFAULT_STREAM_ID = "default";
    private static ClassLoader cl = ClassLoader.getSystemClassLoader();

    public static Object newInstance(String klass) {
        try {
            Class c = Class.forName(klass);
            return c.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Object newInstance(String klass, Object... params) {
        try {
            Class c = Class.forName(klass);
            Constructor[] constructors = c.getConstructors();
            Constructor con = null;
            for (Constructor cons : constructors) {
                if (cons.getParameterTypes().length == params.length) {
                    con = cons;
                    break;
                }
            }

            if (con == null) {
                throw new RuntimeException("Could not found the corresponding constructor, params=" +
                        JStormUtils.mk_list(params));
            } else {
                if (con.getParameterTypes().length == 0) {
                    return c.newInstance();
                } else {
                    return con.newInstance(params);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Go thrift gzip serializer
     */
    public static byte[] serialize(Object obj) {
        /**
         * JStorm disables the thrift.gz.serializer
         */
        // return serializationDelegate.serialize(obj);
        return javaSerialize(obj);
    }

    public static byte[] javaSerialize(Object obj) {
        if (obj instanceof byte[]) {
            return (byte[]) obj;
        }
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(obj);
            oos.close();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object trySerialize(Object obj) {
        if (obj == null) {
            return null;
        }
        try {
            return serialize(obj);
        } catch (Exception e) {
            LOG.info("Failed to serialize. cause={}", e.getCause());
            return null;
        }
    }

    public static String to_json(Object m) {
        // return JSON.toJSONString(m);
        return JSONValue.toJSONString(m);
    }

    public static Object from_json(String json) {
        if (json == null) {
            return null;
        } else {
            // return JSON.parse(json);
            return JSONValue.parse(json);
        }
    }

    public static byte[] gzip(byte[] data) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            GZIPOutputStream out = new GZIPOutputStream(bos);
            out.write(data);
            out.close();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] gunzip(byte[] data) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ByteArrayInputStream bis = new ByteArrayInputStream(data);
            GZIPInputStream in = new GZIPInputStream(bis);
            byte[] buffer = new byte[1024];
            int len;
            while ((len = in.read(buffer)) >= 0) {
                bos.write(buffer, 0, len);
            }
            in.close();
            bos.close();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] toCompressedJsonConf(Map<String, Object> stormConf) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            OutputStreamWriter out = new OutputStreamWriter(new GZIPOutputStream(bos));
            JSONValue.writeJSONString(stormConf, out);
            out.close();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> fromCompressedJsonConf(byte[] serialized) {
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(serialized);
            InputStreamReader in = new InputStreamReader(new GZIPInputStream(bis));
            Object ret = JSONValue.parseWithException(in);
            in.close();
            return (Map<String, Object>) ret;
        } catch (IOException | ParseException ioe) {
            throw new RuntimeException(ioe);
        }
    }

    public static <T> String join(Iterable<T> coll, String sep) {
        Iterator<T> it = coll.iterator();
        StringBuilder ret = new StringBuilder();
        while (it.hasNext()) {
            ret.append(it.next());
            if (it.hasNext()) {
                ret.append(sep);
            }
        }
        return ret.toString();
    }

    public static void sleep(long millis) {
        try {
            Time.sleep(millis);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static <S, T> T get(Map<S, T> m, S key, T def) {
        T ret = m.get(key);
        if (ret == null) {
            ret = def;
        }
        return ret;
    }

    public static List<Object> tuple(Object... values) {
        List<Object> ret = new ArrayList<>();
        for (Object v : values) {
            ret.add(v);
        }
        return ret;
    }

    public static boolean isSystemId(String id) {
        return id.startsWith("__");
    }

    public static <K, V> Map<V, K> reverseMap(Map<K, V> map) {
        Map<V, K> ret = new HashMap<>();
        for (Map.Entry<K, V> entry : map.entrySet()) {
            ret.put(entry.getValue(), entry.getKey());
        }
        return ret;
    }

    public static List<String> getStrings(final Object o) {
        if (o == null) {
            return new ArrayList<>();
        } else if (o instanceof String) {
            return Lists.newArrayList((String) o);
        } else if (o instanceof Collection) {
            List<String> answer = new ArrayList<>();
            for (Object v : (Collection) o) {
                answer.add(v.toString());
            }
            return answer;
        } else {
            throw new IllegalArgumentException("Don't know how to convert to string list");
        }
    }

    public static String getString(Object o) {
        if (null == o) {
            throw new IllegalArgumentException("Don't know how to convert null to String");
        }
        return o.toString();
    }

    public static Integer getInt(Object o) {
        Integer result = getInt(o, null);
        if (null == result) {
            throw new IllegalArgumentException("Don't know how to convert null to int");
        }
        return result;
    }

    public static Integer getInt(Object o, Integer defaultValue) {
        if (null == o) {
            return defaultValue;
        }

        if (o instanceof Integer ||
                o instanceof Short ||
                o instanceof Byte) {
            return ((Number) o).intValue();
        } else if (o instanceof Long) {
            final long l = (Long) o;
            if (Integer.MIN_VALUE <= l && l <= Integer.MAX_VALUE) {
                return (int) l;
            }
        } else if (o instanceof Double) {
            final double d = (Double) o;
            if (Integer.MIN_VALUE <= d && d <= Integer.MAX_VALUE) {
                return (int) d;
            }
        } else if (o instanceof String) {
            return Integer.parseInt((String) o);
        }

        //
        return defaultValue;
    }

    public static Double getDouble(Object o) {
        Double result = getDouble(o, null);
        if (null == result) {
            throw new IllegalArgumentException("Don't know how to convert null to double");
        }
        return result;
    }

    public static Double getDouble(Object o, Double defaultValue) {
        if (null == o) {
            return defaultValue;
        }
        if (o instanceof Number) {
            return ((Number) o).doubleValue();
        } else {
            throw new IllegalArgumentException("Don't know how to convert " + o + " + to double");
        }
    }

    public static boolean getBoolean(Object o, boolean defaultValue) {
        if (null == o) {
            return defaultValue;
        }
        if (o instanceof Boolean) {
            return (Boolean) o;
        } else {
            throw new IllegalArgumentException("Don't know how to convert " + o + " + to boolean");
        }
    }

    public static String getString(Object o, String defaultValue) {
        if (null == o) {
            return defaultValue;
        }
        if (o instanceof String) {
            return (String) o;
        } else {
            throw new IllegalArgumentException("Don't know how to convert " + o + " + to String");
        }
    }

    public static long secureRandomLong() {
        return UUID.randomUUID().getLeastSignificantBits();
    }

    public static long generateId(Random rand) {
        long ret = rand.nextLong();
        while(ret == 0) {
            ret = rand.nextLong();
        }
        return ret;
    }

    /*
     * Unpack matching files from a jar. Entries inside the jar that do
     * not match the given pattern will be skipped.
     *
     * @param jarFile the .jar file to unpack
     * @param toDir the destination directory into which to unpack the jar
     */
    public static void unJar(File jarFile, File toDir)
            throws IOException {
        JarFile jar = new JarFile(jarFile);
        try {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                final JarEntry entry = entries.nextElement();
                if (!entry.isDirectory()) {
                    InputStream in = jar.getInputStream(entry);
                    try {
                        File file = new File(toDir, entry.getName());
                        ensureDirectory(file.getParentFile());
                        OutputStream out = new FileOutputStream(file);
                        try {
                            copyBytes(in, out, 8192);
                        } finally {
                            out.close();
                        }
                    } finally {
                        in.close();
                    }
                }
            }
        } finally {
            jar.close();
        }
    }

    /**
     * Copies from one stream to another.
     *
     * @param in       InputStream to read from
     * @param out      OutputStream to write to
     * @param buffSize the size of the buffer
     */
    public static void copyBytes(InputStream in, OutputStream out, int buffSize)
            throws IOException {
        PrintStream ps = out instanceof PrintStream ? (PrintStream) out : null;
        byte buf[] = new byte[buffSize];
        int bytesRead = in.read(buf);
        while (bytesRead >= 0) {
            out.write(buf, 0, bytesRead);
            if ((ps != null) && ps.checkError()) {
                throw new IOException("Unable to write to output stream.");
            }
            bytesRead = in.read(buf);
        }
    }

    /**
     * Ensure the existence of a given directory.
     *
     * @throws IOException if it cannot be created and does not already exist
     */
    private static void ensureDirectory(File dir) throws IOException {
        if (!dir.mkdirs() && !dir.isDirectory()) {
            throw new IOException("Mkdirs failed to create " +
                    dir.toString());
        }
    }

    public static boolean onWindows() {
        return System.getenv("OS") != null && System.getenv("OS").equals("Windows_NT");
    }

    public static TreeMap<Integer, Integer> integerDivided(int sum, int numPieces) {
        int base = sum / numPieces;
        int numInc = sum % numPieces;
        int numBases = numPieces - numInc;
        TreeMap<Integer, Integer> ret = new TreeMap<>();
        ret.put(base, numBases);
        if (numInc != 0) {
            ret.put(base + 1, numInc);
        }
        return ret;
    }

    public static byte[] toByteArray(ByteBuffer buffer) {
        byte[] ret = new byte[buffer.remaining()];
        buffer.get(ret, 0, ret.length);
        return ret;
    }

    public static void readAndLogStream(String prefix, InputStream in) {
        try {
            BufferedReader r = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = r.readLine()) != null) {
                LOG.info("{}:{}", prefix, line);
            }
        } catch (IOException e) {
            LOG.warn("Error while trying to log stream", e);
        }
    }

    public static boolean exceptionCauseIsInstanceOf(Class klass, Throwable throwable) {
        Throwable t = throwable;
        while (t != null) {
            if (klass.isInstance(t)) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }

    public static void handleUncaughtException(Throwable t) {
        if (t != null && t instanceof Error) {
            if (t instanceof OutOfMemoryError) {
                try {
                    System.err.println("Halting due to Out Of Memory Error..." + Thread.currentThread().getName());
                } catch (Throwable err) {
                    // Again we don't want to exit because of logging issues.
                }
                Runtime.getRuntime().halt(-1);
            } else {
                // Running in daemon mode, we would pass Error to calling thread.
                throw (Error) t;
            }
        }
    }

    public static List<String> tokenize_path(String path) {
        String[] tokens = path.split("/");
        java.util.ArrayList<String> rtn = new ArrayList<>();
        for (String str : tokens) {
            if (!str.isEmpty()) {
                rtn.add(str);
            }
        }
        return rtn;
    }

    public static String toks_to_path(List<String> tokens) {
        StringBuilder buff = new StringBuilder();
        buff.append("/");
        int size = tokens.size();
        for (int i = 0; i < size; i++) {
            buff.append(tokens.get(i));
            if (i < (size - 1)) {
                buff.append("/");
            }

        }
        return buff.toString();
    }

    public static String normalize_path(String path) {
        return toks_to_path(tokenize_path(path));
    }

    public static String printStack() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nCurrent call stack:\n");
        StackTraceElement[] stackElements = Thread.currentThread().getStackTrace();
        for (int i = 2; i < stackElements.length; i++) {
            sb.append("\t").append(stackElements[i]).append("\n");
        }

        return sb.toString();
    }

    private static Map loadProperty(String prop) {
        Map ret = new HashMap<>();
        Properties properties = new Properties();

        try {
            InputStream stream = new FileInputStream(prop);
            properties.load(stream);
            if (properties.size() == 0) {
                return null;
            } else {
                ret.putAll(properties);
            }
        } catch (Exception e1) {
            throw new RuntimeException(e1);
        }

        return ret;
    }

    public static Map loadConf(String arg) {
        Map ret;
        ret = loadProperty(arg);
        return ret;
    }

    public static String getVersion() {
        String ret = "";
        InputStream input = null;
        try {
            input = Thread.currentThread().getContextClassLoader().getResourceAsStream("version");
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            String s = in.readLine();
            if (s != null) {
                ret = s.trim();
            } else {
                LOG.warn("Failed to get version");
            }
        } catch (Exception e) {
            LOG.warn("Failed to get version", e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception e) {
                    LOG.error("Failed to close the reader of RELEASE", e);
                }
            }
        }

        return ret;
    }

    public static String getBuildTime() {
        String ret = "";
        InputStream input = null;
        try {
            input = Thread.currentThread().getContextClassLoader().getResourceAsStream("build");
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            String s = in.readLine();
            if (s != null) {
                ret = s.trim();
            } else {
                LOG.warn("Failed to get build time");
            }
        } catch (Exception e) {
            LOG.warn("Failed to get build time", e);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (Exception ignored) {
                }
            }
        }

        return ret;
    }

    public static void writeIntToByteArray(byte[] bytes, int offset, int value) {
        bytes[offset++] = (byte) (value & 0x000000FF);
        bytes[offset++] = (byte) ((value & 0x0000FF00) >> 8);
        bytes[offset++] = (byte) ((value & 0x00FF0000) >> 16);
        bytes[offset] = (byte) ((value & 0xFF000000) >> 24);
    }

    public static int readIntFromByteArray(byte[] bytes, int offset) {
        int ret = 0;
        ret = ret | (bytes[offset++] & 0x000000FF);
        ret = ret | ((bytes[offset++] << 8) & 0x0000FF00);
        ret = ret | ((bytes[offset++] << 16) & 0x00FF0000);
        ret = ret | ((bytes[offset] << 24) & 0xFF000000);
        return ret;
    }

    /*
     * Given a File input it will unzip the file in a the unzip directory
     * passed as the second parameter
     * @param inFile The zip file as input
     * @param unzipDir The unzip directory where to unzip the zip file.
     * @throws IOException
     */
    public static void unZip(File inFile, File unzipDir) throws IOException {
        Enumeration<? extends ZipEntry> entries;
        ZipFile zipFile = new ZipFile(inFile);

        try {
            entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory()) {
                    InputStream in = zipFile.getInputStream(entry);
                    try {
                        File file = new File(unzipDir, entry.getName());
                        if (!file.getParentFile().mkdirs()) {
                            if (!file.getParentFile().isDirectory()) {
                                throw new IOException("Mkdirs failed to create " +
                                        file.getParentFile().toString());
                            }
                        }
                        OutputStream out = new FileOutputStream(file);
                        try {
                            byte[] buffer = new byte[8192];
                            int i;
                            while ((i = in.read(buffer)) != -1) {
                                out.write(buffer, 0, i);
                            }
                        } finally {
                            out.close();
                        }
                    } finally {
                        in.close();
                    }
                }
            }
        } finally {
            zipFile.close();
        }
    }

    /**
     * Given a zip File input it will return its size
     * Only works for zip files whose uncompressed size is less than 4 GB,
     * otherwise returns the size module 2^32, per gzip specifications
     *
     * @param myFile The zip file as input
     * @return zip file size as a long
     * @throws IOException
     */
    public static long zipFileSize(File myFile) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(myFile, "r");
        raf.seek(raf.length() - 4);
        long b4 = raf.read();
        long b3 = raf.read();
        long b2 = raf.read();
        long b1 = raf.read();
        long val = (b1 << 24) | (b2 << 16) + (b3 << 8) + b4;
        raf.close();
        return val;
    }

    public static double zeroIfNaNOrInf(double x) {
        return (Double.isNaN(x) || Double.isInfinite(x)) ? 0.0 : x;
    }

    /**
     * parses the arguments to extract jvm heap memory size in MB.
     *
     * @return the value of the JVM heap memory setting (in MB) in a java command.
     */
    public static Double parseJvmHeapMemByChildOpts(String input, Double defaultValue) {
        if (input != null) {
            Pattern optsPattern = Pattern.compile("Xmx[0-9]+[mkgMKG]");
            Matcher m = optsPattern.matcher(input);
            String memoryOpts = null;
            while (m.find()) {
                memoryOpts = m.group();
            }
            if (memoryOpts != null) {
                int unit = 1;
                if (memoryOpts.toLowerCase().endsWith("k")) {
                    unit = 1024;
                } else if (memoryOpts.toLowerCase().endsWith("m")) {
                    unit = 1024 * 1024;
                } else if (memoryOpts.toLowerCase().endsWith("g")) {
                    unit = 1024 * 1024 * 1024;
                }
                memoryOpts = memoryOpts.replaceAll("[a-zA-Z]", "");
                Double result = Double.parseDouble(memoryOpts) * unit / 1024.0 / 1024.0;
                return (result < 1.0) ? 1.0 : result;
            } else {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public static void setClassLoaderForJavaDeSerialize(ClassLoader cl) {
        Utils.cl = cl;
    }

    public static void resetClassLoaderForJavaDeSerialize() {
        Utils.cl = ClassLoader.getSystemClassLoader();
    }

    public static boolean flushToFile(String file, String data, boolean append) {
        if (data == null) {
            return true;
        }
        try {
            FileOutputStream fs = new FileOutputStream(file, append);
            fs.write(data.getBytes());
            fs.flush();
            fs.close();
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }
}

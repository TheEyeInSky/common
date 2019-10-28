package com.dongzy.common.common.io;

import org.slf4j.Logger;
import com.gee4j.common.Validate;
import com.gee4j.log.TextLoggerFactory;

import java.io.*;

/**
 * 对对象进行序列号和反序列号处理
 * 如果传入的参数为null值，那么返回值也将为null
 *
 * @author zouyong
 * @since JDK1.0
 */
public class SerializationUtils {

    private final static Logger LOGGER = TextLoggerFactory.getInstance().getLogger(SerializationUtils.class);

    /**
     * 将对象序列化成二进制数据
     *
     * @param object 需要序列号的对象
     * @return 二进制数据
     */
    public static byte[] serialize(final Serializable object) {
        if (object == null) {
            return null;
        }
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            // 序列化
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(object);
            byte[] bytes = baos.toByteArray();
            oos.close();
            return bytes;
        } catch (IOException e) {
            LOGGER.error("序列化对象为二进制数组时发生异常！", e);
            throw new IllegalArgumentException("序列化对象为二进制数组时发生异常！详情查看日志文件。");
        }
    }

    /**
     * 将二进制数据反序列化成对象
     *
     * @param bytes 二进制数据
     * @param <T>   type
     * @return 对象
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T deserialize(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            // 反序列化
            ObjectInputStream ois = new ObjectInputStream(bais);
            return (T) ois.readObject();
        } catch (Exception e) {
            LOGGER.error("将二进制数组转换为JAVA对象时发生异常！", e);
            throw new IllegalArgumentException("将二进制数组转换为JAVA对象时发生异常！详情查看日志文件。");
        }
    }

    // Deserialize
    //-----------------------------------------------------------------------

    /**
     * <p>
     * Deserializes an {@code Object} from the specified stream.
     * </p>
     * <p>
     * <p>
     * The stream will be closed once the object is written. This avoids the need for a finally clause, and maybe also
     * exception handling, in the application code.
     * </p>
     * <p>
     * <p>
     * The stream passed in is not buffered internally within this method. This is the responsibility of your
     * application if desired.
     * </p>
     * <p>
     * <p>
     * If the call site incorrectly types the return value, a {@link ClassCastException} is thrown from the call site.
     * Without Generics in this declaration, the call site must type cast and can cause the same ClassCastException.
     * Note that in both cases, the ClassCastException is in the call site, not in this method.
     * </p>
     *
     * @param inputStream the serialized object input stream, must not be null
     * @param <T>         type
     * @return the deserialized object
     * @throws IllegalArgumentException if {@code inputStream} is {@code null}
     * @throws SerializationException   (runtime) if the serialization fails
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T deserialize(final InputStream inputStream) {
        Validate.notNull(inputStream, "The InputStream must not be null");
        try (ObjectInputStream in = new ObjectInputStream(inputStream)) {
            return (T) in.readObject();
        } catch (final ClassNotFoundException | IOException ex) {
            throw new SerializationException(ex);
        }
    }

    /**
     * 利用序列化类克隆对象
     *
     * @param model 需要被克隆的对象（需要实现序列化接口）
     * @param <T>   type
     */
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T clone(T model) {
        return (T) deserialize(serialize(model));
    }

    // Serialize
    //-----------------------------------------------------------------------

    /**
     * <p>Serializes an {@code Object} to the specified stream.</p>
     * <p>
     * <p>The stream will be closed once the object is written.
     * This avoids the need for a finally clause, and maybe also exception
     * handling, in the application code.</p>
     * <p>
     * <p>The stream passed in is not buffered internally within this method.
     * This is the responsibility of your application if desired.</p>
     *
     * @param obj          the object to serialize to bytes, may be null
     * @param outputStream the stream to write to, must not be null
     * @throws IllegalArgumentException if {@code outputStream} is {@code null}
     * @throws SerializationException   (runtime) if the serialization fails
     */
    public static void serialize(final Serializable obj, final OutputStream outputStream) {
        Validate.isTrue(obj != null || outputStream != null, "Args both is null!");
        try (ObjectOutputStream out = new ObjectOutputStream(outputStream)) {
            out.writeObject(obj);
        } catch (final IOException ex) {
            throw new SerializationException(ex);
        }
    }
}

import com.bc.zarr.ArrayParams;
import com.bc.zarr.ZarrArray;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.file.FileSystem;
import java.text.MessageFormat;
import java.util.Properties;

public class FileSystemArray {

    private static final String S3URI = "s3://{0}:{1}@192.168.56.1:9000";

    private static final String KEY_S3USER = "s3_user";
    private static final String KEY_S3PASSWORD = "s3_pass";
    private static final String KEY_S3BUCKET = "s3_bucket";
    private static final long PROPERTY_MULTIPART_UPLOAD_MINSIZE = Long.MAX_VALUE;
    private static final long PROPERTY_CONNECTION_TIMEOUT = 100l;
    private static FileSystem fileSystem;

    public static void main(String[] args) throws IOException {

        final Properties properties = new Properties();
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("s3.properties")) {
            properties.load(stream);
        }

        final String s3User = properties.getProperty(KEY_S3USER);
        final String s3Pass = properties.getProperty(KEY_S3PASSWORD);
        final String s3Bucket = properties.getProperty(KEY_S3BUCKET);

        String encodedUser = URLEncoder.encode(s3User, "UTF-8");
        String encodedPassword = URLEncoder.encode(s3Pass, "UTF-8");
        URI uri = URI.create(MessageFormat.format("s3://{0}:{1}@obs.eu-de.otc.t-systems.com/{2}", encodedUser, encodedPassword, s3Bucket));


//        final S3FileSystemProvider s3FileSystemProvider = new S3FileSystemProvider();
//        final S3FileSystem s3FileSystem = s3FileSystemProvider.createFileSystem(uri, properties);
//        final S3Path p = s3FileSystem.getPath("");
//        System.out.println("endpoint = " + s3FileSystem.getEndpoint());
//        for (Path rootDirectory : s3FileSystem.getRootDirectories()) {
//            System.out.println("rootDir = " + rootDirectory);
//        }
//        System.out.println("client = " + s3FileSystem.getClient());
//        System.out.println("key = " + s3FileSystem.getKey());
//        System.out.println("key = " + s3FileSystem.getKey());
//
//
//        final S3Path s3Path = s3FileSystem.getPath("jzarr-test", "ramba", "zamba");
//        Files.createDirectories(s3Path);

//        final ZarrArray array = ZarrArray.create(s3Path, new ArrayParams().withShape(5, 7));
    }
}

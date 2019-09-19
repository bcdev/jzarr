import com.bc.zarr.*;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.*;
import java.util.*;

public class S3Array_nio {

    private static final String S3FS_SERVER = "s3fs_server";
    private static final String S3FS_BUCKET_NAME = "s3fs_bucket_name";

    public static void main(String[] args) throws IOException, InvalidRangeException {
        Path bucketNioPath = getS3BucketNioPath();

        Path groupPath = bucketNioPath.resolve("GroupName.zarr");

        writeToS3Bucket(groupPath);

        readFromS3Bucket(groupPath);
    }

    private static void readFromS3Bucket(Path groupPath) throws IOException, InvalidRangeException {
        final ZarrGroup zarrGroup = ZarrGroup.open(groupPath);
        final Set<String> arrayKeys = zarrGroup.getArrayKeys();
        System.out.println("Array names:");
        for (String arrayKey : arrayKeys) {
            System.out.println("   : " + arrayKey);
            ZarrArray array = zarrGroup.openArray(arrayKey);
            byte[] bytes = (byte[]) array.read();
            System.out.println("   : values = " + Arrays.toString(bytes));
        }
    }

    private static void writeToS3Bucket(Path groupPath) throws IOException, InvalidRangeException {
        ZarrGroup zgroup = ZarrGroup.create(groupPath);
        ArrayParams arrayParams = new ArrayParams()
                .shape(40, 30)
                .dataType(DataType.i1)
                // In this example data will be written without compression.
                // So you can display the chunk file content in your amazon s3 console.
                .compressor(CompressorFactory.nullCompressor);
        ZarrArray a42 = zgroup.createArray("AnArray", arrayParams);
        a42.write(42);
    }

    private static Path getS3BucketNioPath() throws IOException {
        final Properties properties = new Properties();
        try (InputStream stream = S3Array_nio.class.getResourceAsStream("s3.properties")) {
            properties.load(stream);
        }

        final String s3Server = properties.getProperty(S3FS_SERVER);
        final String s3BucketName = properties.getProperty(S3FS_BUCKET_NAME);

        URI uri = URI.create("s3://" + s3Server);
        FileSystem s3fs = FileSystems.newFileSystem(uri, (Map) properties);

        Iterable<Path> paths = s3fs.getRootDirectories();
        for (Path path : paths) {
            String[] split = path.toString().split("/");
            if (split[split.length - 1].equals(s3BucketName)) {
                return path;
            }
        }
        throw new IllegalStateException("Bucket '" + s3BucketName + "' not available.");
    }
}

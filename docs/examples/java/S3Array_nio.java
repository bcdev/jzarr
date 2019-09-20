import com.bc.zarr.*;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.text.MessageFormat;
import java.util.Arrays;

public class S3Array_nio {

    public static void main(String[] args) throws IOException, InvalidRangeException {
        Path bucketPath = connectToS3Bucket();

        writeToS3Bucket(bucketPath);

        readFromS3Bucket(bucketPath);
    }

    static Path connectToS3Bucket() throws IOException {
        String s3AccessKey = "<your acceyy key>";
        String s3SecretKey = "<your secret key>";
        String s3Server = "s3.eu-central-1.amazonaws.com"; // example server name
        String s3BucketName = "bucket-abcd"; // example bucket name

        URI uri = URI.create(MessageFormat.format("s3://{0}:{1}@{2}", s3AccessKey, s3SecretKey, s3Server));
        FileSystem s3fs = FileSystems.newFileSystem(uri, null);
        Path bucketPath = s3fs.getPath("/" + s3BucketName);
        return bucketPath;
    }

    static void writeToS3Bucket(Path bucketPath) throws IOException, InvalidRangeException {
        // In this example data will be written without compression.
        // So you can display the chunk file content in your amazon s3 console.
        Path groupPath = bucketPath.resolve("GroupName.zarr");
        ZarrGroup group = ZarrGroup.create(groupPath);
        ZarrArray array = group.createArray("AnArray", new ArrayParams()
                .shape(4, 8).dataType(DataType.i1).compressor(CompressorFactory.nullCompressor));
        byte[] data = {
                11, 12, 13, 14, 15, 16, 17, 18,
                21, 22, 23, 24, 25, 26, 27, 28,
                31, 32, 33, 34, 35, 36, 37, 38,
                41, 42, 43, 44, 45, 46, 47, 48
        };
        int[] shape = {4, 8};
        int[] offset = {0, 0};
        array.write(data, shape, offset);
    } /// end

    static void readFromS3Bucket(Path bucketPath) throws IOException, InvalidRangeException {
        Path groupPath = bucketPath.resolve("GroupName.zarr");
        final ZarrGroup group = ZarrGroup.open(groupPath);
        ZarrArray array = group.openArray("AnArray");
        byte[] bytes = (byte[]) array.read();
        System.out.println(Arrays.toString(bytes));
    } /// end
}

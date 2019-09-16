import com.bc.zarr.*;
import ucar.ma2.InvalidRangeException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.file.*;
import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;

public class S3Array_WithUpplication {

    private static final String S3FS_ACCESS_KEY_ID = "s3fs-access_key_id";
    private static final String S3FS_SECRET_ACCESS_KEY = "s3fs-secret_access_key";
    private static final String S3FS_SERVER = "s3fs-server";
    private static final String S3FS_BUCKET_NAME = "s3fs-bucket-name";

    public static void main(String[] args) throws IOException, InvalidRangeException {
        S3FileSystemTest();
    }

    private static void S3FileSystemTest() throws IOException, InvalidRangeException {
        Path jzarrTestPath = getS3JzarrTestPath();
//        Path jzarrTestPath = getJzarrTestPath();

//        printReport(jzarrTestPath);

        String newGroupName = "newGroup.znap";
//        writeToAwsS3(jzarrTestPath, newGroupName);
        readFromAwsS3(jzarrTestPath, newGroupName);
    }

    private static void readFromAwsS3(Path path, String name) throws IOException, InvalidRangeException {
        Path groupPath = path.resolve(name);
        ZarrGroup zarrGroup = ZarrGroup.open(groupPath);
        System.out.println("S3 group 'name' opened.");
        Set<String> arrayKeys = zarrGroup.getArrayKeys();
        System.out.println("Array names:");
        for (String arrayKey : arrayKeys) {
            System.out.println("   : " +arrayKey);
            ZarrArray a42 = zarrGroup.openArray(arrayKey);
//            ZarrArray a42 = zarrGroup.openArray("a42");
            byte[] bytes = (byte[]) a42.read();
            System.out.println("bytes = " + Arrays.toString(bytes));
        }
    }

    private static void writeToAwsS3(Path path, String name) throws IOException, InvalidRangeException {
        Path grpPath = path.resolve(name);
        ZarrGroup zgroup = ZarrGroup.create(grpPath);
        ZarrArray a42 = zgroup.createArray("a42", new ArrayParams()
                .shape(40, 30).dataType(DataType.i1).compressor(CompressorFactory.nullCompressor));
        a42.write(42);
    }

    private static Path getJzarrTestPath() throws IOException {
        return Paths.get("D:\\temp\\zarr_evaluation\\from_s3_mirror");
    }

    private static Path getS3JzarrTestPath() throws IOException {
        final Properties properties = new Properties();
        try (InputStream stream = S3Array_WithUpplication.class.getResourceAsStream("s3.properties")) {
            properties.load(stream);
        }

        final String s3AccessKeyId = properties.getProperty(S3FS_ACCESS_KEY_ID);
        final String s3SecretAccessKey = properties.getProperty(S3FS_SECRET_ACCESS_KEY);
        final String s3Server = properties.getProperty(S3FS_SERVER);
        final String s3BucketName = properties.getProperty(S3FS_BUCKET_NAME);

        String encodedUser = URLEncoder.encode(s3AccessKeyId, "UTF-8");
        String encodedPassword = URLEncoder.encode(s3SecretAccessKey, "UTF-8");
        URI uri = URI.create(MessageFormat.format("s3://{0}:{1}@{2}/{3}", encodedUser, encodedPassword, s3Server, s3BucketName));
        FileSystem s3fs = FileSystems.newFileSystem(uri, new HashMap<String, String>());

//        Iterable<FileStore> fileStores = s3fs.getFileStores();
//        for (FileStore fileStore : fileStores) {
//            System.out.println("############################################################################");
//            System.out.println("fileStore = " + fileStore.name());
//            System.out.println("fileStore = " + fileStore.type());
//            System.out.println("fileStore = " + fileStore.isReadOnly());
//            System.out.println("fileStore = " + fileStore.getTotalSpace());
//            System.out.println("fileStore = " + fileStore.getUnallocatedSpace());
//            System.out.println("fileStore = " + fileStore.getUsableSpace());
//        }

        return s3fs.getPath("/jzarr-test");
    }

    private static void printReport(Path jzarrTestPath) throws IOException {
        List<Path> list = Files.list(jzarrTestPath).collect(Collectors.toList());
        Path zarrDir = list.get(0);
        ZarrGroup gr = ZarrGroup.open(zarrDir);
        Set<String> arrays = gr.getArrayKeys();
        System.out.println("Arrays:");
        int count = 0;
        for (String array : arrays) {
            System.out.println("   - " + array);
//            ZarrArray zarrArray = gr.openArray(array);
//            System.out.println("      shape = " + Arrays.toString(zarrArray.getShape()));
//            System.out.println("      chunks = " + Arrays.toString(zarrArray.getChunks()));
//            System.out.println("      dataType = " + zarrArray.getDataType());
//            System.out.println("      fill = " + zarrArray.getFillValue());
//            System.out.println("      order = " + zarrArray.getByteOrder());
//            if (count == 0) {
//                int[] chunks = zarrArray.getChunks();
//                Number[] read = (Number[]) zarrArray.read(chunks, new int[chunks.length]);
//                Arrays.toString(read);
//            }
            count++;
        }
    }
}

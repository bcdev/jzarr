/*
 *
 * MIT License
 *
 * Copyright (c) 2020. Brockmann Consult GmbH (info@brockmann-consult.de)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

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
        String s3AccessKey = "<your access key>";
        String s3SecretKey = "<your secret key>";
        String s3Server = "s3.eu-central-1.amazonaws.com"; // example server name
        String s3BucketName = "bucket-abcd"; // example bucket name

        URI uri = URI.create(MessageFormat.format("s3://{0}:{1}@{2}", s3AccessKey, s3SecretKey, s3Server));
        FileSystem s3fs = FileSystems.newFileSystem(uri, null);
        Path bucketPath = s3fs.getPath("/" + s3BucketName);
        return bucketPath;
    }

    static void writeToS3Bucket(Path bucketPath) throws IOException, InvalidRangeException {
        Path groupPath = bucketPath.resolve("GroupName.zarr");
        ZarrGroup group = ZarrGroup.create(groupPath);
        ZarrArray array = group.createArray("AnArray", new ArrayParams()
                .shape(4, 8).chunks(2, 4).dataType(DataType.i1).compressor(CompressorFactory.nullCompressor));
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

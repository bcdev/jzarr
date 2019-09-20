.. _amazonS3:

How to use JZarr with AWS S3
============================
In general JZarr can work with :code:`java.nio.file.Path` objects. So if someone extends the abstract :code:`java.nio.file.FileSystem`
(see `FileSystem <https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileSystem.html>`_) to connect an AWS S3 bucket
this can be used to read from and write directly to such buckets.

In our example we use the `Amazon-S3-FileSystem-NIO2 <https://github.com/lasersonlab/Amazon-S3-FileSystem-NIO2>`_
library which is forked several times by other implementors.

If you want to try the following example, add this maven dependency to your pom::

 <dependency>
     <groupId>org.lasersonlab</groupId>
     <artifactId>s3fs</artifactId>
     <version>2.2.3</version>
 </dependency>

Below you can see code snippets for **connecting** with, **writing** to and **reading** from an s3 bucket.
You can find the entire example code here: `S3Array_nio.java <https://github.com/bcdev/jzarr/blob/master/docs/examples/java/S3Array_nio.java>`_

connect an s3 bucket
--------------------
Fill in your credentials.

.. highlight:: java
.. literalinclude:: ./examples/java/S3Array_nio.java
  :caption: `code example for connecting the s3 bucket <https://github.com/bcdev/jzarr/blob/master/docs/examples/java/S3Array_nio.java>`_
  :start-after: Path connectToS3Bucket()
  :end-before: return bucketPath
  :dedent: 8

write to an s3 bucket
---------------------
.. highlight:: java
.. literalinclude:: ./examples/java/S3Array_nio.java
  :caption: `code example writing to an s3 bucket <https://github.com/bcdev/jzarr/blob/master/docs/examples/java/S3Array_nio.java>`_
  :start-after: void writeToS3Bucket(
  :end-before: } ///
  :dedent: 8

read from an s3 bucket
----------------------
.. highlight:: java
.. literalinclude:: ./examples/java/S3Array_nio.java
  :caption: `code example reading from an s3 bucket <https://github.com/bcdev/jzarr/blob/master/docs/examples/java/S3Array_nio.java>`_
  :start-after: void readFromS3Bucket(
  :end-before: } ///
  :dedent: 8

The System.out should produce the following output::

 [11, 12, 13, 14, 15, 16, 17, 18, 21, 22, 23, 24, 25, 26, 27, 28, 31, 32, 33, 34, 35, 36, 37, 38, 41, 42, 43, 44, 45, 46, 47, 48]

.. _amazonS3:

How to write to an amazon S3 bucket
===================================
In general Jzarr can work with :code:`java.nio.file.Path` objects. So if someone extends the abstract :code:`java.nio.file.FileSystem`
(see `FileSystem <https://docs.oracle.com/javase/8/docs/api/java/nio/file/FileSystem.html>`_) to connect an AWS S3 bucket
this can be used to read and write directly to such buckets.

In our example we use the `Amazon-S3-FileSystem-NIO2 <https://github.com/lasersonlab/Amazon-S3-FileSystem-NIO2>`_
library which is forked several times by other implementors.

If you want to try the following example, add this maven dependency to your pom::

 <dependency>
     <groupId>org.lasersonlab</groupId>
     <artifactId>s3fs</artifactId>
     <version>2.2.3</version>
 </dependency>

Also therefor the example works fine, you need a :code:`s3.properties` file filled
with your s3 properties.

.. highlight:: properties
.. literalinclude:: ./examples/resources/s3-template.properties
  :caption: `example properties <https://github.com/bcdev/jzarr/blob/master/docs/examples/resources/s3-template.properties>`_



.. highlight:: java
.. literalinclude:: ./examples/java/S3Array_nio.java
  :caption: `code example for connecting the s3 bucket <https://github.com/bcdev/jzarr/blob/master/docs/examples/java/S3Array_nio.java>`_
  :start-after: Path getS3BucketNioPath(
  :end-before: throw new
  :dedent: 8

.. highlight:: java
.. literalinclude:: ./examples/java/S3Array_nio.java
  :caption: `code example writing to an s3 bucket <https://github.com/bcdev/jzarr/blob/master/docs/examples/java/S3Array_nio.java>`_
  :start-after: void writeToS3Bucket(
  :end-before: } ///
  :dedent: 8

.. highlight:: java
.. literalinclude:: ./examples/java/S3Array_nio.java
  :caption: `code example reading from an s3 bucket <https://github.com/bcdev/jzarr/blob/master/docs/examples/java/S3Array_nio.java>`_
  :start-after: void readFromS3Bucket(
  :end-before: } ///
  :dedent: 8





.. zarr documentation master file, created by
   sphinx-quickstart on Wen Aug  7 12:12:00 2019.

jzarr
=====

jzarr is a Java package providing an implementation of chunked,
compressed, N-dimensional arrays like the python zarr package.

Highlights
----------

* Create N-dimensional arrays with java primitive data types. At the moment except boolean and char type.
* Chunk arrays along any dimension.
* Compress and/or filter chunks.
* Store arrays in memory, on disk, (not now ... inside a Zip file, on S3, ...
* Read an array concurrently from multiple threads or processes.
* Write to an array concurrently from multiple threads or processes.
* Organize arrays into hierarchies via groups.

Status
------

jzarr is still a young project. Feedback and bug reports are very welcome, please get in touch via
the `GitHub issue tracker <https://github.com/bcdev/jzarr/issues>`_.

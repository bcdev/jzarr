.. _zarr package: https://zarr.readthedocs.io/en/stable/index.html

JZarr documentation
===================

JZarr is a Java library providing an implementation of chunked,
compressed, N-dimensional arrays close to the Python `zarr package`_.

Highlights
----------

* Create N-dimensional arrays with java primitive data types. At the moment boolean and char type are not supported.
* Chunk arrays along any dimension.
* Compress the chunks by using one of the :ref:`compressors <compressors>`
* Store arrays in memory, on disk, (Future plans: inside a Zip file, on S3, ...)
* Read an array concurrently from multiple threads or processes.
* Write an array concurrently from multiple threads or processes.
* Organize arrays into hierarchies via groups.

Status
------

JZarr is in the very beginning phase. Feedback and bug reports are very welcome. Please get in touch via
the `GitHub issue tracker <https://github.com/bcdev/jzarr/issues>`_.

Requirements
------------
Java
^^^^
JZarr needs Java 8 or higher.

blosc
^^^^^
- This API also offers blosc compression. :raw-html:`<br>`
  To use this compression, a compiled c-blosc distributed library must be available on the operating system.
- If such a library is not available ... The C sourcecode and instructions to build the library can be
  found at https://github.com/Blosc/c-blosc.
- If you want to use the JZarr API and the integrated blosc compression, you have to start the Java Virtual Machine
  with the following VM parameter: ::

  -Djna.library.path=<path which contains the compiled c-blosc library>

Maven Dependency
----------------

To use JZarr in your project integrate the following lines into your maven pom.xml::

 <dependency>
    <groupId>dev.zarr</groupId>
    <artifactId>jzarr</artifactId>
    <version>0.4.2</version>
 </dependency>


.. API Examples
.. ------------
..
.. .. include:: examples.rst
..   :start-after: **intro start**
..   :end-before: **intro end**
..
.. :ref:`Read more ... <examples>`
..

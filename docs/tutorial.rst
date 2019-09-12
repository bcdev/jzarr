.. _tutorial:
.. _zarr package: https://zarr.readthedocs.io/en/stable/index.html
.. _Nd4j: https://deeplearning4j.org/docs/latest/nd4j-overview

Tutorial
========
JZarr provides classes and functions to handle N-dimensional arrays data
whose data can be divided into chunks and each chunk can be compressed.

General Information
-------------------
In the JZarr API, data inputs and outputs are allways one-dimensional arrays of primitive
Java types ``double``, ``float``, ``long``, ``int``, ``short``, ``byte``.
Users have to specify the N-dimensionality of the data by a shape parameter requested
by many of the JZarr API operations.

To read or write data portions to or from the array, a shape describing the portion
and an offset is needed. The zarr array offsets are zero-based (:code:`0`).

| **For Example:**
| If you need to write data to the upper left corner of a 2 dimensional zarr array you have to use an offset
  of :code:`new int[]{0, 0}`.

.. note::
   All data persisted using this API can be read in with the Python zarr API without limmitations.

If you are already familiar with the Python `zarr package`_ then JZarr
provide similar functionality, but without NumPy array behavior.

If you need array objects which behave almost like NumPy arrays you can wrap the data
using ND4J INDArray `from deeplearning4j.org <Nd4j>`_.
You can find examples in the data writing and reading examples below.

Alternatively you can use :code:`ucar.ma2.Array` from `netcdf-java Common Data Model
<https://github.com/Unidata/netcdf-java/blob/master/README.md>`_ to wrap the data.

.. _tutorial_create:

Creating an array
-----------------
JZarr has several functions for creating arrays. For example:

.. highlight:: java
.. literalinclude:: ./examples/java/Tutorial_rtd.java
  :caption: `example 1 from Tutorial_rtd.java <https://github.com/bcdev/jzarr/blob/master/docs/examples/java/Tutorial_rtd.java>`_
  :start-after: void example_1(
  :end-before: createOutput
  :dedent: 8


A :code:`System.out.println(array);` then creates the following output

.. highlight:: none
.. literalinclude:: ./examples/output/Tutorial_rtd.txt
   :caption: output
   :start-after: example_1_output_start
   :end-before: __output_end__

The code above creates a 2-dimensional array of 32-bit integers with 10000 rows and 10000 columns,
divided into chunks where each chunk has 1000 rows and 1000 columns (and so there will be 100 chunks in total).

For a complete list of array creation routines see the :ref:`array creation <array_creation>` module documentation.

.. _tutoral_writing_and_reading_data:

Writing and reading data
------------------------
This example shows how to write and read a region to an array.

Creates an array with size [5 rows, 7 columns], with data type :code:`int` and with a fill value of :code:`-9999`.

.. highlight:: java
.. literalinclude:: ./examples/java/Tutorial_rtd.java
  :caption: `example 2 from Tutorial_rtd.java <https://github.com/bcdev/jzarr/blob/master/docs/examples/java/Tutorial_rtd.java>`_
  :start-after: example 2 code snippet 1 begin
  :end-before: example 2 code snippet 1 end
  :dedent: 8

Prepare the data which should be written to the array with a shape of [3, 5] and an offset of [1, 1].

.. literalinclude:: ./examples/java/Tutorial_rtd.java
  :start-after: example 2 code snippet 2 begin
  :end-before: example 2 code snippet 2 end
  :dedent: 8

Write the prepared data.

.. literalinclude:: ./examples/java/Tutorial_rtd.java
  :start-after: example 2 code snippet 3 begin
  :end-before: example 2 code snippet 3 end
  :dedent: 8

Read the entire data from the array.

.. literalinclude:: ./examples/java/Tutorial_rtd.java
  :start-after: example 2 code snippet 4 begin
  :end-before: example 2 code snippet 4 end
  :dedent: 8

Print out the data read.

.. literalinclude:: ./examples/java/Tutorial_rtd.java
  :start-after: example 2 code snippet 5 begin
  :end-before: example 2 code snippet 5 end
  :dedent: 8

Creates the following output

.. highlight:: none
.. literalinclude:: ./examples/output/Tutorial_rtd.txt
   :caption: output
   :start-after: example_2_output_start
   :end-before: __output_end__


The output displays that the data written before (written with an offset of [1, 1]) is surrounded by the fill value :code:`-9999`.

.. note::
   `Nd4j <Nd4j>`_ is not part of the JZarr library. It is only used in this showcase to demonstrate how the data can be used.

.. _tutoral_persistent_arrays:

Persistent arrays
-----------------
In the examples above, compressed data (default compressor) for each chunk of the array was stored
in main memory. JZarr arrays can also be stored on a file system, enabling persistence of data
between sessions. For example:

.. highlight:: java
.. literalinclude:: ./examples/java/Tutorial_rtd.java
  :caption: `example 3 from Tutorial_rtd.java <https://github.com/bcdev/jzarr/blob/master/docs/examples/java/Tutorial_rtd.java>`_
  :start-after: example 3 code snippet 1 begin
  :end-before: example 3 code snippet 1 end
  :dedent: 8

The array above will store its configuration metadata (zarr header :code:`.zarray`) and all compressed chunk data in a
directory called ‘docs/examples/output/example_3.zarr’ relative to the current working directory.

The created zarr header file `.zarray <https://github.com/bcdev/jzarr/blob/master/docs/examples/output/example_3.zarr/.zarray>`_ written in JSON format.

.. highlight:: json
.. literalinclude:: ./examples/output/example_3.zarr/.zarray

Write some data to the created persistent array.

.. highlight:: java
.. literalinclude:: ./examples/java/Tutorial_rtd.java
  :start-after: example 3 code snippet 2 begin
  :end-before: example 3 code snippet 2 end
  :dedent: 8

.. Note::

   There is no need to close an array. Data are automatically flushed to disk, and files are automatically
   closed whenever an array is modified.

Then we can reopen the array and read the data

.. literalinclude:: ./examples/java/Tutorial_rtd.java
  :start-after: example 3 code snippet 3 begin
  :end-before: example 3 code snippet 3 end
  :dedent: 8

Which now looks like:

.. highlight:: none
.. literalinclude:: ./examples/output/Tutorial_rtd.txt
   :caption: output
   :start-after: example_3_output_start
   :end-before: __output_end__

Resizing and appending
----------------------
Currently not implemented.

Compressors
-----------
A number of different compressors can be used with JZarr. Different compressors can be provided
via the compressor parameter e.g.:

.. literalinclude:: ./examples/java/Tutorial_rtd.java
  :caption: `example 4 from Tutorial_rtd.java <https://github.com/bcdev/jzarr/blob/master/docs/examples/java/Tutorial_rtd.java>`_
  :start-after: void example_4(
  :end-before: }
  :dedent: 8

.. note::
   In this very beginning phase we only implemented the **"zlib"** compressor and a **"null"** compressor.
   If no compressor is set at array creation time, a **"zlib"** compressor with :code:`level 1` is used as
   default compressor.
   More compressors will be implemented in the future.
   
   Additionally, in the future, developers should be able to register their own Compressors in the CompressorFactory.
   A compressor must extend the abstract Compressor class.

Filters
-------
Currently not implemented.

Groups
------
JZarr supports hierarchical organization of arrays via groups. As with arrays, groups can be stored in memory, on disk,
or via other storage systems that support a similar interface.

To create a group, use the suitable static ZarrGruup.create() method.

In the following example you can see:

- how to create a group
- how to create sub groups
- how to create arrays within a group

.. highlight:: java
.. literalinclude:: ./examples/java/Tutorial_rtd.java
  :caption: `example 5 from Tutorial_rtd.java <https://github.com/bcdev/jzarr/blob/master/docs/examples/java/Tutorial_rtd.java>`_
  :start-after: void example_5(
  :end-before: createOutput
  :dedent: 8

A :code:`System.out.println(array);` then creates the following output

.. highlight:: none
.. literalinclude:: ./examples/output/Tutorial_rtd.txt
   :caption: output
   :start-after: example_5_output_start
   :end-before: __output_end__


User attributes
---------------
JZarr arrays and groups support custom key/value attributes, which can be useful for
storing application-specific metadata. For example:

.. highlight:: java
.. literalinclude:: ./examples/java/Tutorial_rtd.java
  :caption: `example 6 from Tutorial_rtd.java <https://github.com/bcdev/jzarr/blob/master/docs/examples/java/Tutorial_rtd.java>`_
  :start-after: void example_6(
  :end-before: createOutput
  :dedent: 8

You can easily print out the attributes content using :code:`System.out.println(ZarrUtils.toJson(attributes, true));`

.. highlight:: none
.. literalinclude:: ./examples/output/Tutorial_rtd.txt
   :caption: output
   :start-after: example_6_output_start
   :end-before: __output_end__

.. note::
   If you take user attributes from a group or an array modifications (put, replace or remove) on
   the attributes are not automatically stored. The :code:`writeAttributes()` from ZarrGroup or
   ZarrArray must be used to restore the changed attributes.

Internally JZarr uses JSON to store array attributes, so attribute values must be JSON serializable.

Partly reading writing
----------------------
JZarr ZarrArrays enable a subset of data items to be extracted or updated in an array without
loading the entire array into memory.

.. highlight:: java
.. literalinclude:: ./examples/java/Tutorial_rtd.java
  :caption: `example 7 from Tutorial_rtd.java <https://github.com/bcdev/jzarr/blob/master/docs/examples/java/Tutorial_rtd.java>`_
  :start-after: void example_7(
  :end-before: ///
  :dedent: 8

The output shows the data is written with the given shape to the offset position.

.. highlight:: none
.. literalinclude:: ./examples/output/Tutorial_rtd.txt
   :caption: output
   :start-after: example_7_output_start
   :end-before: __output_end__

Partly read and write is also used in examples above. See `Persistent arrays`_

Chunk size an shape
-------------------
In general, chunks of at least 1 megabyte (1M) uncompressed size seem to provide good performance,
at least when using compression too.

The optimal chunk shape will depend on how you want to access the data. E.g., for a 2-dimensional
array, if you only ever take slices along the first dimension, then chunk across the second
dimenson. If you know you want to chunk across an entire dimension you can use :code:`0` or
:code:`negative value` within the chunks argument, e.g.:

.. highlight:: java
.. literalinclude:: ./examples/java/Tutorial_rtd.java
  :caption: `example 8 from Tutorial_rtd.java <https://github.com/bcdev/jzarr/blob/master/docs/examples/java/Tutorial_rtd.java>`_
  :start-after: void example_8(
  :end-before: createOutput
  :dedent: 8

The output shows the automatically replaced :code:`0` with full size of the first dimension.

.. highlight:: none
.. literalinclude:: ./examples/output/Tutorial_rtd.txt
   :caption: output
   :start-after: example_8_output_start
   :end-before: __output_end__

Alternatively, if you only ever take slices along the second dimension, then chunk across the first dimension, e.g.:

.. highlight:: java
.. literalinclude:: ./examples/java/Tutorial_rtd.java
  :caption: `example 9 from Tutorial_rtd.java <https://github.com/bcdev/jzarr/blob/master/docs/examples/java/Tutorial_rtd.java>`_
  :start-after: void example_9(
  :end-before: createOutput
  :dedent: 8

The output shows the automatically replaced :code:`0` with full size of the second dimension.

.. highlight:: none
.. literalinclude:: ./examples/output/Tutorial_rtd.txt
   :caption: output
   :start-after: example_9_output_start
   :end-before: __output_end__

If you require reasonable performance for both access patterns then you need to find a compromise, e.g.:

.. highlight:: java
.. literalinclude:: ./examples/java/Tutorial_rtd.java
  :caption: `example 10 from Tutorial_rtd.java <https://github.com/bcdev/jzarr/blob/master/docs/examples/java/Tutorial_rtd.java>`_
  :start-after: void example_10(
  :end-before: createOutput
  :dedent: 8

If you are feeling lazy, you can let JZarr guess a chunk shape for your data by providing
:code:`chunked(True)` E.g.:

.. highlight:: java
.. literalinclude:: ./examples/java/Tutorial_rtd.java
  :caption: `example 11 from Tutorial_rtd.java <https://github.com/bcdev/jzarr/blob/master/docs/examples/java/Tutorial_rtd.java>`_
  :start-after: void example_11(
  :end-before: createOutput
  :dedent: 8

.. highlight:: none
.. literalinclude:: ./examples/output/Tutorial_rtd.txt
   :caption: output
   :start-after: example_11_output_start
   :end-before: __output_end__

.. note::
    The algorithm for guessing a chunk shape is based on simple heuristics and may be far from optimal.

If you know you are always going to be loading the entire array into memory, you can turn off chunks by
providing :code:`chunked(false)`, in which case there will be one single chunk for the array:

.. highlight:: java
.. literalinclude:: ./examples/java/Tutorial_rtd.java
  :caption: `example 12 from Tutorial_rtd.java <https://github.com/bcdev/jzarr/blob/master/docs/examples/java/Tutorial_rtd.java>`_
  :start-after: void example_12(
  :end-before: createOutput
  :dedent: 8

.. highlight:: none
.. literalinclude:: ./examples/output/Tutorial_rtd.txt
   :caption: output
   :start-after: example_12_output_start
   :end-before: __output_end__

Chunk memory layout
-------------------
The order of bytes within chunks of an array can be changed via the :code:`byteOrder()` parameter.
To use either :code:`ByteOrder.BIG_ENDIAN` or :code:`ByteOrder.LITTLE_ENDIAN` layout. E.g.:

.. highlight:: java
.. literalinclude:: ./examples/java/Tutorial_rtd.java
  :caption: `example 13 from Tutorial_rtd.java <https://github.com/bcdev/jzarr/blob/master/docs/examples/java/Tutorial_rtd.java>`_
  :start-after: void example_13(
  :end-before: createOutput
  :dedent: 8

These two layouts may provide different compression ratios, depending on the correlation structure within
the data. Changing the order of bytes within chunks of an array may improve the compression ratio, depending
on the structure of the data, the compression algorithm/level is used.

.. note::
    If Byte order is not set, the default Byte order :code:`ByteOrder.BIG_ENDIAN` will be used.

Parallel computing and synchronisation
--------------------------------------
Basically zarr arrays have been designed for use as the source or sink for data in parallel computations.
By data source we mean that multiple concurrent read operations may occur. By data sink we mean
that multiple concurrent write operations may occur, with each writer updating a different array.

Additionally JZarr is thread save in cases if multiple write operations occur on the same array on
the same chunk concurrently from within different threads. This works without data loss.

Currently JZarr is not process save. This means, that if multiple write operations occur on the
same array on the same chunk concurrently from within different processes, this can lead to data loss.
Process synchronizing will be implemented in the future too.

When using a JZarr array as a data sink, some synchronization (locking) may be required to avoid
data loss, depending on how data are being updated.

If each worker in a parallel computation is writing to a separate region of the array, and if
region boundaries are perfectly aligned with chunk boundaries, then no synchronization is required.
However, if region and chunk boundaries are not perfectly aligned, then synchronization is required
to avoid two workers attempting to modify the same chunk at the same time, which could result in
data loss.

To give a simple example, consider a 1-dimensional array of length 30, z, divided into three
chunks of 10 elements each. If three worker processes are running and each attempts to write to a 10
element region (i.e., offset {0}, offset {10} and offset {20}) then each worker will be writing to a
separate chunk and no synchronization is required.

.. highlight:: java
.. literalinclude:: ./examples/java/Tutorial_rtd.java
  :caption: `example 14 from Tutorial_rtd.java <https://github.com/bcdev/jzarr/blob/master/docs/examples/java/Tutorial_rtd.java>`_
  :start-after: void example_14(
  :end-before: createOutput
  :dedent: 8
.. highlight:: none
.. literalinclude:: ./examples/output/Tutorial_rtd.txt
   :caption: output
   :start-after: example_14_output_start
   :end-before: __output_end__


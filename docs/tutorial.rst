.. _tutorial:
.. _zarr package: https://zarr.readthedocs.io/en/stable/index.html

Tutorial
========
JZarr provides classes and functions to handle N-dimensional arrays data
whose data can be divided into chunks and each chunk can be compressed.
If you are already familiar with the python `zarr package`_ then JZarr
provide similar functionality, but without NumPy array behavior.

If you need array objects which behave almost like NumPy arrays you can wrap the data
using ND4J INDArray `from deeplearning4j.org <https://deeplearning4j.org/docs/latest/nd4j-overview>`_.
You can find examples in the data writing and reading examples below.

Alternatively you can use :code:`ucar.ma2.Array` from `netcdf-java Common Data Model
<https://github.com/Unidata/netcdf-java/blob/master/README.md>`_ to wrap the data.

.. _tutorial_create:

Creating an array
-----------------
JZarr has several functions for creating arrays. For example:

.. highlight:: java

.. literalinclude:: ./examples/java/Tutorial.java
  :caption: `example 1 from Tutorial.java <https://github.com/bcdev/jzarr/blob/master/docs/examples/java/Tutorial.java>`_
  :start-after: void example_1
  :end-before: createOutput
  :dedent: 8

.. highlight:: none

A :code:`System.out.println(array);` then creates the following output

.. literalinclude:: ./examples/output/Tutorial_example_1.txt

The code above creates a 2-dimensional array of 32-bit integers with 10000 rows and 10000 columns,
divided into chunks where each chunk has 1000 rows and 1000 columns (and so there will be 100 chunks in total).

For a complete list of array creation routines see the :ref:`array creation <array_creation>` module documentation.

.. _tutoral_writing_and_reading_data:

Writing and reading data
------------------------
This example shows how to write a region to an existing array.

1. First creates an array with size [5, 7] and with a fill value of :code:`-9999`.
2. Then write data with a shape of [3, 5] and an offset of [1, 1] to the center of the array.
3. Read the entire data from the array (int[] with size 5 * 7)
4. | Print out the red data and we can see the data written before
   | is surrounded by the fill value :code:`-9999`.

.. highlight:: java

.. literalinclude:: ./examples/java/Tutorial.java
  :caption: `example 2 from Tutorial.java <https://github.com/bcdev/jzarr/blob/master/docs/examples/java/Tutorial.java>`_
  :start-after: void example_2
  :end-before: createOutput
  :dedent: 8

.. highlight:: none

Creates the following output

.. literalinclude:: ./examples/output/Tutorial_example_2.txt

| The output displays that the data is written (with an offset of [1, 1]) to the center of the array.
| The written data is surrounded by a :code:`-9999` value border which is the fill value defined above.

.. _tutoral_persistent_arrays:

Persistent arrays
-----------------
In the examples above, compressed data (default compressor) for each chunk of the array was stored
in main memory. JZarr arrays can also be stored on a file system, enabling persistence of data
between sessions. For example:

.. highlight:: java

.. literalinclude:: ./examples/java/Tutorial.java
  :caption: `example 3 from Tutorial.java <https://github.com/bcdev/jzarr/blob/master/docs/examples/java/Tutorial.java>`_
  :start-after: void example_3
  :end-before: createOutput
  :dedent: 8

.. highlight:: none

The array above will store its configuration metadata (zarr header :code:`.zarray`) and all compressed chunk data in a
directory called ‘docs/examples/output/example_3.zarr’ relative to the current working directory.

Note that there is no need to close an array: data are automatically flushed to disk, and files are automatically
closed whenever an array is modified.

Creates the following output

.. literalinclude:: ./examples/output/Tutorial_example_3.txt

.. literalinclude:: ./examples/output/example_3.zarr/.zarray
   :caption: `the zarr header written as JSON file <https://github.com/bcdev/jzarr/blob/master/docs/examples/output/example_3.zarr/.zarray>`_


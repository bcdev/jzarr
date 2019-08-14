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

For a complete list of array creation routines see the :ref:`array_creation`_ module documentation.
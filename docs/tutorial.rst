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

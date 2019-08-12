.. _arrayCreation:

Array Creation
==============
.. **intro start**

| JZarr has several functions for creating arrays.
| Arrays can be created with or without a given storage.
| If no storage is given a default in memory store will be used instead.
| Arrays can be created with or without additional user defined attributes.

| At least there is only one mandatory information which must be given at creation time.
| To create an instance of com.bc.zarr.ZarrArray at least a shape must be given


.. literalinclude:: ../src/main/examples/array/creation/InMemoryArray.java
  :caption: `InMemoryArray.java <https://github.com/bcdev/jzarr/blob/master/src/main/examples/array/creation/InMemoryArray.java>`_
  :start-after: snippet 1
  :end-before: end 1
  :dedent: 8

A :code:`System.out.println(array);` then creates the following output::

 com.bc.zarr.ZarrArray{shape=[10, 8], chunks=[10, 8], dataType=f8, fillValue=0, compressor=zlib/level=1, store=InMemoryStore, byteOrder=BIG_ENDIAN}
The output describes that an array with the following characteristics has been created

=================  ==========================================
property           value
=================  ==========================================
shape dimensions   y:10 x:8
chunks dimensions  also y:10 x:8
data type          :ref:`f8 <data-types>` :code:`default`
data type          :ref:`f8 <dtype>` :code:`default`
fill value         :code:`0`
compressor         zlib compressor with level 1 :code:`default`
store              InMemoryStore :code:`default`
byte order         BIG_ENDIAN :code:`default`
=================  ==========================================

Why are chunks dimensions the same as shape dimension?
  | If chunks is not given, the default chunks, with a size of 512 in each dimension, will be applied.
  | If a chunk dimension is bigger than a shape dimension itself, the chunk dimension will be trimmed to shape.


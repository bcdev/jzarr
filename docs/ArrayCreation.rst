.. _f8: :ref:`_ <data types>`

Array Creation
==============
Zarr has several functions for creating arrays.

.. literalinclude:: ../src/main/examples/array/creation/InMemoryArray.java
  :caption: `InMemoryArray.java <https://github.com/bcdev/jzarr/blob/master/src/main/examples/array/creation/InMemoryArray.java>`_
  :start-after: snippet 1
  :end-before: end 1
  :dedent: 8

A :code:`System.out.println(array);` then creates the following output::

 com.bc.zarr.ZarrArray{shape=[10, 8], chunks=[10, 8], dataType=i4, fillValue=0, compressor=zlib/level=1, store=InMemoryStore, byteOrder=BIG_ENDIAN}

The output describes that an array with the following characteristics has been created

=================  ==========================================
property           value
=================  ==========================================
shape dimensions   y:10 x:8
chunks dimensions  also y:10 x:8
data type          f8_ :code:`default` .. means java primitive :code:`double`
fill value         :code:`0`
compressor         zlib compressor with level 1 :code:`default`
store              InMemoryStore :code:`default`
byte order         BIG_ENDIAN :code:`default`
=================  ==========================================

Why the same dimensions as the shape ?
  | If chunks is not given, the default chunks will be applied. 512 in each dimension.
  | If a chunk dimension is bigger than a shape dimension, the chunk dimension will be trimmed to shape.


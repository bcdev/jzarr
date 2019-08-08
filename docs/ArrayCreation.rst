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

As you can see, an array with the following characteristics has been created

* shape dimensions = y:10 x:8
* chunks dimensions = also y:10 x:8
* data type = i4 ... means java primitive :code:`int`
* fill value = :code:`0`
* compressor = zlib compressor with level 1 ``(default)``
* store = InMemoryStore ``(default)``
* byte order = BIG_ENDIAN ``(default)``

Why the same dimensions as the shape ?
  asda
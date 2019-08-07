Array Creation
==============

Zarr has several functions for creating arrays.

.. Code snippet from: `Creating an Array <https://github.com/bcdev/jzarr/blob/master/docs/examples/CreatingAnArray.java>`_
Code snippet from: `Creating an Array <./examples/CreatingAnArray.java>`_

.. literalinclude:: ./examples/CreatingAnArray.java
  :language: java
  :lines: 7-11
  :dedent: 8

creates the following output::

    com.bc.zarr.ZarrArray{shape=[10000, 10000], chunks=[1000, 1000], dataType=i4, fillValue=0, compressor=zlib/level=1, store=InMemoryStore, byteOrder=BIG_ENDIAN}


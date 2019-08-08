Array Creation
==============

Zarr has several functions for creating arrays.

Code snippet from: `/docs/examples/CreatingAnArray.java <https://github.com/bcdev/jzarr/blob/master/docs/examples/CreatingAnArray.java>`_

.. literalinclude:: ../src/main/examples/array/creation/CreatingAnArray.java
  :caption: Code snippet from <../src/main/examples/array/creation/CreatingAnArray.java>
  :language: java
  :start-after: main(String[] args)
  :end-before: System.out.println(array)
  :dedent: 8

creates the following output::

    com.bc.zarr.ZarrArray{shape=[10000, 10000], chunks=[1000, 1000], dataType=i4, fillValue=0, compressor=zlib/level=1, store=InMemoryStore, byteOrder=BIG_ENDIAN}


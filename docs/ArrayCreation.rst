Array Creation
==============

Zarr has several functions for creating arrays. For example::

.. literalinclude:: examples/CreatingAnArray.java
   :caption: 
   :language: java
   :pyobject: CreatingAnArray.main
   :dedent: 8

creates the following output::

    com.bc.zarr.ZarrArray{shape=[10000, 10000], chunks=[1000, 1000], dataType=i4, fillValue=0, compressor=zlib/level=1, store=InMemoryStore, byteOrder=BIG_ENDIAN}


Array Creation
==============

Zarr has several functions for creating arrays. For example::

    ZarrArray array = ZarrArray.create(new ArrayParams()
            .withShape(10000, 10000).withChunks(1000, 1000).withDataType(DataType.i4)
    );

    System.out.println(array);

see `CreatingAnArray.java <https://github.com/bcdev/jzarr/blob/master/src/main/java/com/bc/zarr/examples/CreatingAnArray.java>`_

.. literalinclude:: ./examples/CreatingAnArray.java
   :language: java
   :emphasize-lines: 8-12
   :linenos:

creates the following output::

    com.bc.zarr.ZarrArray{shape=[10000, 10000], chunks=[1000, 1000], dataType=i4, fillValue=0, compressor=zlib/level=1, store=InMemoryStore, byteOrder=BIG_ENDIAN}





# jzarr

A Java version of the API offered by the wonderful Python [zarr](https://zarr.readthedocs.io/) package.


## Tutorial

Zarr provides classes and functions for working with N-dimensional array data.  
The data can be divided into chunks (default) and each chunk can be saved compressed.  
If you are already familiar with HDF5 then Zarr provide similar functionality, but
with some additional flexibility.

### Creating an Array

Zarr has several functions for creating arrays. For example:

~~~
ZarrArray array = ZarrArray.create(new ArrayParams()
        .withShape(10000, 10000).withChunks(1000, 1000).withDataType(DataType.i4)
);

System.out.println(array);
~~~
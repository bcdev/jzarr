# JZarr

A Java version of the API offered by the wonderful Python [zarr](https://zarr.readthedocs.io/) package.


This API also uses blosc compression. To use this compression, a compiled c-blosc distributed library must be available on the operating system. 
If such a library is not available ... The C sourcecode and instructions to build the library can be found at https://github.com/Blosc/c-blosc.
If you want to use the JZarr API and the integrated blosc compression, you have to start the Java Virtual Machine with the following VM parameter:

    -Djna.library.path=<path which contains the compiled c-blosc library>

## Documentation
You can find detailed information at [JZarrs project documentation](https://jzarr.readthedocs.io) page.  


## Build Documentation
The projects documentation is automatically generated and provided using "Read the Docs".
Some files, needed to build the "**read the docs**" documentation must be generated to be up to
date and along with the code. Therefor a class has been implemented which searches for
classes whose filename ends with "_rtd.java" and invokes whose **`main`** method.
Rerun the class (**`ExecuteForReadTheDocs`**) to autogenerate these
referenced files.
# JZarr

A Java version of the wonderful Python [zarr](https://zarr.readthedocs.io/) API.

## Build Documentation
This API is able to use blosc compression. To use this compression, a compiled c-blosc distributed library must be available on the operating system. 
If such a library is not available ... The C sourcecode and instructions to build the library can be found at https://github.com/Blosc/c-blosc.
If you want to use the JZarr API and the integrated blosc compression, you have to start the Java Virtual Machine with the following VM parameter:

    -Djna.library.path=<path which contains the compiled c-blosc library>

In order to include the blosc library for testing during the build process you need to create a copy of the 
`template.blosc.properties` file and rename the copy to `blosc.properties`. 
Provide the path to the `bloscJnaLibraryPath` property as explained in the comments of within the file.

## Library Documentation
The latest detailed information can be found at [JZarrs project documentation](https://jzarr.readthedocs.io) page.  

## Generating the Documentation
The projects documentation is automatically generated and provided using "Read the Docs".
Some files, needed to build the "**Read the Docs**" documentation must be generated to be up to
date and along with the code. Therefore, a class has been implemented which searches for
classes whose filename ends with "_rtd.java" and invokes there `main` method.
Rerun the class (`ExecuteForReadTheDocs`) to autogenerate these
referenced files. The `bloscJnaLibraryPath` is also necessary for generating the "Read the Docs" files.

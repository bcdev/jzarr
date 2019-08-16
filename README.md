# JZarr

A Java version of the API offered by the wonderful Python [zarr](https://zarr.readthedocs.io/) package.

## Documentation
You can find detailed information at [JZarrs project documentation](https://jzarr.readthedocs.io) page.  


## Build Documentation
The projects documentation is automatically generated and provided using "Read the Docs".
Some files, needed to build the "**read the docs**" documentation must be generated to be up to
date and along with the code. Therefor a class has been implemented which searches for
classes whose filename ends with "_rtd.java" and invokes whose **`main`** method.
Rerun the class (**`ExecuteForReadTheDocs`**) to autogenerate these
referenced files.
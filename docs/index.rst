.. zarr documentation master file, created by
   sphinx-quickstart on Wen Aug  7 12:12:00 2019.

.. _zarr package: https://zarr.readthedocs.io/en/stable/index.html

JZarr documentation
===================

JZarr is a Java library providing an implementation of chunked,
compressed, N-dimensional arrays close to the python `zarr package`_.

Highlights
----------

* Create N-dimensional arrays with java primitive data types. At the moment except boolean and char type.
* Chunk arrays along any dimension.
* Compress and/or filter chunks.
* Store arrays in memory, on disk, (not now ... inside a Zip file, on S3, ...
* Read an array concurrently from multiple threads or processes.
* Write to an array concurrently from multiple threads or processes.
* Organize arrays into hierarchies via groups.

Status
------

jzarr is still a young project. Feedback and bug reports are very welcome, please get in touch via
the `GitHub issue tracker <https://github.com/bcdev/jzarr/issues>`_.

Api Examples
------------

.. include:: examples.rst
  :start-after: **intro start**
  :end-before: **intro end**

:ref:`Read more ... <examples>`

.. toctree::
   :hidden:
   :maxdepth: 1

   index
   examples
   arrayParams
   ArrayCreation
   datatype

package org.esa.snap.dataio.znap.snap;

import static org.esa.snap.dataio.znap.snap.ZnapConstantsAndUtils.*;
import static com.bc.zarr.ZarrConstants.*;

import org.esa.snap.core.dataio.DecodeQualification;
import org.esa.snap.core.dataio.ProductReader;
import org.esa.snap.core.dataio.ProductReaderPlugIn;
import org.esa.snap.core.util.io.SnapFileFilter;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ZarrProductReaderPlugIn implements ProductReaderPlugIn {

    @Override
    public DecodeQualification getDecodeQualification(Object input) {
        final Path productRoot = convertToPath(input);
        if (productRoot == null) {
            return DecodeQualification.UNABLE;
        }
        final Path productHeader = productRoot.resolve(FILENAME_DOT_ZGROUP);
        final boolean isValidRootDirName = productRoot.getFileName().toString().toLowerCase().endsWith(SNAP_ZARR_CONTAINER_EXTENSION);
        final boolean productRootIsDirectory = Files.isDirectory(productRoot);
        final boolean productHeaderExist = Files.exists(productHeader);
        final boolean productHeaderIsFile = Files.isRegularFile(productHeader);
        if (isValidRootDirName
            && productRootIsDirectory
            && productHeaderExist
            && productHeaderIsFile
        ) {
            try {
                final Stream<Path> stream = Files.find(productRoot, 3,
                                                       (path, basicFileAttributes) -> Files.isRegularFile(path) && path.endsWith(FILENAME_DOT_ZARRAY),
                                                       FileVisitOption.FOLLOW_LINKS);
                final List<Path> pathList = stream.collect(Collectors.toList());
                if (pathList.size() > 0) {
                    return DecodeQualification.INTENDED;
                }
            } catch (IOException e) {
                // ignore
            }
        }
        return DecodeQualification.UNABLE;
    }

    @Override
    public Class[] getInputTypes() {
        return IO_TYPES;
    }

    @Override
    public ProductReader createReaderInstance() {
        return new ZarrProductReader(this);
    }

    @Override
    public String[] getFormatNames() {
        return new String[]{FORMAT_NAME};
    }

    @Override
    public String[] getDefaultFileExtensions() {
        return new String[]{SNAP_ZARR_CONTAINER_EXTENSION};
    }

    @Override
    public String getDescription(Locale locale) {
        return FORMAT_NAME + " product reader";
    }

    @Override
    public SnapFileFilter getProductFileFilter() {
        return new SnapFileFilter(getFormatNames()[0], getDefaultFileExtensions(), getDescription(null)) {
            @Override
            public FileSelectionMode getFileSelectionMode() {
                return FileSelectionMode.DIRECTORIES_ONLY;
            }
        };
    }
}

package org.ericghara.checker.interfaces;

import java.io.IOException;
import java.nio.file.Path;

public interface Hasher {

    String hash(Path path, String algo) throws IOException;
}

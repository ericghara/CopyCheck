package org.ericghara.checker;

import org.ericghara.parser.Interfaces.FileHashInterface;

public record FileHash(int lineNum, String path, String hash) implements FileHashInterface {}

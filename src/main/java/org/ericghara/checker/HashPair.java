package org.ericghara.checker;

import org.ericghara.parser.Interfaces.FileHashInterface;

public record HashPair(FileHashInterface expected, FileHashInterface found) {}

package org.ddmac.capml.exceptions

class CapmlFileFormatException(
    msg: String = "Invalid Filetype. Please provide a .capml file."
): IllegalArgumentException(msg) {}
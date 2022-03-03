package org.ddmac.capml.exceptions

/**
 * @author Dennis Capone
 */

class CapmlFileFormatException(
    msg: String = "Invalid Filetype. Please provide a .capml file."
): InvalidCapmlException(msg) {}
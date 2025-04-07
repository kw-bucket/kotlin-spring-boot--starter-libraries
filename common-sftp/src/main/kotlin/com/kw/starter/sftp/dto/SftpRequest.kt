package com.kw.starter.sftp.dto

import java.nio.file.Path

data class SftpRequest(
    val command: Command,
    val localPath: Path,
    val remotePath: Path,
) {
    enum class Command {
        Upload,
        Download,
    }
}

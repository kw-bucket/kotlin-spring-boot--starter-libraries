package com.kw.starter.sftp.service

import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.Session
import com.jcraft.jsch.SftpException
import com.kw.starter.sftp.configuration.property.SftpProperty
import com.kw.starter.sftp.dto.SftpRequest
import com.kw.starter.sftp.dto.SftpResult
import com.kw.starter.sftp.manager.SftpConnectionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.nio.file.Path

abstract class SftpService(private val sftpProperty: SftpProperty) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun executeBulk(
        requests: List<SftpRequest>,
        concurrent: Int = requests.size,
        sftpSession: Session? = null,
    ): List<SftpResult> {
        val session =
            sftpSession
                ?: SftpConnectionManager.createSession(sftpProperty)
                ?: throw Exception("Create Session Failure!")

        val semaphore = Semaphore(concurrent)

        return runBlocking(MDCContext()) {
            requests.map { request ->
                async(Dispatchers.IO) { semaphore.withPermit { execute(request = request, sftpSession = session) } }
            }.awaitAll()
        }.also {
            if (sftpSession == null) session.disconnect()

            logger.info("[SFTP] Execute Bulk Done! - Size: {}", requests.size)
        }
    }

    fun execute(
        request: SftpRequest,
        sftpSession: Session? = null,
    ): SftpResult {
        val session =
            sftpSession
                ?: SftpConnectionManager.createSession(sftpProperty)
                ?: throw Exception("Create Session Failure!")

        val channel: ChannelSftp =
            SftpConnectionManager.openSftpChannel(session)
                ?: throw Exception("Open Channel Failure!")

        val isExecuteSuccess: Boolean =
            when (request.command) {
                SftpRequest.Command.Upload ->
                    upload(channel, request.localPath.toString(), request.remotePath.toString())

                SftpRequest.Command.Download ->
                    download(channel, request.localPath.toString(), request.remotePath.toString())
            }

        channel.disconnect()

        if (sftpSession == null) session.disconnect()

        return SftpResult(request = request, isSuccess = isExecuteSuccess)
    }

    fun createDirectories(
        path: Path,
        sftpSession: Session? = null,
    ): Path {
        val session =
            sftpSession
                ?: SftpConnectionManager.createSession(sftpProperty)
                ?: throw Exception("Create Session Failure!")

        val channel: ChannelSftp =
            SftpConnectionManager.openSftpChannel(session)
                ?: throw Exception("Open Channel Failure!")

        if (path.startsWith("/")) channel.cd("/")

        path.toString().split("/").filterNot { it.isEmpty() }.forEach { dir ->
            try {
                channel.cd(dir)
            } catch (ex: SftpException) {
                channel.mkdir(dir)
                channel.cd(dir)
            }
        }

        logger.debug("[SFTP] Create directories: {} -> done!", path.toString())

        channel.disconnect()

        if (sftpSession == null) session.disconnect()

        return path
    }

    private fun upload(
        channel: ChannelSftp,
        localPath: String,
        remotePath: String,
    ): Boolean =
        try {
            channel.put(localPath, remotePath).also {
                logger.debug(
                    """
                    [SFTP] Upload Completed
                        - Local Path: {}
                        - Remote Path: {}
                    """.trimIndent(),
                    localPath,
                    remotePath,
                )
            }

            true
        } catch (ex: SftpException) {
            logger.error(
                """
                [SFTP] Upload Failure
                    - Local Path: {}
                    - Remote Path: {}
                    - Message: {}
                """.trimIndent(),
                localPath,
                remotePath,
                ex.message,
            )

            logger.error(ex.stackTraceToString())

            false
        }

    private fun download(
        channel: ChannelSftp,
        localPath: String,
        remotePath: String,
    ): Boolean =
        try {
            channel.get(localPath, remotePath).also {
                logger.debug(
                    """
                    [SFTP] Download Completed
                        - Local Path: {}
                        - Remote Path: {}
                    """.trimIndent(),
                    localPath,
                    remotePath,
                )
            }

            true
        } catch (ex: SftpException) {
            logger.error(
                """
                [SFTP] Download Failure
                    - Local Path: {}
                    - Remote Path: {}
                    - Message: {}
                """.trimIndent(),
                localPath,
                remotePath,
                ex.message,
            )

            logger.error(ex.stackTraceToString())

            false
        }
}

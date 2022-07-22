package com.shinonometn.jam.networkd.base.utils

import com.sun.org.slf4j.internal.LoggerFactory
import org.apache.commons.lang3.SystemUtils
import java.io.File
import java.nio.file.Files
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import java.util.concurrent.FutureTask
import java.util.concurrent.atomic.AtomicBoolean

private val DefaultProcessExecutorThreadPool = Executors.newCachedThreadPool {
    Thread(it).apply {
        isDaemon = true
    }
}

typealias ShellProvider = (String) -> Array<String>

class ShellExecuteError(val result: ShellExecuteContext.ExecuteResult) : Exception(result.stderr)

class ShellExecuteContext(val script: String, val executor: Executor) {
    private val logger = LoggerFactory.getLogger(ShellExecuteContext::class.java)

    private val isRunning = AtomicBoolean(false)

    var shell : ShellProvider = when {
        SystemUtils.IS_OS_MAC -> Bash
        SystemUtils.IS_OS_WINDOWS -> error("Windows is not supported.")
        else -> Shell
    }

    private val tempFolder = Files.createTempDirectory("jam-networkd-").toFile().also { it.deleteOnExit() }
    private val tempErrOut = File(tempFolder, "stderr.txt").also { it.deleteOnExit() }
    private val tempStdOut = File(tempFolder, "stdout.txt").also { it.deleteOnExit() }

    class ExecuteResult(val exitCode: Int, val stdout: String, val stderr: String)

    private fun internalExecute(): ExecuteResult {
        require(isRunning.compareAndSet(false, true)) { "Process executing." }
        logger.debug("Current PATH : {}", System.getenv()["PATH"])
        val shellCommand = shell(script)
        logger.debug("Shell command: {}", shellCommand)
        val process = Runtime.getRuntime().exec(shellCommand)
        executor.execute { process.inputStream.copyTo(tempStdOut.outputStream()) }
        executor.execute { process.errorStream.copyTo(tempErrOut.outputStream()) }

        try {
            val exitCode = process.waitFor()
            val result = ExecuteResult(exitCode, tempStdOut.readText(), tempErrOut.readText())
            if (exitCode != 0) throw ShellExecuteError(result)
            return result
        } finally {
            isRunning.set(false)
            tempErrOut.delete()
            tempStdOut.delete()
        }
    }

    fun execute(): FutureTask<ExecuteResult> {
        val task = FutureTask { internalExecute() }
        executor.execute(task)
        return task
    }

    fun execute(callback: (Result<ExecuteResult>) -> Unit) = executor.execute {
        try {
            callback(Result.success(internalExecute()))
        } catch (e: Exception) {
            callback(Result.failure(e))
        } finally {
            isRunning.set(false)
            tempErrOut.delete()
            tempStdOut.delete()
        }
    }

    fun finalize() {
        tempFolder.deleteRecursively()
    }

    companion object {
        val Bash : ShellProvider = {
            arrayOf("bash", "-ec", it)
        }

        val Shell : ShellProvider = {
            arrayOf("sh", "-ec", it)
        }
    }
}

fun shell(script: String, executor: Executor = DefaultProcessExecutorThreadPool): ShellExecuteContext {
    return ShellExecuteContext(script, executor = executor)
}
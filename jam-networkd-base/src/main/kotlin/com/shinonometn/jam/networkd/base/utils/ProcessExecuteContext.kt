package com.shinonometn.jam.networkd.base.utils

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

class ProcessExecuteError(val result: ProcessExecuteContext.ExecuteResult) : Exception(result.stderr)

class ProcessExecuteContext(vararg val command: String, val executor: Executor) {

    private val isRunning = AtomicBoolean(false)

    private val tempFolder = Files.createTempDirectory("jam-networkd-").toFile().also { it.deleteOnExit() }
    private val tempErrOut = File(tempFolder, "stderr.txt").also { it.deleteOnExit() }
    private val tempStdOut = File(tempFolder, "stdout.txt").also { it.deleteOnExit() }

    class ExecuteResult(val exitCode: Int, val stdout: String, val stderr: String)

    private fun internalExecute(): ExecuteResult {
        require(isRunning.compareAndSet(false, true)) { "Process executing." }
        val process = Runtime.getRuntime().exec(command)
        executor.execute { process.inputStream.copyTo(tempStdOut.outputStream()) }
        executor.execute { process.errorStream.copyTo(tempErrOut.outputStream()) }

        try {
            val exitCode = process.waitFor()
            val result = ExecuteResult(exitCode, tempStdOut.readText(), tempErrOut.readText())
            if (exitCode != 0) throw ProcessExecuteError(result)
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
}

fun process(vararg command: String, executor: Executor = DefaultProcessExecutorThreadPool): ProcessExecuteContext {
    return ProcessExecuteContext(*command, executor = executor)
}
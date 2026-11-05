package com.bhh.tools.util

import android.annotation.SuppressLint
import com.bhh.tools.log.Logger
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader

object ShellCommend {

    open class Builder(private val isSU: Boolean) {

        private val logger by lazy { Logger.get("ShellCommend#${if (isSU) "SU" else "SH"}") }
        @SuppressLint("[ByDesign12.1]UsingRuntimeExec")
        fun isRooted(): Boolean {
            var process: Process? = null
            try {
                // 尝试执行 su 命令
                process = Runtime.getRuntime().exec("su")
                val outputStream = process.outputStream
                // 向 su 进程发送一个简单的命令来测试
                outputStream.write("id\n".toByteArray())
                outputStream.flush()
                outputStream.close()
                // 等待命令执行完成
                process.waitFor()
                // 检查返回值
                return process.exitValue() == 0
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                process?.destroy()
            }
            return false
        }

        @SuppressLint("[ByDesign12.1]UsingRuntimeExec")
        fun exec(command: String, outputs: ArrayList<String> = arrayListOf()): Boolean {
            var process: Process? = null
            try {
                // 执行命令
                if (isSU) {
                    process = Runtime.getRuntime().exec("su")
                    val outputStream = DataOutputStream(process.outputStream)
                    outputStream.writeBytes("$command\n")
                    outputStream.flush()
                    outputStream.writeBytes("exit\n")
                    outputStream.flush()
                } else {
                    process = Runtime.getRuntime().exec(command)
                }

                // 读取命令输出
                val inputStream = BufferedReader(InputStreamReader(process.inputStream))
                var line: String?
                while (inputStream.readLine().also { line = it } != null) {
                    outputs.add(line?:"")
                }
                // 等待命令执行完成
                process.waitFor()
                return true
            } catch (e: Exception) {
                outputs.add(e.message?:"")
                logger.error(e.message, e)
                return false
            } finally {
                if (isSU) {
                    try {
                        process?.inputStream?.close()
                        process?.outputStream?.close()
                        process?.errorStream?.close()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    class SU: Builder(true)

    class SH: Builder(false)

}
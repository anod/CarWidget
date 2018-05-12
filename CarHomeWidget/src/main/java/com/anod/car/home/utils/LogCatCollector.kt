/*
 *  Copyright 2010 Kevin Gaudin
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.anod.car.home.utils

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.util.ArrayList
import java.util.Arrays
import java.util.LinkedList

import info.anodsplace.framework.AppLog


/**
 * Executes logcat commands and collects it's output.
 *
 * @author Kevin Gaudin
 */
object LogCatCollector {

    private const val DEFAULT_BUFFER_SIZE_IN_BYTES = 8192

    /**
     * Default number of latest lines kept from the logcat output.
     */
    private const val DEFAULT_TAIL_COUNT = 100

    /**
     * Executes the logcat command with arguments taken from
     *
     * @param bufferName The name of the buffer to be read: "main" (default), "radio"
     * or "events".
     * @return A [String] containing the latest lines of the output.
     * Default is 100 lines, use "-t", "300" in
     * if you want 300 lines.
     * You should be aware that increasing this value causes a longer
     * report generation time and a bigger footprint on the device data
     * plan consumption.
     */
    fun collectLogCat(bufferName: String?): LinkedList<String> {
        val myPid = android.os.Process.myPid()
        var myPidStr: String? = null
        if (myPid > 0) {
            myPidStr = Integer.toString(myPid) + "):"
        }

        val commandLine = ArrayList<String>()
        commandLine.add("logcat")
        if (bufferName != null) {
            commandLine.add("-b")
            commandLine.add(bufferName)
        }

        // "-t n" argument has been introduced in FroYo (API level 8). For
        // devices with lower API level, we will have to emulate its job.
        val tailCount: Int
        val logcatArguments = arrayOf("-t", "300", "tag", "CarHomeWidget:V", "*:S")
        val logcatArgumentsList = ArrayList(
                Arrays.asList(*logcatArguments)
        )

        val tailIndex = logcatArgumentsList.indexOf("-t")
        if (tailIndex > -1 && tailIndex < logcatArgumentsList.size) {
            tailCount = Integer.parseInt(logcatArgumentsList[tailIndex + 1])
        } else {
            tailCount = -1
        }

        val logcatBuf = BoundedLinkedList<String>(if (tailCount > 0)
            tailCount
        else
            DEFAULT_TAIL_COUNT)
        commandLine.addAll(logcatArgumentsList)

        try {
            val process = Runtime.getRuntime()
                    .exec(commandLine.toTypedArray())
            val bufferedReader = BufferedReader(
                    InputStreamReader(process.inputStream), DEFAULT_BUFFER_SIZE_IN_BYTES)

            //AppLog.d("Retrieving logcat output...");

            // Dump stderr to null
            Thread(Runnable {
                try {
                    val stderr = process.errorStream
                    val dummy = ByteArray(DEFAULT_BUFFER_SIZE_IN_BYTES)
                    while (stderr.read(dummy) >= 0) {

                    }
                } catch (ignored: IOException) {
                }
            }).start()

            while (true) {
                val line = bufferedReader.readLine() ?: break
                if (myPidStr == null || line.contains(myPidStr)) {
                    logcatBuf.add(line)
                }
            }

        } catch (e: IOException) {
            //"LogCatCollector.collectLogCat could not retrieve data.",
            AppLog.e(e)
        }

        return logcatBuf
    }
}
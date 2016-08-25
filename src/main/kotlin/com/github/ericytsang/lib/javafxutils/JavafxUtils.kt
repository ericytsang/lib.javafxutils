package com.github.ericytsang.lib.javafxutils

import javafx.scene.control.Alert
import javafx.scene.control.TextArea
import java.io.PrintWriter
import java.io.StringWriter

/**
 * Created by surpl on 8/25/2016.
 */
object JavafxUtils
{
    fun showErrorDialog(title:String,header:String,exception:Exception)
    {
        val alert = Alert(Alert.AlertType.ERROR)
        alert.title = title
        alert.headerText = header
        alert.contentText = exception.message

        alert.dialogPane.expandableContent = TextArea().apply()
        {
            text = run()
            {
                val stringWriter = StringWriter()
                val printWriter = PrintWriter(stringWriter)
                exception.printStackTrace(printWriter)
                val stackTraceString = stringWriter.toString()
                printWriter.close()
                stringWriter.close()
                stackTraceString
            }
            isEditable = false
            isWrapText = false
        }

        alert.showAndWait()
    }
}

package com.github.ericytsang.lib.javafxutils

import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.control.TextInputDialog
import javafx.stage.Stage
import org.junit.Test
import java.util.Optional

/**
 * Created by surpl on 7/14/2016.
 */
class EditableListViewTest:Application()
{
    @Test
    fun main()
    {
        Application.launch()
    }

    override fun start(primaryStage:Stage)
    {
        val editableListView = object:EditableListView<String,TextInputDialog,String>()
        {
            override fun isInputCancelled(result:Optional<String>):Boolean
            {
                return !result.isPresent
            }

            override fun tryParseInput(inputDialog:TextInputDialog):String
            {
                if (inputDialog.result == "hello")
                {
                    throw IllegalArgumentException("input cannot be \"${inputDialog.result}\".")
                }
                return inputDialog.result
            }

            override fun makeInputDialog(model:String?):TextInputDialog
            {
                return TextInputDialog(model)
                    .apply()
                    {
                        headerText = "Enter the sentence below."
                    }
            }
        }

        primaryStage.scene = Scene(editableListView,800.0,500.0)
        primaryStage.show()
    }
}

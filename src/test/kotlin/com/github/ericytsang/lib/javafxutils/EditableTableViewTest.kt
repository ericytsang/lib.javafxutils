package com.github.ericytsang.lib.javafxutils

import javafx.application.Application
import javafx.beans.property.ObjectProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.beans.value.ObservableValue
import javafx.scene.Scene
import javafx.scene.control.TableColumn
import javafx.scene.control.TextInputDialog
import javafx.stage.Stage
import javafx.util.Callback
import org.junit.Test
import java.util.Optional

/**
 * Created by surpl on 7/14/2016.
 */
class EditableTableViewTest:Application()
{
    @Test
    fun main()
    {
        Application.launch()
    }

    override fun start(primaryStage:Stage)
    {
        val editableTableView = object:EditableTableView<String,TextInputDialog,String>()
        {
            init
            {
                columns.add(TableColumn<String,String>().apply()
                {
                    text = "First word"
                    cellValueFactory = Callback<TableColumn.CellDataFeatures<String,String>,ObservableValue<String>>()
                    {
                        SimpleObjectProperty<String>(it.value.split(' ').first())
                    }
                })
                columns.add(TableColumn<String,String>().apply()
                {
                    text = "Everything"
                    cellValueFactory = Callback<TableColumn.CellDataFeatures<String,String>,ObservableValue<String>>()
                    {
                        SimpleObjectProperty<String>(it.value)
                    }
                })
            }

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

        primaryStage.scene = Scene(editableTableView,800.0,500.0)
        primaryStage.show()
    }
}

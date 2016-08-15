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
        val editableTableView = object:EditableTableView<String>()
        {
            override fun createItem(previousInput:String?):String?
            {
                val result = TextInputDialog(previousInput).showAndWait()
                if (result.isPresent)
                {
                    return result.get()
                }
                else
                {
                    return null
                }
            }

            override fun isConsistent(items:List<String>):List<String>
            {
                val violatedConstraints = mutableListOf<String>()

                if (items.toSet().size != items.size)
                {
                    violatedConstraints += "all entries must be unique"
                }

                if (this.items.contains("noremove") && !items.contains("noremove"))
                {
                    violatedConstraints += "cannot remove the \"noremove\" entry"
                }

//                if (items.sorted() != items)
//                {
//                    violatedConstraints += "entries must be kept in alphabetical order"
//                }

                return violatedConstraints
            }

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

        }

        primaryStage.scene = Scene(editableTableView,800.0,500.0)
        primaryStage.show()
    }
}

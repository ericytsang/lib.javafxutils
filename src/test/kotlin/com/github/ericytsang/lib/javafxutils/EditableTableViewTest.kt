package com.github.ericytsang.lib.javafxutils

import com.github.ericytsang.lib.collections.ConstrainedList
import com.github.ericytsang.lib.collections.SimpleConstraint
import com.sun.javafx.collections.ObservableListWrapper
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
import java.util.ArrayList
import java.util.Optional
import java.util.function.Predicate

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
            override fun createOrUpdateItem(previousInput:String?):String?
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

            init
            {
                items = ObservableListWrapper(ConstrainedList(ArrayList<String>()).apply()
                {
                    constraints += SimpleConstraint<List<String>>().apply()
                    {
                        isConsistent = Predicate {change -> change.newValue.toSet().size == change.newValue.size}
                        description = "all entries must be unique"
                    }
                    constraints += SimpleConstraint<List<String>>().apply()
                    {
                        isConsistent = Predicate {change -> !change.oldValue.contains("noremove") || change.newValue.contains("noremove")}
                        description = "noremove cannot be removed"
                    }
                    constraints += SimpleConstraint<List<String>>().apply()
                    {
                        isConsistent = Predicate {change -> change.newValue.sorted() == change.newValue}
                        description = "must be in alphabetical order"
                    }
                })

                columns += TableColumn<String,String>().apply()
                {
                    text = "Everything"
                    cellValueFactory = Callback<TableColumn.CellDataFeatures<String,String>,ObservableValue<String>>()
                    {
                        SimpleObjectProperty<String>(it.value)
                    }
                }
            }

        }

        primaryStage.scene = Scene(editableTableView,800.0,500.0)
        primaryStage.show()
    }
}

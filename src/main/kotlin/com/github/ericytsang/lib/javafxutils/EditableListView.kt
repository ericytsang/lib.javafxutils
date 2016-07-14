package com.github.ericytsang.lib.javafxutils

import javafx.beans.InvalidationListener
import javafx.event.EventHandler
import javafx.scene.control.Alert
import javafx.scene.control.ContextMenu
import javafx.scene.control.Dialog
import javafx.scene.control.ListCell
import javafx.scene.control.ListView
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.control.cell.TextFieldListCell
import javafx.scene.input.KeyCode
import javafx.util.Callback
import java.awt.Toolkit
import java.util.Optional

/**
 * a control that contains a [ListView] and some peripheral controls that the
 * user can use to interact with and modify the items within the [ListView].
 *
 * items within the [ListView] may also be modified programmatically.
 */
abstract class EditableListView<Model,InputDialog:Dialog<ResultType>,ResultType>:ListView<Model>()
{
    protected abstract fun tryParseInput(inputDialog:InputDialog):Model
    protected abstract fun makeInputDialog(model:Model?):InputDialog
    protected abstract fun isInputCancelled(result:Optional<ResultType>):Boolean

    protected open fun tryAddToListAt(existingEntries:MutableList<Model>,indexOfEntry:Int,newEntry:Model)
    {
        existingEntries.add(indexOfEntry,newEntry)
    }

    protected open fun tryRemoveFromListAt(existingEntries:MutableList<Model>,indexOfEntry:Int)
    {
        existingEntries.removeAt(indexOfEntry)
    }

    protected open fun tryUpdateListAt(existingEntries:MutableList<Model>,indexOfEntry:Int,newEntry:Model)
    {
        existingEntries[indexOfEntry] = newEntry
    }

    init
    {
        onMouseClicked = EventHandler()
        {
            event ->
            // continue if it is a double click, and an item in the list is selected
            if (event.clickCount == 2 && focusModel.focusedItem != null)
            {
                editFocusedItem()
            }
        }

        onKeyPressed = EventHandler()
        {
            event ->
            when (event.code)
            {
                // allow deletion of entries by pressing delete or backspace
                KeyCode.DELETE,KeyCode.BACK_SPACE ->
                {
                    remoteFocusedItem()
                    event.consume()
                }

                // allow addition of entries by pressing enter
                KeyCode.ENTER ->
                {
                    if (event.isControlDown)
                    {
                        editFocusedItem()
                    }
                    else
                    {
                        addEntryAtFocusedItem()
                    }
                    event.consume()
                }

                // allow insertion of entries by pressing insert
                KeyCode.INSERT ->
                {
                    insertEntryAtFocusedItem()
                    event.consume()
                }

                KeyCode.DOWN -> if (event.isControlDown)
                {
                    moveFocusedItemDown()
                    event.consume()
                }

                KeyCode.UP -> if (event.isControlDown)
                {
                    moveFocusedItemUp()
                    event.consume()
                }

                else -> { /* do nothing */ }
            }
        }

        contextMenu = ContextMenu().apply()
        {
            val onShownListeners = mutableListOf<()->Unit>()

            onShown = EventHandler()
            {
                onShownListeners.forEach {it()}
            }

            items.add(MenuItem().apply()
            {
                text = "Add (enter)"
                onAction = EventHandler()
                {
                    addEntryAtFocusedItem()
                }
            })

            items.add(MenuItem().apply()
            {
                text = "Insert (insert)"
                onAction = EventHandler()
                {
                    insertEntryAtFocusedItem()
                }
            })

            items.add(SeparatorMenuItem())

            items.add(MenuItem().apply()
            {
                text = "Edit (double-click/ctrl+enter)"
                onAction = EventHandler()
                {
                    editFocusedItem()
                }
                onShownListeners.add()
                {
                    isDisable = focusModel.focusedItem == null
                }
            })

            items.add(MenuItem().apply()
            {
                text = "Delete (delete/backspace)"
                onAction = EventHandler()
                {
                    remoteFocusedItem()
                }
                onShownListeners.add()
                {
                    isDisable = focusModel.focusedItem == null
                }
            })

            items.add(MenuItem().apply()
            {
                text = "Move up (ctrl+up)"
                onAction = EventHandler()
                {
                    moveFocusedItemUp()
                }
                onShownListeners.add()
                {
                    isDisable = focusModel.focusedItem == null
                }
            })

            items.add(MenuItem().apply()
            {
                text = "Move down (ctrl+down)"
                onAction = EventHandler()
                {
                    moveFocusedItemDown()
                }
                onShownListeners.add()
                {
                    isDisable = focusModel.focusedItem == null
                }
            })
        }
    }

    private fun remoteFocusedItem()
    {
        try
        {
            focusModel.focusedItem ?: throw IllegalStateException("no item in focus")
        }
        catch (ex:Exception)
        {
            Toolkit.getDefaultToolkit().beep()
        }
        try
        {
            tryRemoveFromListAt(items,focusModel.focusedIndex)
        }
        catch (ex:Exception)
        {
            val alert = Alert(Alert.AlertType.ERROR)
            alert.title = "Remove Existing Entry"
            alert.headerText = "Unable to remove entry"
            alert.contentText = ex.message
            alert.showAndWait()
        }
    }

    private fun moveFocusedItemUp()
    {
        try
        {
            val itemToMove = focusModel.focusedItem ?: throw IllegalStateException("no item in focus")
            val positionToMoveTo = focusModel.focusedIndex-1
            if (positionToMoveTo !in items.indices) throw IndexOutOfBoundsException()
            val positionToRemoveFrom = focusModel.focusedIndex
            items.removeAt(positionToRemoveFrom)
            items.add(positionToMoveTo,itemToMove)
            selectionModel.select(positionToMoveTo)
        }
        catch (ex:Exception)
        {
            Toolkit.getDefaultToolkit().beep()
        }
    }

    private fun moveFocusedItemDown()
    {
        try
        {
            val itemToMove = focusModel.focusedItem ?: throw IllegalStateException("no item in focus")
            val positionToMoveTo = focusModel.focusedIndex+1
            if (positionToMoveTo !in items.indices) throw IndexOutOfBoundsException()
            val positionToRemoveFrom = focusModel.focusedIndex
            items.removeAt(positionToRemoveFrom)
            items.add(positionToMoveTo,itemToMove)
            selectionModel.select(positionToMoveTo)
        }
        catch (ex:Exception)
        {
            Toolkit.getDefaultToolkit().beep()
        }
    }

    private fun editFocusedItem()
    {
        if (focusModel.focusedItem == null)
        {
            Toolkit.getDefaultToolkit().beep()
            return
        }
        val inputDialog = makeInputDialog(focusModel.focusedItem)
        while (true)
        {
            // show input dialog to get input from user
            val result = inputDialog
                .apply {title = "Edit Existing Entry"}
                .showAndWait()

            // break if input is cancelled
            if (isInputCancelled(result))
            {
                break
            }

            // try to parse input
            val entry = try
            {
                tryParseInput(inputDialog)
            }

            // there was an exception while parsing the result...show error
            // then try to get the input again
            catch (ex:Exception)
            {
                // input format is invalid
                val alert = Alert(Alert.AlertType.ERROR)
                alert.title = "Edit Existing Entry"
                alert.headerText = "Invalid input format"
                alert.contentText = ex.message
                alert.showAndWait()

                // try to get input from user again again
                continue
            }

            // try to add the entry to the list
            try
            {
                tryUpdateListAt(items,focusModel.focusedIndex,entry)
                break
            }
            catch (ex:Exception)
            {
                // constraints not satisfied
                val alert = Alert(Alert.AlertType.ERROR)
                alert.title = "Edit Existing Entry"
                alert.headerText = "Unable to update entry"
                alert.contentText = ex.message
                alert.showAndWait()

                // try to get input from user again again
                continue
            }
        }
    }

    private fun addEntryAtFocusedItem()
    {
        if (focusModel.focusedItem != null)
        {
            addNewEntryAt(focusModel.focusedIndex+1)
        }
        else
        {
            addNewEntryAt(items.size)
        }
    }

    private fun insertEntryAtFocusedItem()
    {
        if (focusModel.focusedItem != null)
        {
            addNewEntryAt(focusModel.focusedIndex)
        }
        else
        {
            addNewEntryAt(0)
        }
    }

    private fun addNewEntryAt(index:Int)
    {
        val inputDialog = makeInputDialog(null)
        while (true)
        {
            // show text input dialog to get input from user
            val result = inputDialog
                .apply {title = "Add New Entry"}
                .showAndWait()

            // break if input is cancelled
            if (isInputCancelled(result))
            {
                break
            }

            // try to parse input
            val entry = try
            {
                tryParseInput(inputDialog)
            }

            // there was an exception while parsing the result...show error
            // then try to get the input again
            catch (ex:Exception)
            {
                // input format is invalid
                val alert = Alert(Alert.AlertType.ERROR)
                alert.title = "Add New Entry"
                alert.headerText = "Invalid input format"
                alert.contentText = ex.message
                alert.showAndWait()

                // try to get input from user again again
                continue
            }

            // try to add the entry to the list
            try
            {
                tryAddToListAt(items,index,entry)
                selectionModel.select(index)
                break
            }
            catch (ex:Exception)
            {
                // constraints not satisfied
                val alert = Alert(Alert.AlertType.ERROR)
                alert.title = "Add New Entry"
                alert.headerText = "Constraint error"
                alert.contentText = ex.message
                alert.showAndWait()

                // try to get input from user again again
                continue
            }
        }
    }
}

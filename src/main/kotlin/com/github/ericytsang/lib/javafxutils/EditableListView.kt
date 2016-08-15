package com.github.ericytsang.lib.javafxutils

import com.sun.javafx.collections.ObservableListWrapper
import javafx.event.EventHandler
import javafx.scene.control.Alert
import javafx.scene.control.ContextMenu
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.input.KeyCode
import java.awt.Toolkit
import java.util.ArrayList

/**
 * a control that contains a [ListView] and some peripheral controls that the
 * user can use to interact with and modify the items within the [ListView].
 *
 * items within the [ListView] may also be modified programmatically.
 */
abstract class EditableListView<Item:Any>:ListView<Item>()
{
    /**
     * return null if the operation was cancelled by the user; returns an [Item]
     * created as per user input otherwise.
     */
    protected abstract fun createOrUpdateItem(previousInput:Item?):Item?

    /**
     * returns an empty list if [items] is a consistent list; returns a list of
     * strings. each string should describe a constraint that was violated by
     * [items]. these messages will be displayed to the user via an alert box.
     */
    protected open fun isConsistent(items:List<Item>):List<String> = emptyList()

    init
    {
        placeholder = Label("No entries in list. Right-click for more options.")

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
        if (focusModel.focusedItem == null)
        {
            Toolkit.getDefaultToolkit().beep()
        }
        else
        {
            val testItems = ArrayList(items)
                .apply()
                {
                    removeAt(focusModel.focusedIndex)
                }

            val errorMessages = isConsistent(testItems)
            if (errorMessages.isEmpty())
            {
                items.removeAt(focusModel.focusedIndex)
            }
            else
            {
                val errorMessage = errorMessages.joinToString(
                    prefix = "The operation would have violated the following constraints:\n    - ",
                    separator = "\n    - ")
                showError("Remove Entry","Unable to remove entry.",errorMessage)
            }
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

            val testItems = ArrayList(items)
            testItems.removeAt(positionToRemoveFrom)
            testItems.add(positionToMoveTo,itemToMove)

            val errorMessages = isConsistent(testItems)
            if (errorMessages.isEmpty())
            {
                items = ObservableListWrapper(testItems)
                selectionModel.select(positionToMoveTo)
                scrollTo(positionToMoveTo)
            }
            else
            {
                val errorMessage = errorMessages.joinToString(
                    prefix = "The operation would have violated the following constraints:\n    - ",
                    separator = "\n    - ")
                showError("Update Entry","Unable to update entry.",errorMessage)
            }
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

            val testItems = ArrayList(items)
            testItems.removeAt(positionToRemoveFrom)
            testItems.add(positionToMoveTo,itemToMove)

            val errorMessages = isConsistent(testItems)
            if (errorMessages.isEmpty())
            {
                items = ObservableListWrapper(testItems)
                selectionModel.select(positionToMoveTo)
                scrollTo(positionToMoveTo)
            }
            else
            {
                val errorMessage = errorMessages.joinToString(
                    prefix = "The operation would have violated the following constraints:\n    - ",
                    separator = "\n    - ")
                showError("Update Entry","Unable to update entry.",errorMessage)
            }
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
        }
        else
        {
            var toUpdate:Item = focusModel.focusedItem

            while (true)
            {
                toUpdate = createOrUpdateItem(toUpdate) ?: focusModel.focusedItem
                val testItems = ArrayList(items)
                    .apply()
                    {
                        set(focusModel.focusedIndex,toUpdate)
                    }

                val errorMessages = isConsistent(testItems)
                if (errorMessages.isEmpty())
                {
                    val updateIndex = focusModel.focusedIndex
                    items[focusModel.focusedIndex] = toUpdate
                    selectionModel.select(updateIndex)
                    break
                }
                else
                {
                    val errorMessage = errorMessages.joinToString(
                        prefix = "The operation would have violated the following constraints:\n    - ",
                        separator = "\n    - ")
                    showError("Update Entry","Unable to update entry.",errorMessage)
                }
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
        var newItem:Item? = null

        while (true)
        {
            newItem = createOrUpdateItem(newItem) ?: return
            val testItems = ArrayList(items)
                .apply()
                {
                    add(index,newItem)
                }

            val errorMessages = isConsistent(testItems)
            if (errorMessages.isEmpty())
            {
                items.add(index,newItem)
                selectionModel.select(index)
                break
            }
            else
            {
                val errorMessage = errorMessages.joinToString(
                    prefix = "The operation would have violated the following constraints:\n    - ",
                    separator = "\n    - ")
                showError("Create Entry","Unable to create entry.",errorMessage)
            }
        }
    }

    private fun showError(title:String,header:String,body:String)
    {
        val alert = Alert(Alert.AlertType.ERROR)
        alert.title = title
        alert.headerText = header
        alert.contentText = body
        alert.showAndWait()
    }
}

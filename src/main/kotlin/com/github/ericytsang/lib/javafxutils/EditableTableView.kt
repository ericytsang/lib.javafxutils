package com.github.ericytsang.lib.javafxutils

import javafx.event.EventHandler
import javafx.scene.control.ContextMenu
import javafx.scene.control.Label
import javafx.scene.control.MenuItem
import javafx.scene.control.SeparatorMenuItem
import javafx.scene.control.TableView
import javafx.scene.input.KeyCode
import java.awt.Toolkit
import java.util.ArrayList

/**
 * a control that contains a [ListView] and some peripheral controls that the
 * user can use to interact with and modify the items within the [ListView].
 *
 * items within the [ListView] may also be modified programmatically.
 */
abstract class EditableTableView<Item:Any>:TableView<Item>()
{
    /**
     * return null if the operation was cancelled by the user; returns an [Item]
     * configured as per user input otherwise.
     *
     * if the [Item] returned is a different instance than [previousInput], then
     * [previousInput] in [items] will be replaced with it which will cause a
     * list change event to occur. on the other hand, if the [Item] returned
     * is an alias of [previousInput], then the [refresh] will be called to
     * update the interface but no list change event will occur.
     */
    protected abstract fun createOrUpdateItem(previousInput:Item?):Item?

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
            try
            {
                items.removeAt(focusModel.focusedIndex)
            }
            catch (ex:Exception)
            {
                JavafxUtils.showErrorDialog("Remove Entry","Unable to remove entry.",ex)
            }
        }
    }

    private fun moveFocusedItemUp()
    {
        try
        {
            focusModel.focusedItem ?: throw IllegalStateException("no item in focus")
            val positionToMoveTo = focusModel.focusedIndex-1
            if (positionToMoveTo !in items.indices) throw IndexOutOfBoundsException()
            val positionToRemoveFrom = focusModel.focusedIndex

            moveItem(positionToRemoveFrom,positionToMoveTo)
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
            focusModel.focusedItem ?: throw IllegalStateException("no item in focus")
            val positionToMoveTo = focusModel.focusedIndex+1
            if (positionToMoveTo !in items.indices) throw IndexOutOfBoundsException()
            val positionToRemoveFrom = focusModel.focusedIndex

            moveItem(positionToRemoveFrom,positionToMoveTo)
        }
        catch (ex:Exception)
        {
            Toolkit.getDefaultToolkit().beep()
        }
    }

    private fun moveItem(positionToRemoveFrom:Int,positionToMoveTo:Int)
    {
        // save original tin case rollback is needed
        val originalItems = ArrayList(items)

        // try to perform the move operation
        try
        {
            val itemToMove = items[positionToRemoveFrom]
            items.removeAt(positionToRemoveFrom)
            items.add(positionToMoveTo,itemToMove)
            selectionModel.select(positionToMoveTo)
            scrollTo(positionToMoveTo)
        }

        // a problem occured, roll back and display an error message
        catch (ex:Exception)
        {
            items.clear()
            items.addAll(originalItems)
            selectionModel.select(positionToRemoveFrom)
            JavafxUtils.showErrorDialog("Update Entry","Unable to update entry.",ex)
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
            val toUpdate:Item = focusModel.focusedItem
            val updateIndex = focusModel.focusedIndex

            while (true)
            {
                val updated = createOrUpdateItem(toUpdate) ?: toUpdate
                try
                {
                    // if the entry we need to update is the same instance as
                    // the updated instance, just refresh the tableview and
                    // don't set it in the list because we don't want to trigger
                    // a list change event.
                    if (toUpdate === updated)
                    {
                        refresh()
                    }

                    // else the entry to update and the updated entry are
                    // separate instances...replace the entry to update in the
                    // list with the updated entry.
                    else
                    {
                        items[updateIndex] = updated
                    }

                    // refocus on the update index because if we don't do this,
                    // focus will jump back to the top of the list
                    selectionModel.select(updateIndex)
                    break
                }
                catch (ex:Exception)
                {
                    JavafxUtils.showErrorDialog("Update Entry","Unable to update entry.",ex)
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
            try
            {
                items.add(index,newItem)
                selectionModel.select(index)
                break
            }
            catch (ex:Exception)
            {
                JavafxUtils.showErrorDialog("Create Entry","Unable to create entry.",ex)
            }
        }
    }
}

package com.github.ericytsang.lib.javafxutils

import javafx.scene.Node
import javafx.scene.control.ComboBox
import javafx.scene.layout.VBox

/**
 * this control is a [VBox] that initially contains just [comboBox] that can be
 * used to select a [PolymorphicComboBox.Option].
 *
 * when a [PolymorphicComboBox.Option] is selected, its
 * [PolymorphicComboBox.Option.panel] will be displayed underneath the
 * [comboBox] which enables the user to specify additional information needed
 * for the selected [PolymorphicComboBox.Option].
 *
 * Created by surpl on 8/14/2016.
 */
class PolymorphicComboBox<Option:PolymorphicComboBox.Option<Product>,Product>:VBox()
{
    val comboBox = ComboBox<Option>().apply()
    {
        // whenever the selected value changes, remove the old value's panel
        // from the layout and add the new value's panel to it.
        valueProperty().addListener()
        {
            observableValue,oldValue,newValue ->

            if (oldValue?.panel != null)
            {
                this@PolymorphicComboBox.children.remove(oldValue.panel)
            }

            if (newValue?.panel != null)
            {
                this@PolymorphicComboBox.children.add(newValue.panel)
            }
        }
    }

    init
    {
        children += comboBox
    }

    var product:Product?
        /**
         * tries to [Option.build] a [Product] using the current value selected
         * in [comboBox] and return it; will return null if [comboBox] has
         * nothing selected.
         */
        get() = comboBox.value?.build()
        /**
         * sets the [ComboBox.value] of [comboBox] to the first element in its
         * [ComboBox.items] that can [Option.parse] the passed [value].
         */
        set(value)
        {
            comboBox.value = if (value == null)
            {
                null
            }
            else
            {
                comboBox.items.find()
                {
                    item ->
                    try
                    {
                        item.parse(value)
                        true
                    }
                    catch (ex:Exception)
                    {
                        // the option rejected the product
                        false
                    }
                } ?: run()
                {
                    throw IllegalArgumentException("no options were able to parse $value")
                }
            }
        }

    /**
     * can be selected by the [comboBox].
     */
    interface Option<Product>
    {
        /**
         * displayed when this [Option] is selected in the [comboBox] so the
         * user may interact with it. if it is null, nothing is displayed.
         */
        val panel:Node?

        /**
         * parses user input entered into [panel], and returns a [Product].
         */
        fun build():Product

        /**
         * configures [panel] to reflect [product]. after calling this, invoking
         * [build] should return a [Product] that is structurally equal to
         * [product].
         */
        fun parse(product:Product)

        /**
         * returns the [String] that will be displayed in the combo box to
         * represent this [Option].
         */
        override fun toString():String
    }
}

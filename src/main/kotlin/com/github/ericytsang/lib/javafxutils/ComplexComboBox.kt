package com.github.ericytsang.lib.javafxutils

import javafx.scene.Node
import javafx.scene.control.ComboBox
import javafx.scene.layout.VBox

/**
 * Created by surpl on 8/14/2016.
 */
class ComplexComboBox<Option:ComplexComboBox.OptionalBuilder<Product>,Product>:VBox()
{
    val comboBox = ComboBox<Option>().apply()
    {
        valueProperty().addListener()
        {
            observableValue,oldValue,newValue ->

            if (oldValue.panel != null)
            {
                this@ComplexComboBox.children.remove(oldValue.panel)
            }

            if (newValue.panel != null)
            {
                this@ComplexComboBox.children.add(newValue.panel)
            }
        }
    }

    /**
     * can be selected by the [comboBox].
     */
    interface OptionalBuilder<Product>
    {
        /**
         * displayed in the [ComplexComboBox] control if selected by the
         * [comboBox].
         */
        val panel:Node?

        fun build():Product

        fun configureBuilderFrom(product:Product)
    }

    init
    {
        children += comboBox
    }
}
